package br.com.helder.productactivation.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateActivationRequestDTO(
    @NotBlank String externalId,
    @NotBlank String customerId,
    @NotBlank String productCode
) {
}