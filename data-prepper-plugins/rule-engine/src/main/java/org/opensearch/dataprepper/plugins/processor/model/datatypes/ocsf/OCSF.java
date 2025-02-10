package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.opensearch.dataprepper.expression.ExpressionEvaluator;
import org.opensearch.dataprepper.model.event.DefaultEventHandle;
import org.opensearch.dataprepper.model.event.DefaultEventMetadata;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.event.EventHandle;
import org.opensearch.dataprepper.model.event.EventKey;
import org.opensearch.dataprepper.model.event.EventMetadata;
import org.opensearch.dataprepper.model.event.EventType;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

import java.util.List;
import java.util.Map;

@Data
public class OCSF extends DataType implements Event {
    private Metadata metadata;
    private Long time;
    private Cloud cloud;
    @JsonProperty("dst_endpoint")
    private Endpoint dstEndpoint;
    @JsonProperty("http_request")
    private HttpRequest httpRequest;
    @JsonProperty("src_endpoint")
    private Endpoint srcEndpoint;
    @JsonProperty("class_name")
    private String className;
    @JsonProperty("class_uid")
    private Integer classUid;
    @JsonProperty("category_name")
    private String categoryName;
    @JsonProperty("category_uid")
    private Integer categoryUid;
    @JsonProperty("severity_id")
    private Integer severityId;
    private String severity;
    private String user;
    @JsonProperty("activity_name")
    private String activityName;
    @JsonProperty("activity_id")
    private Integer activityId;
    @JsonProperty("type_uid")
    private Integer typeUid;
    @JsonProperty("type_name")
    private String typeName;
    private String status;
    @JsonProperty("status_id")
    private Integer statusId;
    private Boolean mfa;
    private Api api;
    private List<Resource> resources;
    private Actor actor;
    private Map<String, String> unmapped;

    @JsonIgnore
    private EventMetadata eventMetadata = DefaultEventMetadata.builder().withEventType(EventType.LOG.toString()).build();
    @JsonIgnore
    private EventHandle eventHandle = new DefaultEventHandle(eventMetadata.getTimeReceived());
    @JsonIgnore
    private String timeFieldName = "time";

    public OCSF() {
        super();
    }

    @Override
    public Object getValue(final String fieldName) {
        switch (fieldName) {
            case "metadata.product.version": return metadata.getProduct().getVersion();
            case "metadata.product.name": return metadata.getProduct().getName();
            case "metadata.product.vendor_name": return metadata.getProduct().getVendorName();
            case "metadata.product.feature.name": return metadata.getProduct().getFeature().getName();
            case "metadata.uid": return metadata.getUid();
            case "metadata.profiles": return metadata.getProfiles();
            case "metadata.version": return metadata.getVersion();
            case "time": return time;
            case "cloud.region": return cloud.getRegion();
            case "cloud.provider": return cloud.getProvider();
            case "dst_endpoint": return dstEndpoint;
            case "http_request.user_agent": return httpRequest.getUserAgent();
            case "src_endpoint.uid": return srcEndpoint.getUid();
            case "src_endpoint.ip": return srcEndpoint.getIp();
            case "src_endpoint.domain": return srcEndpoint.getDomain();
            case "class_name": return className;
            case "class_uid": return classUid;
            case "category_name": return categoryName;
            case "category_uid": return categoryUid;
            case "severity_id": return severityId;
            case "severity": return severity;
            case "user": return user;
            case "activity_name": return activityName;
            case "activity_id": return activityId;
            case "type_uid": return typeUid;
            case "type_name": return typeName;
            case "status": return status;
            case "status_id": return statusId;
            case "mfa": return mfa;
            case "api.response.error": return api.getResponse().getError();
            case "api.response.message": return api.getResponse().getMessage();
            case "api.operation": return api.getOperation();
            case "api.version": return api.getVersion();
            case "api.service.name": return api.getService().getName();
            case "api.request.uid": return api.getRequest().getUid();
            case "resources": return resources;
            case "actor.user.type": return actor.getUser().getType();
            case "actor.user.name": return actor.getUser().getName();
            case "actor.user.uid": return actor.getUser().getUid();
            case "actor.user.uuid": return actor.getUser().getUuid();
            case "actor.user.account_uid": return actor.getUser().getAccountUid();
            case "actor.user.credential_uid": return actor.getUser().getCredentialUid();
            case "actor.session.created_time": return actor.getSession().getCreatedTime();
            case "actor.session.mfa": return actor.getSession().getMfa();
            case "actor.session.issuer": return actor.getSession().getIssuer();
            case "actor.invoked_by": return actor.getInvokedBy();
            case "actor.idp.name": return actor.getIdp().getName();
            case "unmapped": return unmapped;
            default: return handleOtherFields(fieldName);
        }
    }

    private Object handleOtherFields(final String fieldName) {
        final String[] parts = fieldName.split("\\.");
        if (parts.length == 0 || !"unmapped".equals(parts[0])) {
            throw new IllegalArgumentException("Field " + fieldName + " does not exist in class " + getClass().getName());
        }
        if (parts.length == 1) {
            return unmapped;
        }
        return unmapped.get(parts[1]);
    }

    @Override
    public String getTimeFieldName() {
        return timeFieldName;
    }

    @Override
    public void put(String key, Object value) {
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public void delete(String key) {
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonNode getJsonNode() {
        return null;
    }

    @Override
    public String getAsJsonString(String key) {
        return null;
    }

    @Override
    public EventMetadata getMetadata() {
        return eventMetadata;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public boolean isValueAList(String key) {
        return false;
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public String formatString(String format) {
        return null;
    }

    @Override
    public String formatString(String format, ExpressionEvaluator expressionEvaluator) {
        return format;
    }

    @Override
    public String formatString(String format, ExpressionEvaluator expressionEvaluator, String defaultValue) {
        String result = formatString(format, expressionEvaluator);
        return result != null ? result : defaultValue;
    }

    @Override
    public EventHandle getEventHandle() {
        return eventHandle;
    }

    @Override
    public JsonStringBuilder jsonBuilder() {
        return null;
    }

    @Override
    public void put(EventKey key, Object value) {
        put(key.toString(), value);
    }

    @Override
    public <T> T get(EventKey key, Class<T> clazz) {
        return get(key.toString(), clazz);
    }

    @Override
    public <T> List<T> getList(EventKey key, Class<T> clazz) {
        return getList(key.toString(), clazz);
    }

    @Override
    public void delete(EventKey key) {
        delete(key.toString());
    }

    @Override
    public String getAsJsonString(EventKey key) {
        return getAsJsonString(key.toString());
    }

    @Override
    public boolean containsKey(EventKey key) {
        return containsKey(key.toString());
    }

    @Override
    public boolean isValueAList(EventKey key) {
        return isValueAList(key.toString());
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear() is not supported on OCSF events");
    }

    @Override
    public void merge(Event other) {
        throw new UnsupportedOperationException("merge() is not supported on OCSF events");
    }
}
