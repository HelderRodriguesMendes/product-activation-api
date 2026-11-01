package br.com.helder.productactivation.application.port;

import br.com.helder.productactivation.domain.model.ActivationModel;

import java.util.Optional;
import java.util.UUID;

public interface ActivationPersistencePort {

    ActivationModel save(ActivationModel activation);

    Optional<ActivationModel> findByExternalId(String externalId);

    ActivationModel create(ActivationModel activation);

    Optional<ActivationModel> findById(UUID id);
}
