package com.community.repository;

import com.community.entity.Payment;
import com.community.entity.PaymentStatus;
import com.community.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /* ================= BASIC ================= */

    // All payments for a booking
    List<Payment> findByBookingBookingId(Long bookingId);

    /* ================= RAZORPAY ================= */

    // Fetch payment by Razorpay order id (unique per order)
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /* ================= SAFETY CHECKS ================= */

    // Check if booking already has a successful payment
    boolean existsByBookingBookingIdAndPaymentStatus(
            Long bookingId,
            PaymentStatus paymentStatus
    );

    // Fetch latest unpaid payment (avoid duplicate orders)
    Optional<Payment> findTopByBookingBookingIdAndPaymentStatusOrderByPaymentDateDesc(
            Long bookingId,
            PaymentStatus paymentStatus
    );

    /* ================= PAYMENT TYPE ================= */

    // Example: DEPOSIT payment
    Optional<Payment> findByBookingBookingIdAndPaymentType(
            Long bookingId,
            PaymentType paymentType
    );
}
