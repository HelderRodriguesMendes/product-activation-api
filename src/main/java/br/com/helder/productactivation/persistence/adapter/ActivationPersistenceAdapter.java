package br.com.helder.productactivation.persistence.adapter;

import br.com.helder.productactivation.application.exception.ActivationAlreadyExistsException;
import br.com.helder.productactivation.application.port.ActivationPersistencePort;
import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import br.com.helder.productactivation.persistence.mapper.ActivationPersistenceMapper;
import br.com.helder.productactivation.persistence.repository.ActivationRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ActivationPersistenceAdapter implements ActivationPersistencePort {

    private static final String EXTERNAL_ID_CONSTRAINT =
        "uk_activation_requests_external_id";

    private final ActivationRepository repository;
    private final ActivationPersistenceMapper mapper;
    private final ActivationInsertService insertService;

    @Override
    public ActivationModel create(ActivationModel activation) {
        try {
            return insertService.insert(activation);
        } catch (DataIntegrityViolationException exception) {
            if (isExternalIdConflict(exception)) {
                throw new ActivationAlreadyExistsException(exception);
            }

            throw exception;
        }
    }

    @Override
    public Optional<ActivationModel> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toModel);
    }

    @Override
    public ActivationModel save(ActivationModel activation) {
        ActivationEntity entity = mapper.toEntity(activation);
        ActivationEntity saved = repository.save(entity);

        return mapper.toModel(saved);
    }

    @Override
    public Optional<ActivationModel> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId)
            .map(mapper::toModel);
    }

    private boolean isExternalIdConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "23505".equals(violation.getSQLState())
                    && EXTERNAL_ID_CONSTRAINT.equals(
                    violation.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }
}
