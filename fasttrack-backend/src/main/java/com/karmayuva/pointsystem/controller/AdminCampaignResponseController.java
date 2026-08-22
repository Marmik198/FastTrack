package com.karmayuva.pointsystem.controller;

import com.karmayuva.pointsystem.dto.*;
import com.karmayuva.pointsystem.service.CampaignResponseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campaign-responses")
public class AdminCampaignResponseController {

    private final CampaignResponseService campaignResponseService;

    public AdminCampaignResponseController(CampaignResponseService campaignResponseService) {
        this.campaignResponseService = campaignResponseService;
    }

    @GetMapping
    public Page<CampaignResponseDto> search(@RequestParam(required = false) Long campaignId,
                                             @RequestParam(required = false) Long childId,
                                             @RequestParam(defaultValue = "false") boolean pendingOnly,
                                             Pageable pageable) {
        return campaignResponseService.search(campaignId, childId, pendingOnly, pageable);
    }

    // Admin fills the form on a child's behalf (e.g. no internet access at home)
    @PostMapping("/manual")
    public CampaignResponseDto createManual(@Valid @RequestBody ManualResponseCreateRequest request) {
        return campaignResponseService.createManual(request);
    }

    // Only allowed when carryForwardFlag = false and a product link was provided
    @PostMapping("/{id}/approve")
    public RedemptionDto approve(@PathVariable Long id,
                                  @Valid @RequestBody ApproveRedemptionRequest request,
                                  Authentication authentication) {
        return campaignResponseService.approve(id, request, authentication.getName());
    }
}
