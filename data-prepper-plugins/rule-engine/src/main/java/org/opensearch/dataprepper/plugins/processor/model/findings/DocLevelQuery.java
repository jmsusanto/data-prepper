package org.opensearch.dataprepper.plugins.processor.model.findings;

import java.util.List;

public class DocLevelQuery {
    private String id;
    private String name;
    private List<String> tags;
    private String query;

    public DocLevelQuery(final String id, final String name, final List<String> tags, final String query) {
        this.id = id;
        this.name = name;
        this.tags = tags;
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getQuery() {
        return query;
    }
}
