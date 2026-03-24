package com.community.controller;

import com.community.dto.AdminResponse;
import com.community.dto.ResourceResponse;
import com.community.entity.Booking;
import com.community.entity.Resource;
import com.community.entity.Role;
import com.community.entity.User;
import com.community.repository.BookingRepository;
import com.community.repository.ConversationRepository;
import com.community.repository.ResourceRepository;
import com.community.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository; // removed stray '/'

    @Autowired
    private ConversationRepository conversationRepository;
    
    // Constructor injection for all repositories
    public AdminController(UserRepository userRepository,
                           ResourceRepository resourceRepository,
                           BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
    }

    // ✅ GET ALL USERS
    @GetMapping("/users")
    public List<AdminResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            AdminResponse dto = new AdminResponse();
            dto.setUserId(user.getUserId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setRole(user.getRole() != null ? user.getRole().name() : "USER");
            dto.setEmailVerified(user.isEmailVerified());
            dto.setEnabled(user.isEnabled());
            return dto;
        }).toList();
    }

    // ✅ ENABLE / DISABLE USER (only USER & OWNER)
    @PutMapping("/users/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Admin users cannot be blocked/unblocked");
        }

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    // ✅ GET ALL RESOURCES
    @GetMapping("/resources")
    public ResponseEntity<List<ResourceResponse>> getAllResources() {
        List<Resource> resources = resourceRepository.findAllWithOwnerAndAvailability();

        List<ResourceResponse> dtoList = resources.stream().map(r -> {
            ResourceResponse dto = new ResourceResponse();
            dto.setResourceId(r.getResourceId());
            dto.setTitle(r.getTitle());
            dto.setCategory(r.getCategory());
            dto.setRentPrice(r.getRentPrice());
            dto.setDeposit(r.getDeposit());
            dto.setOwnerEmail(r.getOwner() != null ? r.getOwner().getEmail() : "");
            // dto.setActive(r.isActive()); // Uncomment if you have 'active' field
            dto.setAvailability(r.getAvailability() != null
                    ? r.getAvailability().stream().map(ra -> ra.getAvailableDate()).toList()
                    : List.of());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

 
    @DeleteMapping("/resources/{id}")
    @Transactional
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // 1️⃣ Fetch all bookings for this resource
        List<Booking> bookings = bookingRepository.findByResource(resource);

        // 2️⃣ Delete related conversations for each booking
        for (Booking booking : bookings) {
            conversationRepository.deleteByBooking(booking); // make sure this method exists
        }

        // 3️⃣ Delete bookings
        bookingRepository.deleteAll(bookings);

        // 4️⃣ Delete the resource
        resourceRepository.delete(resource);

        return ResponseEntity.noContent().build();
    }



}
