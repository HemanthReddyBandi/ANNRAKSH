package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CultivationPlan;
import com.annraksh.backend.entity.CultivationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CultivationPlanRepository extends JpaRepository<CultivationPlan, Long> {

    @Override
    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findAll();

    @Override
    @EntityGraph(attributePaths = {"farmer", "crop"})
    java.util.Optional<CultivationPlan> findById(Long id);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByFarmerIdOrderByPlantingDateDesc(Long farmerId);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByStatusOrderByExpectedHarvestDateAsc(CultivationStatus status);

    long countByStatus(CultivationStatus status);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    @Query("""
            select p from CultivationPlan p
            where (:status is null or p.status = :status)
              and (:search is null
                   or lower(p.farmer.name) like lower(concat('%', :search, '%'))
                   or lower(p.farmer.email) like lower(concat('%', :search, '%'))
                   or lower(p.crop.name) like lower(concat('%', :search, '%')))
            """)
    Page<CultivationPlan> searchAllForAdministration(
            @Param("status") CultivationStatus status,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            select p.crop.id as cropId,
                   p.crop.name as cropName,
                   p.quantityUnit as quantityUnit,
                   count(p.id) as harvestCount,
                   sum(p.plannedQuantity) as totalPlannedQuantity,
                   sum(p.actualHarvestQuantity) as totalHarvestedQuantity,
                   sum(p.soldQuantity) as totalSoldQuantity,
                   sum(p.storedQuantity) as totalStoredQuantity,
                   sum(p.discardedQuantity) as totalDiscardedQuantity,
                   sum(p.areaHectares) as totalAreaHectares
            from CultivationPlan p
            where p.actualHarvestQuantity is not null
            group by p.crop.id, p.crop.name, p.quantityUnit
            order by p.crop.name, p.quantityUnit
            """)
    List<HarvestProductionProjection> getHarvestProductionByCropAndUnit();

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<CultivationPlan> findByCropIdAndActualHarvestQuantityIsNotNull(Long cropId);

    @EntityGraph(attributePaths = "crop")
    List<CultivationPlan> findByFarmerIdAndActualHarvestQuantityIsNotNull(Long farmerId);
}
