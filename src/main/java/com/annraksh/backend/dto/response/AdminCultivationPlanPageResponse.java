package com.annraksh.backend.dto.response;

import java.util.List;

public record AdminCultivationPlanPageResponse(
        List<AdminCultivationPlanResponse> plans,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
