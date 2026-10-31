package br.com.helder.productactivation.persistence.repository;

import aj.org.objectweb.asm.commons.Remapper;
import br.com.helder.productactivation.persistence.entity.ActivationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationRepository extends JpaRepository<ActivationEntity, UUID> {
    Optional<ActivationEntity> findByExternalId(String externalId);
}
