package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Address;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.AddressJpaEntity;

@Component
public class AddressPersistenceMapper {

    public AddressJpaEntity toEntity(Address address) {
        AddressJpaEntity entity = new AddressJpaEntity();
        entity.setAddressId(Identifiers.parse(address.getAddressId()));
        entity.setStreetAddress(address.getStreetAddress());
        entity.setCity(address.getCity());
        entity.setProvince(address.getProvince());
        entity.setPostalCode(address.getPostalCode());
        return entity;
    }

    public Address toDomain(AddressJpaEntity entity) {
        return new Address.Builder()
                .addressId(Identifiers.format(entity.getAddressId()))
                .streetAddress(entity.getStreetAddress())
                .city(entity.getCity())
                .province(entity.getProvince())
                .postalCode(entity.getPostalCode())
                .build();
    }
}
