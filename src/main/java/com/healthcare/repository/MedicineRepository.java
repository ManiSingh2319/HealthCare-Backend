package com.healthcare.repository;

import com.healthcare.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    @Query("""
            select m from Medicine m
            where m.isActive = true
            and (:search is null or lower(m.name) like lower(concat('%', :search, '%'))
                 or lower(m.brand) like lower(concat('%', :search, '%'))
                 or lower(m.genericName) like lower(concat('%', :search, '%')))
            and (:category is null or lower(m.category) = lower(:category))
            """)
    Page<Medicine> search(@Param("search") String search, @Param("category") String category, Pageable pageable);

    @Query("select distinct m.category from Medicine m where m.category is not null and m.isActive = true")
    List<String> findDistinctCategories();
}
