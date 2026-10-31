package br.com.helder.productactivation.persistence.mapper;

import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import org.springframework.stereotype.Component;

@Component
public class ActivationPersistenceMapper {

    public ActivationEntity toEntity(ActivationModel model) {
        return new ActivationEntity(
            model.getId(),
            model.getExternalId(),
            model.getCustomerId(),
            model.getProductCode(),
            model.getStatus(),
            model.getCreatedAt(),
            model.getUpdatedAt()
        );
    }

    public ActivationModel toModel(ActivationEntity entity) {
        return ActivationModel.restore(
            entity.getId(),
            entity.getExternalId(),
            entity.getCustomerId(),
            entity.getProductCode(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}