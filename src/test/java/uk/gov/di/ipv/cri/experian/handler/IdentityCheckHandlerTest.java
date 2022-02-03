package uk.gov.di.ipv.cri.experian.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.service.ServiceFactory;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityCheckHandlerTest {
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private InputValidationExecutor mockInputValidationExecutor;
    @Mock private APIGatewayProxyRequestEvent mockApiGatewayProxyRequestEvent;
    @Mock private Context mockLambdaContext;

    private IdentityCheckHandler identityCheckHandler;

    IdentityCheckHandlerTest() {
        // prevent aws-xray missing context exception from surfacing during unit test execution
        System.setProperty("com.amazonaws.xray.strategy.contextMissingStrategy", "LOG_ERROR");
    }

    @BeforeEach
    void setUp() {
        when(mockServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.identityCheckHandler =
                new IdentityCheckHandler(
                        mockObjectMapper, mockInputValidationExecutor, mockServiceFactory);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidPersonIdentityProvided() throws JsonProcessingException {
        String testRequestBody = "{ \"firstName\": null }";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        String inputValidationErrorMsg = "input validation error";
        String serializedValidationResult = "{ \"error\": \"" + inputValidationErrorMsg + "\" }";
        ValidationResult testValidationResult =
                new ValidationResult(List.of(inputValidationErrorMsg));

        when(mockApiGatewayProxyRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockInputValidationExecutor.performInputValidation(testPersonIdentity))
                .thenReturn(testValidationResult);
        when(mockObjectMapper.writeValueAsString(testValidationResult))
                .thenReturn(serializedValidationResult);

        APIGatewayProxyResponseEvent response =
                this.identityCheckHandler.handleRequest(
                        mockApiGatewayProxyRequestEvent, mockLambdaContext);

        makeResponseStatusAndHeadersAssertions(response, 400);
        assertEquals(serializedValidationResult, response.getBody());
        verify(mockObjectMapper).readValue(testRequestBody, PersonIdentity.class);
        verify(mockObjectMapper).writeValueAsString(testValidationResult);
        verify(mockInputValidationExecutor).performInputValidation(testPersonIdentity);
    }

    @Test
    void shouldInvokeExperianApiWhenValidPersonIdentityProvided() throws JsonProcessingException {
        String testRequestBody = "{ \"firstName\": \"tommy\" }";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        ValidationResult testValidationResult = new ValidationResult(Collections.emptyList());
        String identityVerificationResult = "{\"result\": \"PASS\"}";

        when(mockApiGatewayProxyRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockInputValidationExecutor.performInputValidation(testPersonIdentity))
                .thenReturn(testValidationResult);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(identityVerificationResult);

        APIGatewayProxyResponseEvent response =
                this.identityCheckHandler.handleRequest(
                        mockApiGatewayProxyRequestEvent, mockLambdaContext);

        makeResponseStatusAndHeadersAssertions(response, 201);
        assertEquals(identityVerificationResult, response.getBody());
        verify(mockObjectMapper).readValue(testRequestBody, PersonIdentity.class);
        verify(mockInputValidationExecutor).performInputValidation(testPersonIdentity);
        verify(mockIdentityVerificationService).verifyIdentity(testPersonIdentity);
    }

    @Test
    void shouldReturnInternalServerErrorWhenAnExceptionOccurs() throws JsonProcessingException {
        String testRequestBody = "{ \"firstName\": \"tommy\" }";
        when(mockApiGatewayProxyRequestEvent.getBody()).thenReturn(testRequestBody);
        PersonIdentity testPersonIdentity = new PersonIdentity();
        String errorMsg = "error message";
        JsonParseException mockException = mock(JsonParseException.class);
        when(mockException.getMessage()).thenReturn(errorMsg);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenThrow(mockException);

        APIGatewayProxyResponseEvent response =
                this.identityCheckHandler.handleRequest(
                        mockApiGatewayProxyRequestEvent, mockLambdaContext);

        makeResponseStatusAndHeadersAssertions(response, 500);
        assertEquals("{\"error\": \"" + errorMsg + "\"}", response.getBody());
        verify(mockObjectMapper).readValue(testRequestBody, PersonIdentity.class);
        verify(mockException, Mockito.times(2)).getMessage();
    }

    private void makeResponseStatusAndHeadersAssertions(
            APIGatewayProxyResponseEvent response, int expectedHttpStatusCode) {
        assertEquals(expectedHttpStatusCode, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    /*
    *
    *
    * public APIGatewayProxyResponseEvent handleRequest(
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
    *
    * */

}
