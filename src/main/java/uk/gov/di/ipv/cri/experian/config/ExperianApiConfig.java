package uk.gov.di.ipv.cri.experian.config;

import software.amazon.lambda.powertools.parameters.SSMProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

public class ExperianApiConfig {
    private final String tenantId;
    private final String endpointUri;
    private final String hmacKey;

    public ExperianApiConfig(SSMProvider ssmProvider, SecretsProvider secretsProvider) {
        this.tenantId = ssmProvider.get("/dev/di-cri-experian-fraud-api/experian-api-tenant-id");

        this.endpointUri = ssmProvider.get("/dev/di-cri-experian-fraud-api/experian-api-endpoint");

        this.hmacKey = secretsProvider.get("/dev/di-cri-experian-fraud-api/experian-api-hmac-key");
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
