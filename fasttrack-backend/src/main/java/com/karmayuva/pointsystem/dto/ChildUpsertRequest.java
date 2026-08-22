package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.*;

public record ChildUpsertRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        String motherName,
        String fatherName,
        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        String mobile,
        @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        String alternateMobile,
        @Email(message = "Enter a valid email address")
        String email,
        @Min(value = 1, message = "Age must be positive")
        @Max(value = 100, message = "Enter a valid age")
        Integer age,
        String dateOfBirth,
        String address
) {
}
