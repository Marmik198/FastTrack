package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c WHERE (:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Campaign> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.startDate <= :today ORDER BY c.startDate DESC")
    List<Campaign> findPotentiallyActive(@Param("today") LocalDate today);
}
