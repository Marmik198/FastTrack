package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.CampaignResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CampaignResponseRepository extends JpaRepository<CampaignResponse, Long> {

    boolean existsByCampaignIdAndChildId(Long campaignId, Long childId);

    Optional<CampaignResponse> findByCampaignIdAndChildId(Long campaignId, Long childId);

    @Query("SELECT r FROM CampaignResponse r WHERE (:campaignId IS NULL OR r.campaignId = :campaignId) AND (:childId IS NULL OR r.childId = :childId) AND (:pendingOnly = false OR (r.carryForwardFlag = false AND r.approved = false))")
    Page<CampaignResponse> search(@Param("campaignId") Long campaignId, @Param("childId") Long childId, @Param("pendingOnly") boolean pendingOnly, Pageable pageable);
}
