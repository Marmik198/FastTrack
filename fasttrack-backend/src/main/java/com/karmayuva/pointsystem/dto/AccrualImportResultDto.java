package com.karmayuva.pointsystem.dto;

import java.util.List;

public record AccrualImportResultDto(int rowsProcessed, int accrualsCreated, List<String> errors) {
}
