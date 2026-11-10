package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Manager;
import za.ac.mycput.domain.repository.IManagerRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.ManagerJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.ManagerPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaManagerRepository implements IManagerRepository {

    private final ManagerJpaRepository store;
    private final ManagerPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaManagerRepository(ManagerJpaRepository store, ManagerPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Manager create(Manager entity) {
        if (entity == null || entity.getManagerId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getManagerId());
        if (store.existsByOrganisationIdAndManagerId(organisationId, id)) return null;
        ManagerJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Manager saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "manager", id);
        return saved;
    }

    @Override
    public Manager read(String managerId) {
        if (managerId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndManagerId(persistence.organisationId(), Identifiers.parse(managerId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Manager update(Manager entity) {
        if (entity == null || entity.getManagerId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getManagerId());
        ManagerJpaEntity existing = store.findByOrganisationIdAndManagerId(organisationId, id).orElse(null);
        if (existing == null) return null;
        ManagerJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Manager saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "manager", id);
        return saved;
    }

    @Override
    public boolean delete(String managerId) {
        if (managerId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(managerId);
        if (!store.existsByOrganisationIdAndManagerId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndManagerId(organisationId, id);
        persistence.audit("DELETE", "manager", id);
        return true;
    }

    @Override
    public List<Manager> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
