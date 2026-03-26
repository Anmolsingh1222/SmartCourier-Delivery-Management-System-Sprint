package com.smartcourier.auth.service;

import com.smartcourier.auth.web.dto.AuthResponse;
import com.smartcourier.auth.web.dto.LoginRequest;
import com.smartcourier.auth.web.dto.LogoutRequest;
import com.smartcourier.auth.web.dto.RefreshRequest;
import com.smartcourier.auth.web.dto.SignupRequest;
import com.smartcourier.auth.web.dto.UpdatePasswordRequest;
import com.smartcourier.auth.web.dto.UpdateProfileRequest;
import com.smartcourier.auth.web.dto.UserProfile;

public interface AuthServicePort {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    UserProfile me(Long userId);

    UserProfile updateMe(Long userId, UpdateProfileRequest request);

    void updatePassword(Long userId, UpdatePasswordRequest request);
}
