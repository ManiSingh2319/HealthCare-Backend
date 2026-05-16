package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, LocalDateTime.now());
    }
}
