package uk.gov.di.ipv.cri.experian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import spark.Spark;
import uk.gov.di.ipv.cri.experian.config.ExperianApiConfig;
import uk.gov.di.ipv.cri.experian.gateway.ExperianApiRequestMapper;
import uk.gov.di.ipv.cri.experian.gateway.ExperianGateway;
import uk.gov.di.ipv.cri.experian.gateway.HmacGenerator;
import uk.gov.di.ipv.cri.experian.resource.HealthCheckResource;
import uk.gov.di.ipv.cri.experian.resource.IdentityCheckResource;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class FraudApi {
    private final IdentityCheckResource identityCheckResource;
    private final HealthCheckResource healthCheckResource;

    public FraudApi() {
        try {
            Spark.port(5007);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            InputValidationExecutor inputValidationExecutor =
                    new InputValidationExecutor(validator);

            IdentityVerificationService identityVerificationService =
                    createIdentityVerificationService(objectMapper);
            this.identityCheckResource =
                    new IdentityCheckResource(
                            identityVerificationService, objectMapper, inputValidationExecutor);
            this.healthCheckResource = new HealthCheckResource();

            mapRoutes();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialise API", e); // TODO: create a dedicated Exception class
        }
    }

    private void mapRoutes() {
        Spark.get("/healthcheck", this.healthCheckResource.getCurrentHealth);
        Spark.post("/identity-check", this.identityCheckResource.performIdentityCheckRoute);
    }

    private HttpClient createCrossCoreHttpClient() {
        return java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    private IdentityVerificationService createIdentityVerificationService(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException {
        ExperianApiConfig experianExperianApiConfig = new ExperianApiConfig();
        HttpClient httpClient = createCrossCoreHttpClient();
        HmacGenerator hmacGenerator = new HmacGenerator(experianExperianApiConfig.getHmacKey());
        ExperianApiRequestMapper apiRequestMapper =
                new ExperianApiRequestMapper(experianExperianApiConfig.getTenantId());
        ExperianGateway experianGateway =
                new ExperianGateway(
                        httpClient,
                        apiRequestMapper,
                        objectMapper,
                        hmacGenerator,
                        experianExperianApiConfig);
        return new IdentityVerificationService(experianGateway);
    }
}
