package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CropRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 120) String season,
        @NotNull @Positive Integer averageGrowthDays
) {
}
