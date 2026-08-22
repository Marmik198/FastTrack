package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.NameLookupRequest;
import com.karmayuva.pointsystem.dto.NameLookupResultDto;
import com.karmayuva.pointsystem.service.CampaignResponseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/lookup")
public class PublicLookupController {

    private final CampaignResponseService campaignResponseService;

    public PublicLookupController(CampaignResponseService campaignResponseService) {
        this.campaignResponseService = campaignResponseService;
    }

    // status: CLOSED | NOT_FOUND | ALREADY_SUBMITTED | OK
    @PostMapping
    public NameLookupResultDto lookup(@Valid @RequestBody NameLookupRequest request) {
        return campaignResponseService.lookup(request.name());
    }
}
