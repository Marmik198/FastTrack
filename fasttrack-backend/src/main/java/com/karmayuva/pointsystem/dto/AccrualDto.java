package com.karmayuva.pointsystem.dto;

public record AccrualDto(
        Long id,
        String month,
        Integer frequency,
        Long childId,
        String childName,
        String creationDate,
        String lastModifiedDate
) {
}
