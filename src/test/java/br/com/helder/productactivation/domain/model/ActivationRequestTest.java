package br.com.helder.productactivation.domain.model;

import br.com.helder.productactivation.domain.enums.ActivationStatus;
import br.com.helder.productactivation.domain.exception.ActionDomainException;
import br.com.helder.productactivation.domain.exception.IllegalStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivationRequestTest {

    private static final String EXTERNAL_ID = "EXT-12345";
    private static final String CUSTOMER_ID = "CUST-10001";
    private static final String PRODUCT_CODE = "INTERNET-500";

    private ActivationRequest createValidRequest() {
        return ActivationRequest.create(
            EXTERNAL_ID,
            CUSTOMER_ID,
            PRODUCT_CODE
        );
    }

    @Test
    void shouldCreateActivationRequestSuccessfully() {
        ActivationRequest request = createValidRequest();

        assertNotNull(request.getId());
        assertEquals(EXTERNAL_ID, request.getExternalId());
        assertEquals(CUSTOMER_ID, request.getCustomerId());
        assertEquals(PRODUCT_CODE, request.getProductCode());
        assertEquals(ActivationStatus.PENDING, request.getStatus());
        assertNotNull(request.getCreatedAt());
        assertEquals(request.getCreatedAt(), request.getUpdatedAt());
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentRequests() {
        ActivationRequest first = createValidRequest();
        ActivationRequest second = createValidRequest();

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void shouldThrowExceptionWhenExternalIdIsInvalid() {
        assertAll(
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(null, CUSTOMER_ID, PRODUCT_CODE)),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create("", CUSTOMER_ID, PRODUCT_CODE)),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create("   ", CUSTOMER_ID, PRODUCT_CODE))
        );
    }

    @Test
    void shouldThrowExceptionWhenCustomerIdIsInvalid() {
        assertAll(
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, null, PRODUCT_CODE)),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, "", PRODUCT_CODE)),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, "   ", PRODUCT_CODE))
        );
    }

    @Test
    void shouldThrowExceptionWhenProductCodeIsInvalid() {
        assertAll(
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, CUSTOMER_ID, null)),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, CUSTOMER_ID, "")),
            () -> assertThrows(ActionDomainException.class,
                () -> ActivationRequest.create(EXTERNAL_ID, CUSTOMER_ID, "   "))
        );
    }

    @Test
    void shouldStartProcessingWhenStatusIsPending() {
        ActivationRequest request = createValidRequest();

        request.startProcessing();

        assertEquals(ActivationStatus.PROCESSING, request.getStatus());
        assertFalse(request.getUpdatedAt().isBefore(request.getCreatedAt()));
    }

    @Test
    void shouldCompleteWhenStatusIsProcessing() {
        ActivationRequest request = createValidRequest();
        request.startProcessing();

        request.complete();

        assertEquals(ActivationStatus.COMPLETED, request.getStatus());
    }

    @Test
    void shouldFailWhenStatusIsProcessing() {
        ActivationRequest request = createValidRequest();
        request.startProcessing();

        request.fail();

        assertEquals(ActivationStatus.FAILED, request.getStatus());
    }

    @Test
    void shouldNotStartProcessingWhenStatusIsNotPending() {
        ActivationRequest request = createValidRequest();
        request.startProcessing();

        assertThrows(
            IllegalStatusException.class,
            request::startProcessing
        );

        assertEquals(ActivationStatus.PROCESSING, request.getStatus());
    }

    @Test
    void shouldNotCompleteWhenStatusIsPending() {
        ActivationRequest request = createValidRequest();

        assertThrows(
            IllegalStatusException.class,
            request::complete
        );

        assertEquals(ActivationStatus.PENDING, request.getStatus());
    }

    @Test
    void shouldNotFailWhenStatusIsPending() {
        ActivationRequest request = createValidRequest();

        assertThrows(
            IllegalStatusException.class,
            request::fail
        );

        assertEquals(ActivationStatus.PENDING, request.getStatus());
    }

    @Test
    void shouldNotChangeStatusAfterCompletion() {
        ActivationRequest request = createValidRequest();
        request.startProcessing();
        request.complete();

        assertAll(
            () -> assertThrows(IllegalStatusException.class,
                request::startProcessing),
            () -> assertThrows(IllegalStatusException.class,
                request::complete),
            () -> assertThrows(IllegalStatusException.class,
                request::fail)
        );

        assertEquals(ActivationStatus.COMPLETED, request.getStatus());
    }

    @Test
    void shouldNotChangeStatusAfterFailure() {
        ActivationRequest request = createValidRequest();
        request.startProcessing();
        request.fail();

        assertAll(
            () -> assertThrows(IllegalStatusException.class,
                request::startProcessing),
            () -> assertThrows(IllegalStatusException.class,
                request::complete),
            () -> assertThrows(IllegalStatusException.class,
                request::fail)
        );

        assertEquals(ActivationStatus.FAILED, request.getStatus());
    }
}