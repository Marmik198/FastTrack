package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.ChildPointsSummaryDto;
import com.karmayuva.pointsystem.dto.PointDto;
import com.karmayuva.pointsystem.service.PointService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/points")
public class AdminPointController {

    private final PointService pointService;

    public AdminPointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping
    public Page<PointDto> search(@RequestParam(required = false) Long childId,
                                  @RequestParam(required = false) String month,
                                  Pageable pageable) {
        return pointService.search(childId, month, pageable);
    }

    // Merged view: every child with their accrued / burnt / available point totals
    @GetMapping("/summary")
    public Page<ChildPointsSummaryDto> summary(@RequestParam(required = false) String search, Pageable pageable) {
        return pointService.pointsSummary(search, pageable);
    }
}
