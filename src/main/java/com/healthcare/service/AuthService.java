package com.healthcare.service;

import com.healthcare.dto.AuthDtos.AuthResponse;
import com.healthcare.dto.AuthDtos.LoginRequest;
import com.healthcare.dto.AuthDtos.RefreshTokenRequest;
import com.healthcare.dto.AuthDtos.RegisterRequest;
import com.healthcare.dto.AuthDtos.SendOtpRequest;
import com.healthcare.dto.AuthDtos.VerifyOtpRequest;
import com.healthcare.dto.UserResponse;
import com.healthcare.entity.User;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.exception.ValidationException;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.JwtUtil;
import com.healthcare.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final ModelMapper modelMapper;
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Phone already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return authResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return authResponse(user);
    }

    public void sendOtp(SendOtpRequest request) {
        String otp = otpUtil.generateOtp();
        otpCache.put(request.getPhone(), otp);
        log.info("OTP for {} is {}", request.getPhone(), otp);
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String cachedOtp = otpCache.get(request.getPhone());
        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            throw new UnauthorizedException("Invalid OTP");
        }
        otpCache.remove(request.getPhone());
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UnauthorizedException("Phone is not registered"));
        return authResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtUtil.isValid(request.getRefreshToken()) || !jwtUtil.isRefreshToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findById(jwtUtil.getUserId(request.getRefreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        return authResponse(user);
    }

    public void logout(String token) {
        jwtUtil.blacklist(token);
    }

    private AuthResponse authResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtUtil.generateAccessToken(user.getId(), user.getEmail()));
        response.setRefreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getEmail()));
        response.setUser(modelMapper.map(user, UserResponse.class));
        return response;
    }
}
