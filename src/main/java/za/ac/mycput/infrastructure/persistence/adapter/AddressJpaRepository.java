package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.AddressJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {
    Optional<AddressJpaEntity> findByOrganisationIdAndAddressId(UUID organisationId, UUID addressId);
    List<AddressJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndAddressId(UUID organisationId, UUID addressId);
    void deleteByOrganisationIdAndAddressId(UUID organisationId, UUID addressId);
}
