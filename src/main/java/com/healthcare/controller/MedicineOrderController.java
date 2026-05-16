package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.HealthcareDtos.PlaceOrderRequest;
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
@RequestMapping("/api/v1/medicine-orders")
@RequiredArgsConstructor
public class MedicineOrderController {
    private final HealthcareService healthcareService;

    @PostMapping
    public ApiResponse<?> place(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.success("Order placed successfully", healthcareService.placeOrder(principal.getId(), request));
    }

    @GetMapping("/my")
    public ApiResponse<?> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Operation successful", healthcareService.myOrders(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.order(principal.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success("Order cancelled successfully", healthcareService.cancelOrder(principal.getId(), id));
    }
}
