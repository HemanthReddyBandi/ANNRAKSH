package com.annraksh.backend.config;

import com.annraksh.backend.entity.Farmer;
import com.annraksh.backend.entity.FarmerRole;
import com.annraksh.backend.repository.FarmerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String name;
    private final String password;

    public AdminBootstrapRunner(FarmerRepository farmerRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.bootstrap-admin.email:}") String email,
                                @Value("${app.bootstrap-admin.name:}") String name,
                                @Value("${app.bootstrap-admin.password:}") String password) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (farmerRepository.existsByRole(FarmerRole.ADMIN)) {
            log.info("An administrator already exists; admin bootstrap skipped");
            return;
        }

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedName = name == null ? "" : name.trim();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalStateException("Set BOOTSTRAP_ADMIN_EMAIL to a valid email before enabling admin bootstrap");
        }
        if (normalizedName.isBlank() || normalizedName.length() > 120) {
            throw new IllegalStateException("Set BOOTSTRAP_ADMIN_NAME to a name between 1 and 120 characters");
        }
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Set BOOTSTRAP_ADMIN_PASSWORD to a password with 12–72 UTF-8 bytes");
        }
        if (farmerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("The bootstrap email already belongs to a non-admin account; bootstrap will not change its role");
        }

        Farmer administrator = new Farmer();
        administrator.setName(normalizedName);
        administrator.setEmail(normalizedEmail);
        administrator.setPassword(passwordEncoder.encode(password));
        administrator.setRole(FarmerRole.ADMIN);
        farmerRepository.save(administrator);
        log.info("Created the initial administrator account; disable admin bootstrap after this startup");
    }
}
