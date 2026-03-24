package com.community.controller;

import com.community.dto.BookingRequest;
import com.community.dto.BookingResponse;
import com.community.dto.PaymentRequest;
import com.community.entity.BookingStatus;
import com.community.entity.User;
import com.community.repository.UserRepository;
import com.community.security.JwtUtil;
import com.community.service.BookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BookingController(
            BookingService bookingService,
            UserRepository userRepository,
            JwtUtil jwtUtil
    ) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // ================= AUTH HELPER =================
    private User getUserFromAuth(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================= CREATE BOOKING =================
    @PostMapping("/create/{resourceId}")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long resourceId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BookingRequest dto
    ) {
        User user = getUserFromAuth(authHeader);
        return ResponseEntity.ok(
                bookingService.createBooking(resourceId, user.getUserId(), dto)
        );
    }

    // ================= USER BOOKINGS =================
    @GetMapping("/my-requests")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = getUserFromAuth(authHeader);
        return ResponseEntity.ok(
                bookingService.getBookingsByUser(user.getUserId())
        );
    }

    // ================= OWNER BOOKINGS =================
    @GetMapping("/owner-requests")
    public ResponseEntity<List<BookingResponse>> ownerBookings(
            @RequestHeader("Authorization") String authHeader
    ) {
        User owner = getUserFromAuth(authHeader);
        return ResponseEntity.ok(
                bookingService.getBookingsForOwner(owner.getUserId())
        );
    }
    
    

    // ================= UPDATE BOOKING (USER) =================
    @PutMapping("/{bookingId}/update-user-details")
    public ResponseEntity<?> updateBookingUserDetails(
            @PathVariable Long bookingId,
            @RequestBody BookingRequest dto,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = getUserFromAuth(authHeader);

        bookingService.updateBookingUserDetails(bookingId, dto);

        return ResponseEntity.ok(
                Map.of("message", "Booking updated successfully")
        );
    }

    // ================= CANCEL BOOKING =================
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = getUserFromAuth(authHeader);
        bookingService.cancelBooking(bookingId, user.getEmail());

        return ResponseEntity.ok(
                Map.of("message", "Booking cancelled")
        );
    }

    // ================= PAYMENT =================
    @PutMapping("/{bookingId}/pay")
    public ResponseEntity<?> payBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PaymentRequest request
    ) {
        if (request.getPaymentMethod() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Payment method required"));
        }

        User user = getUserFromAuth(authHeader);

        bookingService.payBooking(
                bookingId,
                user.getEmail(),
                request.getPaymentMethod()
        );

        return ResponseEntity.ok(
                Map.of("message", "Payment successful")
        );
    }

    // ================= OWNER: APPROVE / REJECT =================
    @PutMapping("/{bookingId}/{status}")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long bookingId,
            @PathVariable String status,
            @RequestHeader("Authorization") String authHeader
    ) {
        User owner = getUserFromAuth(authHeader);

        BookingStatus bookingStatus;
        try {
            bookingStatus = BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid status"));
        }

        bookingService.updateBookingStatusByOwner(
                bookingId,
                bookingStatus,
                owner.getUserId()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking status updated",
                        "status", bookingStatus.name()
                )
        );
    }

    // ================= OWNER: BORROWED BOOKINGS =================
    @GetMapping("/owner/borrowed")
    public ResponseEntity<List<BookingResponse>> ownerBorrowed(
            @RequestHeader("Authorization") String authHeader
    ) {
        User owner = getUserFromAuth(authHeader);

        List<BookingStatus> statuses = List.of(
            BookingStatus.BORROWED,
            BookingStatus.RETURN_REQUESTED,
            BookingStatus.RETURNED,
            BookingStatus.OVERDUE
        );

        return ResponseEntity.ok(
            bookingService.getOwnerBookingsByStatuses(owner.getUserId(), statuses)
        );
    }


    // ================= USER: REQUEST RETURN =================
    @PutMapping("/{bookingId}/return-request")
    public ResponseEntity<?> requestReturn(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = getUserFromAuth(authHeader);
        bookingService.requestReturn(bookingId, user.getEmail());

        return ResponseEntity.ok(
                Map.of("message", "Return requested. OTP sent to owner")
        );
    }

    // ================= OWNER: VERIFY RETURN =================
    @PutMapping("/{bookingId}/verify-return")
    public ResponseEntity<?> verifyReturn(
            @PathVariable Long bookingId,
            @RequestParam String otp,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = getUserFromAuth(authHeader);

        bookingService.verifyReturn(bookingId, otp); // only 2 args now

        return ResponseEntity.ok(Map.of("message", "Return verified successfully"));
    }

}
