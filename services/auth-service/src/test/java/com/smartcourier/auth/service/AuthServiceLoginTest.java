package com.smartcourier.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartcourier.auth.domain.RefreshTokenEntity;
import com.smartcourier.auth.domain.UserEntity;
import com.smartcourier.auth.repository.RefreshTokenRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.web.dto.LoginRequest;
import com.smartcourier.auth.web.dto.UpdatePasswordRequest;
import com.smartcourier.auth.web.dto.UpdateProfileRequest;
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
class AuthServiceLoginTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    private AuthService authService;
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        JwtService jwtService = new JwtService(
                "smartcourier-super-secret-key-change-in-prod-32-plus-bytes", 3600, 7200);
        authService = new AuthService(userRepository, refreshTokenRepository, encoder, jwtService);
    }

    private UserEntity makeUser(Long id, String email, String rawPassword, String role) {
        UserEntity u = new UserEntity();
        u.setId(id); u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(role); u.setName("Test User");
        return u;
    }

    @Test
    void loginSucceedsWithCorrectCredentials() {
        UserEntity user = makeUser(1L, "test@example.com", "Password@123", "CUSTOMER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.login(new LoginRequest("test@example.com", "Password@123"));

        assertNotNull(response.accessToken());
        assertEquals("CUSTOMER", response.profile().role());
    }

    @Test
    void loginFailsWithWrongPassword() {
        UserEntity user = makeUser(1L, "test@example.com", "Password@123", "CUSTOMER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("test@example.com", "WrongPassword")));
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "Password@123")));
    }

    @Test
    void getMeReturnsUserProfile() {
        UserEntity user = makeUser(5L, "me@example.com", "pass", "CUSTOMER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        var profile = authService.me(5L);

        assertEquals("me@example.com", profile.email());
        assertEquals("Test User", profile.name());
    }

    @Test
    void updateMeChangesName() {
        UserEntity user = makeUser(5L, "me@example.com", "pass", "CUSTOMER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var profile = authService.updateMe(5L, new UpdateProfileRequest("New Name", "me@example.com"));

        assertEquals("New Name", profile.name());
    }

    @Test
    void updatePasswordSucceedsWithCorrectOldPassword() {
        UserEntity user = makeUser(5L, "me@example.com", "OldPass@1", "CUSTOMER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.updatePassword(5L,
                new UpdatePasswordRequest("OldPass@1", "NewPass@2")));
    }

    @Test
    void updatePasswordFailsWithWrongOldPassword() {
        UserEntity user = makeUser(5L, "me@example.com", "OldPass@1", "CUSTOMER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.updatePassword(5L,
                new UpdatePasswordRequest("WrongOld", "NewPass@2")));
    }
}
