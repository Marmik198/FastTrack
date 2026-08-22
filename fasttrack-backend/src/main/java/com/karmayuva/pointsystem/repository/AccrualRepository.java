package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.Accrual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccrualRepository extends JpaRepository<Accrual, Long> {

    @Query("SELECT a FROM Accrual a WHERE (:childId IS NULL OR a.childId = :childId) AND (:month IS NULL OR :month = '' OR a.month = :month)")
    Page<Accrual> search(@Param("childId") Long childId, @Param("month") String month, Pageable pageable);
}
