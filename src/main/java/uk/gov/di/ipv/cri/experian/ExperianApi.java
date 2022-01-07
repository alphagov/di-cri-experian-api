package uk.gov.di.ipv.cri.experian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import spark.Spark;
import uk.gov.di.ipv.cri.experian.config.CrossCoreApiConfig;
import uk.gov.di.ipv.cri.experian.gateway.crosscore.CrossCoreApiRequestMapper;
import uk.gov.di.ipv.cri.experian.gateway.crosscore.CrossCoreGateway;
import uk.gov.di.ipv.cri.experian.gateway.kbv.KBVGateway;
import uk.gov.di.ipv.cri.experian.gateway.kbv.SAARequestMapper;
import uk.gov.di.ipv.cri.experian.resource.HealthCheckResource;
import uk.gov.di.ipv.cri.experian.resource.IdentityCheckResource;
import uk.gov.di.ipv.cri.experian.resource.QuestionAnswerResource;
import uk.gov.di.ipv.cri.experian.resource.QuestionResource;
import uk.gov.di.ipv.cri.experian.security.HmacGenerator;
import uk.gov.di.ipv.cri.experian.security.SSLContextFactory;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.service.KBVService;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.net.ssl.SSLContext;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class ExperianApi {
    private final IdentityCheckResource identityCheckResource;
    private final HealthCheckResource healthCheckResource;
    private final QuestionResource questionResource;
    private final QuestionAnswerResource questionAnswerResource;

    public ExperianApi() {
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

            KBVService kbvService = createKbvService();
            this.questionResource =
                    new QuestionResource(kbvService, objectMapper, inputValidationExecutor);
            this.questionAnswerResource = new QuestionAnswerResource();

            mapRoutes();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialise API", e); // TODO: create a dedicated Exception class
        }
    }

    private void mapRoutes() {
        Spark.get("/healthcheck", this.healthCheckResource.getCurrentHealth);
        Spark.post("/identity-check", this.identityCheckResource.performIdentityCheckRoute);

        Spark.post("/question-request", this.questionResource.getQuestions);
        Spark.post("/question-answer", this.questionAnswerResource.submitQuestionAnswers);
    }

    private HttpClient createCrossCoreHttpClient(CrossCoreApiConfig crossCoreApiConfig) {
        SSLContext sslContext =
                new SSLContextFactory()
                        .getSSLContext(
                                crossCoreApiConfig.getKeystorePath(),
                                crossCoreApiConfig.getKeystorePassword());
        return java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .sslContext(sslContext)
                .build();
    }

    private KBVService createKbvService() {
        return new KBVService(new KBVGateway(new SAARequestMapper()));
    }

    private IdentityVerificationService createIdentityVerificationService(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException {
        CrossCoreApiConfig experianCrossCoreApiConfig = new CrossCoreApiConfig();
        HttpClient httpClient = createCrossCoreHttpClient(experianCrossCoreApiConfig);
        HmacGenerator hmacGenerator = new HmacGenerator(experianCrossCoreApiConfig.getHmacKey());
        CrossCoreApiRequestMapper apiRequestMapper =
                new CrossCoreApiRequestMapper(experianCrossCoreApiConfig.getTenantId());
        CrossCoreGateway crossCoreGateway =
                new CrossCoreGateway(
                        httpClient,
                        apiRequestMapper,
                        objectMapper,
                        hmacGenerator,
                        experianCrossCoreApiConfig);
        return new IdentityVerificationService(crossCoreGateway);
    }
}
