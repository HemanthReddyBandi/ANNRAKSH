package com.annraksh.backend.service;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.entity.DemandLevel;
import com.annraksh.backend.entity.RecommendationDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExplainableRecommendationEngineTest {

    private final ExplainableRecommendationEngine engine = new ExplainableRecommendationEngine();

    @Test
    void scoresStrongMarketSeasonAndHarvestHistoryAtOneHundred() {
        Crop crop = crop("Tomato", "Zaid");
        CropDemand demand = demand(crop, DemandLevel.HIGH, "100", "50");

        ExplainableRecommendationEngine.Assessment assessment = engine.assess(
                crop, demand, Optional.of(new BigDecimal("1.10")), Month.MAY);

        assertThat(assessment.score()).isEqualTo(100);
        assertThat(assessment.marketScore()).isEqualTo(40);
        assertThat(assessment.seasonScore()).isEqualTo(30);
        assertThat(assessment.historicalPerformanceScore()).isEqualTo(30);
        assertThat(assessment.decision()).isEqualTo(RecommendationDecision.HIGHLY_RECOMMENDED);
        assertThat(assessment.reason()).contains("supply is below demand", "past harvested output averaged 110.0%");
    }

    @Test
    void penalizesOversupplyAndUnsuitableSeasonWithoutHarvestHistory() {
        Crop crop = crop("Wheat", "Rabi");
        CropDemand demand = demand(crop, DemandLevel.LOW, "100", "150");

        ExplainableRecommendationEngine.Assessment assessment = engine.assess(
                crop, demand, Optional.empty(), Month.MAY);

        assertThat(assessment.score()).isEqualTo(23);
        assertThat(assessment.marketScore()).isEqualTo(8);
        assertThat(assessment.seasonScore()).isZero();
        assertThat(assessment.historicalPerformanceScore()).isEqualTo(15);
        assertThat(assessment.decision()).isEqualTo(RecommendationDecision.NOT_RECOMMENDED);
        assertThat(assessment.reason()).contains("supply exceeds demand", "no completed harvest history");
    }

    @Test
    void handlesZeroDemandQuantityWithoutDividingByZero() {
        Crop crop = crop("Beans", "Year-round");
        CropDemand demand = demand(crop, DemandLevel.MEDIUM, "0", "20");

        ExplainableRecommendationEngine.Assessment assessment = engine.assess(
                crop, demand, Optional.empty(), Month.JULY);

        assertThat(assessment.score()).isEqualTo(59);
        assertThat(assessment.reason()).contains("demand quantity is zero");
    }

    private Crop crop(String name, String season) {
        Crop crop = new Crop();
        crop.setName(name);
        crop.setSeason(season);
        return crop;
    }

    private CropDemand demand(Crop crop, DemandLevel level, String demandQuantity, String supplyQuantity) {
        CropDemand demand = new CropDemand();
        demand.setCrop(crop);
        demand.setDemandLevel(level);
        demand.setDemandQuantity(new BigDecimal(demandQuantity));
        demand.setSupplyQuantity(new BigDecimal(supplyQuantity));
        demand.setRecordedDate(LocalDate.of(2026, 5, 1));
        return demand;
    }
}
