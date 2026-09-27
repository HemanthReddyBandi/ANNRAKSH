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
@Table(name = "crop_demand")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CropDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(nullable = false, length = 160)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemandLevel demandLevel;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal demandQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal supplyQuantity;

    @Column(nullable = false, length = 20)
    private String quantityUnit;

    @Column(nullable = false)
    private LocalDate recordedDate;
}
