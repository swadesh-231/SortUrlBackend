package com.sorturlbackend.security.service;

import com.sorturlbackend.dto.request.LoginRequest;
import com.sorturlbackend.dto.request.RegisterRequest;
import com.sorturlbackend.dto.response.AuthResponse;
import com.sorturlbackend.dto.response.LoginResponse;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    String refreshToken(String refreshToken);
}