package br.com.helder.productactivation.application.usecase;

import br.com.helder.productactivation.application.exception.ActivationNotFoundException;
import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.application.port.GetActivationPort;
import br.com.helder.productactivation.domain.enums.ExceptionMessages;
import br.com.helder.productactivation.domain.model.ActivationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetActivationUseCase implements GetActivationPort {

    private final ActivationPersistencePort persistencePort;

    @Override
    public ActivationModel getActivation(UUID id) {
        return persistencePort.findById(id)
            .orElseThrow(() -> new ActivationNotFoundException(
                ExceptionMessages.ACTIVATION_NOT_FOUND.getMessage()
            ));
    }
}
