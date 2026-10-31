
package br.com.helder.productactivation.application.usecase;

import br.com.helder.productactivation.application.exception.ActivationAlreadyExistsException;
import br.com.helder.productactivation.application.exception.ProviderBadRequestException;
import br.com.helder.productactivation.application.exception.ProviderUnavailableException;
import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.application.port.ActivationProviderPort;
import br.com.helder.productactivation.application.port.CreateActivationPort;
import br.com.helder.productactivation.domain.enums.ExceptionMessages;
import br.com.helder.productactivation.domain.exception.ActivationConflictException;
import br.com.helder.productactivation.domain.model.ActivationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateActivationUseCase implements CreateActivationPort {

    private final ActivationPersistencePort persistencePort;
    private final ActivationProviderPort providerPort;

    @Override
    public ActivationModel createActivation(
        String externalId,
        String customerId,
        String productCode
    ) {
        ActivationModel activation = ActivationModel.create(
            externalId,
            customerId,
            productCode
        );

        Optional<ActivationModel> existing =
            persistencePort.findByExternalId(externalId);

        if (existing.isPresent()) {
            return validateIdempotency(
                existing.get(),
                customerId,
                productCode
            );
        }

        return createAndProcess(activation);
    }

    private ActivationModel createAndProcess(ActivationModel activation) {
        try {
            activation = persistencePort.create(activation);
        } catch (ActivationAlreadyExistsException exception) {
            return recoverExistingActivation(activation);
        }

        return processActivation(activation);
    }

    private ActivationModel recoverExistingActivation(ActivationModel activation) {
        ActivationModel existing = persistencePort
            .findByExternalId(activation.getExternalId())
            .orElseThrow(() -> new IllegalStateException(
                ExceptionMessages.ACTIVATION_NOT_FOUND_AFTER_CONFLICT.getMessage()
            ));

        return validateIdempotency(
            existing,
            activation.getCustomerId(),
            activation.getProductCode()
        );
    }

    private ActivationModel validateIdempotency(
        ActivationModel existing,
        String customerId,
        String productCode
    ) {
        boolean sameRequest =
            existing.getCustomerId().equals(customerId)
                && existing.getProductCode().equals(productCode);

        if (!sameRequest) {
            throw new ActivationConflictException(
                ExceptionMessages.EXTERNAL_ID_CONFLICT.getMessage()
            );
        }

        return existing;
    }

    private ActivationModel processActivation(ActivationModel activation) {
        activation.startProcessing();
        activation = persistencePort.save(activation);

        try {
            activateWithProvider(activation);
            activation.complete();
        } catch (ProviderBadRequestException | ProviderUnavailableException exception) {
            activation.fail();
        }

        return persistencePort.save(activation);
    }

    private void activateWithProvider(ActivationModel activation) {
        providerPort.activate(
            activation.getExternalId(),
            activation.getCustomerId(),
            activation.getProductCode()
        );
    }
}
