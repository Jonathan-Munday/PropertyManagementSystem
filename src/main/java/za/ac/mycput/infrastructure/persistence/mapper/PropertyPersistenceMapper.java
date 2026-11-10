package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.PropertyJpaEntity;

@Component
public class PropertyPersistenceMapper {

    public PropertyJpaEntity toEntity(Property property) {
        PropertyJpaEntity entity = new PropertyJpaEntity();
        entity.setPropertyId(Identifiers.parse(property.getPropertyId()));
        entity.setAddress(property.getAddress());
        entity.setDescription(property.getDescription());
        entity.setRentalAmount(MoneyColumns.fromDouble(property.getRentalAmount()));
        entity.setAvailable(property.isAvailable());
        return entity;
    }

    public Property toDomain(PropertyJpaEntity entity) {
        return new Property.Builder()
                .propertyId(Identifiers.format(entity.getPropertyId()))
                .address(entity.getAddress())
                .description(entity.getDescription())
                .rentalAmount(MoneyColumns.toDouble(entity.getRentalAmount()))
                .isAvailable(entity.isAvailable())
                .build();
    }
}
