package com.annraksh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "harvest_disposition_audits")
@Getter
@Setter
@NoArgsConstructor
public class HarvestDispositionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cultivation_plan_id", nullable = false)
    private CultivationPlan cultivationPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private HarvestDispositionAction action;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal previousSoldQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal previousStoredQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal previousDiscardedQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal soldQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal storedQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal discardedQuantity;

    @Column(nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    void setChangedAt() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}
