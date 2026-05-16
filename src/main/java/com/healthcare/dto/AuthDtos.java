package com.healthcare.dto;

import com.healthcare.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDtos {
    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String phone;
        @Size(min = 6)
        private String password;
        private UserRole role = UserRole.PATIENT;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String emailOrPhone;
        @NotBlank
        private String password;
    }

    @Data
    public static class SendOtpRequest {
        @NotBlank
        private String phone;
    }

    @Data
    public static class VerifyOtpRequest {
        @NotBlank
        private String phone;
        @NotBlank
        private String otp;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    public static class LogoutRequest {
        @NotBlank
        private String token;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private UserResponse user;
    }
}
