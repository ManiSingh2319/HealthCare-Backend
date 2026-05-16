package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalIdAndIsActiveTrue(Long hospitalId);
    List<Doctor> findByHospitalIdAndSpecializationIgnoreCaseContainingAndIsActiveTrue(Long hospitalId, String specialization);

    @Query("""
            select d from Doctor d
            where d.isActive = true
            and (:specialization is null or lower(d.specialization) like lower(concat('%', :specialization, '%')))
            and (:city is null or lower(d.hospital.city) like lower(concat('%', :city, '%')))
            and (:hospitalId is null or d.hospital.id = :hospitalId)
            """)
    List<Doctor> search(@Param("specialization") String specialization,
                        @Param("city") String city,
                        @Param("hospitalId") Long hospitalId);
}
