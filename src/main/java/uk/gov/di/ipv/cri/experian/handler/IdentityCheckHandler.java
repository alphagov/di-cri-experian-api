package uk.gov.di.ipv.cri.experian.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.experian.config.ExperianApiConfig;
import uk.gov.di.ipv.cri.experian.gateway.ExperianApiRequestMapper;
import uk.gov.di.ipv.cri.experian.gateway.ExperianGateway;
import uk.gov.di.ipv.cri.experian.gateway.HmacGenerator;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;

public class IdentityCheckHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final int BAD_REQUEST_HTTP_STATUS_CODE = 400;
    private static final int CREATED_HTTP_STATUS_CODE = 201;
    private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityCheckHandler.class);

    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final InputValidationExecutor inputValidationExecutor;

    static {
        //        try {
        //            String keystoreBase64 = System.getenv("KEYSTORE");
        //            Path tempFile = Files.createTempFile(null, null);
        //            Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));
        //
        //            System.setProperty("javax.net.ssl.keyStore", tempFile.toString());
        //            System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
        //            System.setProperty(
        //                    "javax.net.ssl.keyStorePassword", System.getenv("KEYSTORE_PASSWORD"));
        //        } catch (IOException e) {
        //            LOGGER.error("Static initialisation failed", e);
        //        }
    }

    public IdentityCheckHandler()
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        this.inputValidationExecutor = new InputValidationExecutor(validator);
        this.identityVerificationService = createIdentityVerificationService(objectMapper);
    }

    public IdentityCheckHandler(
            IdentityVerificationService identityVerificationService,
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor)
            throws IOException {
        this.identityVerificationService = identityVerificationService;
        this.objectMapper = objectMapper;
        this.inputValidationExecutor = inputValidationExecutor;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
        return createResponseEvent(201, "{\"field\": \"value\"}", responseHeaders);
        //
        //
        //        try {
        //            PersonIdentity personIdentity =
        //                    objectMapper.readValue(input.getBody(), PersonIdentity.class);
        //            ValidationResult validationResult =
        //                    this.inputValidationExecutor.performInputValidation(personIdentity);
        //
        //            String responseBody;
        //            int responseStatusCode;
        //
        //            if (validationResult.isValid()) {
        //                responseStatusCode = CREATED_HTTP_STATUS_CODE;
        //                responseBody =
        // this.identityVerificationService.verifyIdentity(personIdentity);
        //            } else {
        //                responseStatusCode = BAD_REQUEST_HTTP_STATUS_CODE;
        //                responseBody = objectMapper.writeValueAsString(validationResult);
        //            }
        //
        //            return createResponseEvent(responseStatusCode, responseBody, responseHeaders);
        //        } catch (Exception e) {
        //            LOGGER.error("Error handling request", e);
        //            return createResponseEvent(
        //                    INTERNAL_SERVER_ERROR_STATUS_CODE, "An error occurred",
        // responseHeaders);
        //        }
    }

    private IdentityVerificationService createIdentityVerificationService(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException {
        ExperianApiConfig experianExperianApiConfig = new ExperianApiConfig();
        HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
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

    private static APIGatewayProxyResponseEvent createResponseEvent(
            int statusCode, String body, Map<String, String> headers) {
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent =
                new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setHeaders(headers);
        apiGatewayProxyResponseEvent.setStatusCode(statusCode);
        apiGatewayProxyResponseEvent.setBody(body);

        return apiGatewayProxyResponseEvent;
    }
}
