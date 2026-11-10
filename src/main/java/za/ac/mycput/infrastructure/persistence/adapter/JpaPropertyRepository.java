package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.PropertyJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.PropertyPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaPropertyRepository implements IPropertyRepository {

    private final PropertyJpaRepository store;
    private final PropertyPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaPropertyRepository(PropertyJpaRepository store,
                                 PropertyPersistenceMapper mapper,
                                 ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Property create(Property entity) {
        if (entity == null || entity.getPropertyId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getPropertyId());
        if (store.existsByOrganisationIdAndPropertyId(organisationId, id)) return null;
        PropertyJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Property saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "property", id);
        return saved;
    }

    @Override
    public Property read(String propertyId) {
        if (propertyId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndPropertyId(persistence.organisationId(), Identifiers.parse(propertyId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Property update(Property entity) {
        if (entity == null || entity.getPropertyId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getPropertyId());
        PropertyJpaEntity existing = store.findByOrganisationIdAndPropertyId(organisationId, id).orElse(null);
        if (existing == null) return null;
        PropertyJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Property saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "property", id);
        return saved;
    }

    @Override
    public boolean delete(String propertyId) {
        if (propertyId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(propertyId);
        if (!store.existsByOrganisationIdAndPropertyId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndPropertyId(organisationId, id);
        persistence.audit("DELETE", "property", id);
        return true;
    }

    @Override
    public List<Property> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Property> findAvailable() {
        persistence.bind();
        return store.findByOrganisationIdAndAvailableTrue(persistence.organisationId()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean lock(String propertyId) {
        if (propertyId == null) return false;
        persistence.bind();
        return store.findByOrganisationIdAndPropertyIdForUpdate(
                persistence.organisationId(), Identifiers.parse(propertyId)).isPresent();
    }
}
