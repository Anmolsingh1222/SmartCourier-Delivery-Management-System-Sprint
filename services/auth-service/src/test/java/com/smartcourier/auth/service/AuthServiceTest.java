package com.smartcourier.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.smartcourier.auth.domain.RefreshTokenEntity;
import com.smartcourier.auth.domain.UserEntity;
import com.smartcourier.auth.repository.RefreshTokenRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.web.dto.SignupRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        JwtService jwtService = new JwtService("smartcourier-super-secret-key-change-in-prod-32-plus-bytes", 3600, 7200);
        authService = new AuthService(userRepository, refreshTokenRepository, encoder, jwtService);
    }

    @Test
    void signupCreatesCustomerAccount() {
        SignupRequest request = new SignupRequest("Test User", "user@example.com", "Password@123");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity user = inv.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.signup(request);

        assertEquals("CUSTOMER", response.profile().role());
        assertEquals("user@example.com", response.profile().email());
    }

    @Test
    void signupRejectsDuplicateEmail() {
        UserEntity existing = new UserEntity();
        existing.setId(1L);
        existing.setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> authService.signup(new SignupRequest("Any", "user@example.com", "Password@123")));
    }
}
