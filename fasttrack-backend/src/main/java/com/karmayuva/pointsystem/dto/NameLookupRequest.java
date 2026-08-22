package com.karmayuva.pointsystem.dto;

import jakarta.validation.constraints.NotBlank;

public record NameLookupRequest(@NotBlank(message = "Please enter your name") String name) {
}
