package com.karmayuva.pointsystem.dto;

public record PointDto(
        Long id,
        String month,
        Integer value,
        Integer burnt,
        Integer available,
        Long childId,
        String childName,
        Long accrualId,
        String creationDate,
        String lastModifiedDate
) {
}
