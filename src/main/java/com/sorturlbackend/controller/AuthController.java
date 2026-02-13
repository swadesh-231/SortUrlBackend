package com.sorturlbackend.controller;

import com.sorturlbackend.dto.request.LoginRequest;
import com.sorturlbackend.dto.request.RegisterRequest;
import com.sorturlbackend.dto.response.AuthResponse;
import com.sorturlbackend.dto.response.LoginResponse;
import com.sorturlbackend.security.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Value("${spring.app.refreshtoken}")
    private Long refreshExpiration;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest request) {
        LoginResponse loginResponse = authService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(request.isSecure()) // Dynamic: true if https, false if http (localhost)
                .path("/")
                .maxAge(refreshExpiration / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(loginResponse.getAccessToken())
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationServiceException("Refresh token missing");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token missing"));

        String accessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(accessToken)
                .build());
    }
}
