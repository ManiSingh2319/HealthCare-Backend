package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AuthDtos.LoginRequest;
import com.healthcare.dto.AuthDtos.LogoutRequest;
import com.healthcare.dto.AuthDtos.RefreshTokenRequest;
import com.healthcare.dto.AuthDtos.RegisterRequest;
import com.healthcare.dto.AuthDtos.SendOtpRequest;
import com.healthcare.dto.AuthDtos.VerifyOtpRequest;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/send-otp")
    public ApiResponse<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ApiResponse.success("OTP sent successfully", null);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ApiResponse.success("OTP verified successfully", authService.verifyOtp(request));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed successfully", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getToken());
        return ApiResponse.success("Logged out successfully", null);
    }
}
