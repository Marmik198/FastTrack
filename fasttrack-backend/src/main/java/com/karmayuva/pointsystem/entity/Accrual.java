package com.karmayuva.pointsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accruals")
@Getter
@Setter
@NoArgsConstructor
public class Accrual extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored as "YYYY-MM" (e.g. "2026-01") so it sorts and filters cleanly
    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private Integer frequency;

    @Column(nullable = false)
    private Long childId;

    // Denormalized snapshot of the child's name at the time of accrual, as specified -
    // handy for quick display/exports without a join, even though childId is the source of truth.
    @Column(nullable = false)
    private String childName;
}
