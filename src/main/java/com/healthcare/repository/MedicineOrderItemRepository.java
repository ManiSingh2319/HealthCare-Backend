package com.healthcare.repository;

import com.healthcare.entity.MedicineOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineOrderItemRepository extends JpaRepository<MedicineOrderItem, Long> {
}
