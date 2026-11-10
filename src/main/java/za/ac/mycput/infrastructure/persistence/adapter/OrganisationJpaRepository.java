package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationJpaEntity;

import java.util.UUID;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationJpaEntity, UUID> {
}
