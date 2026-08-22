package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.RedemptionDto;
import com.karmayuva.pointsystem.entity.Redemption;
import com.karmayuva.pointsystem.repository.RedemptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RedemptionService {

    private final RedemptionRepository redemptionRepository;

    public RedemptionService(RedemptionRepository redemptionRepository) {
        this.redemptionRepository = redemptionRepository;
    }

    public Page<RedemptionDto> search(Long campaignId, String childName, Pageable pageable) {
        return redemptionRepository.search(campaignId, childName, pageable).map(this::toDto);
    }

    private RedemptionDto toDto(Redemption r) {
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
