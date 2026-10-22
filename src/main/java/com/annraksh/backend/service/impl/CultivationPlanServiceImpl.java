package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.HarvestDispositionAction;
import com.annraksh.backend.entity.HarvestDispositionAudit;
import com.annraksh.backend.exception.BusinessRuleException;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.repository.CultivationPlanRepository;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.repository.HarvestDispositionAuditRepository;
import com.annraksh.backend.service.CultivationPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CultivationPlanServiceImpl implements CultivationPlanService {

    private final CultivationPlanRepository planRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final HarvestDispositionAuditRepository dispositionAuditRepository;

    public CultivationPlanServiceImpl(CultivationPlanRepository planRepository,
                                      FarmerRepository farmerRepository,
                                      CropRepository cropRepository,
                                      HarvestDispositionAuditRepository dispositionAuditRepository) {
        this.planRepository = planRepository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.dispositionAuditRepository = dispositionAuditRepository;
    }

    @Override
    public List<CultivationPlan> getForFarmer(Long farmerId) {
        return planRepository.findByFarmerIdOrderByPlantingDateDesc(farmerId);
    }

    @Override
    public List<CultivationPlan> getAll() {
        return planRepository.findAll();
    }

    @Override
    @Transactional
    public CultivationPlan create(CultivationPlan plan, Long farmerId, Long cropId) {
        if (plan.getExpectedHarvestDate().isBefore(plan.getPlantingDate())) {
            throw new IllegalArgumentException("Expected harvest date must be on or after planting date");
        }
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found: " + farmerId));
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found: " + cropId));
        plan.setFarmer(farmer);
        plan.setCrop(crop);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public CultivationPlan updateStatus(Long id, CultivationStatus status, Long farmerId) {
        CultivationPlan plan = findPlanOwnedByFarmer(id, farmerId);
        if (status == CultivationStatus.HARVESTED) {
            throw new BusinessRuleException("Use the harvest endpoint to record an actual harvest");
        }
        if (plan.getStatus() == CultivationStatus.HARVESTED && status != CultivationStatus.HARVESTED) {
            throw new BusinessRuleException("A harvested plan cannot return to an active status");
        }
        plan.setStatus(status);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public CultivationPlan recordHarvest(Long id, BigDecimal actualQuantity, LocalDate actualDate,
                                         BigDecimal soldQuantity, BigDecimal storedQuantity,
                                         BigDecimal discardedQuantity, Long farmerId) {
        CultivationPlan plan = findPlanOwnedByFarmer(id, farmerId);
        if (actualDate.isBefore(plan.getPlantingDate())) {
            throw new BusinessRuleException("Actual harvest date cannot be before planting date");
        }
        if (plan.getStatus() == CultivationStatus.CANCELLED) {
            throw new BusinessRuleException("A cancelled plan cannot be marked as harvested");
        }
        if (plan.getStatus() == CultivationStatus.HARVESTED) {
            throw new BusinessRuleException("A harvest has already been recorded for this plan");
        }
        BigDecimal normalizedSold = zeroIfNull(soldQuantity);
        BigDecimal normalizedStored = zeroIfNull(storedQuantity);
        BigDecimal normalizedDiscarded = zeroIfNull(discardedQuantity);
        BigDecimal dispositionTotal = normalizedSold.add(normalizedStored).add(normalizedDiscarded);
        if (dispositionTotal.compareTo(actualQuantity) > 0) {
            throw new BusinessRuleException("Sold, stored, and discarded quantities cannot exceed the harvested quantity");
        }
        BigDecimal previousSold = zeroIfNull(plan.getSoldQuantity());
        BigDecimal previousStored = zeroIfNull(plan.getStoredQuantity());
        BigDecimal previousDiscarded = zeroIfNull(plan.getDiscardedQuantity());
        plan.setActualHarvestQuantity(actualQuantity);
        plan.setSoldQuantity(normalizedSold);
        plan.setStoredQuantity(normalizedStored);
        plan.setDiscardedQuantity(normalizedDiscarded);
        plan.setActualHarvestDate(actualDate);
        plan.setStatus(CultivationStatus.HARVESTED);
        CultivationPlan savedPlan = planRepository.save(plan);
        saveDispositionAudit(savedPlan, HarvestDispositionAction.HARVEST_RECORDED,
                previousSold, previousStored, previousDiscarded,
                normalizedSold, normalizedStored, normalizedDiscarded);
        return savedPlan;
    }

    @Override
    @Transactional
    public CultivationPlan updateHarvestDisposition(Long id, BigDecimal soldQuantity,
                                                     BigDecimal storedQuantity, BigDecimal discardedQuantity,
                                                     Long farmerId) {
        CultivationPlan plan = findPlanOwnedByFarmer(id, farmerId);
        if (plan.getStatus() != CultivationStatus.HARVESTED || plan.getActualHarvestQuantity() == null) {
            throw new BusinessRuleException("Harvest disposition can only be updated after a harvest is recorded");
        }
        BigDecimal newSold = zeroIfNull(soldQuantity);
        BigDecimal newStored = zeroIfNull(storedQuantity);
        BigDecimal newDiscarded = zeroIfNull(discardedQuantity);
        ensureDispositionDoesNotExceedHarvest(plan.getActualHarvestQuantity(), newSold, newStored, newDiscarded);

        BigDecimal previousSold = zeroIfNull(plan.getSoldQuantity());
        BigDecimal previousStored = zeroIfNull(plan.getStoredQuantity());
        BigDecimal previousDiscarded = zeroIfNull(plan.getDiscardedQuantity());
        if (previousSold.compareTo(newSold) == 0 && previousStored.compareTo(newStored) == 0
                && previousDiscarded.compareTo(newDiscarded) == 0) {
            return plan;
        }

        plan.setSoldQuantity(newSold);
        plan.setStoredQuantity(newStored);
        plan.setDiscardedQuantity(newDiscarded);
        CultivationPlan savedPlan = planRepository.save(plan);
        saveDispositionAudit(savedPlan, HarvestDispositionAction.DISPOSITION_UPDATED,
                previousSold, previousStored, previousDiscarded, newSold, newStored, newDiscarded);
        return savedPlan;
    }

    @Override
    public List<HarvestDispositionAudit> getHarvestDispositionHistory(Long id, Long farmerId) {
        findPlanOwnedByFarmer(id, farmerId);
        return dispositionAuditRepository.findByCultivationPlanIdAndFarmerIdOrderByChangedAtDescIdDesc(id, farmerId);
    }

    private void ensureDispositionDoesNotExceedHarvest(BigDecimal actualQuantity,
                                                       BigDecimal soldQuantity,
                                                       BigDecimal storedQuantity,
                                                       BigDecimal discardedQuantity) {
        BigDecimal total = soldQuantity.add(storedQuantity).add(discardedQuantity);
        if (total.compareTo(actualQuantity) > 0) {
            throw new BusinessRuleException("Sold, stored, and discarded quantities cannot exceed the harvested quantity");
        }
    }

    private void saveDispositionAudit(CultivationPlan plan, HarvestDispositionAction action,
                                     BigDecimal previousSold, BigDecimal previousStored,
                                     BigDecimal previousDiscarded, BigDecimal sold,
                                     BigDecimal stored, BigDecimal discarded) {
        HarvestDispositionAudit audit = new HarvestDispositionAudit();
        audit.setCultivationPlan(plan);
        audit.setFarmer(plan.getFarmer());
        audit.setAction(action);
        audit.setPreviousSoldQuantity(previousSold);
        audit.setPreviousStoredQuantity(previousStored);
        audit.setPreviousDiscardedQuantity(previousDiscarded);
        audit.setSoldQuantity(sold);
        audit.setStoredQuantity(stored);
        audit.setDiscardedQuantity(discarded);
        dispositionAuditRepository.save(audit);
    }

    private BigDecimal zeroIfNull(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private CultivationPlan findPlanOwnedByFarmer(Long id, Long farmerId) {
        CultivationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cultivation plan not found: " + id));
        if (!plan.getFarmer().getId().equals(farmerId)) {
            throw new EntityNotFoundException("Cultivation plan not found: " + id);
        }
        return plan;
    }
}
