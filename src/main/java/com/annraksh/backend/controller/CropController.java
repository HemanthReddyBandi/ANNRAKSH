package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CropRequest;
import com.annraksh.backend.dto.response.CropResponse;
import com.annraksh.backend.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;
    private final ApiDtoMapper mapper;

    public CropController(CropService cropService, ApiDtoMapper mapper) {
        this.cropService = cropService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<CropResponse> getAll() {
        return cropService.getAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CropResponse getById(@PathVariable Long id) {
        return mapper.toResponse(cropService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CropRequest request) {
        CropResponse response = mapper.toResponse(cropService.create(mapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public CropResponse update(@PathVariable Long id, @Valid @RequestBody CropRequest request) {
        return mapper.toResponse(cropService.update(id, mapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cropService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
