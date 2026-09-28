package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.service.FarmerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerServiceImpl(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Farmer getById(Long id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found: " + id));
    }

    @Override
    public Farmer getByEmail(String email) {
        return farmerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found for email"));
    }

    @Override
    public List<Farmer> getAll() {
        return farmerRepository.findAll();
    }
}
