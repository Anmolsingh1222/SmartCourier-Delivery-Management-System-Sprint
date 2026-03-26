package com.smartcourier.auth.config;

import com.smartcourier.auth.domain.UserEntity;
import com.smartcourier.auth.repository.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    @Bean
    ApplicationRunner bootstrapAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${bootstrap.admin.name:Platform Admin}") String adminName,
            @Value("${bootstrap.admin.email:admin@smartcourier.local}") String adminEmail,
            @Value("${bootstrap.admin.password:Admin@12345}") String adminPassword) {
        return args -> {
            if (!enabled) {
                return;
            }
            userRepository.findByEmail(adminEmail.toLowerCase()).ifPresentOrElse(user -> {
            }, () -> {
                UserEntity admin = new UserEntity();
                admin.setName(adminName);
                admin.setEmail(adminEmail.toLowerCase());
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                admin.setCreatedAt(Instant.now());
                userRepository.save(admin);
                log.info("Bootstrapped default admin user: {}", adminEmail);
            });
        };
    }
}
