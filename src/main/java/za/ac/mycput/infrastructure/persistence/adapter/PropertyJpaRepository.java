package za.ac.mycput.infrastructure.persistence.adapter;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.mycput.infrastructure.persistence.entity.PropertyJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, UUID> {
    Optional<PropertyJpaEntity> findByOrganisationIdAndPropertyId(UUID organisationId, UUID propertyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select property from PropertyJpaEntity property
            where property.organisationId = :organisationId and property.propertyId = :propertyId
            """)
    Optional<PropertyJpaEntity> findByOrganisationIdAndPropertyIdForUpdate(
            @Param("organisationId") UUID organisationId,
            @Param("propertyId") UUID propertyId);

    List<PropertyJpaEntity> findByOrganisationId(UUID organisationId);

    List<PropertyJpaEntity> findByOrganisationIdAndAvailableTrue(UUID organisationId);

    long countByOrganisationId(UUID organisationId);

    boolean existsByOrganisationIdAndPropertyId(UUID organisationId, UUID propertyId);

    void deleteByOrganisationIdAndPropertyId(UUID organisationId, UUID propertyId);
}
