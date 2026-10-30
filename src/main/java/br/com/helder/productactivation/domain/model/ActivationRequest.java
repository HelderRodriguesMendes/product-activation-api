package br.com.helder.productactivation.domain.model;

import br.com.helder.productactivation.domain.enums.ActivationStatus;
import br.com.helder.productactivation.domain.enums.ExceptionMessages;
import br.com.helder.productactivation.domain.exception.ActionDomainException;
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
        validateExternalId(externalId);
        validateCustomerId(customerId);
        validateProductCode(productCode);

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
                ExceptionMessages.NOT_STATUS_CHANGED_PROCESSING.getMessage()
            );
        }

        updateStatus(ActivationStatus.PROCESSING);
    }

    public void complete() {
        if (status != ActivationStatus.PROCESSING) {
            throw new IllegalStatusException(
                ExceptionMessages.NOT_STATUS_CHANGED_COMPLETED.getMessage()
            );
        }

        updateStatus(ActivationStatus.COMPLETED);
    }

    public void fail() {
        if (status != ActivationStatus.PROCESSING) {
            throw new IllegalStatusException(
                ExceptionMessages.NOT_STATUS_CHANGED_FAILED.getMessage()
            );
        }

        updateStatus(ActivationStatus.FAILED);
    }

    private static void validateExternalId(String externalId){
        if (externalId == null || externalId.isBlank()) {
            throw new ActionDomainException(
                ExceptionMessages.EXTERNAL_ID_REQUIRED.getMessage()
            );
        }
    }

    private static void validateCustomerId(String customerId){
        if (customerId == null || customerId.isBlank()) {
            throw new ActionDomainException(
                ExceptionMessages.CUSTOMER_ID_REQUIRED.getMessage()
            );
        }
    }

    private static void validateProductCode(String productCode){
        if (productCode == null || productCode.isBlank()) {
            throw new ActionDomainException(
                ExceptionMessages.PRODUCT_CODE_REQUIRED.getMessage()
            );
        }
    }

    private void updateStatus(ActivationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
}
