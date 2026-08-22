package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.*;
import com.karmayuva.pointsystem.entity.Campaign;
import com.karmayuva.pointsystem.entity.CampaignResponse;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.entity.Redemption;
import com.karmayuva.pointsystem.exception.BusinessException;
import com.karmayuva.pointsystem.exception.ResourceNotFoundException;
import com.karmayuva.pointsystem.repository.CampaignResponseRepository;
import com.karmayuva.pointsystem.repository.RedemptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CampaignResponseService {

    private final CampaignResponseRepository responseRepository;
    private final RedemptionRepository redemptionRepository;
    private final CampaignService campaignService;
    private final ChildService childService;
    private final PointService pointService;
    private final NameMatchingService nameMatchingService;

    public CampaignResponseService(CampaignResponseRepository responseRepository,
                                    RedemptionRepository redemptionRepository,
                                    CampaignService campaignService,
                                    ChildService childService,
                                    PointService pointService,
                                    NameMatchingService nameMatchingService) {
        this.responseRepository = responseRepository;
        this.redemptionRepository = redemptionRepository;
        this.campaignService = campaignService;
        this.childService = childService;
        this.pointService = pointService;
        this.nameMatchingService = nameMatchingService;
    }

    // ---------------- Public flow ----------------

    /**
     * Looks up the entered name against the fuzzy matcher, and reports back exactly
     * what state the public form should render: closed / not found / already
     * submitted / ready-to-fill (with the child's info + available points).
     */
    public NameLookupResultDto lookup(String rawName) {
        Optional<Campaign> activeCampaign = campaignService.findActive();
        if (activeCampaign.isEmpty()) {
            return new NameLookupResultDto("CLOSED", null, null, null, null, null, null, null, null, null);
        }
        Campaign campaign = activeCampaign.get();

        Optional<Child> matched = nameMatchingService.findBestMatch(rawName);
        if (matched.isEmpty()) {
            return new NameLookupResultDto("NOT_FOUND", null, null, null, null, null, null, null, null, null);
        }
        Child child = matched.get();

        if (responseRepository.existsByCampaignIdAndChildId(campaign.getId(), child.getId())) {
            return new NameLookupResultDto("ALREADY_SUBMITTED", child.getId(), null, null, null, null, null, null,
                    campaign.getId(), campaign.getName());
        }

        int available = pointService.getAvailableBalance(child.getId());

        return new NameLookupResultDto(
                "OK",
                child.getId(),
                child.getFirstName(),
                child.getLastName(),
                child.getMobile(),
                child.getEmail(),
                child.getAddress(),
                available,
                campaign.getId(),
                campaign.getName()
        );
    }

    @Transactional
    public CampaignResponseDto submitPublic(CampaignResponseSubmitRequest request) {
        Campaign campaign = campaignService.getEntity(request.campaignId());
        LocalDate today = LocalDate.now();
        if (today.isBefore(campaign.getStartDate()) || today.isAfter(campaign.getEffectiveEndDate())) {
            throw new BusinessException("This campaign's form is not currently open.");
        }
        return submit(request, campaign);
    }

    // ---------------- Admin flow ----------------

    @Transactional
    public CampaignResponseDto createManual(ManualResponseCreateRequest request) {
        Campaign campaign = campaignService.getEntity(request.campaignId());
        CampaignResponseSubmitRequest asSubmit = new CampaignResponseSubmitRequest(
                request.childId(), request.campaignId(), request.carryForwardFlag(),
                request.link(), request.quantity(), request.remarks(), request.email());
        return submit(asSubmit, campaign);
    }

    public Page<CampaignResponseDto> search(Long campaignId, Long childId, boolean pendingOnly, Pageable pageable) {
        return responseRepository.search(campaignId, childId, pendingOnly, pageable).map(this::toDto);
    }

    /**
     * Approves a response into a Redemption. Only allowed when carryForwardFlag is
     * false and a product link was actually provided, per the required business rule.
     * Burns the admin-decided `amount` of points at this point (not at submission time).
     */
    @Transactional
    public RedemptionDto approve(Long responseId, ApproveRedemptionRequest request, String adminUsername) {
        CampaignResponse response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign response not found"));

        if (Boolean.TRUE.equals(response.getApproved())) {
            throw new BusinessException("This response has already been approved");
        }
        if (Boolean.TRUE.equals(response.getCarryForwardFlag())) {
            throw new BusinessException("Cannot approve a redemption for a carry-forward response");
        }
        if (response.getLink() == null || response.getLink().isBlank()) {
            throw new BusinessException("Cannot approve - no product link was provided");
        }

        Child child = childService.getEntity(response.getChildId());

        pointService.burnPoints(child.getId(), request.amount());

        Redemption redemption = new Redemption();
        redemption.setDate(LocalDate.now());
        redemption.setChildName(child.getFullName());
        redemption.setAmount(request.amount());
        redemption.setItem(request.item());
        redemption.setLink(response.getLink());
        redemption.setQuantity(response.getQuantity());
        redemption.setCampaignId(response.getCampaignId());
        redemption.setResponseId(response.getId());
        redemption.setCreatedBy(adminUsername);
        redemption = redemptionRepository.save(redemption);

        response.setApproved(true);
        responseRepository.save(response);

        return toRedemptionDto(redemption);
    }

    // ---------------- shared ----------------

    private CampaignResponseDto submit(CampaignResponseSubmitRequest request, Campaign campaign) {
        Child child = childService.getEntity(request.childId());

        if (responseRepository.existsByCampaignIdAndChildId(campaign.getId(), child.getId())) {
            throw new BusinessException(child.getFullName() + " has already submitted a response for this campaign.");
        }

        if (!request.carryForwardFlag()) {
            if (request.link() == null || request.link().isBlank()) {
                throw new BusinessException("Please provide a product link");
            }
            if (request.quantity() == null || request.quantity() < 1) {
                throw new BusinessException("Please provide a valid quantity");
            }
        }

        CampaignResponse response = new CampaignResponse();
        response.setCampaignId(campaign.getId());
        response.setCampaignName(campaign.getName());
        response.setChildId(child.getId());
        response.setCarryForwardFlag(request.carryForwardFlag());
        response.setLink(request.carryForwardFlag() ? null : request.link().trim());
        response.setQuantity(request.carryForwardFlag() ? null : request.quantity());
        response.setRemarks(request.remarks());
        response.setEmail(request.email());
        response.setApproved(false);

        return toDto(responseRepository.save(response));
    }

    private CampaignResponseDto toDto(CampaignResponse r) {
        Child child = childService.getEntity(r.getChildId());
        return new CampaignResponseDto(
                r.getId(),
                r.getCampaignId(),
                r.getCampaignName(),
                r.getChildId(),
                child.getFullName(),
                r.getCarryForwardFlag(),
                r.getLink(),
                r.getQuantity(),
                r.getRemarks(),
                r.getEmail(),
                r.getApproved(),
                r.getCreationDate() != null ? r.getCreationDate().toString() : null,
                r.getLastModifiedDate() != null ? r.getLastModifiedDate().toString() : null
        );
    }

    private RedemptionDto toRedemptionDto(Redemption r) {
        return new RedemptionDto(
                r.getId(),
                r.getDate().toString(),
                r.getChildName(),
                r.getAmount(),
                r.getItem(),
                r.getLink(),
                r.getQuantity(),
                r.getCampaignId(),
                r.getResponseId(),
                r.getCreatedBy(),
                r.getCreationDate() != null ? r.getCreationDate().toString() : null
        );
    }
}
