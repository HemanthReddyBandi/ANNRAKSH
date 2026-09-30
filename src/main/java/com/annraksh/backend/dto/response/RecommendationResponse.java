package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.DemandLevel;
import com.annraksh.backend.entity.RecommendationDecision;

import java.time.Instant;

public record RecommendationResponse(
        Long id,
        Long cropId,
        String cropName,
        Integer score,
        DemandLevel demandLevel,
        RecommendationDecision decision,
        String reason,
        Instant createdAt
) {
}
