package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CropDemandRequest;
import com.annraksh.backend.dto.response.CropDemandResponse;
import com.annraksh.backend.service.CropDemandService;
import jakarta.validation.Valid;
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
public class CropDemandController {

    private final CropDemandService demandService;
    private final ApiDtoMapper mapper;

    public CropDemandController(CropDemandService demandService, ApiDtoMapper mapper) {
        this.demandService = demandService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<CropDemandResponse> getDemand(@RequestParam(required = false) String location) {
        var records = StringUtils.hasText(location)
                ? demandService.getByLocation(location.trim())
                : demandService.getAll();
        return records.stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<CropDemandResponse> record(@Valid @RequestBody CropDemandRequest request) {
        var demand = demandService.record(mapper.toEntity(request), request.cropId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(demand));
    }
}
