package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.service.HealthcareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {
    private final HealthcareService healthcareService;

    @GetMapping
    public ApiResponse<?> all() {
        return ApiResponse.success("Operation successful", healthcareService.hospitals());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.hospital(id));
    }

    @GetMapping("/{id}/doctors")
    public ApiResponse<?> doctors(@PathVariable Long id,
                                  @RequestParam(required = false) String specialization) {
        return ApiResponse.success("Operation successful", healthcareService.hospitalDoctors(id, specialization));
    }
}
