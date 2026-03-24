package com.community.controller;

import com.community.dto.MessageRequest;
import com.community.dto.MessageResponse;
import com.community.entity.Booking;
import com.community.entity.Conversation;
import com.community.entity.Message;
import com.community.entity.User;
import com.community.repository.BookingRepository;
import com.community.repository.ConversationRepository;
import com.community.repository.MessageRepository;
import com.community.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public MessageController(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // ================= GET MESSAGES =================
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Conversation conversation = conversationRepository
                .findByIdWithUsers(conversationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation not found"
                        )
                );

        String email = authentication.getName();

        if (!email.equals(conversation.getBorrower().getEmail())
                && !email.equals(conversation.getOwner().getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                messageRepository.findMessagesByConversationId(conversationId)
        );
    }

    // ================= SEND MESSAGE =================
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody MessageRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if ((request.getConversationId() == null || request.getConversationId() == 0)
                && request.getBookingId() == null) {
            return ResponseEntity.badRequest().body("Either conversationId or bookingId is required");
        }

        // If bookingId is provided, create or fetch conversation
        Conversation conversation;
        if (request.getConversationId() != null && request.getConversationId() > 0) {
            conversation = conversationRepository
                    .findByIdWithUsers(request.getConversationId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found")
                    );
        } else {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            boolean allowed = currentUser.getUserId().equals(booking.getBorrower().getUserId()) ||
                    currentUser.getUserId().equals(booking.getResource().getOwner().getUserId());

            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            conversation = conversationRepository.findByBookingIdWithUsers(booking.getBookingId())
                    .orElseGet(() ->
                            conversationRepository.save(
                                    new Conversation(
                                            booking,
                                            booking.getBorrower(),
                                            booking.getResource().getOwner()
                                    )
                            )
                    );
        }

        String email = authentication.getName();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!email.equals(conversation.getBorrower().getEmail())
                && !email.equals(conversation.getOwner().getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message content is required");
        }


        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent().trim());

        messageRepository.save(message);

        return ResponseEntity.ok().build();
    }
}
