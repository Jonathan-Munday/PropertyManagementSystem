package za.ac.mycput.factory;

import za.ac.mycput.domain.Manager;
import java.util.UUID;
/*
 * ManagerFactory.java
 * Factory for the Manager
 * Author:Anas Pansari (230195342)
 * Date: 28th June 2026
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

    public static Invoice createInvoice(String invoiceDate, String amount, String managerId) {
        if (invoiceDate == null || invoiceDate.isEmpty()) {
            return null;
        }

        String invoiceId = UUID.randomUUID().toString();

        return new Invoice.Builder()
                .setInvoiceId(invoiceId)
                .setInvoiceDate(invoiceDate)
                .setAmount(amount)
                .setManagerId(managerId)
                .build();
    }

}