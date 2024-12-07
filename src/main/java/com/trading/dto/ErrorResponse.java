package com.trading.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private String path;

    public ErrorResponse(String message, String details, String path) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.details = details;
        this.path = path;
    }
}
