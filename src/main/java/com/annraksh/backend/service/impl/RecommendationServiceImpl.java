package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Recommendation;
import com.annraksh.backend.repository.RecommendationRepository;
import com.annraksh.backend.service.RecommendationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public List<Recommendation> getForFarmer(Long farmerId) {
        return recommendationRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }

    @Override
    public Recommendation getForFarmerById(Long recommendationId, Long farmerId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found: " + recommendationId));
        if (!recommendation.getFarmer().getId().equals(farmerId)) {
            throw new EntityNotFoundException("Recommendation not found: " + recommendationId);
        }
        return recommendation;
    }
}
