package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CultivationPlanRequest;
import com.annraksh.backend.dto.request.CultivationStatusRequest;
import com.annraksh.backend.dto.request.HarvestRecordRequest;
import com.annraksh.backend.dto.request.HarvestDispositionUpdateRequest;
import com.annraksh.backend.dto.response.ApiErrorResponse;
import com.annraksh.backend.dto.response.CultivationPlanResponse;
import com.annraksh.backend.dto.response.HarvestDispositionAuditResponse;
import com.annraksh.backend.service.FarmerService;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cultivation-plans")
@Tag(name = "Cultivation plans", description = "Plans owned by the authenticated farmer")
public class CultivationPlanController {

    private final CultivationPlanService planService;
    private final FarmerService farmerService;
    private final ApiDtoMapper mapper;

    public CultivationPlanController(CultivationPlanService planService, FarmerService farmerService,
                                     ApiDtoMapper mapper) {
        this.planService = planService;
        this.farmerService = farmerService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List my cultivation plans", description = "Returns cultivation plans belonging to the signed-in farmer.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<CultivationPlanResponse> getPlans(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return planService.getForFarmer(farmerId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    @Operation(summary = "Create a cultivation plan", description = "Creates a plan for the signed-in farmer. The farmer identity is taken from the JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cultivation plan created"),
            @ApiResponse(responseCode = "400", description = "Invalid plan data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Crop not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CultivationPlanResponse> create(@Valid @RequestBody CultivationPlanRequest request,
                                                          @AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        var plan = planService.create(mapper.toEntity(request), farmerId, request.cropId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(plan));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update a plan status", description = "Advances or cancels a cultivation plan owned by the signed-in farmer.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status request", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Requested status transition is not allowed", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public CultivationPlanResponse updateStatus(@PathVariable Long id,
                                                @AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody CultivationStatusRequest request) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(planService.updateStatus(id, request.status(), farmerId));
    }

    @PatchMapping("/{id}/harvest")
    @Operation(summary = "Record a harvest", description = "Records actual harvested quantity and date plus optional sold, stored, and discarded quantities for a plan owned by the signed-in farmer. Disposition quantities cannot exceed the harvest; any remainder stays unallocated.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Harvest recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid harvest data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Plan cannot be harvested in its current state", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public CultivationPlanResponse recordHarvest(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody HarvestRecordRequest request) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(planService.recordHarvest(id, request.actualHarvestQuantity(),
                request.actualHarvestDate(), request.soldQuantity(), request.storedQuantity(),
                request.discardedQuantity(), farmerId));
    }

    @PutMapping("/{id}/harvest-disposition")
    @Operation(summary = "Update harvest disposition", description = "Revises the current sold, stored, and discarded totals for a harvested plan. The change is recorded in the plan audit history.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Harvest disposition updated"),
            @ApiResponse(responseCode = "400", description = "Invalid disposition quantities", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Disposition exceeds the harvest or the plan is not harvested", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public CultivationPlanResponse updateHarvestDisposition(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody HarvestDispositionUpdateRequest request) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(planService.updateHarvestDisposition(id, request.soldQuantity(),
                request.storedQuantity(), request.discardedQuantity(), farmerId));
    }

    @GetMapping("/{id}/harvest-disposition/history")
    @Operation(summary = "View harvest disposition history", description = "Returns the signed-in farmer's recorded harvest disposition changes for this plan, newest first.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disposition history returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FARMER role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<HarvestDispositionAuditResponse> getHarvestDispositionHistory(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return planService.getHarvestDispositionHistory(id, farmerId).stream()
                .map(mapper::toResponse).toList();
    }
}
