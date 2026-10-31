package br.com.helder.productactivation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionMessages {
    NOT_STATUS_CHANGED_COMPLETED("Only PROCESSING requests can be completed"),
    NOT_STATUS_CHANGED_FAILED("Only PROCESSING requests can fail"),
    NOT_STATUS_CHANGED_PROCESSING("Only PENDING requests can start processing"),

    EXTERNAL_ID_CONFLICT("An activation request already exists with this externalId and different data."),
    ACTIVATION_NOT_FOUND_AFTER_CONFLICT("Activation not found after unique constraint violation"),

    PRODUCT_CODE_REQUIRED("ProductCode is required"),
    EXTERNAL_ID_REQUIRED("ExternalId is required"),
    CUSTOMER_ID_REQUIRED("CustomerId is required");

    private final String message;
}
