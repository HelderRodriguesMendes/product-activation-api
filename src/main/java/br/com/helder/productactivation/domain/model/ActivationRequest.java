package br.com.helder.productactivation.domain.model;

import br.com.helder.productactivation.domain.enums.ActivationStatus;
import br.com.helder.productactivation.domain.exception.IllegalStatusException;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActivationRequest {

    private final UUID id;
    private final String externalId;
    private final String customerId;
    private final String productCode;

    private ActivationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static ActivationRequest create(
        String externalId,
        String customerId,
        String productCode
    ) {
        Instant now = Instant.now();

        return new ActivationRequest(
            UUID.randomUUID(),
            externalId,
            customerId,
            productCode,
            ActivationStatus.PENDING,
            now,
            now
        );
    }

    public void startProcessing() {
        if (status != ActivationStatus.PENDING) {
            throw new IllegalStatusException(
                "Only PENDING requests can start processing"
            );
        }

        updateStatus(ActivationStatus.PROCESSING);
    }

    public void complete() {
        if (status != ActivationStatus.PROCESSING) {
            throw new IllegalStatusException(
                "Only PROCESSING requests can be completed"
            );
        }

        updateStatus(ActivationStatus.COMPLETED);
    }

    public void fail() {
        if (status != ActivationStatus.PROCESSING) {
            throw new IllegalStatusException(
                "Only PROCESSING requests can fail"
            );
        }

        updateStatus(ActivationStatus.FAILED);
    }

    private void updateStatus(ActivationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
}
