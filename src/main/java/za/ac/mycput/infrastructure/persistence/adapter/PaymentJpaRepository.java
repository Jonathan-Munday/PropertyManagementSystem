package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.PaymentJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    Optional<PaymentJpaEntity> findByOrganisationIdAndPaymentId(UUID organisationId, UUID paymentId);
    List<PaymentJpaEntity> findByOrganisationId(UUID organisationId);
    List<PaymentJpaEntity> findByOrganisationIdAndPaymentMethodIgnoreCase(UUID organisationId, String paymentMethod);
    boolean existsByOrganisationIdAndPaymentId(UUID organisationId, UUID paymentId);
    void deleteByOrganisationIdAndPaymentId(UUID organisationId, UUID paymentId);
}
