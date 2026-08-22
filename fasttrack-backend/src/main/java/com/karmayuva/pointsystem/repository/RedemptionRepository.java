package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    @Query("SELECT r FROM Redemption r WHERE (:campaignId IS NULL OR r.campaignId = :campaignId) AND (:childName IS NULL OR :childName = '' OR LOWER(r.childName) LIKE LOWER(CONCAT('%', :childName, '%')))")
    Page<Redemption> search(@Param("campaignId") Long campaignId, @Param("childName") String childName, Pageable pageable);
}
