package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.UserDtos.ChangePasswordRequest;
import com.healthcare.dto.UserDtos.UpdateProfileRequest;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Operation successful", userService.me(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<?> update(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", userService.update(principal.getId(), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<?> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ApiResponse.success("Password updated successfully", null);
    }

    @GetMapping("/me/dashboard")
    public ApiResponse<?> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Operation successful", userService.dashboard(principal.getId()));
    }
}
