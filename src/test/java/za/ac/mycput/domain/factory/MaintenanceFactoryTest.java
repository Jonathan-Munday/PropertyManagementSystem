package za.ac.mycput.domain.factory;
import za.ac.mycput.domain.factory.MaintenanceFactory;

/*MaintenanceFactoryTest.java
*Factory class for Maintenance
* Olwethu Mtwazi (230036937)
* 25/3/2026
 */


import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.model.Vendor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MaintenanceFactoryTest {
    @Test
    public void testcreateMaintenanceSuccess() {
        Maintenance maintenance = MaintenanceFactory.createMaintenance("R001", "Broken geyser", "Open", LocalDate.now(), 700);
        assertNotNull(maintenance);
    }

    @Test
    public void testcreateMaintenanceFailure() {
        Maintenance maintenance = MaintenanceFactory.createMaintenance("", "", "", null, -100);
        assertNull(maintenance);
    }

    @Test
    public void testCreateVendorSuccess() {
        Vendor vendor = MaintenanceFactory.createVendor("QuickFix Plumbing", "075512569", "Plumbing");
        assertNotNull(vendor);
    }

    @Test
    public void testCreateVendorFailure() {
        Vendor vendor = MaintenanceFactory.createVendor("", "", "");
        assertNull(vendor);
    }
}
