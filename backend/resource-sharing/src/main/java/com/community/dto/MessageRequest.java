package com.community.dto;

public class MessageRequest {

    private Long conversationId;  // Optional: use if conversation already exists
    private Long bookingId;       // Optional: use to create/fetch conversation via booking
    private String content;       // Message text

    // Getters and setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
