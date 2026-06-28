
package za.ac.mycput.factory;

/*
 * TenantFactoryTest.java
 * Test class for Tenant and Notification Factories
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026 (Updated: 28 June 2026)
 */

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Notification;
import za.ac.mycput.domain.Tenant;

import static org.junit.jupiter.api.Assertions.*;

public class TenantFactoryTest {

    @Test
    void createTenantSuccess() {
        Tenant tenant = TenantFactory.createTenant(
                "John",
                "Doe",
                "john@test.com",
                "0123456789"
        );

        assertNotNull(tenant);
        assertEquals("John", tenant.getFirstName());
        assertEquals("Doe", tenant.getLastName());

        System.out.println(tenant);
    }

    @Test
    void createTenantFailure() {
        Tenant tenant = TenantFactory.createTenant(
                "",
                "Doe",
                "john@test.com",
                "0123456789"
        );

        assertNull(tenant);
    }

    @Test
    void createNotificationSuccess() {
        // We simulate a tenant ID that would be passed to the notification
        String sampleTenantId = "1234989310";

        Notification notification = TenantFactory.createNotification(
                "Your rent payment is due.",
                "Email",
                "2026-06-28",
                sampleTenantId
        );

        assertNotNull(notification);

        assertNotNull(notification.getNotificationId());
        assertEquals("Your rent payment is due.", notification.getMessage());
        assertEquals(sampleTenantId, notification.getTenantId());

        System.out.println(notification);
    }

    @Test
    void createNotificationFailure() {
        String sampleTenantId = "1234989310";


        Notification notification = TenantFactory.createNotification(
                "",
                "SMS",
                "2026-06-28",
                sampleTenantId
        );

        assertNull(notification);
    }
}

