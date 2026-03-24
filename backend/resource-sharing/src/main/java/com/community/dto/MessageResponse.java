package com.community.dto;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long id;
    private String content;
    private LocalDateTime sentAt;

    private Long senderId;
    private String senderName;
    private String senderEmail;

    private String ownerEmail;
    private String resourceTitle;

    public MessageResponse(
            Long id,
            String content,
            LocalDateTime sentAt,
            Long senderId,
            String senderName,
            String senderEmail,
            String ownerEmail,
            String resourceTitle
    ) {
        this.id = id;
        this.content = content;
        this.sentAt = sentAt;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.ownerEmail = ownerEmail;
        this.resourceTitle = resourceTitle;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getResourceTitle() { return resourceTitle; }
}
