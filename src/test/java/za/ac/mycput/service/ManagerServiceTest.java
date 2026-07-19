package za.ac.mycput.service;

/*
 * ManagerServiceTest.java
 * Service test for Manager entity.
 * Author: Anas Pansari (230195342)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Manager;
import za.ac.mycput.factory.ManagerFactory;
import za.ac.mycput.service.impl.ManagerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManagerServiceTest {

    private static IManagerService service = new ManagerService();
    private static Manager manager;

    @BeforeAll
    static void cleanUp() {
        // We know from factory tests that employeeId EMP01 is used
        // Since getManagerId() might generate a UUID or be based on employeeId, let's delete EMP01 if it's there
        // Actually, we can get the managerId by building a dummy manager instance to check
        Manager dummy = ManagerFactory.createManager("James Smith", "08231243756", "EMP01");
        if (dummy != null) {
            service.delete(dummy.getManagerId());
        }
    }

    @BeforeEach
    void setUp() {
        if (manager == null) {
            manager = ManagerFactory.createManager("James Smith", "08231243756", "EMP01");
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Manager created = service.create(manager);
        assertNotNull(created);
        assertEquals(manager.getManagerId(), created.getManagerId());
        System.out.println("Created Manager: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Manager read = service.read(manager.getManagerId());
        assertNotNull(read);
        assertEquals(manager.getManagerId(), read.getManagerId());
        System.out.println("Read Manager: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Manager updatedManager = new Manager.Builder()
                .setManagerId(manager.getManagerId())
                .setName(manager.getName())
                .setContactNumber("08231243799")
                .setEmployeeId(manager.getEmployeeId())
                .build();
        Manager updated = service.update(updatedManager);
        assertNotNull(updated);
        assertEquals("08231243799", updated.getContactNumber());
        System.out.println("Updated Manager: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Manager> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Managers: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(manager.getManagerId());
        assertTrue(success);
        System.out.println("Deleted Manager successfully: " + success);
    }
}
