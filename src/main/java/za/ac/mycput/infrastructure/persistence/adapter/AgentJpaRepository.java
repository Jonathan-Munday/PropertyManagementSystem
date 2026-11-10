package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.AgentJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentJpaRepository extends JpaRepository<AgentJpaEntity, UUID> {
    Optional<AgentJpaEntity> findByOrganisationIdAndAgentId(UUID organisationId, UUID agentId);
    List<AgentJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndAgentId(UUID organisationId, UUID agentId);
    void deleteByOrganisationIdAndAgentId(UUID organisationId, UUID agentId);
}
