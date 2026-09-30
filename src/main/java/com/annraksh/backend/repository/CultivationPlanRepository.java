package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivationPlanRepository extends JpaRepository<CultivationPlan, Long> {

    @Override
    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findAll();

    @Override
    @EntityGraph(attributePaths = {"farmer", "crop"})
    java.util.Optional<CultivationPlan> findById(Long id);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByFarmerIdOrderByPlantingDateDesc(Long farmerId);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByStatusOrderByExpectedHarvestDateAsc(CultivationStatus status);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByCropIdAndActualHarvestQuantityIsNotNull(Long cropId);
}
