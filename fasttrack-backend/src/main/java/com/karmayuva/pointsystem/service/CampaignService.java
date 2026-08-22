package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.CampaignDto;
import com.karmayuva.pointsystem.dto.CampaignUpsertRequest;
import com.karmayuva.pointsystem.entity.Campaign;
import com.karmayuva.pointsystem.exception.ResourceNotFoundException;
import com.karmayuva.pointsystem.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public Page<CampaignDto> search(String search, Pageable pageable) {
        return campaignRepository.search(search, pageable).map(this::toDto);
    }

    public CampaignDto getById(Long id) {
        return toDto(getEntity(id));
    }

    public Campaign getEntity(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
    }

    public CampaignDto create(CampaignUpsertRequest request) {
        Campaign campaign = new Campaign();
        applyRequest(campaign, request);
        return toDto(campaignRepository.save(campaign));
    }

    public CampaignDto update(Long id, CampaignUpsertRequest request) {
        Campaign campaign = getEntity(id);
        applyRequest(campaign, request);
        return toDto(campaignRepository.save(campaign));
    }

    // The form is open for a child at any point in time when today falls between
    // startDate and the effective end date (extendedEndDate if set, else endDate).
    // If more than one campaign is technically "open" at once, we take the most
    // recently started one as authoritative.
    public Optional<Campaign> findActive() {
        LocalDate today = LocalDate.now();
        List<Campaign> candidates = campaignRepository.findPotentiallyActive(today);
        return candidates.stream()
                .filter(c -> !today.isAfter(c.getEffectiveEndDate()))
                .max(Comparator.comparing(Campaign::getStartDate));
    }

    private void applyRequest(Campaign campaign, CampaignUpsertRequest request) {
        campaign.setName(request.name().trim());
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        campaign.setExtendedEndDate(request.extendedEndDate());
    }

    private CampaignDto toDto(Campaign c) {
        LocalDate today = LocalDate.now();
        boolean open = !today.isBefore(c.getStartDate()) && !today.isAfter(c.getEffectiveEndDate());
        String formLink = frontendBaseUrl + "/form/" + c.getId();

        return new CampaignDto(
                c.getId(),
                c.getName(),
                c.getStartDate().toString(),
                c.getEndDate().toString(),
                c.getExtendedEndDate() != null ? c.getExtendedEndDate().toString() : null,
                open,
                formLink,
                c.getCreationDate() != null ? c.getCreationDate().toString() : null,
                c.getLastModifiedDate() != null ? c.getLastModifiedDate().toString() : null
        );
    }
}
