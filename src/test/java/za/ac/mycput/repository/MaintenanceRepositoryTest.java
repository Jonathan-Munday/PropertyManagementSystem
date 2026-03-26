package za.ac.mycput.repository;
/*
*MaintenanceRepositoryTest.java
* TDD for maintenance repository
* Olwethu Mtwazi (230036937)
* 25/3/2026
 */

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Maintenance;
import za.ac.mycput.factory.MaintenanceFactory;
import za.ac.mycput.repository.impl.MaintenanceRepositoryImpl;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceRepositoryTest {
    private MaintenanceRepositoryImpl repository = MaintenanceRepositoryImpl.getRepository();

    private Maintenance maintenance = MaintenanceFactory.createMaintenance("R001","Broken geyser","Open", LocalDate.now(),700);
    @Test
    public void testSave(){
        Maintenance saved = repository.save(maintenance);
        assertNotNull(saved);
        System.out.println(saved);
    }
    @Test
    public void testFindById(){
        repository.save(maintenance);
        Maintenance found = repository.findById("R001");
        assertNotNull(found);
        System.out.println(found);
    }
    @Test
    public void testUpdate(){
        repository.save(maintenance);
        Maintenance updated = new Maintenance.Builder()
                .copy(maintenance)
                .setStatus("Closed")
                .build();
        Maintenance result = repository.update(updated);
        assertEquals("Closed", result.getStatus());
        System.out.println(result);
    }
    @Test
    public void testDelete(){
        repository.save(maintenance);
        repository.delete("R001");
        Maintenance deleted = repository.findById("R001");
        assertNull(deleted);
    }
    @Test
    public void testFindAll(){
        repository.save(maintenance);
        assertFalse(repository.findAll().isEmpty());
        System.out.println(repository.findAll());
    }
}
