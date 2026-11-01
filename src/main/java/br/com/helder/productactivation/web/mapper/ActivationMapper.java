package br.com.helder.productactivation.web.mapper;

import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.web.dto.response.CreateActivationResponseDTO;

public final class ActivationMapper {

    private ActivationMapper() {
    }

    public static CreateActivationResponseDTO toResponse(ActivationModel model) {
        return new CreateActivationResponseDTO(
            model.getId(),
            model.getExternalId(),
            model.getCustomerId(),
            model.getProductCode(),
            model.getStatus(),
            model.getCreatedAt(),
            model.getUpdatedAt()
        );
    }
}
