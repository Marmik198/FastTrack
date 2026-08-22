package com.karmayuva.pointsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "redemptions")
@Getter
@Setter
@NoArgsConstructor
public class Redemption extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String childName;

    // Points consumed by this redemption, decided by the admin at approval time
    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String item;

    @Column(columnDefinition = "TEXT")
    private String link;

    private Integer quantity;

    @Column(nullable = false)
    private Long campaignId;

    @Column(nullable = false, unique = true)
    private Long responseId;

    // Admin username who approved/created this redemption, for audit trail
    @Column(nullable = false)
    private String createdBy;
}
