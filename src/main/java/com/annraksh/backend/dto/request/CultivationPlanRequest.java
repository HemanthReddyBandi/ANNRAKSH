package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CultivationPlanRequest(
        @NotNull @Positive Long farmerId,
        @NotNull @Positive Long cropId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal areaHectares,
        @NotNull @DecimalMin(value = "0.001") BigDecimal plannedQuantity,
        @NotBlank @Size(max = 20) String quantityUnit,
        @NotNull LocalDate plantingDate,
        @NotNull LocalDate expectedHarvestDate
) {
}
