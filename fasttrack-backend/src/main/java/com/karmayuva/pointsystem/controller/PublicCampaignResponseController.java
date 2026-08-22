package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.CampaignResponseDto;
import com.karmayuva.pointsystem.dto.CampaignResponseSubmitRequest;
import com.karmayuva.pointsystem.service.CampaignResponseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/campaign-responses")
public class PublicCampaignResponseController {

    private final CampaignResponseService campaignResponseService;

    public PublicCampaignResponseController(CampaignResponseService campaignResponseService) {
        this.campaignResponseService = campaignResponseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponseDto submit(@Valid @RequestBody CampaignResponseSubmitRequest request) {
        return campaignResponseService.submitPublic(request);
    }
}
