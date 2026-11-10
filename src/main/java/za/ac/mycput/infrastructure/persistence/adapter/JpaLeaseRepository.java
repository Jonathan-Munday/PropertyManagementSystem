package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.LeaseJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.LeasePersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaLeaseRepository implements ILeaseRepository {

    private final LeaseJpaRepository store;
    private final LeasePersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaLeaseRepository(LeaseJpaRepository store, LeasePersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Lease create(Lease entity) {
        if (entity == null || entity.getLeaseId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getLeaseId());
        if (store.existsByOrganisationIdAndLeaseId(organisationId, id)) return null;
        LeaseJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Lease saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "lease", id);
        return saved;
    }

    @Override
    public Lease read(String leaseId) {
        if (leaseId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndLeaseId(persistence.organisationId(), Identifiers.parse(leaseId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Lease update(Lease entity) {
        if (entity == null || entity.getLeaseId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getLeaseId());
        LeaseJpaEntity existing = store.findByOrganisationIdAndLeaseId(organisationId, id).orElse(null);
        if (existing == null) return null;
        LeaseJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Lease saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "lease", id);
        return saved;
    }

    @Override
    public boolean delete(String leaseId) {
        if (leaseId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(leaseId);
        if (!store.existsByOrganisationIdAndLeaseId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndLeaseId(organisationId, id);
        persistence.audit("DELETE", "lease", id);
        return true;
    }

    @Override
    public List<Lease> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
