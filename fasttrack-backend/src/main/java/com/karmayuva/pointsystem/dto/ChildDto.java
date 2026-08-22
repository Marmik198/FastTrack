package com.karmayuva.pointsystem.dto;

public record ChildDto(
        Long id,
        String firstName,
        String lastName,
        String motherName,
        String fatherName,
        String mobile,
        String alternateMobile,
        String email,
        Integer age,
        String dateOfBirth,
        String address,
        String creationDate,
        String lastModifiedDate
) {
}
