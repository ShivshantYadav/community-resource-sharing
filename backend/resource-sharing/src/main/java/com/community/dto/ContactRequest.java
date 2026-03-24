package com.community.dto;

import jakarta.validation.constraints.NotBlank;

public class ContactRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    // Optional: If frontend sends userId (or you can get from token)
    private Long userId;

    // ===== Getters & Setters =====
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
