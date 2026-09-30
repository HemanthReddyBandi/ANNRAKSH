package com.annraksh.backend.dto;

import com.annraksh.backend.dto.request.CropDemandRequest;
import com.annraksh.backend.dto.request.CropRequest;
import com.annraksh.backend.dto.request.CultivationPlanRequest;
import com.annraksh.backend.dto.response.CropDemandResponse;
import com.annraksh.backend.dto.response.CropResponse;
import com.annraksh.backend.dto.response.CultivationPlanResponse;
import com.annraksh.backend.dto.response.RecommendationResponse;
import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.Recommendation;
import org.springframework.stereotype.Component;

@Component
public class ApiDtoMapper {

    public Crop toEntity(CropRequest request) {
        Crop crop = new Crop();
        crop.setName(request.name().trim());
        crop.setCategory(request.category().trim());
        crop.setSeason(request.season().trim());
        crop.setAverageGrowthDays(request.averageGrowthDays());
        return crop;
    }

    public CropResponse toResponse(Crop crop) {
        return new CropResponse(crop.getId(), crop.getName(), crop.getCategory(),
                crop.getSeason(), crop.getAverageGrowthDays());
    }

    public CropDemand toEntity(CropDemandRequest request) {
        CropDemand demand = new CropDemand();
        demand.setLocation(request.location().trim());
        demand.setDemandLevel(request.demandLevel());
        demand.setDemandQuantity(request.demandQuantity());
        demand.setSupplyQuantity(request.supplyQuantity());
        demand.setQuantityUnit(request.quantityUnit().trim());
        demand.setRecordedDate(request.recordedDate());
        return demand;
    }

    public CropDemandResponse toResponse(CropDemand demand) {
        return new CropDemandResponse(demand.getId(), demand.getCrop().getId(), demand.getCrop().getName(),
                demand.getLocation(), demand.getDemandLevel(), demand.getDemandQuantity(),
                demand.getSupplyQuantity(), demand.getQuantityUnit(), demand.getRecordedDate());
    }

    public CultivationPlan toEntity(CultivationPlanRequest request) {
        CultivationPlan plan = new CultivationPlan();
        plan.setAreaHectares(request.areaHectares());
        plan.setPlannedQuantity(request.plannedQuantity());
        plan.setQuantityUnit(request.quantityUnit().trim());
        plan.setPlantingDate(request.plantingDate());
        plan.setExpectedHarvestDate(request.expectedHarvestDate());
        return plan;
    }

    public CultivationPlanResponse toResponse(CultivationPlan plan) {
        return new CultivationPlanResponse(plan.getId(), plan.getFarmer().getId(), plan.getCrop().getId(),
                plan.getCrop().getName(), plan.getAreaHectares(), plan.getPlannedQuantity(),
                plan.getActualHarvestQuantity(), plan.getQuantityUnit(), plan.getPlantingDate(),
                plan.getExpectedHarvestDate(), plan.getActualHarvestDate(), plan.getStatus());
    }

    public RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(recommendation.getId(), recommendation.getCrop().getId(),
                recommendation.getCrop().getName(), recommendation.getScore(), recommendation.getDemandLevel(),
                recommendation.getDecision(), recommendation.getReason(), recommendation.getCreatedAt());
    }
}
