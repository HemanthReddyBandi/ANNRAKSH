package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CultivationPlan updateStatus(Long id, CultivationStatus status) {
        CultivationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cultivation plan not found: " + id));
        plan.setStatus(status);
        return planRepository.save(plan);
    }
}
