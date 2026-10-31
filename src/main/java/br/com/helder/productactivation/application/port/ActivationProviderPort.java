package br.com.helder.productactivation.application.port;

public interface ActivationProviderPort {

    void activate(String externalId, String customerId, String productCode);
}
