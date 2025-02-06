package org.opensearch.dataprepper.plugins.processor.parser.types;

public class SigmaFloat implements SigmaType {
    private final Float aFloat;

    public SigmaFloat(Float aFloat) {
        this.aFloat = aFloat;
    }

    public Float getFloat() {
        return aFloat;
    }
}
