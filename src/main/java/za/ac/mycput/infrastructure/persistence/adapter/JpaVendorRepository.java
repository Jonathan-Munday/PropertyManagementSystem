package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Vendor;
import za.ac.mycput.domain.repository.IVendorRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.VendorJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.VendorPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaVendorRepository implements IVendorRepository {

    private final VendorJpaRepository store;
    private final VendorPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaVendorRepository(VendorJpaRepository store, VendorPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Vendor create(Vendor entity) {
        if (entity == null || entity.getVendorId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getVendorId());
        if (store.existsByOrganisationIdAndVendorId(organisationId, id)) return null;
        VendorJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Vendor saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "vendor", id);
        return saved;
    }

    @Override
    public Vendor read(String vendorId) {
        if (vendorId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndVendorId(persistence.organisationId(), Identifiers.parse(vendorId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Vendor update(Vendor entity) {
        if (entity == null || entity.getVendorId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getVendorId());
        VendorJpaEntity existing = store.findByOrganisationIdAndVendorId(organisationId, id).orElse(null);
        if (existing == null) return null;
        VendorJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Vendor saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "vendor", id);
        return saved;
    }

    @Override
    public boolean delete(String vendorId) {
        if (vendorId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(vendorId);
        if (!store.existsByOrganisationIdAndVendorId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndVendorId(organisationId, id);
        persistence.audit("DELETE", "vendor", id);
        return true;
    }

    @Override
    public List<Vendor> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
