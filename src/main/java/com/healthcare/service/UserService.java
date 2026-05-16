package com.healthcare.service;

import com.healthcare.dto.UserDtos.ChangePasswordRequest;
import com.healthcare.dto.UserDtos.DashboardResponse;
import com.healthcare.dto.UserDtos.UpdateProfileRequest;
import com.healthcare.dto.UserResponse;
import com.healthcare.entity.User;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.exception.ValidationException;
import com.healthcare.repository.LabBookingRepository;
import com.healthcare.repository.MedicineOrderRepository;
import com.healthcare.repository.OpBookingRepository;
import com.healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MedicineOrderRepository medicineOrderRepository;
    private final OpBookingRepository opBookingRepository;
    private final LabBookingRepository labBookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse me(Long userId) {
        return modelMapper.map(getUser(userId), UserResponse.class);
    }

    public UserResponse update(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> { throw new ValidationException("Email already registered"); });
        userRepository.findByPhone(request.getPhone())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> { throw new ValidationException("Phone already registered"); });
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public DashboardResponse dashboard(Long userId) {
        return new DashboardResponse(
                medicineOrderRepository.countByUserId(userId),
                opBookingRepository.countByUserId(userId),
                labBookingRepository.countByUserId(userId));
    }
}
