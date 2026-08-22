package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.RedemptionDto;
import com.karmayuva.pointsystem.service.RedemptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/redemptions")
public class AdminRedemptionController {

    private final RedemptionService redemptionService;

    public AdminRedemptionController(RedemptionService redemptionService) {
        this.redemptionService = redemptionService;
    }

    @GetMapping
    public Page<RedemptionDto> search(@RequestParam(required = false) Long campaignId,
                                       @RequestParam(required = false) String childName,
                                       Pageable pageable) {
        return redemptionService.search(campaignId, childName, pageable);
    }
}
