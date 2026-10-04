package com.annraksh.backend.service;

import com.annraksh.backend.dto.request.FarmerLoginRequest;
import com.annraksh.backend.dto.request.FarmerRegistrationRequest;
import com.annraksh.backend.dto.response.AuthResponse;

public interface AuthenticationService {

    AuthResponse register(FarmerRegistrationRequest request);

    AuthResponse login(FarmerLoginRequest request);
}
