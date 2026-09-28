package com.annraksh.backend.repository;

import com.annraksh.backend.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
