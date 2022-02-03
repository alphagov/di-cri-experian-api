package uk.gov.di.ipv.cri.experian.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.tracing.CaptureMode;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.service.ServiceFactory;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

public final class IdentityCheckHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final int BAD_REQUEST_HTTP_STATUS_CODE = 400;
    private static final int CREATED_HTTP_STATUS_CODE = 201;
    private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityCheckHandler.class);

    private final ServiceFactory serviceFactory;
    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final InputValidationExecutor inputValidationExecutor;

    public IdentityCheckHandler()
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        this.inputValidationExecutor = new InputValidationExecutor(validator);
        this.serviceFactory = new ServiceFactory(objectMapper);

        this.identityVerificationService = this.serviceFactory.getIdentityVerificationService();
    }

    IdentityCheckHandler(
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor,
            ServiceFactory serviceFactory) {
        this.objectMapper = objectMapper;
        this.inputValidationExecutor = inputValidationExecutor;
        this.serviceFactory = serviceFactory;
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
    }

    @Tracing(captureMode = CaptureMode.DISABLED)
    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        String responseBody;
        int responseStatusCode;

        try {
            PersonIdentity personIdentity =
                    objectMapper.readValue(input.getBody(), PersonIdentity.class);
            ValidationResult validationResult =
                    this.inputValidationExecutor.performInputValidation(personIdentity);

            if (validationResult.isValid()) {
                responseStatusCode = CREATED_HTTP_STATUS_CODE;
                responseBody = this.identityVerificationService.verifyIdentity(personIdentity);
                if (Objects.isNull(responseBody)) {
                    responseStatusCode = INTERNAL_SERVER_ERROR_STATUS_CODE;
                    responseBody = "{}";
                }
            } else {
                responseStatusCode = BAD_REQUEST_HTTP_STATUS_CODE;
                responseBody = objectMapper.writeValueAsString(validationResult);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling request", e);
            responseStatusCode = INTERNAL_SERVER_ERROR_STATUS_CODE;
            responseBody = "{\"error\": \"" + e.getMessage() + "\"}";
        }

        return createResponseEvent(responseStatusCode, responseBody);
    }

    private static APIGatewayProxyResponseEvent createResponseEvent(int statusCode, String body) {
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent =
                new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setHeaders(Map.of("Content-Type", "application/json"));
        apiGatewayProxyResponseEvent.setStatusCode(statusCode);
        apiGatewayProxyResponseEvent.setBody(body);

        return apiGatewayProxyResponseEvent;
    }
}
