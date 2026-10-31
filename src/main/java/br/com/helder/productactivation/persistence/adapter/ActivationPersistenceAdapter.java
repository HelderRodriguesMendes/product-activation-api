package br.com.helder.productactivation.persistence.adapter;

import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import br.com.helder.productactivation.persistence.mapper.ActivationPersistenceMapper;
import br.com.helder.productactivation.persistence.repository.ActivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ActivationPersistenceAdapter implements ActivationPersistencePort {

    private final ActivationRepository repository;
    private final ActivationPersistenceMapper mapper;

    @Override
    public ActivationModel save(ActivationModel activation) {
        ActivationEntity entity = mapper.toEntity(activation);
        ActivationEntity saved = repository.save(entity);

        return mapper.toModel(saved);
    }

    @Override
    public Optional<ActivationModel> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId).map(mapper::toModel);
    }
}
