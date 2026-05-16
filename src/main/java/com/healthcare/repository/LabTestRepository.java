package com.healthcare.repository;

import com.healthcare.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByIsActiveTrueAndNameContainingIgnoreCase(String name);
    List<LabTest> findByIsActiveTrue();
}
