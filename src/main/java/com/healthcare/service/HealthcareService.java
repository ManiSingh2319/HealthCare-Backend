package com.healthcare.service;

import com.healthcare.dto.HealthcareDtos.BookLabRequest;
import com.healthcare.dto.HealthcareDtos.BookOpRequest;
import com.healthcare.dto.HealthcareDtos.DoctorResponse;
import com.healthcare.dto.HealthcareDtos.HospitalResponse;
import com.healthcare.dto.HealthcareDtos.LabBookingResponse;
import com.healthcare.dto.HealthcareDtos.LabTestResponse;
import com.healthcare.dto.HealthcareDtos.MedicineOrderItemResponse;
import com.healthcare.dto.HealthcareDtos.MedicineOrderResponse;
import com.healthcare.dto.HealthcareDtos.MedicineResponse;
import com.healthcare.dto.HealthcareDtos.OpBookingResponse;
import com.healthcare.dto.HealthcareDtos.PlaceOrderRequest;
import com.healthcare.dto.HealthcareDtos.RescheduleRequest;
import com.healthcare.entity.BookingStatus;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Hospital;
import com.healthcare.entity.LabBooking;
import com.healthcare.entity.LabBookingStatus;
import com.healthcare.entity.LabTest;
import com.healthcare.entity.Medicine;
import com.healthcare.entity.MedicineOrder;
import com.healthcare.entity.MedicineOrderItem;
import com.healthcare.entity.OpBooking;
import com.healthcare.entity.OrderStatus;
import com.healthcare.entity.User;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.exception.ValidationException;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.HospitalRepository;
import com.healthcare.repository.LabBookingRepository;
import com.healthcare.repository.LabTestRepository;
import com.healthcare.repository.MedicineOrderRepository;
import com.healthcare.repository.MedicineRepository;
import com.healthcare.repository.OpBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HealthcareService {
    private static final Set<BookingStatus> ACTIVE_OP_STATUSES = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final UserService userService;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final OpBookingRepository opBookingRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineOrderRepository medicineOrderRepository;
    private final LabTestRepository labTestRepository;
    private final LabBookingRepository labBookingRepository;

    public List<HospitalResponse> hospitals() {
        return hospitalRepository.findByIsActiveTrue().stream().map(h -> toHospitalResponse(h, false)).toList();
    }

    public HospitalResponse hospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return toHospitalResponse(hospital, true);
    }

    public List<DoctorResponse> hospitalDoctors(Long hospitalId, String specialization) {
        List<Doctor> doctors = specialization == null || specialization.isBlank()
                ? doctorRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                : doctorRepository.findByHospitalIdAndSpecializationIgnoreCaseContainingAndIsActiveTrue(hospitalId, specialization);
        return doctors.stream().map(this::toDoctorResponse).toList();
    }

    public List<DoctorResponse> doctors(String specialization, String city, Long hospitalId) {
        return doctorRepository.search(blankToNull(specialization), blankToNull(city), hospitalId).stream()
                .map(this::toDoctorResponse)
                .toList();
    }

    public DoctorResponse doctor(Long id) {
        return toDoctorResponse(getDoctor(id));
    }

    public List<LocalTime> availableSlots(Long doctorId, LocalDate date) {
        getDoctor(doctorId);
        Set<LocalTime> booked = opBookingRepository.findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, date, ACTIVE_OP_STATUSES)
                .stream().map(OpBooking::getSlotTime).collect(java.util.stream.Collectors.toSet());
        return java.util.stream.Stream.iterate(LocalTime.of(9, 0), time -> time.plusMinutes(30))
                .limit(16)
                .filter(time -> !booked.contains(time))
                .toList();
    }

    @Transactional
    public OpBookingResponse bookOp(Long userId, BookOpRequest request) {
        Doctor doctor = getDoctor(request.getDoctorId());
        ensureSlotAvailable(doctor.getId(), request.getAppointmentDate(), request.getSlotTime());
        OpBooking booking = new OpBooking();
        booking.setUser(userService.getUser(userId));
        booking.setDoctor(doctor);
        booking.setHospital(doctor.getHospital());
        booking.setAppointmentDate(request.getAppointmentDate());
        booking.setSlotTime(request.getSlotTime());
        booking.setConsultationFee(doctor.getConsultationFee());
        booking.setPatientNotes(request.getPatientNotes());
        return toOpBookingResponse(opBookingRepository.save(booking));
    }

    public List<OpBookingResponse> myOpBookings(Long userId) {
        return opBookingRepository.findByUserIdOrderByAppointmentDateDescSlotTimeDesc(userId).stream()
                .map(this::toOpBookingResponse).toList();
    }

    public OpBookingResponse opBooking(Long userId, Long id) {
        return toOpBookingResponse(ownedOpBooking(userId, id));
    }

    @Transactional
    public OpBookingResponse cancelOp(Long userId, Long id) {
        OpBooking booking = ownedOpBooking(userId, id);
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException("Only pending or confirmed bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toOpBookingResponse(booking);
    }

    @Transactional
    public OpBookingResponse rescheduleOp(Long userId, Long id, RescheduleRequest request) {
        OpBooking booking = ownedOpBooking(userId, id);
        ensureSlotAvailable(booking.getDoctor().getId(), request.getAppointmentDate(), request.getSlotTime());
        booking.setAppointmentDate(request.getAppointmentDate());
        booking.setSlotTime(request.getSlotTime());
        return toOpBookingResponse(booking);
    }

    public Page<MedicineResponse> medicines(String search, String category, Pageable pageable) {
        return medicineRepository.search(blankToNull(search), blankToNull(category), pageable).map(this::toMedicineResponse);
    }

    public MedicineResponse medicine(Long id) {
        return toMedicineResponse(getMedicine(id));
    }

    public List<String> medicineCategories() {
        return medicineRepository.findDistinctCategories();
    }

    @Transactional
    public MedicineOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userService.getUser(userId);
        MedicineOrder order = new MedicineOrder();
        order.setUser(user);
        order.setDiscount(request.getDiscount() == null ? BigDecimal.ZERO : request.getDiscount());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPrescriptionUrl(request.getPrescriptionUrl());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemRequest : request.getItems()) {
            Medicine medicine = getMedicine(itemRequest.getMedicineId());
            if (medicine.getStockQuantity() == null || medicine.getStockQuantity() < itemRequest.getQuantity()) {
                throw new ValidationException("Insufficient stock for " + medicine.getName());
            }
            medicine.setStockQuantity(medicine.getStockQuantity() - itemRequest.getQuantity());
            MedicineOrderItem item = new MedicineOrderItem();
            item.setOrder(order);
            item.setMedicine(medicine);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(medicine.getPrice());
            item.setTotalPrice(medicine.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            total = total.add(item.getTotalPrice());
            order.getItems().add(item);
        }
        order.setTotalAmount(total);
        order.setFinalAmount(total.subtract(order.getDiscount()));
        return toMedicineOrderResponse(medicineOrderRepository.save(order));
    }

    public List<MedicineOrderResponse> myOrders(Long userId) {
        return medicineOrderRepository.findByUserIdOrderByOrderedAtDesc(userId).stream()
                .map(this::toMedicineOrderResponse).toList();
    }

    public MedicineOrderResponse order(Long userId, Long id) {
        return toMedicineOrderResponse(ownedOrder(userId, id));
    }

    @Transactional
    public MedicineOrderResponse cancelOrder(Long userId, Long id) {
        MedicineOrder order = ownedOrder(userId, id);
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new ValidationException("Only placed orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toMedicineOrderResponse(order);
    }

    public List<LabTestResponse> labTests(String search) {
        List<LabTest> tests = search == null || search.isBlank()
                ? labTestRepository.findByIsActiveTrue()
                : labTestRepository.findByIsActiveTrueAndNameContainingIgnoreCase(search);
        return tests.stream().map(this::toLabTestResponse).toList();
    }

    public LabTestResponse labTest(Long id) {
        return toLabTestResponse(getLabTest(id));
    }

    @Transactional
    public LabBookingResponse bookLab(Long userId, BookLabRequest request) {
        LabTest labTest = getLabTest(request.getLabTestId());
        LabBooking booking = new LabBooking();
        booking.setUser(userService.getUser(userId));
        booking.setLabTest(labTest);
        booking.setTestDate(request.getTestDate());
        booking.setTimeSlot(request.getTimeSlot());
        booking.setAmount(labTest.getPrice());
        booking.setHomeCollection(Boolean.TRUE.equals(request.getHomeCollection()));
        booking.setCollectionAddress(request.getCollectionAddress());
        return toLabBookingResponse(labBookingRepository.save(booking));
    }

    public List<LabBookingResponse> myLabBookings(Long userId) {
        return labBookingRepository.findByUserIdOrderByBookedAtDesc(userId).stream()
                .map(this::toLabBookingResponse).toList();
    }

    public LabBookingResponse labBooking(Long userId, Long id) {
        return toLabBookingResponse(ownedLabBooking(userId, id));
    }

    public String labReport(Long userId, Long id) {
        return ownedLabBooking(userId, id).getReportUrl();
    }

    @Transactional
    public LabBookingResponse cancelLab(Long userId, Long id) {
        LabBooking booking = ownedLabBooking(userId, id);
        if (booking.getStatus() != LabBookingStatus.BOOKED) {
            throw new ValidationException("Only booked lab tests can be cancelled");
        }
        booking.setStatus(LabBookingStatus.CANCELLED);
        return toLabBookingResponse(booking);
    }

    private void ensureSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        if (time.isBefore(LocalTime.of(9, 0)) || !time.isBefore(LocalTime.of(17, 0)) || time.getMinute() % 30 != 0) {
            throw new ValidationException("Slot must be between 09:00 and 17:00 in 30 minute intervals");
        }
        if (opBookingRepository.existsByDoctorIdAndAppointmentDateAndSlotTimeAndStatusIn(doctorId, date, time, ACTIVE_OP_STATUSES)) {
            throw new ValidationException("Slot already booked");
        }
    }

    private Doctor getDoctor(Long id) {
        return doctorRepository.findById(id).filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private Medicine getMedicine(Long id) {
        return medicineRepository.findById(id).filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
    }

    private LabTest getLabTest(Long id) {
        return labTestRepository.findById(id).filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found"));
    }

    private OpBooking ownedOpBooking(Long userId, Long id) {
        OpBooking booking = opBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot access this booking");
        }
        return booking;
    }

    private MedicineOrder ownedOrder(Long userId, Long id) {
        MedicineOrder order = medicineOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot access this order");
        }
        return order;
    }

    private LabBooking ownedLabBooking(Long userId, Long id) {
        LabBooking booking = labBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot access this lab booking");
        }
        return booking;
    }

    private HospitalResponse toHospitalResponse(Hospital hospital, boolean includeDoctors) {
        HospitalResponse response = new HospitalResponse();
        response.setId(hospital.getId());
        response.setName(hospital.getName());
        response.setAddress(hospital.getAddress());
        response.setCity(hospital.getCity());
        response.setPhone(hospital.getPhone());
        response.setEmail(hospital.getEmail());
        response.setIsActive(hospital.getIsActive());
        if (includeDoctors) {
            response.setDoctors(hospitalDoctors(hospital.getId(), null));
        }
        return response;
    }

    private DoctorResponse toDoctorResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setHospitalId(doctor.getHospital().getId());
        response.setHospitalName(doctor.getHospital().getName());
        response.setName(doctor.getName());
        response.setSpecialization(doctor.getSpecialization());
        response.setQualification(doctor.getQualification());
        response.setExperienceYears(doctor.getExperienceYears());
        response.setConsultationFee(doctor.getConsultationFee());
        response.setAvailableDays(doctor.getAvailableDays());
        response.setIsActive(doctor.getIsActive());
        return response;
    }

    private OpBookingResponse toOpBookingResponse(OpBooking booking) {
        OpBookingResponse response = new OpBookingResponse();
        response.setId(booking.getId());
        response.setDoctorId(booking.getDoctor().getId());
        response.setDoctorName(booking.getDoctor().getName());
        response.setHospitalId(booking.getHospital().getId());
        response.setHospitalName(booking.getHospital().getName());
        response.setAppointmentDate(booking.getAppointmentDate());
        response.setSlotTime(booking.getSlotTime());
        response.setStatus(booking.getStatus().name());
        response.setConsultationFee(booking.getConsultationFee());
        response.setPatientNotes(booking.getPatientNotes());
        response.setDoctorNotes(booking.getDoctorNotes());
        return response;
    }

    private MedicineResponse toMedicineResponse(Medicine medicine) {
        MedicineResponse response = new MedicineResponse();
        response.setId(medicine.getId());
        response.setName(medicine.getName());
        response.setBrand(medicine.getBrand());
        response.setGenericName(medicine.getGenericName());
        response.setCategory(medicine.getCategory());
        response.setDescription(medicine.getDescription());
        response.setPrice(medicine.getPrice());
        response.setStockQuantity(medicine.getStockQuantity());
        response.setRequiresPrescription(medicine.getRequiresPrescription());
        response.setIsActive(medicine.getIsActive());
        return response;
    }

    private MedicineOrderResponse toMedicineOrderResponse(MedicineOrder order) {
        MedicineOrderResponse response = new MedicineOrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscount(order.getDiscount());
        response.setFinalAmount(order.getFinalAmount());
        response.setStatus(order.getStatus().name());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setPrescriptionUrl(order.getPrescriptionUrl());
        response.setItems(order.getItems().stream().map(this::toMedicineOrderItemResponse).toList());
        return response;
    }

    private MedicineOrderItemResponse toMedicineOrderItemResponse(MedicineOrderItem item) {
        MedicineOrderItemResponse response = new MedicineOrderItemResponse();
        response.setMedicineId(item.getMedicine().getId());
        response.setMedicineName(item.getMedicine().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }

    private LabTestResponse toLabTestResponse(LabTest test) {
        LabTestResponse response = new LabTestResponse();
        response.setId(test.getId());
        response.setName(test.getName());
        response.setDescription(test.getDescription());
        response.setPrice(test.getPrice());
        response.setLabName(test.getLabName());
        response.setPreparationInstructions(test.getPreparationInstructions());
        response.setReportTimeHours(test.getReportTimeHours());
        response.setIsActive(test.getIsActive());
        return response;
    }

    private LabBookingResponse toLabBookingResponse(LabBooking booking) {
        LabBookingResponse response = new LabBookingResponse();
        response.setId(booking.getId());
        response.setLabTestId(booking.getLabTest().getId());
        response.setLabTestName(booking.getLabTest().getName());
        response.setTestDate(booking.getTestDate());
        response.setTimeSlot(booking.getTimeSlot());
        response.setStatus(booking.getStatus().name());
        response.setAmount(booking.getAmount());
        response.setHomeCollection(booking.getHomeCollection());
        response.setCollectionAddress(booking.getCollectionAddress());
        response.setReportUrl(booking.getReportUrl());
        return response;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
