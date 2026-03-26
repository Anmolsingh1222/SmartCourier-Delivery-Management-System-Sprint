package com.smartcourier.auth.service;

import com.smartcourier.auth.domain.RefreshTokenEntity;
import com.smartcourier.auth.domain.UserEntity;
import com.smartcourier.auth.repository.RefreshTokenRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.web.dto.AuthResponse;
import com.smartcourier.auth.web.dto.LoginRequest;
import com.smartcourier.auth.web.dto.LogoutRequest;
import com.smartcourier.auth.web.dto.RefreshRequest;
import com.smartcourier.auth.web.dto.SignupRequest;
import com.smartcourier.auth.web.dto.UpdatePasswordRequest;
import com.smartcourier.auth.web.dto.UpdateProfileRequest;
import com.smartcourier.auth.web.dto.UserProfile;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthServicePort {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        userRepository.findByEmail(request.email().toLowerCase()).ifPresent(user -> {
            throw new IllegalArgumentException("Email already registered");
        });

        UserEntity user = new UserEntity();
        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("CUSTOMER");
        user.setCreatedAt(Instant.now());

        UserEntity saved = userRepository.save(user);
        return tokenResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return tokenResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        Map<String, Object> claims = jwtService.parse(request.refreshToken());
        if (!"refresh".equals(claims.get("type"))) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshTokenEntity stored = refreshTokenRepository.findByTokenValue(request.refreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        Long userId = Long.valueOf(String.valueOf(claims.get("sub")));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        return tokenResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByTokenValue(request.refreshToken()).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile me(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfile updateMe(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase());
        return toProfile(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).forEach(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse tokenResponse(UserEntity user) {
        cleanupExpiredTokens();
        String access = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refresh = jwtService.createRefreshToken(user.getId());
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenValue(refresh);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(access, refresh, "Bearer", jwtService.getAccessTtlSeconds(), toProfile(user));
    }

    private UserProfile toProfile(UserEntity user) {
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now().minusSeconds(3600));
    }
}
