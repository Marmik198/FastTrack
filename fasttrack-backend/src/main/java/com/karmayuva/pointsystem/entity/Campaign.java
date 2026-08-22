package com.karmayuva.pointsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
public class Campaign extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // If set, this takes precedence over endDate when deciding whether the form is still open -
    // lets an admin extend a campaign without losing the originally planned end date.
    private LocalDate extendedEndDate;

    @Transient
    public LocalDate getEffectiveEndDate() {
        return extendedEndDate != null ? extendedEndDate : endDate;
    }
}
