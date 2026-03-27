package za.ac.mycput.factory;
/*
 * TenantFactory.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

import za.ac.mycput.domain.Tenant;
import java.util.UUID;

public class TenantFactory {

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
}