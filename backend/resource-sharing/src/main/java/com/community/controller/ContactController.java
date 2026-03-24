package com.community.controller;

import com.community.dto.ContactRequest;
import com.community.dto.ContactResponse;
import com.community.entity.ContactMessage;
import com.community.entity.User;
import com.community.repository.ContactMessageRepository;
import com.community.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private ContactMessageRepository contactRepo;

    @Autowired
    private UserRepository userRepo;

    // ✅ USER SEND MESSAGE (POST)
    @PostMapping
    public ResponseEntity<?> submitContact(
            @RequestBody ContactRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ContactMessage message = new ContactMessage();
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        message.setUser(user);
        message.setStatus("OPEN"); // or ContactStatus.OPEN

        contactRepo.save(message);

        return ResponseEntity.ok(
                Map.of("msg", "Message sent successfully")
        );
    }

    // ✅ ADMIN GET ALL MESSAGES (DTO ONLY)
    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllMessages() {

        List<ContactResponse> response = contactRepo.findAll()
                .stream()
                .map(msg -> {
                    ContactResponse dto = new ContactResponse();
                    dto.setId(msg.getId());
                    dto.setUserEmail(msg.getUser().getEmail());
                    dto.setSubject(msg.getSubject());
                    dto.setMessage(msg.getMessage());
                    dto.setStatus(msg.getStatus());
                    dto.setCreatedAt(msg.getCreatedAt());
                    return dto;
                }).toList();

        return ResponseEntity.ok(response);
    }
}
