package com.karmayuva.pointsystem.entity;

import com.karmayuva.pointsystem.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A child's (or admin-entered manual) response to a campaign form.
 * `childId` here corresponds to the "customerId" column named in the original spec.
 * Submitting this does NOT burn points or create a Redemption by itself - an admin
 * must explicitly approve it (see CampaignResponseService.approve), which is what
 * actually deducts points and produces the Redemption record used for ordering gifts.
 */
@Entity
@Table(name = "campaign_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campaign_id", "child_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CampaignResponse extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(nullable = false)
    private String campaignName;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(nullable = false)
    private Boolean carryForwardFlag;

    @Column(columnDefinition = "TEXT")
    private String link;

    private Integer quantity;

    @Column(length = 1000)
    private String remarks;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String email;

    // Set true once an admin has approved this response into a Redemption -
    // prevents double-approval and makes the "pending approval" queue a simple filter.
    @Column(nullable = false)
    private Boolean approved = false;
}
