package com.community.service;

import com.community.entity.*;
import com.community.repository.BookingRepository;
import com.community.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    /* =====================================================
       CREATE PAYMENT ORDER
       ===================================================== */
    @Transactional
    public Payment createOrder(
            Long bookingId,
            PaymentMethod paymentMethod,
            PaymentType paymentType
    ) throws RazorpayException {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Booking already paid");
        }

        booking.calculateTotalPayable(); // recalculate totals

        /* ---------- CASH ON PICKUP ---------- */
        if (paymentMethod == PaymentMethod.CASH_ON_PICKUP) {
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPayable());
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentType(PaymentType.OFFLINE);
            payment.setPaymentStatus(PaymentStatus.UNPAID);
            payment.setPaymentDate(LocalDateTime.now());

            booking.setPaymentMethod(paymentMethod);
            booking.setPaymentStatus(PaymentStatus.UNPAID);

            bookingRepository.save(booking);
            return paymentRepository.save(payment);
        }

        /* ---------- REUSE EXISTING UNPAID ---------- */
        Optional<Payment> existing =
                paymentRepository.findTopByBookingBookingIdAndPaymentStatusOrderByPaymentDateDesc(
                        bookingId, PaymentStatus.UNPAID
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        /* ---------- CREATE RAZORPAY ORDER ---------- */
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject request = new JSONObject();
        request.put("amount", Math.round(booking.getTotalPayable() * 100)); // in paise
        request.put("currency", "INR");
        request.put("receipt", "rcpt_" + bookingId + "_" + System.currentTimeMillis());

        Order order = client.orders.create(request);

        // Update booking with Razorpay order ID
        booking.setRazorpayOrderId(order.get("id"));
        booking.setPaymentMethod(paymentMethod);
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPayable());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentType(paymentType != null ? paymentType : PaymentType.ONLINE);
        payment.setPaymentStatus(PaymentStatus.UNPAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setRazorpayOrderId(order.get("id"));

        return paymentRepository.save(payment);
    }

    /* =====================================================
       VERIFY PAYMENT
       ===================================================== */
    @Transactional
    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) {

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for Razorpay order ID: " + razorpayOrderId));

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return true;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            Utils.verifyPaymentSignature(options, keySecret);

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setRazorpaySignature(razorpaySignature);
            payment.setPaymentStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.markPaid(payment.getPaymentMethod());
            bookingRepository.save(booking);

            return true;

        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return false;
        }
    }
}
