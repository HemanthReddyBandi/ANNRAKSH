package com.annraksh.backend.service;

import com.annraksh.backend.entity.Crop;

import java.util.List;

public interface CropService {

    List<Crop> getAll();

    Crop getById(Long id);

    Crop create(Crop crop);

    Crop update(Long id, Crop changes);

    void delete(Long id);
}
