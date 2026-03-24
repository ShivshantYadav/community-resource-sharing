package com.community.controller;

import com.community.dto.ConversationResponse;
import com.community.entity.Conversation;
import com.community.entity.User;
import com.community.repository.UserRepository;
import com.community.service.ConversationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public ConversationController(
            ConversationService conversationService,
            UserRepository userRepository
    ) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

 // ---------------- GET CONVERSATION BY BOOKING ----------------
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ConversationResponse> getConversationByBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // This ensures a conversation exists or creates a new one
        Conversation conv = conversationService.getOrCreateConversation(bookingId, user);

        return ResponseEntity.ok(new ConversationResponse(conv));
    }

    // ---------------- GET USER CONVERSATIONS ----------------
    @GetMapping("/user")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ConversationResponse> response =
                conversationService.getUserConversations(user)
                        .stream()
                        .map(ConversationResponse::new)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ---------------- OPEN OR CREATE CHAT ----------------
    @PostMapping("/booking/{bookingId}/user")
    public ResponseEntity<ConversationResponse> openChat(
            @PathVariable Long bookingId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation =
                conversationService.getOrCreateConversation(bookingId, user);

        return ResponseEntity.ok(new ConversationResponse(conversation));
    }
}
