package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.HealthcareDtos.BookLabRequest;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.HealthcareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lab-bookings")
@RequiredArgsConstructor
public class LabBookingController {
    private final HealthcareService healthcareService;

    @PostMapping
    public ApiResponse<?> book(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody BookLabRequest request) {
        return ApiResponse.success("Lab test booked successfully", healthcareService.bookLab(principal.getId(), request));
    }

    @GetMapping("/my")
    public ApiResponse<?> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Operation successful", healthcareService.myLabBookings(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.labBooking(principal.getId(), id));
    }

    @GetMapping("/{id}/report")
    public ApiResponse<?> report(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.labReport(principal.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Lab booking cancelled successfully", healthcareService.cancelLab(principal.getId(), id));
    }
}
