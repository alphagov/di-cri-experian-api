package uk.gov.di.ipv.cri.experian.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SecretsProvider;
import uk.gov.di.ipv.cri.experian.config.ExperianApiConfig;
import uk.gov.di.ipv.cri.experian.gateway.ExperianApiRequestMapper;
import uk.gov.di.ipv.cri.experian.gateway.ExperianGateway;
import uk.gov.di.ipv.cri.experian.gateway.HmacGenerator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final SSLContextFactory sslContextFactory;
    private final ExperianApiConfig experianApiConfig;
    private final SecretsProvider secretsProvider;
    private final ObjectMapper objectMapper;

    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.secretsProvider = ParamManager.getSecretsProvider();
        this.objectMapper = objectMapper;
        this.experianApiConfig = new ExperianApiConfig(this.secretsProvider);
        this.sslContextFactory =
                new SSLContextFactory(
                        this.experianApiConfig.getEncodedKeyStore(),
                        this.experianApiConfig.getKeyStorePassword());
        this.identityVerificationService = createIdentityVerificationService();
    }

    ServiceFactory(
            ObjectMapper objectMapper,
            SecretsProvider secretsProvider,
            ExperianApiConfig experianApiConfig,
            SSLContextFactory sslContextFactory)
            throws NoSuchAlgorithmException, InvalidKeyException {
        this.objectMapper = objectMapper;
        this.secretsProvider = secretsProvider;
        this.experianApiConfig = experianApiConfig;
        this.sslContextFactory = sslContextFactory;
        this.identityVerificationService = createIdentityVerificationService();
    }

    public IdentityVerificationService getIdentityVerificationService() {
        return this.identityVerificationService;
    }

    private IdentityVerificationService createIdentityVerificationService()
            throws NoSuchAlgorithmException, InvalidKeyException {
        HttpClient httpClient = createHttpClient();

        HmacGenerator hmacGenerator = new HmacGenerator(this.experianApiConfig.getHmacKey());

        ExperianApiRequestMapper apiRequestMapper =
                new ExperianApiRequestMapper(this.experianApiConfig.getTenantId());

        ExperianGateway experianGateway =
                new ExperianGateway(
                        httpClient,
                        apiRequestMapper,
                        this.objectMapper,
                        hmacGenerator,
                        this.experianApiConfig.getEndpointUrl());

        return new IdentityVerificationService(experianGateway);
    }

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .sslContext(this.sslContextFactory.getSSLContext())
                .build();
    }
}
