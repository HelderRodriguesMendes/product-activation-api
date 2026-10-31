
package br.com.helder.productactivation.integration.provider.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

class ActivationProviderAdapterTest {

    private static final String PROVIDER_URL = "http://localhost:8089";
    private static final String PROVIDER_ENDPOINT = "/provider/v1/activate";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldActivateProductSuccessfully() throws Exception {
        // Arrange
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        RestClient restClient = RestClient.builder()
            .baseUrl(PROVIDER_URL)
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .build();

        ActivationProviderAdapter adapter =
            new ActivationProviderAdapter(restClient);

        String externalId = "EXT-001";
        String customerId = "CUSTOMER-001";
        String productCode = "PRODUCT-001";

        // Limpa o histórico de requisições do WireMock
        restClient.delete()
            .uri("/__admin/requests")
            .retrieve()
            .toBodilessEntity();

        // Act
        assertDoesNotThrow(() -> adapter.activate(
            externalId,
            customerId,
            productCode
        ));

        // Assert
        String response = restClient.get()
            .uri("/__admin/requests")
            .retrieve()
            .body(String.class);

        JsonNode requests = objectMapper.readTree(response)
            .path("requests");

        assertEquals(1, requests.size());

        JsonNode request = requests.get(0).path("request");

        assertEquals("POST", request.path("method").asText());
        assertEquals(PROVIDER_ENDPOINT, request.path("url").asText());

        String requestBody = request.path("body").asText();
        JsonNode jsonBody = objectMapper.readTree(requestBody);

        assertEquals(externalId, jsonBody.path("externalId").asText());
        assertEquals(customerId, jsonBody.path("customerId").asText());
        assertEquals(productCode, jsonBody.path("productCode").asText());
    }
}
