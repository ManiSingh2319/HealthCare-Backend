package com.healthcare.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class HealthcareDtos {
    @Data
    public static class HospitalResponse {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String phone;
        private String email;
        private Boolean isActive;
        private List<DoctorResponse> doctors;
    }

    @Data
    public static class DoctorResponse {
        private Long id;
        private Long hospitalId;
        private String hospitalName;
        private String name;
        private String specialization;
        private String qualification;
        private Integer experienceYears;
        private BigDecimal consultationFee;
        private String availableDays;
        private Boolean isActive;
    }

    @Data
    public static class BookOpRequest {
        @NotNull
        private Long doctorId;
        @FutureOrPresent
        @NotNull
        private LocalDate appointmentDate;
        @NotNull
        private LocalTime slotTime;
        private String patientNotes;
    }

    @Data
    public static class RescheduleRequest {
        @FutureOrPresent
        @NotNull
        private LocalDate appointmentDate;
        @NotNull
        private LocalTime slotTime;
    }

    @Data
    public static class OpBookingResponse {
        private Long id;
        private Long doctorId;
        private String doctorName;
        private Long hospitalId;
        private String hospitalName;
        private LocalDate appointmentDate;
        private LocalTime slotTime;
        private String status;
        private BigDecimal consultationFee;
        private String patientNotes;
        private String doctorNotes;
    }

    @Data
    public static class MedicineResponse {
        private Long id;
        private String name;
        private String brand;
        private String genericName;
        private String category;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Boolean requiresPrescription;
        private Boolean isActive;
    }

    @Data
    public static class PlaceOrderRequest {
        @NotEmpty
        @Valid
        private List<OrderItemRequest> items;
        @DecimalMin("0.0")
        private BigDecimal discount = BigDecimal.ZERO;
        @NotBlank
        private String deliveryAddress;
        private String prescriptionUrl;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long medicineId;
        @Min(1)
        private Integer quantity;
    }

    @Data
    public static class MedicineOrderResponse {
        private Long id;
        private BigDecimal totalAmount;
        private BigDecimal discount;
        private BigDecimal finalAmount;
        private String status;
        private String deliveryAddress;
        private String prescriptionUrl;
        private List<MedicineOrderItemResponse> items;
    }

    @Data
    public static class MedicineOrderItemResponse {
        private Long medicineId;
        private String medicineName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data
    public static class LabTestResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String labName;
        private String preparationInstructions;
        private Integer reportTimeHours;
        private Boolean isActive;
    }

    @Data
    public static class BookLabRequest {
        @NotNull
        private Long labTestId;
        @FutureOrPresent
        @NotNull
        private LocalDate testDate;
        @NotBlank
        private String timeSlot;
        private Boolean homeCollection = false;
        private String collectionAddress;
    }

    @Data
    public static class LabBookingResponse {
        private Long id;
        private Long labTestId;
        private String labTestName;
        private LocalDate testDate;
        private String timeSlot;
        private String status;
        private BigDecimal amount;
        private Boolean homeCollection;
        private String collectionAddress;
        private String reportUrl;
    }
}
