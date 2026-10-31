
package br.com.helder.productactivation.persistence.adapter;

import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import br.com.helder.productactivation.persistence.mapper.ActivationPersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ActivationInsertService {

    private final EntityManager entityManager;
    private final ActivationPersistenceMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActivationModel insert(ActivationModel activation) {
        ActivationEntity entity = mapper.toEntity(activation);

        entityManager.persist(entity);
        entityManager.flush();

        return mapper.toModel(entity);
    }
}
