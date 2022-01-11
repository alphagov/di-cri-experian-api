package uk.gov.di.ipv.cri.experian.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.experian.util.TestDataCreator.createTestPersonIdentity;

@ExtendWith(MockitoExtension.class)
class IdentityCheckResourceTest {

    @Mock private ObjectMapper mockObjectMapper;
    @Mock private InputValidationExecutor inputValidationExecutor;
    @Mock private IdentityVerificationService mockIdentityVerificationService;

    private IdentityCheckResource identityCheckResource;

    @BeforeEach
    void setUp() {
        this.identityCheckResource =
                new IdentityCheckResource(
                        mockIdentityVerificationService, mockObjectMapper, inputValidationExecutor);
    }

    @Test
    void shouldPerformIdentityCheckWhenValidInputProvided() throws Exception {
        final String requestBody = "request-body";
        PersonIdentity testPersonIdentity = createTestPersonIdentity();
        final String identityVerificationResult = "identity-verification-result";
        when(mockObjectMapper.readValue(requestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(identityVerificationResult);
        Response mockResponse = Mockito.mock(Response.class);
        Request mockRequest = Mockito.mock(Request.class);
        when(mockRequest.body()).thenReturn(requestBody);
        when(inputValidationExecutor.performInputValidation(testPersonIdentity))
                .thenReturn(new ValidationResult(Collections.emptyList()));

        identityCheckResource.performIdentityCheckRoute.handle(mockRequest, mockResponse);

        verify(mockObjectMapper).readValue(requestBody, PersonIdentity.class);
        verify(mockIdentityVerificationService).verifyIdentity(testPersonIdentity);
        verify(mockResponse).status(HttpServletResponse.SC_CREATED);
        verify(mockResponse).header("Content-Type", "application/json");
        verify(mockResponse).body(identityVerificationResult);
    }

    @Test
    void shouldReturn400ResponseWhenInvalidInputProvided() throws Exception {
        final String requestBody = "request-body";
        final String errorMessage = "firstname must not be null or empty";
        final String mockResponseBody = "{\"errors\":[\"" + errorMessage + "\"]}";
        PersonIdentity testPersonIdentity = createTestPersonIdentity();
        when(mockObjectMapper.readValue(requestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockObjectMapper.writeValueAsString(any(ValidationResult.class)))
                .thenReturn(mockResponseBody);

        when(inputValidationExecutor.performInputValidation(testPersonIdentity))
                .thenReturn(new ValidationResult(List.of("firstname must not be null or empty")));

        Response mockResponse = Mockito.mock(Response.class);
        Request mockRequest = Mockito.mock(Request.class);
        when(mockRequest.body()).thenReturn(requestBody);

        identityCheckResource.performIdentityCheckRoute.handle(mockRequest, mockResponse);

        verify(mockObjectMapper).readValue(requestBody, PersonIdentity.class);
        verify(mockIdentityVerificationService, never()).verifyIdentity(testPersonIdentity);
        verify(mockResponse).status(HttpServletResponse.SC_BAD_REQUEST);
        verify(mockResponse).header("Content-Type", "application/json");
        verify(mockResponse).body(mockResponseBody);
    }
}
