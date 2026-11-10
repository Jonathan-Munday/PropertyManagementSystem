package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Agent;
import za.ac.mycput.domain.repository.IAgentRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.AgentJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.AgentPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaAgentRepository implements IAgentRepository {

    private final AgentJpaRepository store;
    private final AgentPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaAgentRepository(AgentJpaRepository store, AgentPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Agent create(Agent entity) {
        if (entity == null || entity.getAgentId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getAgentId());
        if (store.existsByOrganisationIdAndAgentId(organisationId, id)) return null;
        AgentJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Agent saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "agent", id);
        return saved;
    }

    @Override
    public Agent read(String agentId) {
        if (agentId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndAgentId(persistence.organisationId(), Identifiers.parse(agentId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Agent update(Agent entity) {
        if (entity == null || entity.getAgentId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getAgentId());
        AgentJpaEntity existing = store.findByOrganisationIdAndAgentId(organisationId, id).orElse(null);
        if (existing == null) return null;
        AgentJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Agent saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "agent", id);
        return saved;
    }

    @Override
    public boolean delete(String agentId) {
        if (agentId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(agentId);
        if (!store.existsByOrganisationIdAndAgentId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndAgentId(organisationId, id);
        persistence.audit("DELETE", "agent", id);
        return true;
    }

    @Override
    public List<Agent> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
