package za.ac.mycput.interfaces.rest.dto;

public record ManagerResponse(
        String managerId,
        String name,
        String contactNumber,
        String employeeId
) {
}
