package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.VendorJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorJpaRepository extends JpaRepository<VendorJpaEntity, UUID> {
    Optional<VendorJpaEntity> findByOrganisationIdAndVendorId(UUID organisationId, UUID vendorId);
    List<VendorJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndVendorId(UUID organisationId, UUID vendorId);
    void deleteByOrganisationIdAndVendorId(UUID organisationId, UUID vendorId);
}
