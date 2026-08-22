package com.karmayuva.pointsystem.dto;

public record RedemptionDto(
        Long id,
        String date,
        String childName,
        Integer amount,
        String item,
        String link,
        Integer quantity,
        Long campaignId,
        Long responseId,
        String createdBy,
        String creationDate
) {
}
