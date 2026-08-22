package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.*;

public record CampaignResponseSubmitRequest(
        @NotNull(message = "Child is required") Long childId,
        @NotNull(message = "Campaign is required") Long campaignId,
        @NotNull(message = "Please choose carry forward or redeem") Boolean carryForwardFlag,
        @Pattern(regexp = "^$|^https?://.+", message = "Enter a valid product link starting with http:// or https://")
        String link,
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,
        @Size(max = 1000, message = "Remarks are too long")
        String remarks,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email
) {
}
