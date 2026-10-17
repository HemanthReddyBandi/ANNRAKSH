package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.ApiDtoMapper;
import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.dto.response.AdminFarmerPageResponse;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.RecommendationRepository;
import com.annraksh.backend.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final CropDemandRepository cropDemandRepository;
    private final CultivationPlanRepository cultivationPlanRepository;
    private final RecommendationRepository recommendationRepository;
    private final ApiDtoMapper mapper;

    public AdminServiceImpl(FarmerRepository farmerRepository, CropRepository cropRepository,
                            CropDemandRepository cropDemandRepository,
                            CultivationPlanRepository cultivationPlanRepository,
                            RecommendationRepository recommendationRepository, ApiDtoMapper mapper) {
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.cropDemandRepository = cropDemandRepository;
        this.cultivationPlanRepository = cultivationPlanRepository;
        this.recommendationRepository = recommendationRepository;
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
