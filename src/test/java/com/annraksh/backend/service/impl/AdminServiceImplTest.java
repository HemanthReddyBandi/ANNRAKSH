package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.dto.response.AdminCultivationPlanPageResponse;
import com.annraksh.backend.dto.response.AdminAgriculturalStatisticsResponse;
import com.annraksh.backend.dto.response.MarketBalanceResponse;
import com.annraksh.backend.dto.response.HarvestProductionResponse;
import com.annraksh.backend.dto.response.HarvestDispositionAuditResponse;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.FarmerRole;
import com.annraksh.backend.entity.HarvestDispositionAction;
import com.annraksh.backend.entity.HarvestDispositionAudit;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.RecommendationRepository;
import com.annraksh.backend.repository.HarvestProductionProjection;
import com.annraksh.backend.repository.HarvestDispositionAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private FarmerRepository farmerRepository;
    @Mock private CropRepository cropRepository;
    @Mock private CropDemandRepository cropDemandRepository;
    @Mock private CultivationPlanRepository cultivationPlanRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private HarvestDispositionAuditRepository dispositionAuditRepository;
    @Mock private ApiDtoMapper mapper;

    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void dashboardCombinesPersistedCountsAndEveryPlanStatus() {
        when(farmerRepository.count()).thenReturn(12L);
        when(cropRepository.count()).thenReturn(8L);
        when(cropDemandRepository.count()).thenReturn(25L);
        when(cultivationPlanRepository.count()).thenReturn(19L);
        when(cultivationPlanRepository.countByStatus(CultivationStatus.PLANNED)).thenReturn(4L);
        when(cultivationPlanRepository.countByStatus(CultivationStatus.PLANTED)).thenReturn(3L);
        when(cultivationPlanRepository.countByStatus(CultivationStatus.GROWING)).thenReturn(5L);
        when(cultivationPlanRepository.countByStatus(CultivationStatus.HARVESTED)).thenReturn(6L);
        when(cultivationPlanRepository.countByStatus(CultivationStatus.CANCELLED)).thenReturn(1L);
        when(recommendationRepository.count()).thenReturn(30L);

        AdminDashboardResponse response = adminService.getDashboard();

        assertThat(response).isEqualTo(new AdminDashboardResponse(12, 8, 25, 19, 4, 3, 5, 6, 1, 30));
        verify(cultivationPlanRepository).countByStatus(CultivationStatus.HARVESTED);
    }

    @Test
    void farmerListingMapsOnlyTheSafeFarmerResponse() {
        Farmer farmer = new Farmer();
        farmer.setId(3L);
        farmer.setName("Asha");
        farmer.setEmail("asha@example.com");
        farmer.setPassword("bcrypt-hash");
        farmer.setRole(FarmerRole.FARMER);
        farmer.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        FarmerResponse expected = new FarmerResponse(3L, "Asha", "asha@example.com", null,
                FarmerRole.FARMER, null, farmer.getCreatedAt());
        when(farmerRepository.searchFarmers(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(farmer)));
        when(mapper.toResponse(farmer)).thenReturn(expected);

        var result = adminService.getFarmers(0, 20, "  ");

        assertThat(result.farmers()).containsExactly(expected);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(FarmerResponse.class.getRecordComponents()).extracting("name")
                .doesNotContain("password");
    }

    @Test
    void cultivationPlanListingAppliesStatusSearchAndPagination() {
        when(cultivationPlanRepository.searchAllForAdministration(
                eq(CultivationStatus.GROWING), eq("Asha"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 26));

        AdminCultivationPlanPageResponse result = adminService.getCultivationPlans(
                1, 10, CultivationStatus.GROWING, "  Asha ");

        assertThat(result.plans()).isEmpty();
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(26);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void agriculturalStatisticsClassifiesLatestBalancesAndGroupsHarvestOutputByUnit() {
        CropDemand surplus = new CropDemand();
        CropDemand gap = new CropDemand();
        CropDemand balanced = new CropDemand();
        when(cropDemandRepository.findLatestRecordsAcrossLocations()).thenReturn(List.of(surplus, gap, balanced));
        when(mapper.toMarketBalanceResponse(surplus)).thenReturn(balance("POTENTIAL_SURPLUS"));
        when(mapper.toMarketBalanceResponse(gap)).thenReturn(balance("SUPPLY_GAP"));
        when(mapper.toMarketBalanceResponse(balanced)).thenReturn(balance("BALANCED"));

        HarvestProductionProjection harvest = mock(HarvestProductionProjection.class);
        when(harvest.getCropId()).thenReturn(9L);
        when(harvest.getCropName()).thenReturn("Tomato");
        when(harvest.getQuantityUnit()).thenReturn("kg");
        when(harvest.getHarvestCount()).thenReturn(2L);
        when(harvest.getTotalPlannedQuantity()).thenReturn(new BigDecimal("250"));
        when(harvest.getTotalHarvestedQuantity()).thenReturn(new BigDecimal("300"));
        when(harvest.getTotalSoldQuantity()).thenReturn(new BigDecimal("200"));
        when(harvest.getTotalStoredQuantity()).thenReturn(new BigDecimal("50"));
        when(harvest.getTotalDiscardedQuantity()).thenReturn(new BigDecimal("10"));
        when(harvest.getTotalAreaHectares()).thenReturn(new BigDecimal("2"));
        when(cultivationPlanRepository.getHarvestProductionByCropAndUnit()).thenReturn(List.of(harvest));

        AdminAgriculturalStatisticsResponse result = adminService.getAgriculturalStatistics();

        assertThat(result.latestMarketSnapshotCount()).isEqualTo(3);
        assertThat(result.potentialSurplusSignalCount()).isEqualTo(1);
        assertThat(result.supplyGapSignalCount()).isEqualTo(1);
        assertThat(result.balancedSignalCount()).isEqualTo(1);
        HarvestProductionResponse production = result.harvestProductionByCropAndUnit().get(0);
        assertThat(production.harvestedQuantityPerHectare()).isEqualByComparingTo("150.000");
    }

    @Test
    void adminCanReadDispositionHistoryForAnExistingPlan() {
        HarvestDispositionAudit audit = mock(HarvestDispositionAudit.class);
        HarvestDispositionAuditResponse expected = new HarvestDispositionAuditResponse(4L,
                HarvestDispositionAction.DISPOSITION_UPDATED, BigDecimal.ZERO, new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("6"), new BigDecimal("4"),
                Instant.parse("2026-10-18T00:00:00Z"));
        when(cultivationPlanRepository.existsById(42L)).thenReturn(true);
        when(dispositionAuditRepository.findByCultivationPlanIdOrderByChangedAtDescIdDesc(42L))
                .thenReturn(List.of(audit));
        when(mapper.toResponse(audit)).thenReturn(expected);

        assertThat(adminService.getHarvestDispositionHistory(42L)).containsExactly(expected);
    }

    @Test
    void adminHistoryReturnsNotFoundForAnUnknownPlan() {
        when(cultivationPlanRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.getHarvestDispositionHistory(404L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    private MarketBalanceResponse balance(String signal) {
        return new MarketBalanceResponse(1L, "Tomato", "Pune", BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.ZERO, "kg", signal, LocalDate.parse("2026-10-18"));
    }
}
