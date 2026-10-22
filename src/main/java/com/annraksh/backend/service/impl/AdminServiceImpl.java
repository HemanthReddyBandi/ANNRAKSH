package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.dto.response.AdminFarmerPageResponse;
import com.annraksh.backend.dto.response.AdminCultivationPlanPageResponse;
import com.annraksh.backend.dto.response.AdminCultivationPlanResponse;
import com.annraksh.backend.dto.response.AdminAgriculturalStatisticsResponse;
import com.annraksh.backend.dto.response.HarvestProductionResponse;
import com.annraksh.backend.dto.response.MarketBalanceResponse;
import com.annraksh.backend.dto.response.HarvestDispositionAuditResponse;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.RecommendationRepository;
import com.annraksh.backend.repository.HarvestDispositionAuditRepository;
import jakarta.persistence.EntityNotFoundException;
import com.annraksh.backend.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final CropDemandRepository cropDemandRepository;
    private final CultivationPlanRepository cultivationPlanRepository;
    private final RecommendationRepository recommendationRepository;
    private final HarvestDispositionAuditRepository dispositionAuditRepository;
    private final ApiDtoMapper mapper;

    public AdminServiceImpl(FarmerRepository farmerRepository, CropRepository cropRepository,
                            CropDemandRepository cropDemandRepository,
                            CultivationPlanRepository cultivationPlanRepository,
                            RecommendationRepository recommendationRepository,
                            HarvestDispositionAuditRepository dispositionAuditRepository,
                            ApiDtoMapper mapper) {
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.cropDemandRepository = cropDemandRepository;
        this.cultivationPlanRepository = cultivationPlanRepository;
        this.recommendationRepository = recommendationRepository;
        this.dispositionAuditRepository = dispositionAuditRepository;
        this.mapper = mapper;
    }

    @Override
    public AdminFarmerPageResponse getFarmers(int page, int size, String search) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        Page<FarmerResponse> farmerPage = farmerRepository.searchFarmers(normalizedSearch,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by(Sort.Direction.DESC, "id"))))
                .map(mapper::toResponse);
        return new AdminFarmerPageResponse(farmerPage.getContent(), farmerPage.getNumber(), farmerPage.getSize(),
                farmerPage.getTotalElements(), farmerPage.getTotalPages());
    }

    @Override
    public AdminCultivationPlanPageResponse getCultivationPlans(int page, int size,
                                                                 CultivationStatus status, String search) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        Page<AdminCultivationPlanResponse> planPage = cultivationPlanRepository
                .searchAllForAdministration(status, normalizedSearch,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "plantingDate")
                                .and(Sort.by(Sort.Direction.DESC, "id"))))
                .map(mapper::toAdminResponse);
        return new AdminCultivationPlanPageResponse(planPage.getContent(), planPage.getNumber(),
                planPage.getSize(), planPage.getTotalElements(), planPage.getTotalPages());
    }

    @Override
    public AdminAgriculturalStatisticsResponse getAgriculturalStatistics() {
        List<MarketBalanceResponse> balances = cropDemandRepository.findLatestRecordsAcrossLocations().stream()
                .map(mapper::toMarketBalanceResponse).toList();
        long potentialSurplusCount = balances.stream()
                .filter(balance -> "POTENTIAL_SURPLUS".equals(balance.signal())).count();
        long supplyGapCount = balances.stream().filter(balance -> "SUPPLY_GAP".equals(balance.signal())).count();
        long balancedCount = balances.size() - potentialSurplusCount - supplyGapCount;

        List<HarvestProductionResponse> production = cultivationPlanRepository
                .getHarvestProductionByCropAndUnit().stream()
                .map(group -> new HarvestProductionResponse(
                        group.getCropId(), group.getCropName(), group.getQuantityUnit(),
                        group.getHarvestCount(), group.getTotalPlannedQuantity(),
                        group.getTotalHarvestedQuantity(), group.getTotalSoldQuantity(),
                        group.getTotalStoredQuantity(), group.getTotalDiscardedQuantity(),
                        group.getTotalHarvestedQuantity().subtract(group.getTotalSoldQuantity())
                                .subtract(group.getTotalStoredQuantity()).subtract(group.getTotalDiscardedQuantity()),
                        group.getTotalAreaHectares(),
                        group.getTotalAreaHectares().signum() == 0 ? BigDecimal.ZERO
                                : group.getTotalHarvestedQuantity().divide(group.getTotalAreaHectares(), 3, RoundingMode.HALF_UP)))
                .toList();

        return new AdminAgriculturalStatisticsResponse(balances.size(), potentialSurplusCount,
                supplyGapCount, balancedCount, balances, production);
    }

    @Override
    public List<HarvestDispositionAuditResponse> getHarvestDispositionHistory(Long cultivationPlanId) {
        if (!cultivationPlanRepository.existsById(cultivationPlanId)) {
            throw new EntityNotFoundException("Cultivation plan not found: " + cultivationPlanId);
        }
        return dispositionAuditRepository.findByCultivationPlanIdOrderByChangedAtDescIdDesc(cultivationPlanId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
                farmerRepository.count(), cropRepository.count(), cropDemandRepository.count(),
                cultivationPlanRepository.count(),
                cultivationPlanRepository.countByStatus(CultivationStatus.PLANNED),
                cultivationPlanRepository.countByStatus(CultivationStatus.PLANTED),
                cultivationPlanRepository.countByStatus(CultivationStatus.GROWING),
                cultivationPlanRepository.countByStatus(CultivationStatus.HARVESTED),
                cultivationPlanRepository.countByStatus(CultivationStatus.CANCELLED),
                recommendationRepository.count());
    }
}
