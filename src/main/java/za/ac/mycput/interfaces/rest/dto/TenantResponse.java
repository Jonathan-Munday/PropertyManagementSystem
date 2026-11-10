package za.ac.mycput.interfaces.rest.dto;

public record TenantResponse(
        String tenantId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
