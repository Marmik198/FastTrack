package com.karmayuva.pointsystem.repository;

import com.karmayuva.pointsystem.entity.Child;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChildRepository extends JpaRepository<Child, Long> {

    @Query("SELECT c FROM Child c WHERE (:search IS NULL OR :search = '' OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Child> search(@Param("search") String search, Pageable pageable);
}
