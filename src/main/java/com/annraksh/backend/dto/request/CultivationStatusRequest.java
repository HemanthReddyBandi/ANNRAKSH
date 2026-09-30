package com.annraksh.backend.dto.request;

import com.annraksh.backend.entity.CultivationStatus;
import jakarta.validation.constraints.NotNull;

public record CultivationStatusRequest(@NotNull CultivationStatus status) {
}
