package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CropDemandRequest;
import com.annraksh.backend.dto.response.ApiErrorResponse;
import com.annraksh.backend.dto.response.CropDemandResponse;
import com.annraksh.backend.service.CropDemandService;
import jakarta.validation.Valid;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demand")
@Tag(name = "Demand", description = "Market demand and supply snapshots")
public class CropDemandController {

    private final CropDemandService demandService;
    private final ApiDtoMapper mapper;

    public CropDemandController(CropDemandService demandService, ApiDtoMapper mapper) {
        this.demandService = demandService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List demand snapshots", description = "Returns recorded demand and supply data. This endpoint is public; filter by location when needed.")
    public List<CropDemandResponse> getDemand(@Parameter(description = "Optional market location filter", example = "Pune") @RequestParam(required = false) String location) {
        var records = StringUtils.hasText(location)
                ? demandService.getByLocation(location.trim())
                : demandService.getAll();
        return records.stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    @Operation(summary = "Record a demand snapshot", description = "Adds market demand and supply data for a crop. Requires the ADMIN role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Demand snapshot created"),
            @ApiResponse(responseCode = "400", description = "Invalid demand snapshot", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Crop not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CropDemandResponse> record(@Valid @RequestBody CropDemandRequest request) {
        var demand = demandService.record(mapper.toEntity(request), request.cropId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(demand));
    }
}
