package com.annraksh.backend.dto.request;

import com.annraksh.backend.entity.DemandLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropDemandRequest(
        @NotNull @Positive Long cropId,
        @NotBlank @Size(max = 160) String location,
        @NotNull DemandLevel demandLevel,
        @NotNull @DecimalMin("0.0") BigDecimal demandQuantity,
        @NotNull @DecimalMin("0.0") BigDecimal supplyQuantity,
        @NotBlank @Size(max = 20) String quantityUnit,
        @NotNull LocalDate recordedDate
) {
}
