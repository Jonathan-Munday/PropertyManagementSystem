
package za.ac.mycput.factory;

import za.ac.mycput.domain.Tenant;
import za.ac.mycput.domain.Notification;
import java.util.UUID;

/*
 * TenantFactory.java
 * Factory for Tenant and Notification entities
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026 (Updated: 28 June 2026)
 */

public class TenantFactory {

    // Factory method for Tenant
    public static Tenant createTenant(String firstName,
                                      String lastName,
                                      String email,
                                      String phoneNumber) {
        if (firstName == null || firstName.isEmpty()) {
            return null;
        }

        String tenantId = UUID.randomUUID().toString();

        return new Tenant.Builder()
                .setTenantId(tenantId)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .build();
    }


    public static Notification createNotification(String message,
                                                  String type,
                                                  String dateSent,
                                                  String tenantId) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        String notificationId = UUID.randomUUID().toString();

        return new Notification.Builder()
                .setNotificationId(notificationId)
                .setMessage(message)
                .setType(type)
                .setDateSent(dateSent)
                .setTenantId(tenantId) // Links the notification to tenant
                .build();
    }
}

}