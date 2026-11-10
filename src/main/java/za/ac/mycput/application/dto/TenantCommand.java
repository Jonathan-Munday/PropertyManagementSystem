package za.ac.mycput.application.dto;

public record TenantCommand(
        String tenantId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
