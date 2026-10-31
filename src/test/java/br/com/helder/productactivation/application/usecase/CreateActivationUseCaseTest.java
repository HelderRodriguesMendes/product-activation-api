
package br.com.helder.productactivation.application.usecase;

import br.com.helder.productactivation.application.exception.ActivationAlreadyExistsException;
import br.com.helder.productactivation.application.exception.ProviderBadRequestException;
import br.com.helder.productactivation.application.exception.ProviderTimeoutException;
import br.com.helder.productactivation.application.exception.ProviderUnavailableException;
import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.application.port.ActivationProviderPort;
import br.com.helder.productactivation.domain.enums.ActivationStatus;
import br.com.helder.productactivation.domain.exception.ActivationConflictException;
import br.com.helder.productactivation.domain.model.ActivationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateActivationUseCaseTest {

    @Mock
    private ActivationPersistencePort persistencePort;

    @Mock
    private ActivationProviderPort providerPort;

    @InjectMocks
    private CreateActivationUseCase useCase;

    private ActivationModel existingActivation;

    @BeforeEach
    void setUp() {
        existingActivation = ActivationModel.create(
            "EXT-001",
            "CUSTOMER-001",
            "PRODUCT-001"
        );
    }

    @Test
    void shouldReturnExistingActivationWhenRequestDataIsTheSame() {
        when(persistencePort.findByExternalId("EXT-001"))
            .thenReturn(Optional.of(existingActivation));

        ActivationModel result = useCase.createActivation(
            "EXT-001",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        assertSame(existingActivation, result);

        verify(persistencePort).findByExternalId("EXT-001");
        verify(persistencePort, never()).create(any(ActivationModel.class));
        verify(persistencePort, never()).save(any(ActivationModel.class));
        verifyNoInteractions(providerPort);
    }

    @Test
    void shouldThrowConflictWhenExternalIdExistsWithDifferentData() {
        when(persistencePort.findByExternalId("EXT-001"))
            .thenReturn(Optional.of(existingActivation));

        assertThrows(
            ActivationConflictException.class,
            () -> useCase.createActivation(
                "EXT-001",
                "CUSTOMER-002",
                "PRODUCT-001"
            )
        );

        verify(persistencePort).findByExternalId("EXT-001");
        verify(persistencePort, never()).create(any(ActivationModel.class));
        verify(persistencePort, never()).save(any(ActivationModel.class));
        verifyNoInteractions(providerPort);
    }

    @Test
    void shouldCreateAndCompleteActivationSuccessfully() {
        when(persistencePort.findByExternalId("EXT-002"))
            .thenReturn(Optional.empty());

        List<ActivationStatus> savedStatuses = mockPersistence();

        ActivationModel result = useCase.createActivation(
            "EXT-002",
            "CUSTOMER-002",
            "PRODUCT-002"
        );

        assertEquals(ActivationStatus.COMPLETED, result.getStatus());

        assertEquals(
            List.of(
                ActivationStatus.PENDING,
                ActivationStatus.PROCESSING,
                ActivationStatus.COMPLETED
            ),
            savedStatuses
        );

        verify(providerPort).activate(
            "EXT-002",
            "CUSTOMER-002",
            "PRODUCT-002"
        );

        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, times(2)).save(any(ActivationModel.class));
    }

    @Test
    void shouldFailActivationWhenProviderReturnsBadRequest() {
        when(persistencePort.findByExternalId("EXT-400"))
            .thenReturn(Optional.empty());

        List<ActivationStatus> savedStatuses = mockPersistence();

        doThrow(new ProviderBadRequestException("Bad request"))
            .when(providerPort)
            .activate("EXT-400", "CUSTOMER-001", "PRODUCT-001");

        ActivationModel result = useCase.createActivation(
            "EXT-400",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        assertEquals(ActivationStatus.FAILED, result.getStatus());

        assertEquals(
            List.of(
                ActivationStatus.PENDING,
                ActivationStatus.PROCESSING,
                ActivationStatus.FAILED
            ),
            savedStatuses
        );

        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, times(2)).save(any(ActivationModel.class));
    }

    @Test
    void shouldFailActivationWhenProviderIsUnavailable() {
        when(persistencePort.findByExternalId("EXT-500"))
            .thenReturn(Optional.empty());

        List<ActivationStatus> savedStatuses = mockPersistence();

        doThrow(new ProviderUnavailableException(
            "Provider unavailable",
            new RuntimeException("HTTP 500")
        ))
            .when(providerPort)
            .activate("EXT-500", "CUSTOMER-001", "PRODUCT-001");

        ActivationModel result = useCase.createActivation(
            "EXT-500",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        assertEquals(ActivationStatus.FAILED, result.getStatus());

        assertEquals(
            List.of(
                ActivationStatus.PENDING,
                ActivationStatus.PROCESSING,
                ActivationStatus.FAILED
            ),
            savedStatuses
        );

        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, times(2)).save(any(ActivationModel.class));
    }

    @Test
    void shouldFailActivationWhenProviderTimesOut() {
        when(persistencePort.findByExternalId("EXT-TIMEOUT"))
            .thenReturn(Optional.empty());

        List<ActivationStatus> savedStatuses = mockPersistence();

        doThrow(new ProviderTimeoutException(
            "Provider timeout",
            new RuntimeException("Read timed out")
        ))
            .when(providerPort)
            .activate("EXT-TIMEOUT", "CUSTOMER-001", "PRODUCT-001");

        ActivationModel result = useCase.createActivation(
            "EXT-TIMEOUT",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        assertEquals(ActivationStatus.FAILED, result.getStatus());

        assertEquals(
            List.of(
                ActivationStatus.PENDING,
                ActivationStatus.PROCESSING,
                ActivationStatus.FAILED
            ),
            savedStatuses
        );

        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, times(2)).save(any(ActivationModel.class));
    }

    @Test
    void shouldReturnExistingActivationWhenConcurrentRequestHasSameData() {
        when(persistencePort.findByExternalId("EXT-001"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingActivation));

        when(persistencePort.create(any(ActivationModel.class)))
            .thenThrow(new ActivationAlreadyExistsException(
                new RuntimeException("Duplicate externalId")
            ));

        ActivationModel result = useCase.createActivation(
            "EXT-001",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        assertSame(existingActivation, result);

        verify(persistencePort, times(2)).findByExternalId("EXT-001");
        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, never()).save(any(ActivationModel.class));
        verifyNoInteractions(providerPort);
    }

    @Test
    void shouldThrowConflictWhenConcurrentRequestHasDifferentData() {
        when(persistencePort.findByExternalId("EXT-001"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingActivation));

        when(persistencePort.create(any(ActivationModel.class)))
            .thenThrow(new ActivationAlreadyExistsException(
                new RuntimeException("Duplicate externalId")
            ));

        assertThrows(
            ActivationConflictException.class,
            () -> useCase.createActivation(
                "EXT-001",
                "CUSTOMER-002",
                "PRODUCT-001"
            )
        );

        verify(persistencePort, times(2)).findByExternalId("EXT-001");
        verify(persistencePort).create(any(ActivationModel.class));
        verify(persistencePort, never()).save(any(ActivationModel.class));
        verifyNoInteractions(providerPort);
    }

    private List<ActivationStatus> mockPersistence() {
        List<ActivationStatus> savedStatuses = new ArrayList<>();

        when(persistencePort.create(any(ActivationModel.class)))
            .thenAnswer(invocation -> {
                ActivationModel activation = invocation.getArgument(0);
                savedStatuses.add(activation.getStatus());
                return activation;
            });

        when(persistencePort.save(any(ActivationModel.class)))
            .thenAnswer(invocation -> {
                ActivationModel activation = invocation.getArgument(0);
                savedStatuses.add(activation.getStatus());
                return activation;
            });

        return savedStatuses;
    }
}