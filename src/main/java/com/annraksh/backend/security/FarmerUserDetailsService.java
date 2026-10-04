package com.annraksh.backend.security;

import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.repository.FarmerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FarmerUserDetailsService implements UserDetailsService {

    private final FarmerRepository farmerRepository;

    public FarmerUserDetailsService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Farmer farmer = farmerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        return User.withUsername(farmer.getEmail())
                .password(farmer.getPassword())
                .roles(farmer.getRole().name())
                .build();
    }
}
