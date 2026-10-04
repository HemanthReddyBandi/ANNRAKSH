package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.DemandLevel;
import com.annraksh.backend.entity.RecommendationDecision;

import java.time.Instant;
import java.time.LocalDate;

public record RecommendationResponse(
        Long id,
        Long cropId,
        String cropName,
        Integer score,
        Integer marketScore,
        Integer seasonScore,
        Integer historicalPerformanceScore,
        DemandLevel demandLevel,
        RecommendationDecision decision,
        String reason,
        LocalDate demandRecordedDate,
        Instant createdAt
) {
}
