package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Agent;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.AgentJpaEntity;

@Component
public class AgentPersistenceMapper {

    public AgentJpaEntity toEntity(Agent agent) {
        AgentJpaEntity entity = new AgentJpaEntity();
        entity.setAgentId(Identifiers.parse(agent.getAgentId()));
        entity.setName(agent.getName());
        entity.setEmail(agent.getEmail());
        entity.setRole(agent.getRole());
        return entity;
    }

    public Agent toDomain(AgentJpaEntity entity) {
        return new Agent.Builder()
                .setAgentId(Identifiers.format(entity.getAgentId()))
                .setName(entity.getName())
                .setEmail(entity.getEmail())
                .setRole(entity.getRole())
                .build();
    }
}
