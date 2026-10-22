package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HarvestDispositionUpdateRequest(
        @NotNull @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal soldQuantity,
        @NotNull @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal storedQuantity,
        @NotNull @DecimalMin("0.000") @Digits(integer = 11, fraction = 3) BigDecimal discardedQuantity
) {
}
