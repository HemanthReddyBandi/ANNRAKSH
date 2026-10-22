package com.annraksh.backend.service;

import com.annraksh.backend.entity.Farmer;

import java.util.List;

public interface FarmerService {

    Farmer getById(Long id);

    Farmer getByEmail(String email);

    Farmer updateProfile(String email, String name, String phone, String location);

    List<Farmer> getAll();
}
