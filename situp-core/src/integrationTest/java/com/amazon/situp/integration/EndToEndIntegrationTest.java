package com.amazon.situp.integration;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.amazon.situp.model.configuration.PluginSetting;
import com.amazon.situp.model.record.Record;
import com.amazon.situp.pipeline.Pipeline;
import com.amazon.situp.plugins.buffer.BlockingBuffer;
import com.amazon.situp.plugins.processor.oteltrace.OTelTraceRawProcessor;
import com.amazon.situp.plugins.sink.elasticsearch.ConnectionConfiguration;
import com.amazon.situp.plugins.sink.elasticsearch.ElasticsearchSink;
import com.amazon.situp.plugins.sink.elasticsearch.IndexConfiguration;
import com.amazon.situp.plugins.source.oteltracesource.OTelTraceSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.SessionProtocol;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.Test;

public class EndToEndIntegrationTest {

    private static final Random RANDOM = new Random();
    private static final List<Span.SpanKind> SPAN_KINDS =
            Arrays.asList(Span.SpanKind.CLIENT, Span.SpanKind.CONSUMER, Span.SpanKind.INTERNAL, Span.SpanKind.PRODUCER, Span.SpanKind.SERVER);
    private static final String INDEX_NAME = "otel-v1-apm-span-000001";

