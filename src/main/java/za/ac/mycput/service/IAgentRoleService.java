/*
 * IAgentRoleService.java
 * Service interface for AgentRole entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.AgentRole;
import java.util.List;

public interface IAgentRoleService extends IService<AgentRole, String> {
    List<AgentRole> getAll();
}
