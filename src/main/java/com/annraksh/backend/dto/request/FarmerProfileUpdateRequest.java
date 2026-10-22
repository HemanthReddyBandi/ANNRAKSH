package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FarmerProfileUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 160) String location
) {
}
