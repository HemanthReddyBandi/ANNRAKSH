package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CultivationPlanRequest;
import com.annraksh.backend.dto.request.CultivationStatusRequest;
import com.annraksh.backend.dto.request.HarvestRecordRequest;
import com.annraksh.backend.dto.response.CultivationPlanResponse;
import com.annraksh.backend.service.FarmerService;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cultivation-plans")
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
    public List<CultivationPlanResponse> getPlans(@AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return planService.getForFarmer(farmerId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<CultivationPlanResponse> create(@Valid @RequestBody CultivationPlanRequest request,
                                                          @AuthenticationPrincipal Jwt jwt) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        var plan = planService.create(mapper.toEntity(request), farmerId, request.cropId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(plan));
    }

    @PatchMapping("/{id}/status")
    public CultivationPlanResponse updateStatus(@PathVariable Long id,
                                                @AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody CultivationStatusRequest request) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(planService.updateStatus(id, request.status(), farmerId));
    }

    @PatchMapping("/{id}/harvest")
    public CultivationPlanResponse recordHarvest(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody HarvestRecordRequest request) {
        Long farmerId = farmerService.getByEmail(jwt.getSubject()).getId();
        return mapper.toResponse(planService.recordHarvest(id, request.actualHarvestQuantity(),
                request.actualHarvestDate(), farmerId));
    }
}
