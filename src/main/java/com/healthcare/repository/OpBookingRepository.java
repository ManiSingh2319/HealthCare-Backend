package com.healthcare.repository;

import com.healthcare.entity.BookingStatus;
import com.healthcare.entity.OpBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface OpBookingRepository extends JpaRepository<OpBooking, Long> {
    List<OpBooking> findByUserIdOrderByAppointmentDateDescSlotTimeDesc(Long userId);
    List<OpBooking> findByDoctorIdAndAppointmentDateAndStatusIn(Long doctorId, LocalDate appointmentDate, Collection<BookingStatus> statuses);
    boolean existsByDoctorIdAndAppointmentDateAndSlotTimeAndStatusIn(Long doctorId, LocalDate date, LocalTime time, Collection<BookingStatus> statuses);
    long countByUserId(Long userId);
}
