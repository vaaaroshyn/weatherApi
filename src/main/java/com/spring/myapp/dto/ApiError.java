package com.spring.myapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "API error response")
public class ApiError {
    @Schema(description = "Time when the error was produced", example = "2026-09-03T09:15:30Z")
    private Instant timestamp;
    @Schema(example = "400")
    private int status;
    @Schema(example = "Bad Request")
    private String error;
    @Schema(example = "name - The name should not be blank!")
    private String message;
    @Schema(example = "/api/v1/sensors")
    private String path;

    public ApiError(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
