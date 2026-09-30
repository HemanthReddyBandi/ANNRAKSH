package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.DemandLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropDemandResponse(
        Long id,
        Long cropId,
        String cropName,
        String location,
        DemandLevel demandLevel,
        BigDecimal demandQuantity,
        BigDecimal supplyQuantity,
        String quantityUnit,
        LocalDate recordedDate
) {
}
