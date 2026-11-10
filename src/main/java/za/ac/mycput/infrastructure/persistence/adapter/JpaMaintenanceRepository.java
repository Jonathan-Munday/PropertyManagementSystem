package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.repository.IMaintenanceRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.MaintenanceJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.MaintenancePersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaMaintenanceRepository implements IMaintenanceRepository {

    private final MaintenanceJpaRepository store;
    private final MaintenancePersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaMaintenanceRepository(MaintenanceJpaRepository store,
                                    MaintenancePersistenceMapper mapper,
                                    ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Maintenance save(Maintenance maintenance) {
        if (maintenance == null || maintenance.getRequestId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(maintenance.getRequestId());
        if (store.existsByOrganisationIdAndRequestId(organisationId, id)) return null;
        MaintenanceJpaEntity row = mapper.toEntity(maintenance);
        row.markCreated(organisationId);
        Maintenance saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "maintenance_request", id);
        return saved;
    }

    @Override
    public Maintenance findById(String requestId) {
        if (requestId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndRequestId(persistence.organisationId(), Identifiers.parse(requestId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Maintenance> findAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Maintenance update(Maintenance maintenance) {
        if (maintenance == null || maintenance.getRequestId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(maintenance.getRequestId());
        MaintenanceJpaEntity existing = store.findByOrganisationIdAndRequestId(organisationId, id).orElse(null);
        if (existing == null) return null;
        MaintenanceJpaEntity row = mapper.toEntity(maintenance);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Maintenance saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "maintenance_request", id);
        return saved;
    }

    @Override
    public void delete(String requestId) {
        if (requestId == null) return;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(requestId);
        if (!store.existsByOrganisationIdAndRequestId(organisationId, id)) return;
        store.deleteByOrganisationIdAndRequestId(organisationId, id);
        persistence.audit("DELETE", "maintenance_request", id);
    }
}
