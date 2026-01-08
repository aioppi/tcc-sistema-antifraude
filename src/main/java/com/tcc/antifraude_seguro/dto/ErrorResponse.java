package com.tcc.antifraude_seguro.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> messages;
    private String path;

    public ErrorResponse(int status, String error, List<String> messages, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.messages = messages;
        this.path = path;
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public List<String> messages() { return messages; }
    public String getPath() { return path; }
}