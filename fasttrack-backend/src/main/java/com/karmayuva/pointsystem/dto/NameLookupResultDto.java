package com.karmayuva.pointsystem.dto;

/**
 * status is one of: CLOSED, NOT_FOUND, ALREADY_SUBMITTED, OK
 * The remaining fields are only populated when status = OK.
 */
public record NameLookupResultDto(
        String status,
        Long childId,
        String firstName,
        String lastName,
        String mobile,
        String email,
        String address,
        Integer availablePoints,
        Long campaignId,
        String campaignName
) {
}
