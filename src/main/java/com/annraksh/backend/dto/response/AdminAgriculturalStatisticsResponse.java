package com.annraksh.backend.dto.response;

import java.util.List;

public record AdminAgriculturalStatisticsResponse(
        long latestMarketSnapshotCount,
        long potentialSurplusSignalCount,
        long supplyGapSignalCount,
        long balancedSignalCount,
        List<MarketBalanceResponse> latestMarketBalances,
        List<HarvestProductionResponse> harvestProductionByCropAndUnit
) {
}
