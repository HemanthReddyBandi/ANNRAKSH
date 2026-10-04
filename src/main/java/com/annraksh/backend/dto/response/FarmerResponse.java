package com.annraksh.backend.dto.response;

import com.annraksh.backend.entity.FarmerRole;

import java.time.Instant;

public record FarmerResponse(Long id, String name, String email, String phone,
                             FarmerRole role, String location, Instant createdAt) {
}
