package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.HarvestDispositionAction;

import java.math.BigDecimal;
import java.time.Instant;

public record HarvestDispositionAuditResponse(
        Long id,
        HarvestDispositionAction action,
        BigDecimal previousSoldQuantity,
        BigDecimal previousStoredQuantity,
        BigDecimal previousDiscardedQuantity,
        BigDecimal soldQuantity,
        BigDecimal storedQuantity,
        BigDecimal discardedQuantity,
        Instant changedAt
) {
}
