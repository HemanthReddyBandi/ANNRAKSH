package com.annraksh.backend.service.impl;

import com.annraksh.backend.dto.request.FarmerLoginRequest;
import com.annraksh.backend.dto.request.FarmerRegistrationRequest;
import com.annraksh.backend.dto.response.AuthResponse;
import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.FarmerRole;
import com.annraksh.backend.exception.DuplicateResourceException;
import com.annraksh.backend.repository.FarmerRepository;
import com.annraksh.backend.security.IssuedToken;
import com.annraksh.backend.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void registrationNormalizesEmailHashesPasswordAndAlwaysAssignsFarmerRole() {
        FarmerRegistrationRequest request = new FarmerRegistrationRequest(
                "  Asha Rao ", " ASHA@Example.com ", " 12345 ", "password123", " Pune ");
        when(farmerRepository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed-value");
        when(farmerRepository.save(any(Farmer.class))).thenAnswer(invocation -> {
            Farmer saved = invocation.getArgument(0);
            saved.setId(42L);
            saved.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return saved;
        });
        when(jwtTokenService.issue(any(Farmer.class))).thenReturn(new IssuedToken("signed.jwt", 3600));

        AuthResponse response = authenticationService.register(request);

        ArgumentCaptor<Farmer> farmerCaptor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).save(farmerCaptor.capture());
        Farmer savedFarmer = farmerCaptor.getValue();
        assertThat(savedFarmer.getName()).isEqualTo("Asha Rao");
        assertThat(savedFarmer.getEmail()).isEqualTo("asha@example.com");
        assertThat(savedFarmer.getPassword()).isEqualTo("$2a$hashed-value").isNotEqualTo("password123");
        assertThat(savedFarmer.getRole()).isEqualTo(FarmerRole.FARMER);
        assertThat(savedFarmer.getLocation()).isEqualTo("Pune");
        assertThat(response.accessToken()).isEqualTo("signed.jwt");
        assertThat(response.farmer().id()).isEqualTo(42L);
        assertThat(response.farmer().role()).isEqualTo(FarmerRole.FARMER);
    }

    @Test
    void registrationRejectsAnExistingEmailBeforeEncodingOrSaving() {
        when(farmerRepository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(new FarmerRegistrationRequest(
                "Asha", "Asha@Example.com", null, "password123", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("An account with this email already exists");

    }

    @Test
    void loginAuthenticatesNormalizedEmailAndReturnsFarmerToken() {
        Farmer farmer = new Farmer();
        farmer.setId(7L);
        farmer.setName("Asha");
        farmer.setEmail("asha@example.com");
        farmer.setPassword("$2a$hashed-value");
        farmer.setRole(FarmerRole.FARMER);
        farmer.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(farmerRepository.findByEmailIgnoreCase("asha@example.com")).thenReturn(Optional.of(farmer));
        when(jwtTokenService.issue(farmer)).thenReturn(new IssuedToken("signed.jwt", 3600));

        AuthResponse response = authenticationService.login(new FarmerLoginRequest(" Asha@Example.com ", "password123"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.accessToken()).isEqualTo("signed.jwt");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
        assertThat(response.farmer().email()).isEqualTo("asha@example.com");
    }
}
