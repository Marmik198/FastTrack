package com.karmayuva.pointsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One Point row is created per Accrual, with value = frequency * 100.
 * `burnt` tracks how much of THIS row has been redeemed so far, so a child's
 * available balance is always sum(value) - sum(burnt) across their rows.
 * Redemptions consume points FIFO across a child's Point rows (oldest first) -
 * see PointService.burnPoints.
 */
@Entity
@Table(name = "points")
@Getter
@Setter
@NoArgsConstructor
public class Point extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private Integer value;

    @Column(nullable = false)
    private Integer burnt = 0;

    @Column(nullable = false)
    private Long childId;

    @Column(nullable = false)
    private String childName;

    @Column(nullable = false)
    private Long accrualId;
}
