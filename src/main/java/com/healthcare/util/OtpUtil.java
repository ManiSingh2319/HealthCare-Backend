package com.healthcare.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {
    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
