package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.exception.BusinessRuleException;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CultivationPlanServiceImpl implements CultivationPlanService {

    private final CultivationPlanRepository planRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;

    public CultivationPlanServiceImpl(CultivationPlanRepository planRepository,
                                      FarmerRepository farmerRepository,
                                      CropRepository cropRepository) {
        this.planRepository = planRepository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public List<CultivationPlan> getForFarmer(Long farmerId) {
        return planRepository.findByFarmerIdOrderByPlantingDateDesc(farmerId);
    }

    @Override
    public List<CultivationPlan> getAll() {
        return planRepository.findAll();
    }

    @Override
    @Transactional
    public CultivationPlan create(CultivationPlan plan, Long farmerId, Long cropId) {
        if (plan.getExpectedHarvestDate().isBefore(plan.getPlantingDate())) {
            throw new IllegalArgumentException("Expected harvest date must be on or after planting date");
        }
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found: " + farmerId));
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found: " + cropId));
        plan.setFarmer(farmer);
        plan.setCrop(crop);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public CultivationPlan updateStatus(Long id, CultivationStatus status, Long farmerId) {
        CultivationPlan plan = findPlanOwnedByFarmer(id, farmerId);
        if (status == CultivationStatus.HARVESTED) {
            throw new BusinessRuleException("Use the harvest endpoint to record an actual harvest");
        }
        if (plan.getStatus() == CultivationStatus.HARVESTED && status != CultivationStatus.HARVESTED) {
            throw new BusinessRuleException("A harvested plan cannot return to an active status");
        }
        plan.setStatus(status);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public CultivationPlan recordHarvest(Long id, BigDecimal actualQuantity, LocalDate actualDate, Long farmerId) {
        CultivationPlan plan = findPlanOwnedByFarmer(id, farmerId);
        if (actualDate.isBefore(plan.getPlantingDate())) {
            throw new BusinessRuleException("Actual harvest date cannot be before planting date");
        }
        if (plan.getStatus() == CultivationStatus.CANCELLED) {
            throw new BusinessRuleException("A cancelled plan cannot be marked as harvested");
        }
        plan.setActualHarvestQuantity(actualQuantity);
        plan.setActualHarvestDate(actualDate);
        plan.setStatus(CultivationStatus.HARVESTED);
        return planRepository.save(plan);
    }

    private CultivationPlan findPlanOwnedByFarmer(Long id, Long farmerId) {
        CultivationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cultivation plan not found: " + id));
        if (!plan.getFarmer().getId().equals(farmerId)) {
            throw new EntityNotFoundException("Cultivation plan not found: " + id);
        }
        return plan;
    }
}
