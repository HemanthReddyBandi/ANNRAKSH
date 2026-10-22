package com.annraksh.backend.service;

import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.dto.response.AdminFarmerPageResponse;
import com.annraksh.backend.dto.response.AdminCultivationPlanPageResponse;
import com.annraksh.backend.dto.response.AdminAgriculturalStatisticsResponse;
import com.annraksh.backend.dto.response.HarvestDispositionAuditResponse;
import com.annraksh.backend.entity.CultivationStatus;

import java.util.List;

public interface AdminService {

    AdminFarmerPageResponse getFarmers(int page, int size, String search);

    AdminCultivationPlanPageResponse getCultivationPlans(int page, int size, CultivationStatus status, String search);

    AdminAgriculturalStatisticsResponse getAgriculturalStatistics();

    List<HarvestDispositionAuditResponse> getHarvestDispositionHistory(Long cultivationPlanId);

    AdminDashboardResponse getDashboard();
}
