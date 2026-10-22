package com.annraksh.backend.controller;

import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.AdminFarmerPageResponse;
import com.annraksh.backend.dto.response.AdminCultivationPlanPageResponse;
import com.annraksh.backend.dto.response.AdminAgriculturalStatisticsResponse;
import com.annraksh.backend.dto.response.HarvestDispositionAuditResponse;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.dto.response.ApiErrorResponse;
import com.annraksh.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Administrator farmer management and platform statistics")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/farmers")
    @Operation(summary = "List farmers", description = "Returns a page of farmer profiles without password hashes. Search matches name, email, phone, and location. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Farmers returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AdminFarmerPageResponse getFarmers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search) {
        return adminService.getFarmers(page, size, search);
    }

    @GetMapping("/cultivation-plans")
    @Operation(summary = "Monitor cultivation plans", description = "Returns paginated plans across farmers, optionally filtered by status or farmer/crop search. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cultivation plans returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page, size, or status filter", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AdminCultivationPlanPageResponse getCultivationPlans(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) CultivationStatus status,
            @RequestParam(required = false) String search) {
        return adminService.getCultivationPlans(page, size, status, search);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get administrative dashboard statistics", description = "Returns persisted platform totals and cultivation-plan status counts. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard statistics returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AdminDashboardResponse getDashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/agricultural-statistics")
    @Operation(summary = "Get agricultural statistics", description = "Returns the latest crop demand/supply snapshot for each location, marked as potential surplus, supply gap, or balanced, plus recorded harvest totals grouped by crop and quantity unit. These are market signals and recorded yields, not measured food-waste data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agricultural statistics returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AdminAgriculturalStatisticsResponse getAgriculturalStatistics() {
        return adminService.getAgriculturalStatistics();
    }

    @GetMapping("/cultivation-plans/{id}/harvest-disposition/history")
    @Operation(summary = "Review harvest disposition history", description = "Returns the audit history of sold, stored, and discarded totals for a cultivation plan. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disposition history returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cultivation plan not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<HarvestDispositionAuditResponse> getHarvestDispositionHistory(@PathVariable Long id) {
        return adminService.getHarvestDispositionHistory(id);
    }
}