    @Test
    public void testPipelineEndToEnd() throws IOException, InterruptedException {
        //Setup pipeline
        final HashMap<String, Object> integerHashMap = new HashMap<>();
        integerHashMap.put("request_timeout", 1);
        final OTelTraceSource oTelTraceSource = new OTelTraceSource(new PluginSetting("otel_trace_source", integerHashMap));
        final OTelTraceRawProcessor oTelTraceRawProcessor = new OTelTraceRawProcessor();
        final PluginSetting pluginSetting = generatePluginSetting(true, false, "testIndex", null);
        ElasticsearchSink sink = new ElasticsearchSink(pluginSetting);
        final Pipeline pipeline = new Pipeline(
                "integTestPipeline",
                oTelTraceSource,
                getBuffer(),
                Collections.singletonList(oTelTraceRawProcessor),
                Arrays.asList(sink),
                1, 100
        );
        pipeline.execute();

        //Wait for source server to start up before sending data to it
        Thread.sleep(3000);

        //Send data to otel trace source
        final ExportTraceServiceRequest exportTraceServiceRequest1 = getExportTraceServiceRequest(
                getRandomResourceSpans(10)
        );

        final ExportTraceServiceRequest exportTraceServiceRequest2 = getExportTraceServiceRequest(
                getRandomResourceSpans(10)
        );

        sendExportTraceServiceRequestToSource(exportTraceServiceRequest1);
        sendExportTraceServiceRequestToSource(exportTraceServiceRequest2);

        //Wait for data to flow through pipeline and be indexed by ES
        Thread.sleep(2000);

        //Verify data in elasticsearch sink
        final List<Map<String, Object>> expectedDocuments = getExpectedDocuments(exportTraceServiceRequest1, exportTraceServiceRequest2);
        final RestHighLevelClient restHighLevelClient =new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                )
        );
        final SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(
                SearchSourceBuilder.searchSource().size(100)
        );
        final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final List<Map<String, Object>> foundSources = getSourcesFromSearchHits(searchResponse.getHits());
        Assert.assertEquals(expectedDocuments.size(), foundSources.size());
        expectedDocuments.forEach(expectedDoc -> Assert.assertTrue(foundSources.contains(expectedDoc)));
    }

    private void sendExportTraceServiceRequestToSource(ExportTraceServiceRequest request) throws InvalidProtocolBufferException {
        WebClient.of().execute(RequestHeaders.builder()
                        .scheme(SessionProtocol.HTTP)
                        .authority("127.0.0.1:21890")
                        .method(HttpMethod.POST)
                        .path("/opentelemetry.proto.collector.trace.v1.TraceService/Export")
                        .contentType(MediaType.JSON_UTF_8)
                        .build(),
                HttpData.copyOf(JsonFormat.printer().print(request).getBytes())).aggregate().join();
    }

    private List<Map<String, Object>> getSourcesFromSearchHits(final SearchHits searchHits) {
        final List<Map<String, Object>> sources = new ArrayList<>();
        searchHits.forEach(hit -> sources.add(hit.getSourceAsMap()));
        return sources;
    }


    public static ResourceSpans getResourceSpans(final String serviceName, final String spanName, final byte[]
            spanId, final byte[] parentId, final byte[] traceId, final Span.SpanKind spanKind) throws UnsupportedEncodingException {
        final ByteString parentSpanId = parentId != null ? ByteString.copyFrom(parentId) : ByteString.EMPTY;
        return ResourceSpans.newBuilder()
                .setResource(
                        Resource.newBuilder()
                                .addAttributes(KeyValue.newBuilder()
                                        .setKey("service.name")
                                        .setValue(AnyValue.newBuilder().setStringValue(serviceName).build()).build())
                                .build()
                )
                .addInstrumentationLibrarySpans(
                        0,
                        InstrumentationLibrarySpans.newBuilder()
                                .addSpans(
                                        Span.newBuilder()
                                                .setName(spanName)
                                                .setKind(spanKind)
                                                .setSpanId(ByteString.copyFrom(spanId))
                                                .setParentSpanId(parentSpanId)
                                                .setTraceId(ByteString.copyFrom(traceId))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public static ExportTraceServiceRequest getExportTraceServiceRequest(List<ResourceSpans> spans){
        return ExportTraceServiceRequest.newBuilder()
                .addAllResourceSpans(spans)
                .build();
    }

    private List<Map<String, Object>> getExpectedDocuments(ExportTraceServiceRequest...exportTraceServiceRequests) {
        final List<Map<String, Object>> expectedDocuments = new ArrayList<>();
        for(int i=0; i<exportTraceServiceRequests.length; i++) {
            exportTraceServiceRequests[i].getResourceSpansList().forEach( resourceSpans -> {
                final String resourceName = getServiceName(resourceSpans);
                resourceSpans.getInstrumentationLibrarySpansList().forEach( instrumentationLibrarySpans -> {
                    instrumentationLibrarySpans.getSpansList().forEach(span -> {
                        expectedDocuments.add(getExpectedEsDocumentSource(span, resourceName));
                    });
                });
            });
        }
        return expectedDocuments;
    }

    private Map<String, Object> getExpectedEsDocumentSource(final Span span, final String serviceName) {
        final Map<String, Object> esDocSource = new HashMap<>();
        esDocSource.put("traceId", Base64.encodeBase64String(span.getTraceId().toByteArray()));
        esDocSource.put("spanId", Base64.encodeBase64String(span.getSpanId().toByteArray()));
        esDocSource.put("parentSpanId", Base64.encodeBase64String(span.getParentSpanId().toByteArray()));
        esDocSource.put("name", span.getName());
        esDocSource.put("kind", span.getKind().name());
        esDocSource.put("resource.attributes.service.name", serviceName);
        return esDocSource;
    }

    private byte[] getRandomBytes(int len) {
        final byte[] bytes = new byte[len];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private List<ResourceSpans> getRandomResourceSpans(int len) throws UnsupportedEncodingException {
        final ArrayList<ResourceSpans> spansList = new ArrayList<>();
        for(int i=0; i<len; i++) {
            spansList.add(
                    getResourceSpans(
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString(),
                            getRandomBytes(8),
                            getRandomBytes(8),
                            getRandomBytes(16),
                            SPAN_KINDS.get(RANDOM.nextInt(SPAN_KINDS.size()))
                    )
            );
        }
        return spansList;
    }

    private String getServiceName(final ResourceSpans resourceSpans) {
        return resourceSpans.getResource().getAttributesList().stream().filter(kv -> kv.getKey() == "service.name")
                .findFirst().get().getValue().getStringValue();
    }

    private static BlockingBuffer<Record<ExportTraceServiceRequest>> getBuffer() {
        final HashMap<String, Object> integerHashMap = new HashMap<>();
        integerHashMap.put("buffer_size", 1);
        return new BlockingBuffer<>(new PluginSetting("blocking_buffer", integerHashMap));
    }

    private PluginSetting generatePluginSetting(final boolean isRaw, final boolean isServiceMap, final String indexAlias, final String templateFilePath) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put(IndexConfiguration.TRACE_ANALYTICS_RAW_FLAG, isRaw);
        metadata.put(IndexConfiguration.TRACE_ANALYTICS_SERVICE_MAP_FLAG, isServiceMap);
        metadata.put(ConnectionConfiguration.HOSTS, Arrays.asList("localhost:9200"));
        metadata.put(IndexConfiguration.INDEX_ALIAS, indexAlias);
        metadata.put(IndexConfiguration.TEMPLATE_FILE, templateFilePath);

        return new PluginSetting("elasticsearch", metadata);
    }
}
