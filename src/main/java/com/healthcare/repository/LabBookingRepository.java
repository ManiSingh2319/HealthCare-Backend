package com.healthcare.repository;

import com.healthcare.entity.LabBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabBookingRepository extends JpaRepository<LabBooking, Long> {
    List<LabBooking> findByUserIdOrderByBookedAtDesc(Long userId);
    long countByUserId(Long userId);
}
