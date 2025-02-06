package org.opensearch.dataprepper.plugins.processor.retrievers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class OpenSearchSubMatchAccessor implements SubMatchAccessor {
    private static final String SUB_MATCHES_INDEX_NAME = "sub-matches";
    private final OpenSearchClient openSearchClient;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public OpenSearchSubMatchAccessor(final OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public List<Match> getSubMatches(final StatefulRule statefulRule, final DataType dataType) {
        // TODO - add size and sort as well. Need to return <-max(N), +max(N)> window
        final SearchResponse<Match> subMatches = getSubMatchesFromStore(statefulRule, dataType);
        log.info("Retrieved {} hits from query", subMatches.hits().total().value());
        return subMatches.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    @Override
    public void storeSubMatches(final List<Match> subMatches) {
        final List<BulkOperation> bulkOperations = subMatches.stream()
                .map(this::getBulkOperation)
                .collect(Collectors.toList());
        final BulkRequest bulkRequest = new BulkRequest.Builder()
                .index(SUB_MATCHES_INDEX_NAME)
                .operations(bulkOperations)
                .build();

        try {
            final BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest);
            if (bulkResponse.errors()) {
                log.error("Error writing submatches to store");
                bulkResponse.items().stream()
                        .filter(item -> item.error() != null)
                        .forEach(item -> log.error("BulkItem error: {}", item.error().reason()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed storing sub matches", e);
        }
    }

    private BulkOperation getBulkOperation(final Match match) {

        final IndexOperation.Builder<Object> indexOperationBuilder =
                new IndexOperation.Builder<>()
                        .index(SUB_MATCHES_INDEX_NAME)
                        .document(match);
        return new BulkOperation.Builder()
                .index(indexOperationBuilder.build())
                .build();
    }

    private SearchResponse<Match> getSubMatchesFromStore(final StatefulRule statefulRule, final DataType dataType) {
        try {
            final SearchRequest searchRequest = getSubMatchesSearchRequest(statefulRule, dataType);
            return openSearchClient.search(searchRequest, Match.class);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get submatches for rule with ID " + statefulRule.getId(), e);
        }
    }

    private SearchRequest getSubMatchesSearchRequest(final StatefulRule statefulRule, final DataType dataType) {
        return new SearchRequest.Builder()
                .index(SUB_MATCHES_INDEX_NAME)
                .query(getSubMatchesQuery(statefulRule, dataType)._toQuery())
                .build();
    }

    private BoolQuery getSubMatchesQuery(final StatefulRule statefulRule, final DataType dataType) {
        final List<Query> filterQueries = statefulRule.getFilterFields().stream()
                .map(fieldName -> getFilterFieldMatchQuery(fieldName, (String) dataType.getValue(fieldName)))
                .collect(Collectors.toList());
        filterQueries.add(MatchQuery.of(m -> m.field("statelessRuleMatches.id").query(FieldValue.of(statefulRule.getId())))._toQuery());

        final String timeFieldName = dataType.getTimeFieldName();
        final Instant timestamp = Instant.ofEpochMilli((long) dataType.getValue(timeFieldName));
        final RangeQuery rangeQuery = RangeQuery.of(r -> r.field("dataType." + timeFieldName)
                .gte(JsonData.of(timestamp.minus(statefulRule.getTimeframe()).toEpochMilli()))
                .lte(JsonData.of(timestamp.plus(statefulRule.getTimeframe()).toEpochMilli())));
        filterQueries.add(rangeQuery._toQuery());

        return BoolQuery.of(q -> q.must(filterQueries));
    }

    private Query getFilterFieldMatchQuery(final String filterFieldName, final String filterFieldValue) {
        return MatchQuery.of(m -> m.field("dataType." + filterFieldName).query(FieldValue.of(filterFieldValue)))._toQuery();
    }
}
