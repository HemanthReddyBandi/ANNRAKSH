package com.annraksh.backend.service.impl;

import com.annraksh.backend.entity.Crop;
import com.annraksh.backend.entity.CropDemand;
import com.annraksh.backend.repository.CropDemandRepository;
import com.annraksh.backend.repository.CropRepository;
import com.annraksh.backend.service.CropDemandService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CropDemandServiceImpl implements CropDemandService {

    private final CropDemandRepository cropDemandRepository;
    private final CropRepository cropRepository;

    public CropDemandServiceImpl(CropDemandRepository cropDemandRepository, CropRepository cropRepository) {
        this.cropDemandRepository = cropDemandRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public List<CropDemand> getAll() {
        return cropDemandRepository.findAll();
    }

    @Override
    public List<CropDemand> getByLocation(String location) {
        return cropDemandRepository.findByLocationIgnoreCaseOrderByRecordedDateDesc(location);
    }

    @Override
    public CropDemand getLatest(Long cropId, String location) {
        return cropDemandRepository
                .findFirstByCropIdAndLocationIgnoreCaseOrderByRecordedDateDesc(cropId, location)
                .orElseThrow(() -> new EntityNotFoundException("No demand record found for crop and location"));
    }

    @Override
    @Transactional
    public CropDemand record(CropDemand demand, Long cropId) {
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found: " + cropId));
        demand.setCrop(crop);
        return cropDemandRepository.save(demand);
    }
}
