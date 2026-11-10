package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.AgentCommand;
import za.ac.mycput.domain.model.Agent;
import za.ac.mycput.interfaces.rest.dto.AgentRequest;
import za.ac.mycput.interfaces.rest.dto.AgentResponse;

@Component
public class AgentRestMapper {

    public AgentCommand toCommand(AgentRequest request) {
        return new AgentCommand(
                request.agentId(),
                request.name(),
                request.email(),
                request.role());
    }

    public AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getAgentId(),
                agent.getName(),
                agent.getEmail(),
                agent.getRole());
    }
}
