package za.ac.mycput.domain.factory;
import za.ac.mycput.domain.factory.ManagerFactory;

/*
  ManagerFactoryTest.java
  Entity for the Manager
  Author: Anas Pansari (230195342)
  Date: 25th March 2026
 */

import za.ac.mycput.domain.model.Manager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagerFactoryTest {
    @Test
    void testCreateManagerSuccess() {
        Manager manager = ManagerFactory.createManager(
                "James Smith",
                "08231243756",
                "EMP01"
        );
        assertNotNull(manager);
        assertNotNull(manager.getManagerId());
    }

    @Test
    void testCreateManagerFailure() {
        Manager manager = ManagerFactory.createManager(
                "",
                "08231243756",
                "EMP01"
        );

        assertNull(manager);


    }



}
