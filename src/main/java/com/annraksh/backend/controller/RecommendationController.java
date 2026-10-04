package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.RecommendationResponse;
import com.annraksh.backend.service.FarmerService;
import com.annraksh.backend.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final FarmerService farmerService;
    private final ApiDtoMapper mapper;

    public RecommendationController(RecommendationService recommendationService,
                                    FarmerService farmerService, ApiDtoMapper mapper) {
        this.recommendationService = recommendationService;
        this.farmerService = farmerService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<RecommendationResponse> getForFarmer(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return recommendationService.getForFarmer(farmerId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/generate")
    public ResponseEntity<List<RecommendationResponse>> generate(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        List<RecommendationResponse> recommendations = recommendationService.generateForFarmer(farmerId)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendations);
    }

    @GetMapping("/{id}")
    public RecommendationResponse getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(recommendationService.getForFarmerById(id, farmerId));
    }
}
