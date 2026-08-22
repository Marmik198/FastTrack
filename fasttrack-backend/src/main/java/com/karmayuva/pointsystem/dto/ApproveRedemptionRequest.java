package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApproveRedemptionRequest(
        @NotNull(message = "Points amount is required") @Min(value = 1, message = "Amount must be positive") Integer amount,
        @NotBlank(message = "Item name is required") String item
) {
}
