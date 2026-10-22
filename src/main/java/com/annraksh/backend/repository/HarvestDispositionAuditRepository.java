package com.annraksh.backend.repository;

import com.annraksh.backend.entity.HarvestDispositionAudit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HarvestDispositionAuditRepository extends JpaRepository<HarvestDispositionAudit, Long> {

    @EntityGraph(attributePaths = "farmer")
    List<HarvestDispositionAudit> findByCultivationPlanIdAndFarmerIdOrderByChangedAtDescIdDesc(
            Long cultivationPlanId, Long farmerId);

    List<HarvestDispositionAudit> findByCultivationPlanIdOrderByChangedAtDescIdDesc(Long cultivationPlanId);
}
