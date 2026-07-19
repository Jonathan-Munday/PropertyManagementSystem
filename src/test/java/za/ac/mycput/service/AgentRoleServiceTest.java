package za.ac.mycput.service;

/*
 * AgentRoleServiceTest.java
 * Service test for AgentRole entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.AgentRole;
import za.ac.mycput.factory.AgentRoleFactory;
import za.ac.mycput.service.impl.AgentRoleService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentRoleServiceTest {

    private static IAgentRoleService service = new AgentRoleService();
    private static AgentRole agentRole;

    @BeforeEach
    void setUp() {
        if (agentRole == null) {
            agentRole = AgentRoleFactory.createAgentRole("Zia Modarrisi", "Payment Manager", 7.5);
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        AgentRole created = service.create(agentRole);
        assertNotNull(created);
        assertEquals(agentRole.getAgentId(), created.getAgentId());
        System.out.println("Created AgentRole: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        AgentRole read = service.read(agentRole.getAgentId());
        assertNotNull(read);
        assertEquals(agentRole.getAgentId(), read.getAgentId());
        System.out.println("Read AgentRole: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        AgentRole updatedAgentRole = new AgentRole.Builder()
                .copy(agentRole)
                .setCommissionRate(8.5)
                .build();
        AgentRole updated = service.update(updatedAgentRole);
        assertNotNull(updated);
        assertEquals(8.5, updated.getCommissionRate());
        System.out.println("Updated AgentRole: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<AgentRole> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All AgentRoles: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(agentRole.getAgentId());
        assertTrue(success);
        System.out.println("Deleted AgentRole successfully: " + success);
    }
}
