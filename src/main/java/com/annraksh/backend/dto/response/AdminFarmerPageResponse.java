package com.annraksh.backend.dto.response;

import java.util.List;

public record AdminFarmerPageResponse(
        List<FarmerResponse> farmers,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
