package uk.gov.di.ipv.cri.experian.config;

import software.amazon.lambda.powertools.parameters.SecretsProvider;

public class ExperianApiConfig {
    private static final String SECRET_KEY_TEMPLATE = "/%s/di-cri-experian-fraud-api/%s";

    private final String tenantId;
    private final String endpointUrl;
    private final String hmacKey;
    private final String encodedKeyStore;
    private final String keyStorePassword;

    public ExperianApiConfig(SecretsProvider secretsProvider) {
        String env = System.getenv("ENVIRONMENT");
        this.tenantId = System.getenv("EXPERIAN_API_TENANT_ID");
        this.endpointUrl = System.getenv("EXPERIAN_API_ENDPOINT_URL");
        this.hmacKey =
                secretsProvider.get(
                        String.format(SECRET_KEY_TEMPLATE, env, "experian-api-hmac-key"));
        this.encodedKeyStore =
                secretsProvider.get(String.format(SECRET_KEY_TEMPLATE, env, "experian-api"));
        this.keyStorePassword =
                secretsProvider.get(
                        String.format(SECRET_KEY_TEMPLATE, env, "experian-api-keystore-password"));
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getEncodedKeyStore() {
        return encodedKeyStore;
    }
}
