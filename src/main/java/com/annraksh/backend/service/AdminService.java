package com.annraksh.backend.service;

import com.annraksh.backend.dto.response.AdminDashboardResponse;
import com.annraksh.backend.dto.response.FarmerResponse;
import com.annraksh.backend.dto.response.AdminFarmerPageResponse;

public interface AdminService {

    AdminFarmerPageResponse getFarmers(int page, int size, String search);

    AdminDashboardResponse getDashboard();
}
