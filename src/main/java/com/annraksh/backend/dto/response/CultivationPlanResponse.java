package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.CultivationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CultivationPlanResponse(
        Long id,
        Long farmerId,
        Long cropId,
        String cropName,
        BigDecimal areaHectares,
        BigDecimal plannedQuantity,
        BigDecimal actualHarvestQuantity,
        BigDecimal soldQuantity,
        BigDecimal storedQuantity,
        BigDecimal discardedQuantity,
        BigDecimal unallocatedHarvestQuantity,
        String quantityUnit,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        LocalDate actualHarvestDate,
        CultivationStatus status
) {
}
