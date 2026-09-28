package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.service.CropService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CropServiceImpl implements CropService {

    private final CropRepository cropRepository;

    public CropServiceImpl(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public List<Crop> getAll() {
        return cropRepository.findAll();
    }

    @Override
    public Crop getById(Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found: " + id));
    }

    @Override
    @Transactional
    public Crop create(Crop crop) {
        if (cropRepository.existsByNameIgnoreCase(crop.getName())) {
            throw new IllegalArgumentException("A crop with this name already exists");
        }
        return cropRepository.save(crop);
    }

    @Override
    @Transactional
    public Crop update(Long id, Crop changes) {
        Crop existing = getById(id);
        cropRepository.findByNameIgnoreCase(changes.getName())
                .filter(match -> !match.getId().equals(id))
                .ifPresent(match -> {
                    throw new IllegalArgumentException("A crop with this name already exists");
                });

        existing.setName(changes.getName());
        existing.setCategory(changes.getCategory());
        existing.setSeason(changes.getSeason());
        existing.setAverageGrowthDays(changes.getAverageGrowthDays());
        return cropRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        cropRepository.delete(getById(id));
    }
}
