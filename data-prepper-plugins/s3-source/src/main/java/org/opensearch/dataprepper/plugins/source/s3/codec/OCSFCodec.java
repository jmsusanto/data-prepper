package org.opensearch.dataprepper.plugins.source.s3.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.codec.InputCodec;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf.OCSF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

@DataPrepperPlugin(name = "ocsf", pluginType = InputCodec.class, pluginConfigurationType = OCSFCodecConfig.class)
public class OCSFCodec implements InputCodec {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DataPrepperPluginConstructor
    public OCSFCodec(final OCSFCodecConfig config) {

    }

    @Override
    public void parse(InputStream inputStream, Consumer<Record<Event>> eventConsumer) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            parseBufferedReader(reader, eventConsumer);
        }
    }

    private void parseBufferedReader(final BufferedReader reader, final Consumer<Record<Event>> eventConsumer) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            final OCSF ocsf = objectMapper.readValue(line, OCSF.class);
            eventConsumer.accept(new Record<>(ocsf));
        }
    }
}
