package com.healthcare.repository;

import com.healthcare.entity.MedicineOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {
    List<MedicineOrder> findByUserIdOrderByOrderedAtDesc(Long userId);
    long countByUserId(Long userId);
}
