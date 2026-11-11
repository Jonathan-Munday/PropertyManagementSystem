package za.ac.mycput.interfaces.rest.dto;

public record UsageResponse(long properties, long users, Integer maxProperties, Integer maxUsers) {
}
