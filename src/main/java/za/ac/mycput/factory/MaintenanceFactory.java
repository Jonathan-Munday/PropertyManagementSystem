package za.ac.mycput.factory;

import za.ac.mycput.domain.Maintenance;
import za.ac.mycput.domain.Vendor;

import java.time.LocalDate;
import java.util.UUID;

/*
* MaintenanceFactory.java
* Factory class for Maintenance
* Olwethu Mtwazi (230036937)
* 21/3/2026
 */
public class MaintenanceFactory {
    public static Maintenance createMaintenance(String requestId, String description, String status, LocalDate reportedDate, double cost){
        if(requestId == null || requestId.isEmpty()){
            return null;
        }
        if(description == null || description.isEmpty()){
            return null;
        }
        if(status == null || status.isEmpty()){
            return null;
        }
        if(reportedDate == null){
            return null;
        }
        if(cost < 0){
            return null;
        }

        return new Maintenance.Builder()
                .setRequestId(requestId)
                .setDescription(description)
                .setStatus(status)
                .setReportedDate(reportedDate)
                .setCost(cost)
                .build();
    }
    public static Vendor createVendor(String companyName, String contactNumber, String speciality){
        if (companyName == null || companyName.isEmpty()){
            return null;
        }
        if (contactNumber == null || contactNumber.isEmpty()) {
            return null;
        }
        if (speciality == null || speciality.isEmpty()) {
            return null;
        }
        String generatedVendorId = "VND-" + UUID.randomUUID().toString().substring(0,8);

        return new Vendor.Builder()
                .setVendorId(generatedVendorId)
                .setCompanyName(companyName)
                .setContactNumber(contactNumber)
                .setSpecialty(speciality)
                .build();
    }


}
