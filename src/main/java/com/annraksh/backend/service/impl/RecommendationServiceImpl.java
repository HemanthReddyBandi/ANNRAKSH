package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Recommendation;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.exception.BusinessRuleException;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.RecommendationRepository;
import com.annraksh.backend.service.ExplainableRecommendationEngine;
import com.annraksh.backend.service.RecommendationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FarmerRepository farmerRepository;
    private final CropDemandRepository cropDemandRepository;
    private final CultivationPlanRepository planRepository;
    private final ExplainableRecommendationEngine recommendationEngine;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository,
                                     FarmerRepository farmerRepository,
                                     CropDemandRepository cropDemandRepository,
                                     CultivationPlanRepository planRepository,
                                     ExplainableRecommendationEngine recommendationEngine) {
        this.recommendationRepository = recommendationRepository;
        this.farmerRepository = farmerRepository;
        this.cropDemandRepository = cropDemandRepository;
        this.planRepository = planRepository;
        this.recommendationEngine = recommendationEngine;
    }

    @Override
    public List<Recommendation> getForFarmer(Long farmerId) {
        return recommendationRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }

    @Override
    public Recommendation getForFarmerById(Long recommendationId, Long farmerId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found: " + recommendationId));
        if (!recommendation.getFarmer().getId().equals(farmerId)) {
            throw new EntityNotFoundException("Recommendation not found: " + recommendationId);
        }
        return recommendation;
    }

    @Override
    @Transactional
    public List<Recommendation> generateForFarmer(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found: " + farmerId));
        if (farmer.getLocation() == null || farmer.getLocation().isBlank()) {
            throw new BusinessRuleException("Add your location to your profile before generating recommendations");
        }

        List<CropDemand> latestDemand = cropDemandRepository.findLatestRecordsByLocation(farmer.getLocation());
        if (latestDemand.isEmpty()) {
            throw new BusinessRuleException("No crop demand data is available for your location yet");
        }

        Map<Long, List<CultivationPlan>> historyByCrop = planRepository
                .findByFarmerIdAndActualHarvestQuantityIsNotNull(farmerId)
                .stream()
                .collect(Collectors.groupingBy(plan -> plan.getCrop().getId()));

        Month currentMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).getMonth();
        List<Recommendation> recommendations = new ArrayList<>(latestDemand.size());
        for (CropDemand demand : latestDemand) {
            Optional<BigDecimal> performance = averageHarvestRatio(
                    historyByCrop.getOrDefault(demand.getCrop().getId(), List.of()));
            ExplainableRecommendationEngine.Assessment assessment = recommendationEngine.assess(
                    demand.getCrop(), demand, performance, currentMonth);

            Recommendation recommendation = new Recommendation();
            recommendation.setFarmer(farmer);
            recommendation.setCrop(demand.getCrop());
            recommendation.setScore(assessment.score());
            recommendation.setMarketScore(assessment.marketScore());
            recommendation.setSeasonScore(assessment.seasonScore());
            recommendation.setHistoricalPerformanceScore(assessment.historicalPerformanceScore());
            recommendation.setDemandLevel(demand.getDemandLevel());
            recommendation.setDecision(assessment.decision());
            recommendation.setReason(assessment.reason());
            recommendation.setDemandRecordedDate(demand.getRecordedDate());
            recommendations.add(recommendation);
        }
        return recommendationRepository.saveAll(recommendations);
    }

    private Optional<BigDecimal> averageHarvestRatio(List<CultivationPlan> plans) {
        BigDecimal sumOfRatios = BigDecimal.ZERO;
        int completedPlanCount = 0;
        for (CultivationPlan plan : plans) {
            if (plan.getPlannedQuantity() != null && plan.getPlannedQuantity().signum() > 0
                    && plan.getActualHarvestQuantity() != null) {
                BigDecimal ratio = plan.getActualHarvestQuantity().divide(
                        plan.getPlannedQuantity(), 4, RoundingMode.HALF_UP);
                sumOfRatios = sumOfRatios.add(ratio);
                completedPlanCount++;
            }
        }
        if (completedPlanCount == 0) {
            return Optional.empty();
        }
        return Optional.of(sumOfRatios.divide(BigDecimal.valueOf(completedPlanCount),
                4, RoundingMode.HALF_UP));
    }
}
