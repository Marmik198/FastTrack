package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.ChildDto;
import com.karmayuva.pointsystem.dto.ChildUpsertRequest;
import com.karmayuva.pointsystem.service.ChildService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/children")
public class AdminChildController {

    private final ChildService childService;

    public AdminChildController(ChildService childService) {
        this.childService = childService;
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

    @PutMapping("/{id}")
    public ChildDto update(@PathVariable Long id, @Valid @RequestBody ChildUpsertRequest request) {
        return childService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        childService.delete(id);
    }
}
