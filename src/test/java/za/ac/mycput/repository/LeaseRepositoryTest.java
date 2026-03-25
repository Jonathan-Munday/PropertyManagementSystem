package za.ac.mycput.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Lease;
import za.ac.mycput.factory.LeaseFactory;
import za.ac.mycput.repository.impl.LeaseRepositoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeaseRepositoryTest {

    private LeaseRepositoryImpl repository;
    private static Lease lease1;

    @BeforeEach
    void setUp() {
        repository = LeaseRepositoryImpl.getRepository();

        // FIXED: Passing 3 Strings and 1 double exactly as the Factory demands.
        // You can change "String1", "String2", etc., to make sense for your project (e.g. "TenantID", "StartDate", "EndDate")
        lease1 = LeaseFactory.createLease(
                "String1",
                "String2",
                "String3",
                8500.00
        );
    }

    @Test
    @Order(1)
    void testCreate() {
        Lease created = repository.create(lease1);
        assertNotNull(created);
        System.out.println("Created: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Lease read = repository.read(lease1.getLeaseId());
        assertNotNull(read);
        System.out.println("Read: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        // This will still be red until you add a 'Setter' to your Lease.java class!
        lease1.setMonthlyRent(9500.00);
        Lease updated = repository.update(lease1);

        assertNotNull(updated);
        System.out.println("Updated: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Lease> all = repository.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All entries: " + all.size());
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = repository.delete(lease1.getLeaseId());
        assertTrue(success);
        System.out.println("Deleted successfully: " + success);
    }
}
