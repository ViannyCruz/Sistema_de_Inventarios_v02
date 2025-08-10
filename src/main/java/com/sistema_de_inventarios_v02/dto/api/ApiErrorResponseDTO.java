package com.sistema_de_inventarios_v02.dto.api;

import java.time.LocalDateTime;

public class ApiErrorResponseDTO {
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ApiErrorResponseDTO(String error, String message, String path, LocalDateTime timestamp) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
