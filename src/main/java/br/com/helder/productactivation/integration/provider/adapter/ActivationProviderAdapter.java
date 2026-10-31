package br.com.helder.productactivation.integration.provider.adapter;

import br.com.helder.productactivation.application.port.ActivationProviderPort;
import br.com.helder.productactivation.integration.provider.dto.ProviderActivationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ActivationProviderAdapter implements ActivationProviderPort {

    private static final String PROVIDER_URL = "/provider/v1/activate";

    @Qualifier("activationProviderRestClient")
    private final RestClient restClient;

    @Override
    public void activate(
        String externalId,
        String customerId,
        String productCode
    ) {
        ProviderActivationRequest request = new ProviderActivationRequest(
            externalId,
            customerId,
            productCode
        );

        sendActivationRequest(request);

    }

    private void sendActivationRequest(ProviderActivationRequest request){
        restClient.post()
            .uri(PROVIDER_URL)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}