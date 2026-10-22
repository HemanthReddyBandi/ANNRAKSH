package com.annraksh.backend.dto.response;

import java.math.BigDecimal;

public record HarvestProductionResponse(
        Long cropId,
        String cropName,
        String quantityUnit,
        long harvestCount,
        BigDecimal totalPlannedQuantity,
        BigDecimal totalHarvestedQuantity,
        BigDecimal totalSoldQuantity,
        BigDecimal totalStoredQuantity,
        BigDecimal totalDiscardedQuantity,
        BigDecimal totalUnallocatedQuantity,
        BigDecimal totalAreaHectares,
        BigDecimal harvestedQuantityPerHectare
) {
}
