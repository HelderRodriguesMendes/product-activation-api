
package br.com.helder.productactivation.application.usecase;

import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.domain.exception.ActivationConflictException;
import br.com.helder.productactivation.domain.model.ActivationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateActivationUseCaseTest {

    @Mock
    private ActivationPersistencePort persistencePort;

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
        verify(persistencePort, never()).save(any(ActivationModel.class));
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
        verify(persistencePort, never()).save(any(ActivationModel.class));
    }
}
