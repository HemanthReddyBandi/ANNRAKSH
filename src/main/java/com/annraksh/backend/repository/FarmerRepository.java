package com.annraksh.backend.repository;

import com.annraksh.backend.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select f from Farmer f
            where :search is null
               or lower(f.name) like lower(concat('%', :search, '%'))
               or lower(f.email) like lower(concat('%', :search, '%'))
               or lower(coalesce(f.phone, '')) like lower(concat('%', :search, '%'))
               or lower(coalesce(f.location, '')) like lower(concat('%', :search, '%'))
            """)
    Page<Farmer> searchFarmers(@Param("search") String search, Pageable pageable);
}
