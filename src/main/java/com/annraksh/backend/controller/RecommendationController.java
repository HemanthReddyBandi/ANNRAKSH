package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.ApiErrorResponse;
import com.annraksh.backend.dto.response.RecommendationResponse;
import com.annraksh.backend.service.FarmerService;
import com.annraksh.backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Recommendations", description = "Rule-based, explainable crop planning suggestions")
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
    @Operation(summary = "List my recommendations", description = "Returns saved recommendations belonging to the signed-in farmer.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<RecommendationResponse> getForFarmer(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return recommendationService.getForFarmer(farmerId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate recommendations", description = "Scores crops using current local demand and supply, season suitability, and the farmer's recorded harvest history. Requires demand data for the profile location.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recommendations generated and saved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Farmer account not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Profile location is missing or no demand data exists for it", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<RecommendationResponse>> generate(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        List<RecommendationResponse> recommendations = recommendationService.generateForFarmer(farmerId)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a recommendation", description = "Returns one saved recommendation owned by the signed-in farmer.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recommendation not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public RecommendationResponse getById(@Parameter(description = "Recommendation identifier", example = "1") @PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(recommendationService.getForFarmerById(id, farmerId));
    }
}
