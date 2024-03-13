package org.opensearch.dataprepper.plugins.processor.model.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public class OCSF extends DataType {
    private final String metadataProductVersion;
    private final String metadataProductName;
    private final String metadataProductVendorName;
    private final String metadataProductFeatureName;
    private final String metadataUid;
    private final List<String> metadataProfiles;
    private final String metadataVersion;
    private final Long time;
    private final String cloudRegion;
    private final String cloudProvider;
    private final String dstEndpoint;
    private final String httpRequestUserAgent;
    private final String srcEndpointUid;
    private final String srcEndpointIp;
    private final String srcEndpointDomain;
    private final String className;
    private final Integer classUid;
    private final String categoryName;
    private final Integer categoryUid;
    private final Integer severityId;
    private final String severity;
    private final String user;
    private final String activityName;
    private final Integer activityId;
    private final Integer typeUid;
    private final String typeName;
    private final String status;
    private final Integer statusId;
    private final Boolean mfa;
    private final String apiResponseError;
    private final String apiResponseMessage;
    private final String apiOperation;
    private final String apiVersion;
    private final String apiServiceName;
    private final String apiRequestUid;
    private final List<Resource> resources;
    private final String actorUserType;
    private final String actorUserName;
    private final String actorUserUid;
    private final String actorUserUuid;
    private final String actorUserAccountUid;
    private final String actorUserCredentialUid;
    private final Long actorSessionCreatedTime;
    private final Boolean actorSessionMfa;
    private final String actorSessionIssuer;
    private final String actorInvokedBy;
    private final String actorIdpName;
    private final Map<String, String> unmapped;

    @Override
    public Object getValue(final String fieldName) {
        switch (fieldName) {
            case "metadata.product.version": return metadataProductVersion;
            case "metadata.product.name": return metadataProductName;
            case "metadata.product.vendor_name": return metadataProductVendorName;
            case "metadata.product.feature.name": return metadataProductFeatureName;
            case "metadata.uid": return metadataUid;
            case "metadata.profiles": return metadataProfiles;
            case "metadata.version": return metadataVersion;
            case "time": return time;
            case "cloud.region": return cloudRegion;
            case "cloud.provider": return cloudProvider;
            case "dst_endpoint": return dstEndpoint;
            case "http_request.user_agent": return httpRequestUserAgent;
            case "src_endpoint.uid": return srcEndpointUid;
            case "src_endpoint.ip": return srcEndpointIp;
            case "src_endpoint.domain": return srcEndpointDomain;
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
            case "api.response.error": return apiResponseError;
            case "api.response.message": return apiResponseMessage;
            case "api.operation": return apiOperation;
            case "api.version": return apiVersion;
            case "api.service.name": return apiServiceName;
            case "api.request.uid": return apiRequestUid;
            case "resources": return resources;
            case "actor.user.type": return actorUserType;
            case "actor.user.name": return actorUserName;
            case "actor.user.uid": return actorUserUid;
            case "actor.user.uuid": return actorUserUuid;
            case "actor.user.account_uid": return actorUserAccountUid;
            case "actor.user.credential_uid": return actorUserCredentialUid;
            case "actor.session.created_time": return actorSessionCreatedTime;
            case "actor.session.mfa": return actorSessionMfa;
            case "actor.session.issuer": return actorSessionIssuer;
            case "actor.invoked_by": return actorInvokedBy;
            case "actor.idp.name": return actorIdpName;
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

    public static class Resource {
        private String uid;
        @JsonProperty("account_uid")
        private String accountUid;
        private String type;

        public Resource() {
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getAccountUid() {
            return accountUid;
        }

        public void setAccountUid(String accountUid) {
            this.accountUid = accountUid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
