package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.CampaignDto;
import com.karmayuva.pointsystem.dto.CampaignUpsertRequest;
import com.karmayuva.pointsystem.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campaigns")
public class AdminCampaignController {

    private final CampaignService campaignService;

    public AdminCampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public Page<CampaignDto> search(@RequestParam(required = false) String search, Pageable pageable) {
        return campaignService.search(search, pageable);
    }

    @GetMapping("/{id}")
    public CampaignDto getById(@PathVariable Long id) {
        return campaignService.getById(id);
    }

    // Response includes formLink - the shareable URL for this campaign's form
    @PostMapping
    public CampaignDto create(@Valid @RequestBody CampaignUpsertRequest request) {
        return campaignService.create(request);
    }

    @PutMapping("/{id}")
    public CampaignDto update(@PathVariable Long id, @Valid @RequestBody CampaignUpsertRequest request) {
        return campaignService.update(id, request);
    }
}
