package com.karmayuva.pointsystem.dto;

public record CampaignResponseDto(
        Long id,
        Long campaignId,
        String campaignName,
        Long childId,
        String childName,
        Boolean carryForwardFlag,
        String link,
        Integer quantity,
        String remarks,
        String email,
        Boolean approved,
        String creationDate,
        String lastModifiedDate
) {
}
