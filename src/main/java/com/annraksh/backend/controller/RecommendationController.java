package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.RecommendationResponse;
import com.annraksh.backend.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ApiDtoMapper mapper;

    public RecommendationController(RecommendationService recommendationService, ApiDtoMapper mapper) {
        this.recommendationService = recommendationService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<RecommendationResponse> getForFarmer(@RequestParam Long farmerId) {
        return recommendationService.getForFarmer(farmerId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public RecommendationResponse getById(@PathVariable Long id, @RequestParam Long farmerId) {
        return mapper.toResponse(recommendationService.getForFarmerById(id, farmerId));
    }
}
