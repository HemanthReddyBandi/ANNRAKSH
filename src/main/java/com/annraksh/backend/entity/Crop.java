package com.annraksh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String category;

    // Kept as descriptive text for now: growing seasons vary by location.
    @Column(nullable = false, length = 120)
    private String season;

    @Column(nullable = false)
    private Integer averageGrowthDays;
}
