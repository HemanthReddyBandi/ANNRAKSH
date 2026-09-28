package com.annraksh.backend.service;

import com.annraksh.backend.entity.Recommendation;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> getForFarmer(Long farmerId);

    Recommendation getForFarmerById(Long recommendationId, Long farmerId);
}
