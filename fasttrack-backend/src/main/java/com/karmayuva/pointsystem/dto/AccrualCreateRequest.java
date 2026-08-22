package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccrualCreateRequest(
        @NotNull(message = "Child is required") Long childId,
        @NotBlank(message = "Month is required, format YYYY-MM") String month,
        @NotNull(message = "Frequency is required") @Min(value = 1, message = "Frequency must be at least 1") Integer frequency
) {
}
