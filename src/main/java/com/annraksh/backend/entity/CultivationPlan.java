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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cultivation_plans")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CultivationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal areaHectares;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal plannedQuantity;

    // Optional until the plan is harvested; enables real historical yield comparisons.
    @Column(precision = 14, scale = 3)
    private BigDecimal actualHarvestQuantity;

    @Column(nullable = false, length = 20)
    private String quantityUnit;

    @Column(nullable = false)
    private LocalDate plantingDate;

    @Column(nullable = false)
    private LocalDate expectedHarvestDate;

    private LocalDate actualHarvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CultivationStatus status = CultivationStatus.PLANNED;
}
