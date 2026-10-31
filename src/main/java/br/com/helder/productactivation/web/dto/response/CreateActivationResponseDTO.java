package br.com.helder.productactivation.web.dto.response;

import br.com.helder.productactivation.domain.enums.ActivationStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateActivationResponseDTO(
    UUID id,
    String externalId,
    String customerId,
    String productCode,
    ActivationStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
