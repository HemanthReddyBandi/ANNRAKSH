package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.request.FarmerLoginRequest;
import com.annraksh.backend.dto.request.FarmerRegistrationRequest;
import com.annraksh.backend.dto.response.AuthResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.FarmerRole;
import com.annraksh.backend.exception.DuplicateResourceException;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.security.IssuedToken;
import com.annraksh.backend.security.JwtTokenService;
import com.annraksh.backend.service.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthenticationServiceImpl(FarmerRepository farmerRepository,
                                     PasswordEncoder passwordEncoder,
                                     AuthenticationManager authenticationManager,
                                     JwtTokenService jwtTokenService) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    @Transactional
    public AuthResponse register(FarmerRegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (farmerRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Farmer farmer = new Farmer();
        farmer.setName(request.name().trim());
        farmer.setEmail(email);
        farmer.setPhone(request.phone() == null ? null : request.phone().trim());
        farmer.setPassword(passwordEncoder.encode(request.password()));
        farmer.setRole(FarmerRole.FARMER);
        farmer.setLocation(request.location() == null ? null : request.location().trim());
        farmer = farmerRepository.save(farmer);
        return createAuthResponse(farmer);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(FarmerLoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        Farmer farmer = farmerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found"));
        return createAuthResponse(farmer);
    }

    private AuthResponse createAuthResponse(Farmer farmer) {
        IssuedToken token = jwtTokenService.issue(farmer);
        FarmerResponse response = new FarmerResponse(farmer.getId(), farmer.getName(), farmer.getEmail(),
                farmer.getPhone(), farmer.getRole(), farmer.getLocation(), farmer.getCreatedAt());
        return new AuthResponse(token.value(), "Bearer", token.expiresInSeconds(), response);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
