package com.annraksh.backend.security;

import com.annraksh.backend.entity.Farmer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    public IssuedToken issue(Farmer farmer) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConstants.ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(farmer.getEmail())
                .claim(JwtConstants.ROLES_CLAIM, List.of(farmer.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expirationMinutes * 60);
    }
}
