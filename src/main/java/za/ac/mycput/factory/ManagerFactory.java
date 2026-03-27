package za.ac.mycput.factory;

import za.ac.mycput.domain.Manager;
import java.util.UUID;
/*
 * ManagerFactory.java
 * Factory for the Manager
 * Author:Anas Pansari (230195342)
 * Date: 25 March 2026
 */

public class ManagerFactory {

    public static Manager createManager(String name, String contactNumber, String employeeId) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String managerId = UUID.randomUUID().toString();
        return new Manager.Builder()
                .setManagerId(managerId)
                .setName(name)
                .setContactNumber(contactNumber)
                .setEmployeeId(employeeId)
                .build();
    }



}

