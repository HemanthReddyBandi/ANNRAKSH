package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HarvestRecordRequest(
        @NotNull @DecimalMin("0.001") @Digits(integer = 11, fraction = 3) BigDecimal actualHarvestQuantity,
        @NotNull @PastOrPresent LocalDate actualHarvestDate,
        @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal soldQuantity,
        @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal storedQuantity,
        @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal discardedQuantity
) {
}
