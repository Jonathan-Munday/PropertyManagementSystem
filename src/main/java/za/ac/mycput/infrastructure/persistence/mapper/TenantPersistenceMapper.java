package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.TenantJpaEntity;

@Component
public class TenantPersistenceMapper {

    public TenantJpaEntity toEntity(Tenant tenant) {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setTenantId(Identifiers.parse(tenant.getTenantId()));
        entity.setFirstName(tenant.getFirstName());
        entity.setLastName(tenant.getLastName());
        entity.setEmail(tenant.getEmail());
        entity.setPhoneNumber(tenant.getPhoneNumber());
        return entity;
    }

    public Tenant toDomain(TenantJpaEntity entity) {
        return new Tenant.Builder()
                .setTenantId(Identifiers.format(entity.getTenantId()))
                .setFirstName(entity.getFirstName())
                .setLastName(entity.getLastName())
                .setEmail(entity.getEmail())
                .setPhoneNumber(entity.getPhoneNumber())
                .build();
    }
}
