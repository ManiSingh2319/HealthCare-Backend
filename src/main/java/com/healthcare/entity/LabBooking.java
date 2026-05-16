package com.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lab_bookings")
public class LabBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;
    private LocalDate testDate;
    @Column(length = 20)
    private String timeSlot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabBookingStatus status = LabBookingStatus.BOOKED;
    private BigDecimal amount;
    private Boolean homeCollection = false;
    @Column(length = 1000)
    private String collectionAddress;
    private String reportUrl;
    private LocalDateTime bookedAt;

    @PrePersist
    void onCreate() {
        bookedAt = LocalDateTime.now();
    }
}
