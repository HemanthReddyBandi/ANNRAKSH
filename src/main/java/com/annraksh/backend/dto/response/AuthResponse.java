package com.annraksh.backend.dto.response;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds,
                           FarmerResponse farmer) {
}
