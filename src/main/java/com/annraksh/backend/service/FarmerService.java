package com.annraksh.backend.service;

import com.annraksh.backend.entity.Farmer;

import java.util.List;

public interface FarmerService {

    Farmer getById(Long id);

    Farmer getByEmail(String email);

    List<Farmer> getAll();
}
