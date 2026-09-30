package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CropDemand;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropDemandRepository extends JpaRepository<CropDemand, Long> {

    @Override
    @EntityGraph(attributePaths = "crop")
    List<CropDemand> findAll();

    @Override
    @EntityGraph(attributePaths = "crop")
    Optional<CropDemand> findById(Long id);

    @EntityGraph(attributePaths = "crop")
    List<CropDemand> findByLocationIgnoreCaseOrderByRecordedDateDesc(String location);

    @EntityGraph(attributePaths = "crop")
    List<CropDemand> findByCropIdOrderByRecordedDateDesc(Long cropId);

    @EntityGraph(attributePaths = "crop")
    Optional<CropDemand> findFirstByCropIdAndLocationIgnoreCaseOrderByRecordedDateDesc(
            Long cropId, String location);
}
