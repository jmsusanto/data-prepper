package org.opensearch.dataprepper.plugins.processor.parser.types;

public class SigmaInteger implements SigmaType {
    private final Integer integer;

    public SigmaInteger(Integer integer) {
        this.integer = integer;
    }

    public Integer getInteger() {
        return integer;
    }
}
