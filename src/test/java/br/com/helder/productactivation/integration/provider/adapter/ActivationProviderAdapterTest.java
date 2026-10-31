
package br.com.helder.productactivation.integration.provider.adapter;

import br.com.helder.productactivation.application.enums.ProviderErrorMessages;
import br.com.helder.productactivation.application.exception.ProviderBadRequestException;
import br.com.helder.productactivation.application.exception.ProviderTimeoutException;
import br.com.helder.productactivation.application.exception.ProviderUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ActivationProviderAdapterTest {

    private static final String PROVIDER_URL = "http://localhost:8089";
    private static final String PROVIDER_ENDPOINT = "/provider/v1/activate";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;
    private ActivationProviderAdapter adapter;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        JdkClientHttpRequestFactory requestFactory =
            new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        restClient = RestClient.builder()
            .baseUrl(PROVIDER_URL)
            .requestFactory(requestFactory)
            .build();

        adapter = new ActivationProviderAdapter(restClient);
    }

    @Test
    void shouldActivateProductSuccessfully() throws Exception {
        // Arrange
        String externalId = "EXT-001";
        String customerId = "CUSTOMER-001";
        String productCode = "PRODUCT-001";

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

        JsonNode jsonBody = objectMapper.readTree(
            request.path("body").asText()
        );

        assertEquals(
            externalId,
            jsonBody.path("externalId").asText()
        );

        assertEquals(
            customerId,
            jsonBody.path("customerId").asText()
        );

        assertEquals(
            productCode,
            jsonBody.path("productCode").asText()
        );
    }

    @Test
    void shouldThrowExceptionWhenProviderReturnsBadRequest() {
        // Act
        ProviderBadRequestException exception = assertThrows(
            ProviderBadRequestException.class,
            () -> adapter.activate(
                "EXT-400",
                "CUSTOMER-001",
                "PRODUCT-001"
            )
        );

        // Assert
        assertEquals(
            ProviderErrorMessages.BAD_REQUEST.getMessage(),
            exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenProviderReturnsInternalServerError() {
        // Act
        ProviderUnavailableException exception = assertThrows(
            ProviderUnavailableException.class,
            () -> adapter.activate(
                "EXT-500",
                "CUSTOMER-001",
                "PRODUCT-001"
            )
        );

        // Assert
        assertEquals(
            ProviderErrorMessages.UNAVAILABLE.getMessage(),
            exception.getMessage()
        );

        assertInstanceOf(
            HttpServerErrorException.class,
            exception.getCause()
        );
    }

    @Test
    void shouldThrowExceptionWhenProviderTimesOut() {
        // Act
        ProviderTimeoutException exception = assertThrows(
            ProviderTimeoutException.class,
            () -> adapter.activate(
                "EXT-TIMEOUT",
                "CUSTOMER-001",
                "PRODUCT-001"
            )
        );

        // Assert
        assertEquals(
            ProviderErrorMessages.TIMEOUT.getMessage(),
            exception.getMessage()
        );

        assertNotNull(exception.getCause());
    }
}
