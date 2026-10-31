package br.com.helder.productactivation.application.port;

import br.com.helder.productactivation.domain.model.ActivationModel;

import java.util.Optional;

public interface ActivationPersistencePort {

    ActivationModel save(ActivationModel activation);

    Optional<ActivationModel> findByExternalId(String externalId);
}
