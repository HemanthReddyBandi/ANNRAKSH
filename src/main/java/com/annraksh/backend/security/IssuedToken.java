package com.annraksh.backend.security;

public record IssuedToken(String value, long expiresInSeconds) {
}
