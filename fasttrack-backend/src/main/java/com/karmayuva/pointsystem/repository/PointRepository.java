package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT p FROM Point p WHERE (:childId IS NULL OR p.childId = :childId) AND (:month IS NULL OR :month = '' OR p.month = :month)")
    Page<Point> search(@Param("childId") Long childId, @Param("month") String month, Pageable pageable);

    List<Point> findByChildIdOrderByCreationDateAsc(Long childId);

    @Query("SELECT COALESCE(SUM(p.value), 0) FROM Point p WHERE p.childId = :childId")
    Integer sumValueByChildId(@Param("childId") Long childId);

    @Query("SELECT COALESCE(SUM(p.burnt), 0) FROM Point p WHERE p.childId = :childId")
    Integer sumBurntByChildId(@Param("childId") Long childId);
}
