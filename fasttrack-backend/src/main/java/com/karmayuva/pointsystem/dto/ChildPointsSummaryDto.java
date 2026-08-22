package com.karmayuva.pointsystem.dto;

public record ChildPointsSummaryDto(
        Long childId,
        String firstName,
        String lastName,
        String mobile,
        String email,
        Integer totalAccrued,
        Integer totalBurnt,
        Integer availablePoints
) {
}
