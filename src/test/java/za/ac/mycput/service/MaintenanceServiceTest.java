package za.ac.mycput.service;

/*
 * MaintenanceServiceTest.java
 * Service test for Maintenance entity.
 * Author: Olwethu Mtwazi (230036937)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Maintenance;
import za.ac.mycput.factory.MaintenanceFactory;
import za.ac.mycput.service.impl.MaintenanceService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MaintenanceServiceTest {

    private static IMaintenanceService service = new MaintenanceService();
    private static Maintenance maintenance;

    @BeforeAll
    static void cleanUp() {
        service.delete("R001");
    }

    @BeforeEach
    void setUp() {
        if (maintenance == null) {
            maintenance = MaintenanceFactory.createMaintenance("R001", "Broken geyser", "Open", LocalDate.now(), 700.0);
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Maintenance created = service.create(maintenance);
        assertNotNull(created);
        assertEquals(maintenance.getRequestId(), created.getRequestId());
        System.out.println("Created Maintenance: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Maintenance read = service.read(maintenance.getRequestId());
        assertNotNull(read);
        assertEquals(maintenance.getRequestId(), read.getRequestId());
        System.out.println("Read Maintenance: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Maintenance updatedMaintenance = new Maintenance.Builder()
                .copy(maintenance)
                .setStatus("Closed")
                .build();
        Maintenance updated = service.update(updatedMaintenance);
        assertNotNull(updated);
        assertEquals("Closed", updated.getStatus());
        System.out.println("Updated Maintenance: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Maintenance> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Maintenance: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(maintenance.getRequestId());
        assertTrue(success);
        System.out.println("Deleted Maintenance successfully: " + success);
    }
}
