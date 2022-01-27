package uk.gov.di.ipv.cri.experian.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.service.ServiceFactory;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(400, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals(serializedValidationResult, response.getBody());
    }
}
