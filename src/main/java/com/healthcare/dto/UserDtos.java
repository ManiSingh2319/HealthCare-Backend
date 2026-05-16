package com.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

public class UserDtos {
    @Data
    public static class UpdateProfileRequest {
        @NotBlank
        private String name;
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String phone;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;
        @Size(min = 6)
        private String newPassword;
    }

    @Data
    @AllArgsConstructor
    public static class DashboardResponse {
        private long totalOrders;
        private long totalBookings;
        private long totalLabTests;
    }
}
