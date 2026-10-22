package com.annraksh.backend.service.impl;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class CultivationPlanServiceImplTest {

    @Mock private CultivationPlanRepository planRepository;
    @Mock private FarmerRepository farmerRepository;
    @Mock private CropRepository cropRepository;
    @Mock private HarvestDispositionAuditRepository dispositionAuditRepository;
    @InjectMocks private CultivationPlanServiceImpl planService;

    @Test
    void harvestRecordsDispositionTotalsAndAllowsUnallocatedQuantity() {
        CultivationPlan plan = growingPlan();
        when(planRepository.findById(7L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(CultivationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CultivationPlan result = planService.recordHarvest(7L, new BigDecimal("100"),
                LocalDate.parse("2026-10-18"), new BigDecimal("60"), new BigDecimal("20"),
                new BigDecimal("5"), 3L);

        assertThat(result.getActualHarvestQuantity()).isEqualByComparingTo("100");
        assertThat(result.getSoldQuantity()).isEqualByComparingTo("60");
        assertThat(result.getStoredQuantity()).isEqualByComparingTo("20");
        assertThat(result.getDiscardedQuantity()).isEqualByComparingTo("5");
        assertThat(result.getStatus()).isEqualTo(CultivationStatus.HARVESTED);
        ArgumentCaptor<HarvestDispositionAudit> auditCaptor = ArgumentCaptor.forClass(HarvestDispositionAudit.class);
        verify(dispositionAuditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo(HarvestDispositionAction.HARVEST_RECORDED);
    }

    @Test
    void harvestRejectsDispositionQuantitiesGreaterThanTheActualHarvest() {
        CultivationPlan plan = growingPlan();
        when(planRepository.findById(7L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> planService.recordHarvest(7L, new BigDecimal("100"),
                LocalDate.parse("2026-10-18"), new BigDecimal("60"), new BigDecimal("30"),
                new BigDecimal("20"), 3L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Sold, stored, and discarded quantities cannot exceed the harvested quantity");
        verify(planRepository, never()).save(any(CultivationPlan.class));
    }

    @Test
    void dispositionUpdateChangesCurrentTotalsAndPersistsAuditSnapshot() {
        CultivationPlan plan = growingPlan();
        plan.setStatus(CultivationStatus.HARVESTED);
        plan.setActualHarvestQuantity(new BigDecimal("100"));
        plan.setSoldQuantity(new BigDecimal("60"));
        plan.setStoredQuantity(new BigDecimal("20"));
        plan.setDiscardedQuantity(new BigDecimal("5"));
        when(planRepository.findById(7L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(CultivationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CultivationPlan result = planService.updateHarvestDisposition(7L, new BigDecimal("75"),
                new BigDecimal("5"), new BigDecimal("5"), 3L);

        assertThat(result.getSoldQuantity()).isEqualByComparingTo("75");
        assertThat(result.getStoredQuantity()).isEqualByComparingTo("5");
        ArgumentCaptor<HarvestDispositionAudit> auditCaptor = ArgumentCaptor.forClass(HarvestDispositionAudit.class);
        verify(dispositionAuditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo(HarvestDispositionAction.DISPOSITION_UPDATED);
        assertThat(auditCaptor.getValue().getPreviousStoredQuantity()).isEqualByComparingTo("20");
        assertThat(auditCaptor.getValue().getStoredQuantity()).isEqualByComparingTo("5");
    }

    private CultivationPlan growingPlan() {
        Farmer farmer = new Farmer();
        farmer.setId(3L);
        CultivationPlan plan = new CultivationPlan();
        plan.setId(7L);
        plan.setFarmer(farmer);
        plan.setStatus(CultivationStatus.GROWING);
        plan.setPlantingDate(LocalDate.parse("2026-06-01"));
        return plan;
    }
}
