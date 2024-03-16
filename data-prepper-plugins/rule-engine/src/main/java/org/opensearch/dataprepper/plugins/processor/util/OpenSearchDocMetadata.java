package org.opensearch.dataprepper.plugins.processor.util;

public enum OpenSearchDocMetadata {
    INDEX("index"),
    RULE_ENGINE_ID("RULE_ENGINE_ID"),
    RULE_ENGINE_DOC_MATCH_ID("RULE_ENGINE_DOC_MATCH_ID"),
    RULE_ENGINE_DOC_ID_REPLACEMENT_FIELDS("RULE_ENGINE_DOC_ID_REPLACEMENT_FIELDS"),
    FINDINGS_INDEX_NAME("FINDINGS_INDEX_NAME");

    private final String fieldName;

    OpenSearchDocMetadata(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
