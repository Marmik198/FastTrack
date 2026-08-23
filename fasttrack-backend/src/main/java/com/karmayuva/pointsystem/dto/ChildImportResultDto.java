package com.karmayuva.pointsystem.dto;

import java.util.List;

public record ChildImportResultDto(int rowsProcessed, int childrenCreated, int childrenSkipped, List<String> errors) {
}
