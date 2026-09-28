package com.annraksh.backend.service;

import com.annraksh.backend.entity.CropDemand;

import java.util.List;

public interface CropDemandService {

    List<CropDemand> getAll();

    List<CropDemand> getByLocation(String location);

    CropDemand getLatest(Long cropId, String location);

    CropDemand record(CropDemand demand, Long cropId);
}
