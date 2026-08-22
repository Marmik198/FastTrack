package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CampaignUpsertRequest(
        @NotBlank(message = "Campaign name is required") String name,
        @NotNull(message = "Start date is required") LocalDate startDate,
        @NotNull(message = "End date is required") LocalDate endDate,
        LocalDate extendedEndDate
) {
}
