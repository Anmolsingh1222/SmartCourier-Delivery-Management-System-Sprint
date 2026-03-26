package com.smartcourier.auth;

import com.smartcourier.auth.service.AuthServicePort;
import com.smartcourier.auth.service.CurrentUserService;
import com.smartcourier.auth.web.dto.AuthResponse;
import com.smartcourier.auth.web.dto.LoginRequest;
import com.smartcourier.auth.web.dto.LogoutRequest;
import com.smartcourier.auth.web.dto.RefreshRequest;
import com.smartcourier.auth.web.dto.SignupRequest;
import com.smartcourier.auth.web.dto.UpdatePasswordRequest;
import com.smartcourier.auth.web.dto.UpdateProfileRequest;
import com.smartcourier.auth.web.dto.UserProfile;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServicePort authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthServicePort authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public UserProfile me() {
        return authService.me(currentUserService.userId());
    }

    @PutMapping("/me")
    public UserProfile updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateMe(currentUserService.userId(), request);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(currentUserService.userId(), request);
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    @GetMapping("/roles")
    public Map<String, Object> roles() {
        return Map.of("roles", new String[] {"CUSTOMER", "ADMIN"});
    }
}
