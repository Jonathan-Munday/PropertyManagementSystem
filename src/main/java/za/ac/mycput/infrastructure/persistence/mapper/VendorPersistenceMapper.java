package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Vendor;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.VendorJpaEntity;

@Component
public class VendorPersistenceMapper {

    public VendorJpaEntity toEntity(Vendor vendor) {
        VendorJpaEntity entity = new VendorJpaEntity();
        entity.setVendorId(Identifiers.parse(vendor.getVendorId()));
        entity.setCompanyName(vendor.getCompanyName());
        entity.setContactNumber(vendor.getContactNumber());
        entity.setSpecialty(vendor.getSpecialty());
        return entity;
    }

    public Vendor toDomain(VendorJpaEntity entity) {
        return new Vendor.Builder()
                .setVendorId(Identifiers.format(entity.getVendorId()))
                .setCompanyName(entity.getCompanyName())
                .setContactNumber(entity.getContactNumber())
                .setSpecialty(entity.getSpecialty())
                .build();
    }
}
