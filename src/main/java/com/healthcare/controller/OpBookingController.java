package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.HealthcareDtos.BookOpRequest;
import com.healthcare.dto.HealthcareDtos.RescheduleRequest;
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
@RequestMapping("/api/v1/op-bookings")
@RequiredArgsConstructor
public class OpBookingController {
    private final HealthcareService healthcareService;

    @PostMapping
    public ApiResponse<?> book(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody BookOpRequest request) {
        return ApiResponse.success("Appointment booked successfully", healthcareService.bookOp(principal.getId(), request));
    }

    @GetMapping("/my")
    public ApiResponse<?> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Operation successful", healthcareService.myOpBookings(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.opBooking(principal.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Booking cancelled successfully", healthcareService.cancelOp(principal.getId(), id));
    }

    @PutMapping("/{id}/reschedule")
    public ApiResponse<?> reschedule(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long id,
                                     @Valid @RequestBody RescheduleRequest request) {
        return ApiResponse.success("Booking rescheduled successfully", healthcareService.rescheduleOp(principal.getId(), id, request));
    }
}
