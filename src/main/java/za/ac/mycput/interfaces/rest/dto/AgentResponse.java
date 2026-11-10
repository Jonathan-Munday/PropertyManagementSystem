package za.ac.mycput.interfaces.rest.dto;

public record AgentResponse(
        String agentId,
        String name,
        String email,
        String role
) {
}
