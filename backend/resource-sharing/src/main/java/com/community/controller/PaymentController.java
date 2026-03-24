package com.community.controller;

import com.community.dto.BookingResponse;
import com.community.dto.CreateOrderRequest;
import com.community.dto.CreateOrderResponse;
import com.community.dto.VerifyPaymentRequest;
import com.community.entity.Booking;
import com.community.entity.Payment;
import com.community.entity.PaymentMethod;
import com.community.entity.PaymentType;
import com.community.service.BookingService;
import com.community.service.PaymentService;
import com.razorpay.RazorpayException;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    // ================= CREATE ORDER =================
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            // ---------------- Validate bookingId ----------------
            if (request.getBookingId() == null) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "Booking ID is required"));
            }

            // ---------------- Fetch booking entity ----------------
            Booking booking = bookingService.getBookingEntity(request.getBookingId());
            if (booking == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Booking not found"));
            }

            // ---------------- Update booking user details ----------------
            try {
                bookingService.updateBookingUserDetails(booking.getBookingId(), request.toBookingRequest());
            } catch (Exception ex) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "Failed to update booking details: " + ex.getMessage()));
            }

            // ---------------- Determine payment method ----------------
            PaymentMethod paymentMethod;
            PaymentType paymentType;

            String reqPayment = request.getPaymentMethod();
            if (reqPayment == null || reqPayment.isBlank()) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "Payment method is required"));
            }

            // ---------------- CASH ON PICKUP ----------------
            if ("CASH_ON_PICKUP".equalsIgnoreCase(reqPayment)) {
                paymentMethod = PaymentMethod.CASH_ON_PICKUP;
                paymentType = PaymentType.OFFLINE;

                // Safety check
                if (booking.getBorrower() == null || booking.getBorrower().getEmail() == null) {
                    return ResponseEntity.status(400)
                            .body(Map.of("message", "Borrower email is missing"));
                }

                // Mark booking as to be paid offline
                bookingService.payBooking(booking.getBookingId(), booking.getBorrower().getEmail(), paymentMethod);

                // Return updated booking
                BookingResponse response = bookingService.mapToDTO(booking);
                return ResponseEntity.ok(response);
            }

            // ---------------- ONLINE PAYMENT ----------------
            try {
                paymentMethod = PaymentMethod.valueOf(reqPayment.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "Invalid payment method: " + reqPayment));
            }
            paymentType = PaymentType.ONLINE;

            // ---------------- Create online payment ----------------
            Payment payment = paymentService.createOrder(
                    booking.getBookingId(),
                    paymentMethod,
                    paymentType
            );

            if (payment == null || payment.getRazorpayOrderId() == null) {
                return ResponseEntity.status(500)
                        .body(Map.of("message", "Failed to create online payment"));
            }

            return ResponseEntity.ok(new CreateOrderResponse(payment.getRazorpayOrderId()));

        } catch (RazorpayException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Razorpay error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Unexpected error: " + e.getMessage()));
        }
    }


    // ================= VERIFY PAYMENT =================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        try {
            boolean success = paymentService.verifyPayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (!success) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "Payment verification failed"));
            }

            Booking booking = bookingService.getBookingEntityByRazorpayOrderId(request.getRazorpayOrderId());
            if (booking == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Booking not found for this payment"));
            }

            BookingResponse response = bookingService.mapToDTO(booking);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }
}
