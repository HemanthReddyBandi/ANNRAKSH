package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.FarmerRole;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private FarmerRepository farmerRepository;
    @Mock private CropRepository cropRepository;
    @Mock private CropDemandRepository cropDemandRepository;
    @Mock private CultivationPlanRepository cultivationPlanRepository;
    @Mock private RecommendationRepository recommendationRepository;
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
}
