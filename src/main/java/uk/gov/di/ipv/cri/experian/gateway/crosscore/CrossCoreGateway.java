package uk.gov.di.ipv.cri.experian.gateway.crosscore;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.ipv.cri.experian.config.CrossCoreApiConfig;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.crosscore.dto.CrossCoreApiRequest;
import uk.gov.di.ipv.cri.experian.security.HmacGenerator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class CrossCoreGateway {

    private final HttpClient httpClient;
    private final CrossCoreApiRequestMapper requestMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;
    private final CrossCoreApiConfig crossCoreApiConfig;

    public CrossCoreGateway(
            HttpClient httpClient,
            CrossCoreApiRequestMapper requestMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            CrossCoreApiConfig crossCoreApiConfig) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(crossCoreApiConfig, "crossCoreApiConfig must not be null");

        this.httpClient = httpClient;
        this.requestMapper = requestMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.crossCoreApiConfig = crossCoreApiConfig;
    }

    public String performIdentityCheck(PersonIdentity personIdentity)
            throws IOException, InterruptedException {
        CrossCoreApiRequest apiRequest = requestMapper.mapPersonIdentity(personIdentity);
        String requestBody = objectMapper.writeValueAsString(apiRequest);
        String requestBodyHmac = hmacGenerator.generateHmac(requestBody);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(crossCoreApiConfig.getEndpointUri()))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("hmac-signature", requestBodyHmac)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
