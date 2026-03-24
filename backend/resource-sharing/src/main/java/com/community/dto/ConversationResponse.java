package com.community.dto;

import com.community.entity.Conversation;
import com.community.entity.User;

import java.time.format.DateTimeFormatter;

public class ConversationResponse {

    private Long conversationId;
    private Long bookingId;
    private String borrowerEmail;
    private String ownerEmail;
    private String createdAt;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ConversationResponse(Conversation conversation) {

        this.conversationId = conversation.getId();

        this.bookingId = conversation.getBooking() != null
                ? conversation.getBooking().getBookingId()
                : null;

        User borrower = conversation.getBorrower();
        User owner = conversation.getOwner();

        this.borrowerEmail = borrower != null ? borrower.getEmail() : null;
        this.ownerEmail = owner != null ? owner.getEmail() : null;

        this.createdAt = conversation.getCreatedAt() != null
                ? conversation.getCreatedAt().format(DATE_FORMAT)
                : null;
    }

    public Long getConversationId() { return conversationId; }
    public Long getBookingId() { return bookingId; }
    public String getBorrowerEmail() { return borrowerEmail; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getCreatedAt() { return createdAt; }
}
