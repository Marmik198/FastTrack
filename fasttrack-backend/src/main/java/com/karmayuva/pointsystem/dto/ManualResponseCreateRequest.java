package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.*;

// Same shape as the public submit request - lets an admin fill the form on a
// child's behalf (e.g. for kids without internet access), going through the
// exact same validation/business rules.
public record ManualResponseCreateRequest(
        @NotNull(message = "Child is required") Long childId,
        @NotNull(message = "Campaign is required") Long campaignId,
        @NotNull(message = "Please choose carry forward or redeem") Boolean carryForwardFlag,
        String link,
        Integer quantity,
        String remarks,
        @Email(message = "Enter a valid email address")
        String email
) {
}
