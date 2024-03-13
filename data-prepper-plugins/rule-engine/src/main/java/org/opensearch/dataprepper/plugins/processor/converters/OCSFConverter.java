package org.opensearch.dataprepper.plugins.processor.converters;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.OCSF;

import java.util.Map;

public class OCSFConverter {
    public OCSF convert(final Record<Event> record) {
        final Event event = record.getData();
        return OCSF.builder()
                .metadataProductVersion(event.get("/metadata/product/version", String.class))
                .metadataProductName(event.get("/metadata/product/name", String.class))
                .metadataProductVendorName(event.get("/metadata/product/vendor_name", String.class))
                .metadataProductFeatureName(event.get("/metadata/product/feature/name", String.class))
                .metadataUid(event.get("/metadata/uid", String.class))
                .metadataProfiles(event.getList("/metadata/profiles", String.class))
                .metadataVersion(event.get("/metadata/version", String.class))
                .time(event.get("/time", Long.class))
                .cloudRegion(event.get("/cloud/region", String.class))
                .cloudProvider(event.get("/cloud/provider", String.class))
                .dstEndpoint(event.get("/dst_endpoint", String.class))
                .httpRequestUserAgent(event.get("/http_request/user_agent", String.class))
                .srcEndpointUid(event.get("/src_endpoint/uid", String.class))
                .srcEndpointIp(event.get("/src_endpoint/ip", String.class))
                .srcEndpointDomain(event.get("/src_endpoint/domain", String.class))
                .className(event.get("/class_name", String.class))
                .classUid(event.get("/class_uid", Integer.class))
                .categoryName(event.get("/category_name", String.class))
                .categoryUid(event.get("/category_uid", Integer.class))
                .severityId(event.get("/severity_id", Integer.class))
                .severity(event.get("/severity", String.class))
                .user(event.get("/user", String.class))
                .activityName(event.get("/activity_name", String.class))
                .activityId(event.get("/activity_id", Integer.class))
                .typeUid(event.get("/type_uid", Integer.class))
                .typeName(event.get("/type_name", String.class))
                .status(event.get("/status", String.class))
                .statusId(event.get("/status_id", Integer.class))
                .mfa(event.get("/mfa", Boolean.class))
                .apiResponseError(event.get("/api/response/error", String.class))
                .apiResponseMessage(event.get("/api/response/message", String.class))
                .apiOperation(event.get("/api/operation", String.class))
                .apiVersion(event.get("/api/version", String.class))
                .apiServiceName(event.get("/api/service/name", String.class))
                .apiRequestUid(event.get("/api/request/uid", String.class))
                .resources(event.getList("/resources", OCSF.Resource.class))
                .actorUserType(event.get("/actor/user/type", String.class))
                .actorUserName(event.get("/actor/user/name", String.class))
                .actorUserUid(event.get("/actor/user/uid", String.class))
                .actorUserUuid(event.get("/actor/user/uuid", String.class))
                .actorUserAccountUid(event.get("/actor/user/account_uid", String.class))
                .actorUserCredentialUid(event.get("/actor/user/credential_uid", String.class))
                .actorSessionCreatedTime(event.get("/actor/session/created_time", Long.class))
                .actorSessionMfa(event.get("/actor/session/mfa", Boolean.class))
                .actorSessionIssuer(event.get("/actor/session/issuer", String.class))
                .actorInvokedBy(event.get("/actor/invoked_by", String.class))
                .actorIdpName(event.get("/actor/idp/name", String.class))
                .unmapped((Map<String, String>) event.get("/unmapped", Map.class))
                .build();
    }
}
