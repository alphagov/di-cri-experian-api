package uk.gov.di.ipv.cri.experian.config;

public class CrossCoreApiConfig {
    private final String tenantId;
    private final String endpointUri;
    private final String hmacKey;

    public CrossCoreApiConfig() {
        this.tenantId = System.getenv("EXPERIAN_CROSS_CORE_API_TENANT_ID");
        this.endpointUri = System.getenv("EXPERIAN_CROSS_CORE_API_ENDPOINT_URI");
        this.hmacKey = System.getenv("EXPERIAN_CROSS_CORE_API_HMAC_KEY");
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public String getHmacKey() {
        return hmacKey;
    }
}
