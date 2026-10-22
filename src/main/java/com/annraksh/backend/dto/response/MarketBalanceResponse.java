package com.annraksh.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketBalanceResponse(
        Long cropId,
        String cropName,
        String location,
        BigDecimal demandQuantity,
        BigDecimal supplyQuantity,
        BigDecimal supplyMinusDemand,
        String quantityUnit,
        String signal,
        LocalDate recordedDate
) {
}
