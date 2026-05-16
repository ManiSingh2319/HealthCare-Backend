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
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
public class LabTestController {
    private final HealthcareService healthcareService;

    @GetMapping
    public ApiResponse<?> all(@RequestParam(required = false) String search) {
        return ApiResponse.success("Operation successful", healthcareService.labTests(search));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.labTest(id));
    }
}
