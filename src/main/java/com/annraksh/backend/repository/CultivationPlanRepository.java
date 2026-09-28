package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivationPlanRepository extends JpaRepository<CultivationPlan, Long> {

    List<CultivationPlan> findByFarmerIdOrderByPlantingDateDesc(Long farmerId);

    List<CultivationPlan> findByStatusOrderByExpectedHarvestDateAsc(CultivationStatus status);

    List<CultivationPlan> findByCropIdAndActualHarvestQuantityIsNotNull(Long cropId);
}
