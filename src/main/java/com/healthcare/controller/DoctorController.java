package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.service.HealthcareService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final HealthcareService healthcareService;

    @GetMapping
    public ApiResponse<?> all(@RequestParam(required = false) String specialization,
                              @RequestParam(required = false) String city,
                              @RequestParam(required = false) Long hospitalId) {
        return ApiResponse.success("Operation successful", healthcareService.doctors(specialization, city, hospitalId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.doctor(id));
    }

    @GetMapping("/{id}/slots")
    public ApiResponse<?> slots(@PathVariable Long id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("Operation successful", healthcareService.availableSlots(id, date));
    }
}
