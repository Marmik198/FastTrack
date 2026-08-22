package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.AccrualCreateRequest;
import com.karmayuva.pointsystem.dto.AccrualDto;
import com.karmayuva.pointsystem.dto.AccrualImportResultDto;
import com.karmayuva.pointsystem.service.AccrualImportService;
import com.karmayuva.pointsystem.service.AccrualService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/accruals")
public class AdminAccrualController {

    private final AccrualService accrualService;
    private final AccrualImportService accrualImportService;

    public AdminAccrualController(AccrualService accrualService, AccrualImportService accrualImportService) {
        this.accrualService = accrualService;
        this.accrualImportService = accrualImportService;
    }

    @GetMapping
    public Page<AccrualDto> search(@RequestParam(required = false) Long childId,
                                    @RequestParam(required = false) String month,
                                    Pageable pageable) {
        return accrualService.search(childId, month, pageable);
    }

    @PostMapping
    public AccrualDto create(@Valid @RequestBody AccrualCreateRequest request) {
        return accrualService.create(request);
    }

    // Accepts .csv, .txt, or .xlsx - see AccrualImportService for the expected columns
    @PostMapping("/upload")
    public AccrualImportResultDto upload(@RequestParam("file") MultipartFile file) {
        return accrualImportService.importSheet(file);
    }
}
