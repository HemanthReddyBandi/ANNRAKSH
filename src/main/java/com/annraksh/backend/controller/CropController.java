package com.annraksh.backend.controller;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.request.CropRequest;
import com.annraksh.backend.dto.response.ApiErrorResponse;
import com.annraksh.backend.dto.response.CropResponse;
import com.annraksh.backend.service.CropService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Crops", description = "Public crop catalogue and administrator crop maintenance")
public class CropController {

    private final CropService cropService;
    private final ApiDtoMapper mapper;

    public CropController(CropService cropService, ApiDtoMapper mapper) {
        this.cropService = cropService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List crops", description = "Returns the crop catalogue. This endpoint is public.")
    public List<CropResponse> getAll() {
        return cropService.getAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a crop", description = "Returns one crop by its identifier. This endpoint is public.")
    @ApiResponse(responseCode = "404", description = "Crop not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public CropResponse getById(@Parameter(description = "Crop identifier", example = "1") @PathVariable Long id) {
        return mapper.toResponse(cropService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a crop", description = "Adds a crop to the shared catalogue. Requires the ADMIN role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Crop created"),
            @ApiResponse(responseCode = "400", description = "Invalid crop data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Crop name conflicts with an existing crop", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CropRequest request) {
        CropResponse response = mapper.toResponse(cropService.create(mapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a crop", description = "Replaces a crop's catalogue fields. Requires the ADMIN role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Crop updated"),
            @ApiResponse(responseCode = "400", description = "Invalid crop data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Crop not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Crop name conflicts with an existing crop", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public CropResponse update(@Parameter(description = "Crop identifier", example = "1") @PathVariable Long id, @Valid @RequestBody CropRequest request) {
        return mapper.toResponse(cropService.update(id, mapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a crop", description = "Removes a crop if no related data prevents deletion. Requires the ADMIN role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Crop deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Crop not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Crop is referenced by existing records", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Crop identifier", example = "1") @PathVariable Long id) {
        cropService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
