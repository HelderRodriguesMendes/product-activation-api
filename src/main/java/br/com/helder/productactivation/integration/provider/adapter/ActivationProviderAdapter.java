
package br.com.helder.productactivation.integration.provider.adapter;

import br.com.helder.productactivation.application.enums.ProviderErrorMessages;
import br.com.helder.productactivation.application.exception.ProviderBadRequestException;
import br.com.helder.productactivation.application.exception.ProviderTimeoutException;
import br.com.helder.productactivation.application.exception.ProviderUnavailableException;
import br.com.helder.productactivation.application.port.ActivationProviderPort;
import br.com.helder.productactivation.integration.provider.dto.ProviderActivationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

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

        try {
            restClient.post()
                .uri(PROVIDER_URL)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ProviderBadRequestException(
                    ProviderErrorMessages.BAD_REQUEST.getMessage()
                );
            }

            throw new ProviderUnavailableException(
                ProviderErrorMessages.CLIENT_ERROR.getMessage(),
                exception
            );

        } catch (HttpServerErrorException exception) {
            throw new ProviderUnavailableException(
                ProviderErrorMessages.UNAVAILABLE.getMessage(),
                exception
            );

        } catch (ResourceAccessException exception) {
            if (isTimeout(exception)) {
                throw new ProviderTimeoutException(
                    ProviderErrorMessages.TIMEOUT.getMessage(),
                    exception
                );
            }

            throw new ProviderUnavailableException(
                ProviderErrorMessages.CONNECTION_ERROR.getMessage(),
                exception
            );
        }
    }

    private boolean isTimeout(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof HttpTimeoutException
                || cause instanceof SocketTimeoutException) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
