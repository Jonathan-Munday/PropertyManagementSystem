/*
 * AgentRoleService.java
 * Service implementation for AgentRole entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.AgentRole;
import za.ac.mycput.service.IAgentRoleService;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentRoleService implements IAgentRoleService {

    private final List<AgentRole> agentRoleList = new ArrayList<>();

    @Override
    public AgentRole create(AgentRole agentRole) {
        if (read(agentRole.getAgentId()) != null) {
            return null;
        }
        agentRoleList.add(agentRole);
        return agentRole;
    }

    @Override
    public AgentRole read(String agentId) {
        return agentRoleList.stream()
                .filter(a -> a.getAgentId().equals(agentId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AgentRole update(AgentRole agentRole) {
        AgentRole existing = read(agentRole.getAgentId());
        if (existing != null) {
            agentRoleList.remove(existing);
            agentRoleList.add(agentRole);
            return agentRole;
        }
        return null;
    }

    @Override
    public boolean delete(String agentId) {
        AgentRole existing = read(agentId);
        if (existing != null) {
            agentRoleList.remove(existing);
            return true;
        }
        return false;
    }

    @Override
    public List<AgentRole> getAll() {
        return new ArrayList<>(agentRoleList);
    }
}
