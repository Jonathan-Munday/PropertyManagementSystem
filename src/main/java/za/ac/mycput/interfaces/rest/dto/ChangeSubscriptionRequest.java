package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeSubscriptionRequest(@NotBlank String planCode, String outcome) {
}
