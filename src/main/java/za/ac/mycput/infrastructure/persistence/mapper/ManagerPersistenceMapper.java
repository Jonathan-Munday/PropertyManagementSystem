package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Manager;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.ManagerJpaEntity;

@Component
public class ManagerPersistenceMapper {

    public ManagerJpaEntity toEntity(Manager manager) {
        ManagerJpaEntity entity = new ManagerJpaEntity();
        entity.setManagerId(Identifiers.parse(manager.getManagerId()));
        entity.setName(manager.getName());
        entity.setContactNumber(manager.getContactNumber());
        entity.setEmployeeId(manager.getEmployeeId());
        return entity;
    }

    public Manager toDomain(ManagerJpaEntity entity) {
        return new Manager.Builder()
                .setManagerId(Identifiers.format(entity.getManagerId()))
                .setName(entity.getName())
                .setContactNumber(entity.getContactNumber())
                .setEmployeeId(entity.getEmployeeId())
                .build();
    }
}
