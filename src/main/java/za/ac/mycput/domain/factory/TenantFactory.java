
package za.ac.mycput.domain.factory;

import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.domain.model.Notification;
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
        return createTenant(UUID.randomUUID().toString(), firstName, lastName, email, phoneNumber);
    }

    public static Tenant createTenant(String tenantId,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      String phoneNumber) {
        if (tenantId == null || tenantId.isBlank() || firstName == null || firstName.isEmpty()) {
            return null;
        }

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
        return createNotification(UUID.randomUUID().toString(), message, type, dateSent, tenantId);
    }

    public static Notification createNotification(String notificationId,
                                                  String message,
                                                  String type,
                                                  String dateSent,
                                                  String tenantId) {
        if (notificationId == null || notificationId.isBlank() || message == null || message.isEmpty()) {
            return null;
        }

        return new Notification.Builder()
                .setNotificationId(notificationId)
                .setMessage(message)
                .setType(type)
                .setDateSent(dateSent)
                .setTenantId(tenantId)
                .build();
    }
}