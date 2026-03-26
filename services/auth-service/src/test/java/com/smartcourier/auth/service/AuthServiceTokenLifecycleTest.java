package com.smartcourier.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.auth.domain.RefreshTokenEntity;
import com.smartcourier.auth.domain.UserEntity;
import com.smartcourier.auth.repository.RefreshTokenRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.web.dto.LogoutRequest;
import com.smartcourier.auth.web.dto.RefreshRequest;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTokenLifecycleTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthService authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("smartcourier-super-secret-key-change-in-prod-32-plus-bytes", 3600, 7200);
        authService = new AuthService(userRepository, refreshTokenRepository, encoder, jwtService);
    }

    @Test
    void refreshRotatesTokenWhenValid() {
        String token = jwtService.createRefreshToken(44L);

        RefreshTokenEntity stored = new RefreshTokenEntity();
        stored.setTokenValue(token);
        stored.setUserId(44L);
        stored.setExpiresAt(Instant.now().plusSeconds(300));

        UserEntity user = new UserEntity();
        user.setId(44L);
        user.setEmail("valid@example.com");
        user.setRole("CUSTOMER");
        user.setName("Valid User");

        when(refreshTokenRepository.findByTokenValue(token)).thenReturn(Optional.of(stored));
        when(userRepository.findById(44L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.refresh(new RefreshRequest(token));

        assertEquals("Bearer", response.tokenType());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refreshRejectsRevokedToken() {
        String token = jwtService.createRefreshToken(77L);

        RefreshTokenEntity stored = new RefreshTokenEntity();
        stored.setTokenValue(token);
        stored.setUserId(77L);
        stored.setExpiresAt(Instant.now().plusSeconds(300));
        stored.setRevokedAt(Instant.now());

        when(refreshTokenRepository.findByTokenValue(token)).thenReturn(Optional.of(stored));

        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshRequest(token)));
    }

    @Test
    void logoutRevokesRefreshToken() {
        String token = "sample-refresh-token";
        RefreshTokenEntity stored = new RefreshTokenEntity();
        stored.setTokenValue(token);
        stored.setUserId(99L);
        stored.setExpiresAt(Instant.now().plusSeconds(500));

        when(refreshTokenRepository.findByTokenValue(token)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(new LogoutRequest(token));

        verify(refreshTokenRepository).save(stored);
    }
}
