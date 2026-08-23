package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.ChildDto;
import com.karmayuva.pointsystem.dto.ChildImportResultDto;
import com.karmayuva.pointsystem.dto.ChildUpsertRequest;
import com.karmayuva.pointsystem.service.ChildImportService;
import com.karmayuva.pointsystem.service.ChildService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/children")
public class AdminChildController {

    private final ChildService childService;
    private final ChildImportService childImportService;

    public AdminChildController(ChildService childService, ChildImportService childImportService) {
        this.childService = childService;
        this.childImportService = childImportService;
    }

    @GetMapping
    public Page<ChildDto> search(@RequestParam(required = false) String search, Pageable pageable) {
        return childService.search(search, pageable);
    }

    @GetMapping("/{id}")
    public ChildDto getById(@PathVariable Long id) {
        return childService.getById(id);
    }

    @PostMapping
    public ChildDto create(@Valid @RequestBody ChildUpsertRequest request) {
        return childService.create(request);
    }

    // JSON bulk-create - partial success: invalid or duplicate entries are skipped and
    // reported in the response rather than failing the whole batch.
    @PostMapping("/bulk")
    public ChildImportResultDto createMany(@RequestBody List<ChildUpsertRequest> requests) {
        return childService.createMany(requests);
    }

    // Accepts .csv, .txt, or .xlsx - see ChildImportService for the expected columns
    @PostMapping("/upload")
    public ChildImportResultDto upload(@RequestParam("file") MultipartFile file) {
        return childImportService.importSheet(file);
    }

    @PutMapping("/{id}")
    public ChildDto update(@PathVariable Long id, @Valid @RequestBody ChildUpsertRequest request) {
        return childService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        childService.delete(id);
    }
}

