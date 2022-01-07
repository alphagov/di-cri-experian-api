package uk.gov.di.ipv.cri.experian.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.experian.config.CrossCoreApiConfig;
import uk.gov.di.ipv.cri.experian.domain.AddressType;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.dto.CrossCoreApiRequest;

import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.experian.util.TestDataCreator.createTestPersonIdentity;

@ExtendWith(MockitoExtension.class)
class CrossCoreGatewayTest {

    private class CrossCoreGatewayConstructorArgs {
        private final HttpClient httpClient;
        private final CrossCoreApiRequestMapper requestMapper;
        private final ObjectMapper objectMapper;
        private final HmacGenerator hmacGenerator;
        private final CrossCoreApiConfig crossCoreApiConfig;

        private CrossCoreGatewayConstructorArgs(
                HttpClient httpClient,
                CrossCoreApiRequestMapper requestMapper,
                ObjectMapper objectMapper,
                HmacGenerator hmacGenerator,
                CrossCoreApiConfig crossCoreApiConfig) {

            this.httpClient = httpClient;
            this.requestMapper = requestMapper;
            this.objectMapper = objectMapper;
            this.hmacGenerator = hmacGenerator;
            this.crossCoreApiConfig = crossCoreApiConfig;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private CrossCoreGateway crossCoreGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private CrossCoreApiRequestMapper mockRequestMapper;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private HmacGenerator mockHmacGenerator;
    @Mock private CrossCoreApiConfig mockCrossCoreApiConfig;

    @BeforeEach
    void setUp() {
        this.crossCoreGateway =
                new CrossCoreGateway(
                        mockHttpClient,
                        mockRequestMapper,
                        mockObjectMapper,
                        mockHmacGenerator,
                        mockCrossCoreApiConfig);
    }

    @Test
    void shouldInvokeExperianApi() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final CrossCoreApiRequest testApiRequest = new CrossCoreApiRequest();
        final String testEndpointUri = "https://test-endpoint";
        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity = createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);
        when(this.mockCrossCoreApiConfig.getEndpointUri()).thenReturn(testEndpointUri);
        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse());

        String identityCheckResult = crossCoreGateway.performIdentityCheck(personIdentity);

        assertEquals(TEST_API_RESPONSE_BODY, identityCheckResult);
        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);
        verify(mockCrossCoreApiConfig).getEndpointUri();
        verify(mockHttpClient)
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertEquals(testEndpointUri, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, CrossCoreGatewayConstructorArgs> testCases =
                Map.of(
                        "httpClient must not be null",
                        new CrossCoreGatewayConstructorArgs(null, null, null, null, null),
                        "requestMapper must not be null",
                        new CrossCoreGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class), null, null, null, null),
                        "objectMapper must not be null",
                        new CrossCoreGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(CrossCoreApiRequestMapper.class),
                                null,
                                null,
                                null),
                        "hmacGenerator must not be null",
                        new CrossCoreGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(CrossCoreApiRequestMapper.class),
                                Mockito.mock(ObjectMapper.class),
                                null,
                                null),
                        "crossCoreApiConfig must not be null",
                        new CrossCoreGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(CrossCoreApiRequestMapper.class),
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(HmacGenerator.class),
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) -> {
                    assertThrows(
                            NullPointerException.class,
                            () -> {
                                new CrossCoreGateway(
                                        constructorArgs.httpClient,
                                        constructorArgs.requestMapper,
                                        constructorArgs.objectMapper,
                                        constructorArgs.hmacGenerator,
                                        constructorArgs.crossCoreApiConfig);
                            },
                            errorMessage);
                });
    }

    private HttpResponse<String> createMockApiResponse() {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return HttpServletResponse.SC_OK;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return TEST_API_RESPONSE_BODY;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}
