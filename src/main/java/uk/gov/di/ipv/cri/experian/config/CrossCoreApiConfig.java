package uk.gov.di.ipv.cri.experian.config;

public class CrossCoreApiConfig {
    private final String tenantId;
    private final String endpointUri;
    private final String hmacKey;
    private final String keystorePath;
    private final String keystorePassword;

    public CrossCoreApiConfig() {
        this.tenantId = System.getenv("EXPERIAN_CROSS_CORE_API_TENANT_ID");
        this.endpointUri = System.getenv("EXPERIAN_CROSS_CORE_API_ENDPOINT_URI");
        this.hmacKey = System.getenv("EXPERIAN_CROSS_CORE_API_HMAC_KEY");
        this.keystorePath = System.getenv("EXPERIAN_CROSS_CORE_API_KEYSTORE_PATH");
        this.keystorePassword = System.getenv("EXPERIAN_CROSS_CORE_API_KEYSTORE_PASSWORD");
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

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}
