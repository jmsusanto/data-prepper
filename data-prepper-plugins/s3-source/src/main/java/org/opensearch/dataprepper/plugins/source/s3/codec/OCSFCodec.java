package org.opensearch.dataprepper.plugins.source.s3.codec;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.codec.InputCodec;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf.OCSF;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@DataPrepperPlugin(name = "ocsf", pluginType = InputCodec.class, pluginConfigurationType = OCSFCodecConfig.class)
public class OCSFCodec implements InputCodec {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new AfterburnerModule());
    private static final ObjectReader OBJECT_READER = OBJECT_MAPPER.readerFor(OCSF.class);

    @DataPrepperPluginConstructor
    public OCSFCodec(final OCSFCodecConfig config) {

    }

    @Override
    public void parse(InputStream inputStream, Consumer<Record<Event>> eventConsumer) throws IOException {
        final MappingIterator<OCSF> mappingIterator = OBJECT_READER.readValues(inputStream);
        mappingIterator.forEachRemaining(ocsf -> eventConsumer.accept(new Record<>(ocsf)));
    }
}
