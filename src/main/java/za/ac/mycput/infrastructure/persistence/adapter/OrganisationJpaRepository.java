package za.ac.mycput.infrastructure.persistence.adapter;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select organisation from OrganisationJpaEntity organisation where organisation.id = :id")
    Optional<OrganisationJpaEntity> lockById(@Param("id") UUID id);
}
