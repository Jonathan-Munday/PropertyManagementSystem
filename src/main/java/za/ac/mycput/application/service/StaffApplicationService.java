package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.AgentCommand;
import za.ac.mycput.application.dto.ManagerCommand;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.ManagerFactory;
import za.ac.mycput.domain.factory.PaymentFactory;
import za.ac.mycput.domain.model.Agent;
import za.ac.mycput.domain.model.Manager;
import za.ac.mycput.domain.repository.IAgentRepository;
import za.ac.mycput.domain.repository.IManagerRepository;
import za.ac.mycput.domain.valueobject.EmailAddress;

import java.util.List;

@Service
public class StaffApplicationService {

    private final IManagerRepository managers;
    private final IAgentRepository agents;

    public StaffApplicationService(IManagerRepository managers, IAgentRepository agents) {
        this.managers = managers;
        this.agents = agents;
    }

    @Transactional
    public List<Manager> listManagers() {
        return managers.getAll();
    }

    @Transactional
    public Manager getManager(String managerId) {
        return RepositoryOutcomes.existing(managers.read(managerId), "Manager", managerId);
    }

    @Transactional
    public Manager createManager(ManagerCommand command) {
        Manager manager = RepositoryOutcomes.built(
                ManagerFactory.createManager(command.managerId(), command.name(), command.contactNumber(), command.employeeId()),
                "A manager needs an id and a name");
        RepositoryOutcomes.absent(managers.read(manager.getManagerId()), "Manager", manager.getManagerId());
        return RepositoryOutcomes.created(managers.create(manager), "Manager", manager.getManagerId());
    }

    @Transactional
    public Manager updateManager(String managerId, ManagerCommand command) {
        if (!managerId.equals(command.managerId())) {
            throw new DomainRuleException("The manager id in the path and body must match");
        }
        RepositoryOutcomes.existing(managers.read(managerId), "Manager", managerId);
        Manager manager = RepositoryOutcomes.built(
                ManagerFactory.createManager(managerId, command.name(), command.contactNumber(), command.employeeId()),
                "A manager needs an id and a name");
        return RepositoryOutcomes.updated(managers.update(manager), "Manager", managerId);
    }

    @Transactional
    public void deleteManager(String managerId) {
        RepositoryOutcomes.deleted(managers.delete(managerId), "Manager", managerId);
    }

    @Transactional
    public List<Agent> listAgents() {
        return agents.getAll();
    }

    @Transactional
    public Agent getAgent(String agentId) {
        return RepositoryOutcomes.existing(agents.read(agentId), "Agent", agentId);
    }

    @Transactional
    public Agent createAgent(AgentCommand command) {
        Agent agent = buildAgent(command);
        RepositoryOutcomes.absent(agents.read(agent.getAgentId()), "Agent", agent.getAgentId());
        return RepositoryOutcomes.created(agents.create(agent), "Agent", agent.getAgentId());
    }

    @Transactional
    public Agent updateAgent(String agentId, AgentCommand command) {
        if (!agentId.equals(command.agentId())) {
            throw new DomainRuleException("The agent id in the path and body must match");
        }
        RepositoryOutcomes.existing(agents.read(agentId), "Agent", agentId);
        return RepositoryOutcomes.updated(agents.update(buildAgent(command)), "Agent", agentId);
    }

    @Transactional
    public void deleteAgent(String agentId) {
        RepositoryOutcomes.deleted(agents.delete(agentId), "Agent", agentId);
    }

    private Agent buildAgent(AgentCommand command) {
        new EmailAddress(command.email());
        return RepositoryOutcomes.built(
                PaymentFactory.createAgent(command.agentId(), command.name(), command.email(), command.role()),
                "An agent needs an id, a name, an email, and a role");
    }
}
