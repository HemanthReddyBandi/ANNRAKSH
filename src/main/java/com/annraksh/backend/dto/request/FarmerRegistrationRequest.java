package com.annraksh.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FarmerRegistrationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 30) String phone,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 160) String location
) {
}
