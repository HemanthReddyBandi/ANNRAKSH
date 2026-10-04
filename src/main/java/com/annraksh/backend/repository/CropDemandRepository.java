package com.annraksh.backend.repository;

import com.annraksh.backend.entity.CropDemand;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = "crop")
    @Query("""
            select d from CropDemand d
            where lower(d.location) = lower(:location)
              and d.recordedDate = (
                  select max(recent.recordedDate) from CropDemand recent
                  where recent.crop = d.crop and lower(recent.location) = lower(:location)
              )
              and d.id = (
                  select max(tieBreak.id) from CropDemand tieBreak
                  where tieBreak.crop = d.crop
                    and lower(tieBreak.location) = lower(:location)
                    and tieBreak.recordedDate = d.recordedDate
              )
            order by d.crop.name
            """)
    List<CropDemand> findLatestRecordsByLocation(@Param("location") String location);
}
