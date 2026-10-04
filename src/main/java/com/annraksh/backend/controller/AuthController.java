package com.annraksh.backend.controller;

import com.annraksh.backend.dto.request.FarmerLoginRequest;
import com.annraksh.backend.dto.request.FarmerRegistrationRequest;
import com.annraksh.backend.dto.response.AuthResponse;
import com.annraksh.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody FarmerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody FarmerLoginRequest request) {
        return authenticationService.login(request);
    }
}
