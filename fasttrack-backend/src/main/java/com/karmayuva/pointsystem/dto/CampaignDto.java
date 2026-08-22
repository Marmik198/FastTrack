package com.karmayuva.pointsystem.dto;

public record CampaignDto(
        Long id,
        String name,
        String startDate,
        String endDate,
        String extendedEndDate,
        boolean currentlyOpen,
        String formLink,
        String creationDate,
        String lastModifiedDate
) {
}
