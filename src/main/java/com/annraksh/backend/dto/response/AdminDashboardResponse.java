package com.annraksh.backend.dto.response;

public record AdminDashboardResponse(
        long totalFarmers,
        long totalCrops,
        long totalDemandRecords,
        long totalCultivationPlans,
        long plannedPlans,
        long plantedPlans,
        long growingPlans,
        long harvestedPlans,
        long cancelledPlans,
        long totalRecommendations
) {
}
