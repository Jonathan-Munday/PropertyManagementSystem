package za.ac.mycput.domain.factory;

import za.ac.mycput.domain.model.Manager;
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
        return createManager(UUID.randomUUID().toString(), name, contactNumber, employeeId);
    }

    public static Manager createManager(String managerId, String name, String contactNumber, String employeeId) {
        if (managerId == null || managerId.isBlank() || name == null || name.isEmpty()) {
            return null;
        }
        return new Manager.Builder()
                .setManagerId(managerId)
                .setName(name)
                .setContactNumber(contactNumber)
                .setEmployeeId(employeeId)
                .build();
    }



}

