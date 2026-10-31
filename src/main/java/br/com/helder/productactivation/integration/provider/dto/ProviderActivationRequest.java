package br.com.helder.productactivation.integration.provider.dto;

public record ProviderActivationRequest(
    String externalId,
    String customerId,
    String productCode
) {
}