package com.annraksh.backend.dto.response;

public record CropResponse(Long id, String name, String category, String season, Integer averageGrowthDays) {
}
