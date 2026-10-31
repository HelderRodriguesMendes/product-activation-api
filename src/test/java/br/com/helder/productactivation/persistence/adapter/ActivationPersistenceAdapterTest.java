
package br.com.helder.productactivation.persistence.adapter;

import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import br.com.helder.productactivation.persistence.mapper.ActivationPersistenceMapper;
import br.com.helder.productactivation.persistence.repository.ActivationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationPersistenceAdapterTest {

    @Mock
    private ActivationRepository repository;

    @Mock
    private ActivationPersistenceMapper mapper;

    @InjectMocks
    private ActivationPersistenceAdapter adapter;

    @Test
    void shouldSaveActivationSuccessfully() {

        ActivationModel model = createActivationModel();
        ActivationEntity entity = createActivationEntity(model);

        when(mapper.toEntity(model)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toModel(entity)).thenReturn(model);

        ActivationModel result = adapter.save(model);

        assertNotNull(result);
        assertEquals(model.getId(), result.getId());
        assertEquals(model.getExternalId(), result.getExternalId());
        assertEquals(model.getStatus(), result.getStatus());

        verify(mapper).toEntity(model);
        verify(repository).save(entity);
        verify(mapper).toModel(entity);
    }

    @Test
    void shouldFindActivationByExternalId() {

        ActivationModel model = createActivationModel();
        ActivationEntity entity = createActivationEntity(model);

        when(repository.findByExternalId(model.getExternalId()))
            .thenReturn(Optional.of(entity));

        when(mapper.toModel(entity)).thenReturn(model);

        Optional<ActivationModel> result =
            adapter.findByExternalId(model.getExternalId());

        assertTrue(result.isPresent());
        assertEquals(model.getId(), result.get().getId());
        assertEquals(model.getExternalId(), result.get().getExternalId());

        verify(repository).findByExternalId(model.getExternalId());
        verify(mapper).toModel(entity);
    }

    @Test
    void shouldReturnEmptyWhenExternalIdNotFound() {
        String externalId = "EXT-NOT-FOUND";

        when(repository.findByExternalId(externalId))
            .thenReturn(Optional.empty());

        Optional<ActivationModel> result =
            adapter.findByExternalId(externalId);

        assertTrue(result.isEmpty());

        verify(repository).findByExternalId(externalId);
        verifyNoInteractions(mapper);
    }

    private ActivationModel createActivationModel() {
        return ActivationModel.create(
            "EXT-001",
            "CUSTOMER-001",
            "PRODUCT-001"
        );
    }

    private ActivationEntity createActivationEntity(ActivationModel model) {
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
}
