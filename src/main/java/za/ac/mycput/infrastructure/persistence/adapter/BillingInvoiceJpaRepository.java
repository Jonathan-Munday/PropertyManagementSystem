package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.BillingInvoiceJpaEntity;

import java.util.List;
import java.util.UUID;

public interface BillingInvoiceJpaRepository extends JpaRepository<BillingInvoiceJpaEntity, UUID> {

    List<BillingInvoiceJpaEntity> findByOrganisationIdOrderByIssuedOnDesc(UUID organisationId);

    void deleteByOrganisationId(UUID organisationId);
}
