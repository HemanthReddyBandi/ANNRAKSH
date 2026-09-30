package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CultivationPlanRequest;
import com.annraksh.backend.dto.request.CultivationStatusRequest;
import com.annraksh.backend.dto.response.CultivationPlanResponse;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cultivation-plans")
public class CultivationPlanController {

    private final CultivationPlanService planService;
    private final ApiDtoMapper mapper;

    public CultivationPlanController(CultivationPlanService planService, ApiDtoMapper mapper) {
        this.planService = planService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<CultivationPlanResponse> getPlans(@RequestParam(required = false) Long farmerId) {
        var plans = farmerId == null ? planService.getAll() : planService.getForFarmer(farmerId);
        return plans.stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<CultivationPlanResponse> create(@Valid @RequestBody CultivationPlanRequest request) {
        var plan = planService.create(mapper.toEntity(request), request.farmerId(), request.cropId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(plan));
    }

    @PatchMapping("/{id}/status")
    public CultivationPlanResponse updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody CultivationStatusRequest request) {
        return mapper.toResponse(planService.updateStatus(id, request.status()));
    }
}
