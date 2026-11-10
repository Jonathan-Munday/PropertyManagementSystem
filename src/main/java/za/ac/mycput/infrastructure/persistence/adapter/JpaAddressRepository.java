package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Address;
import za.ac.mycput.domain.repository.IAddressRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.AddressJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.AddressPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaAddressRepository implements IAddressRepository {

    private final AddressJpaRepository store;
    private final AddressPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaAddressRepository(AddressJpaRepository store, AddressPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Address create(Address entity) {
        if (entity == null || entity.getAddressId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getAddressId());
        if (store.existsByOrganisationIdAndAddressId(organisationId, id)) return null;
        AddressJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Address saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "address", id);
        return saved;
    }

    @Override
    public Address read(String addressId) {
        if (addressId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndAddressId(persistence.organisationId(), Identifiers.parse(addressId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Address update(Address entity) {
        if (entity == null || entity.getAddressId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getAddressId());
        AddressJpaEntity existing = store.findByOrganisationIdAndAddressId(organisationId, id).orElse(null);
        if (existing == null) return null;
        AddressJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Address saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "address", id);
        return saved;
    }

    @Override
    public boolean delete(String addressId) {
        if (addressId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(addressId);
        if (!store.existsByOrganisationIdAndAddressId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndAddressId(organisationId, id);
        persistence.audit("DELETE", "address", id);
        return true;
    }

    @Override
    public List<Address> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
