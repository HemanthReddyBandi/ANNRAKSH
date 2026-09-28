package com.annraksh.backend.service;

import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;

import java.util.List;

public interface CultivationPlanService {

    List<CultivationPlan> getForFarmer(Long farmerId);

    List<CultivationPlan> getAll();

    CultivationPlan create(CultivationPlan plan, Long farmerId, Long cropId);

    CultivationPlan updateStatus(Long id, CultivationStatus status);
}
