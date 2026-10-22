package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.FarmerProfileUpdateRequest;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmer/profile")
@Tag(name = "Farmer profile", description = "View and update the authenticated farmer's profile")
@SecurityRequirement(name = "bearerAuth")
public class FarmerController {

    private final FarmerService farmerService;
    private final ApiDtoMapper mapper;

    public FarmerController(FarmerService farmerService, ApiDtoMapper mapper) {
        this.farmerService = farmerService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get my profile")
    public FarmerResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(farmerService.getByEmail(jwt.getSubject()));
    }

    @PutMapping
    @Operation(summary = "Update my profile", description = "Updates the farmer's name, phone, and market location. Email and role cannot be changed here.")
    public FarmerResponse updateProfile(@Valid @RequestBody FarmerProfileUpdateRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(farmerService.updateProfile(jwt.getSubject(), request.name(),
                request.phone(), request.location()));
    }
}
