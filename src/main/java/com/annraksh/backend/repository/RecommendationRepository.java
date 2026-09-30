package com.annraksh.backend.repository;

import com.annraksh.backend.entity.Recommendation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Override
    @EntityGraph(attributePaths = {"farmer", "crop"})
    Optional<Recommendation> findById(Long id);

    @EntityGraph(attributePaths = {"farmer", "crop"})
    List<Recommendation> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
}
