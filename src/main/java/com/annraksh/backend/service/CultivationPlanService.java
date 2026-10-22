package com.annraksh.backend.service;

import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.HarvestDispositionAudit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CultivationPlanService {

    List<CultivationPlan> getForFarmer(Long farmerId);

    List<CultivationPlan> getAll();

    CultivationPlan create(CultivationPlan plan, Long farmerId, Long cropId);

    CultivationPlan updateStatus(Long id, CultivationStatus status, Long farmerId);

    CultivationPlan recordHarvest(Long id, BigDecimal actualQuantity, LocalDate actualDate,
                                  BigDecimal soldQuantity, BigDecimal storedQuantity,
                                  BigDecimal discardedQuantity, Long farmerId);

    CultivationPlan updateHarvestDisposition(Long id, BigDecimal soldQuantity,
                                              BigDecimal storedQuantity, BigDecimal discardedQuantity,
                                              Long farmerId);

    List<HarvestDispositionAudit> getHarvestDispositionHistory(Long id, Long farmerId);
}
