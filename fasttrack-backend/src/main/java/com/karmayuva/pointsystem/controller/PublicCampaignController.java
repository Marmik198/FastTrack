package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.CampaignDto;
import com.karmayuva.pointsystem.entity.Campaign;
import com.karmayuva.pointsystem.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/campaign")
public class PublicCampaignController {

    private final CampaignService campaignService;

    public PublicCampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/active")
    public ResponseEntity<CampaignDto> getActive() {
        return campaignService.findActive()
                .map(Campaign::getId)
                .map(campaignService::getById)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
