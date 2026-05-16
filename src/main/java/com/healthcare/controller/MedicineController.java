package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.service.HealthcareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {
    private final HealthcareService healthcareService;

    @GetMapping
    public ApiResponse<?> all(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String category,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Operation successful", healthcareService.medicines(search, category, pageable));
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.success("Operation successful", healthcareService.medicineCategories());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> one(@PathVariable Long id) {
        return ApiResponse.success("Operation successful", healthcareService.medicine(id));
    }
}
