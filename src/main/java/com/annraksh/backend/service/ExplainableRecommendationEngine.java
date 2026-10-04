package com.annraksh.backend.service;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.entity.DemandLevel;
import com.annraksh.backend.entity.RecommendationDecision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.Locale;
import java.util.Optional;

/** Deterministic scoring rules; no model training or opaque prediction is involved. */
@Component
public class ExplainableRecommendationEngine {

    public Assessment assess(Crop crop, CropDemand demand, Optional<BigDecimal> averageHarvestRatio,
                             Month currentMonth) {
        int demandLevelScore = demandLevelScore(demand.getDemandLevel());
        int supplyBalanceScore = supplyBalanceScore(demand);
        int marketScore = demandLevelScore + supplyBalanceScore;

        String currentSeason = seasonFor(currentMonth);
        boolean seasonSuitable = isSuitableSeason(crop.getSeason(), currentSeason);
        int seasonScore = seasonSuitable ? 30 : 0;

        int historyScore = averageHarvestRatio
                .map(ExplainableRecommendationEngine::historicalScore)
                .orElse(15);
        int score = marketScore + seasonScore + historyScore;
        RecommendationDecision decision = decisionFor(score);
        String reason = buildReason(demand, averageHarvestRatio, currentSeason, seasonSuitable,
                demandLevelScore, supplyBalanceScore, marketScore, seasonScore, historyScore, score);

        return new Assessment(score, marketScore, seasonScore, historyScore, decision, reason);
    }

    public String seasonFor(Month month) {
        return switch (month) {
            case JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER -> "Kharif";
            case NOVEMBER, DECEMBER, JANUARY, FEBRUARY, MARCH -> "Rabi";
            case APRIL, MAY -> "Zaid";
        };
    }

    private int demandLevelScore(DemandLevel level) {
        return switch (level) {
            case HIGH -> 20;
            case MEDIUM -> 14;
            case LOW -> 6;
        };
    }

    private int supplyBalanceScore(CropDemand demand) {
        if (demand.getDemandQuantity().signum() <= 0) {
            return 0;
        }
        BigDecimal supplyToDemand = demand.getSupplyQuantity().divide(
                demand.getDemandQuantity(), 4, RoundingMode.HALF_UP);
        if (supplyToDemand.compareTo(new BigDecimal("0.50")) <= 0) return 20;
        if (supplyToDemand.compareTo(new BigDecimal("0.80")) <= 0) return 17;
        if (supplyToDemand.compareTo(BigDecimal.ONE) <= 0) return 14;
        if (supplyToDemand.compareTo(new BigDecimal("1.20")) <= 0) return 8;
        return 2;
    }

    private static int historicalScore(BigDecimal averageHarvestRatio) {
        if (averageHarvestRatio.compareTo(new BigDecimal("1.10")) >= 0) return 30;
        if (averageHarvestRatio.compareTo(new BigDecimal("0.90")) >= 0) return 25;
        if (averageHarvestRatio.compareTo(new BigDecimal("0.75")) >= 0) return 20;
        if (averageHarvestRatio.compareTo(new BigDecimal("0.50")) >= 0) return 12;
        return 5;
    }

    private boolean isSuitableSeason(String cropSeason, String currentSeason) {
        if (cropSeason == null || cropSeason.isBlank()) {
            return false;
        }
        String normalized = cropSeason.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ');
        if (normalized.contains("year round") || normalized.contains("all season")) {
            return true;
        }
        String season = currentSeason.toLowerCase(Locale.ROOT);
        if (normalized.contains(season)) {
            return true;
        }
        return switch (currentSeason) {
            case "Kharif" -> normalized.contains("monsoon");
            case "Rabi" -> normalized.contains("winter");
            case "Zaid" -> normalized.contains("summer");
            default -> false;
        };
    }

    private String buildReason(CropDemand demand, Optional<BigDecimal> averageHarvestRatio,
                               String currentSeason, boolean seasonSuitable,
                               int demandLevelScore, int supplyBalanceScore, int marketScore,
                               int seasonScore, int historyScore, int score) {
        String demandText = switch (demand.getDemandLevel()) {
            case HIGH -> "High";
            case MEDIUM -> "Moderate";
            case LOW -> "Low";
        };
        String supplyText;
        if (demand.getDemandQuantity().signum() <= 0) {
            supplyText = "demand quantity is zero, so supply balance cannot add market points";
        } else {
            BigDecimal ratio = demand.getSupplyQuantity().divide(
                    demand.getDemandQuantity(), 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.80")) < 0) {
                supplyText = "supply is below demand";
            } else if (ratio.compareTo(new BigDecimal("1.20")) <= 0) {
                supplyText = "supply is close to demand";
            } else {
                supplyText = "supply exceeds demand";
            }
        }
        String seasonText = seasonSuitable
                ? "the crop's listed season suits current " + currentSeason + " conditions"
                : "the crop is not listed for current " + currentSeason + " conditions";
        String historyText = averageHarvestRatio
                .map(ratio -> "past harvested output averaged " + formatPercent(ratio)
                        + " of planned quantity")
                .orElse("no completed harvest history is available; a neutral history score was used");

        return String.format(Locale.ROOT,
                "%s market demand and %s. Market: %d/40 (level %d/20, supply balance %d/20); "
                        + "season: %d/30 (%s); historical performance: %d/30 (%s). Total: %d/100. Demand data recorded %s.",
                demandText, supplyText, marketScore, demandLevelScore, supplyBalanceScore,
                seasonScore, seasonText, historyScore, historyText, score, demand.getRecordedDate());
    }

    private static String formatPercent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private RecommendationDecision decisionFor(int score) {
        if (score >= 80) return RecommendationDecision.HIGHLY_RECOMMENDED;
        if (score >= 65) return RecommendationDecision.RECOMMENDED;
        if (score >= 45) return RecommendationDecision.NEUTRAL;
        return RecommendationDecision.NOT_RECOMMENDED;
    }

    public record Assessment(int score, int marketScore, int seasonScore, int historicalPerformanceScore,
                             RecommendationDecision decision, String reason) {
    }
}
