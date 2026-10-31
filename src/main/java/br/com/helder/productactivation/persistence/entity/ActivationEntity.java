package br.com.helder.productactivation.persistence.entity;

import br.com.helder.productactivation.domain.enums.ActivationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivationEntity {

    @Id
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ActivationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
