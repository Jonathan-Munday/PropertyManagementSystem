package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.domain.repository.ITenantRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.TenantJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.TenantPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaTenantRepository implements ITenantRepository {

    private final TenantJpaRepository store;
    private final TenantPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaTenantRepository(TenantJpaRepository store, TenantPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Tenant create(Tenant entity) {
        if (entity == null || entity.getTenantId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getTenantId());
        if (store.existsByOrganisationIdAndTenantId(organisationId, id)) return null;
        TenantJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Tenant saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "tenant", id);
        return saved;
    }

    @Override
    public Tenant read(String tenantId) {
        if (tenantId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndTenantId(persistence.organisationId(), Identifiers.parse(tenantId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Tenant update(Tenant entity) {
        if (entity == null || entity.getTenantId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getTenantId());
        TenantJpaEntity existing = store.findByOrganisationIdAndTenantId(organisationId, id).orElse(null);
        if (existing == null) return null;
        TenantJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Tenant saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "tenant", id);
        return saved;
    }

    @Override
    public boolean delete(String tenantId) {
        if (tenantId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(tenantId);
        if (!store.existsByOrganisationIdAndTenantId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndTenantId(organisationId, id);
        persistence.audit("DELETE", "tenant", id);
        return true;
    }

    @Override
    public List<Tenant> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
