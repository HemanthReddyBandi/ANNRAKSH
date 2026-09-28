package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CropDemand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropDemandRepository extends JpaRepository<CropDemand, Long> {

    List<CropDemand> findByLocationIgnoreCaseOrderByRecordedDateDesc(String location);

    List<CropDemand> findByCropIdOrderByRecordedDateDesc(Long cropId);

    Optional<CropDemand> findFirstByCropIdAndLocationIgnoreCaseOrderByRecordedDateDesc(
            Long cropId, String location);
}
