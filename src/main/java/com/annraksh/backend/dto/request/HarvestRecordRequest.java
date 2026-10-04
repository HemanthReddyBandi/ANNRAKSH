package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HarvestRecordRequest(
        @NotNull @DecimalMin("0.001") BigDecimal actualHarvestQuantity,
        @NotNull @PastOrPresent LocalDate actualHarvestDate
) {
}
