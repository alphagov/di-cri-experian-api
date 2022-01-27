package uk.gov.di.ipv.cri.experian.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.dto.CrossCoreApiRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class ExperianGateway {

    private final HttpClient httpClient;
    private final ExperianApiRequestMapper requestMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;
    private final URI experianEndpointUri;

    public ExperianGateway(
            HttpClient httpClient,
            ExperianApiRequestMapper requestMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String experianEndpointUrl) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(experianEndpointUrl, "experianEndpointUri must not be null");
        if (experianEndpointUrl.isEmpty() || experianEndpointUrl.isBlank()) {
            throw new IllegalArgumentException("experianEndpointUrl must be specified");
        }
        this.httpClient = httpClient;
        this.requestMapper = requestMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.experianEndpointUri = URI.create(experianEndpointUrl);
    }

    public String performIdentityCheck(PersonIdentity personIdentity)
            throws IOException, InterruptedException {
        CrossCoreApiRequest apiRequest = requestMapper.mapPersonIdentity(personIdentity);
        String requestBody = objectMapper.writeValueAsString(apiRequest);
        String requestBodyHmac = hmacGenerator.generateHmac(requestBody);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(experianEndpointUri)
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
