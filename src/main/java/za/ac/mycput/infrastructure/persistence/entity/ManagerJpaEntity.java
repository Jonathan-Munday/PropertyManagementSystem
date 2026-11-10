package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "managers")
public class ManagerJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "contact_number", length = 40)
    private String contactNumber;

    @Column(name = "employee_id", length = 64)
    private String employeeId;

    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}
