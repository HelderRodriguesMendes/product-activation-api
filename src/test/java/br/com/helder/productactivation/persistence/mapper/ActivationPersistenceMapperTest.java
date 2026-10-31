
package br.com.helder.productactivation.persistence.mapper;

import br.com.helder.productactivation.domain.enums.ActivationStatus;
import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivationPersistenceMapperTest {

    private final ActivationPersistenceMapper mapper =
        new ActivationPersistenceMapper();

    @Test
    void shouldConvertModelToEntity() {
        ActivationModel model = ActivationModel.create(
            "EXT-001",
            "CUSTOMER-001",
            "PRODUCT-001"
        );

        ActivationEntity entity = mapper.toEntity(model);

        assertAll(
            () -> assertEquals(model.getId(), entity.getId()),
            () -> assertEquals(model.getExternalId(), entity.getExternalId()),
            () -> assertEquals(model.getCustomerId(), entity.getCustomerId()),
            () -> assertEquals(model.getProductCode(), entity.getProductCode()),
            () -> assertEquals(model.getStatus(), entity.getStatus()),
            () -> assertEquals(model.getCreatedAt(), entity.getCreatedAt()),
            () -> assertEquals(model.getUpdatedAt(), entity.getUpdatedAt())
        );
    }

    @Test
    void shouldConvertEntityToModel() {
        ActivationModel original = ActivationModel.create(
            "EXT-002",
            "CUSTOMER-002",
            "PRODUCT-002"
        );

        ActivationEntity entity = mapper.toEntity(original);
        ActivationModel restored = mapper.toModel(entity);

        assertAll(
            () -> assertEquals(original.getId(), restored.getId()),
            () -> assertEquals(original.getExternalId(), restored.getExternalId()),
            () -> assertEquals(original.getCustomerId(), restored.getCustomerId()),
            () -> assertEquals(original.getProductCode(), restored.getProductCode()),
            () -> assertEquals(original.getStatus(), restored.getStatus()),
            () -> assertEquals(original.getCreatedAt(), restored.getCreatedAt()),
            () -> assertEquals(original.getUpdatedAt(), restored.getUpdatedAt())
        );
    }

    @Test
    void shouldPreserveCompletedStatusWhenRestoringModel() {
        ActivationModel original = ActivationModel.create(
            "EXT-003",
            "CUSTOMER-003",
            "PRODUCT-003"
        );

        original.startProcessing();
        original.complete();

        ActivationEntity entity = mapper.toEntity(original);
        ActivationModel restored = mapper.toModel(entity);

        assertEquals(ActivationStatus.COMPLETED, restored.getStatus());
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
    }
}
