package com.community.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    /* ================= Relations ================= */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ElementCollection
    @CollectionTable(
        name = "booking_dates",
        joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "borrow_date")
    private List<LocalDate> borrowDates;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    /* ================= Booking Details ================= */
    private int quantity = 1;
    private double rentPerDay;
    private double deposit;
    private double totalPayable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(nullable = false)
    private boolean returned = false;

    private LocalDateTime createdAt;
    private LocalDateTime returnedAt;
    
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;
    

 // ================= RETURN OTP =================
    @Column(name = "return_otp")
    private String returnOtp;

    // Getter and Setter
    public String getReturnOtp() { return returnOtp; }
    public void setReturnOtp(String returnOtp) { this.returnOtp = returnOtp; }

    

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; 
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    /* ================= Lifecycle ================= */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        calculateTotalPayable();
    }

    /* ================= Business Logic ================= */

    public void calculateTotalPayable() {
        int days = borrowDates != null ? borrowDates.size() : 0;
        this.totalPayable = (rentPerDay * quantity * days) + deposit;
    }

    public void markPaid(PaymentMethod method) {
        this.paid = true;
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PAID;

        if (this.bookingStatus == BookingStatus.APPROVED) {
            this.bookingStatus = BookingStatus.BORROWED;
        }
    }

    public void markReturned() {
        this.returned = true;
        this.returnedAt = LocalDateTime.now();
        this.bookingStatus = BookingStatus.RETURNED;
    }

    public void addPayment(Payment payment) {
        if (this.payments == null) {
            this.payments = new ArrayList<>();
        }
        this.payments.add(payment);
        markPaid(payment.getPaymentMethod());
    }

    /* ================= Getters & Setters ================= */
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public User getBorrower() { return borrower; }
    public void setBorrower(User borrower) { this.borrower = borrower; }

    public List<LocalDate> getBorrowDates() { return borrowDates; }

    // ✅ Corrected setter type
    public void setBorrowDates(List<LocalDate> borrowDates) {
        this.borrowDates = borrowDates;
        calculateTotalPayable();
    }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; calculateTotalPayable(); }

    public double getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(double rentPerDay) { this.rentPerDay = rentPerDay; calculateTotalPayable(); }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; calculateTotalPayable(); }

    public double getTotalPayable() { return totalPayable; }

    // ✅ Added setter for totalPayable
    public void setTotalPayable(double totalPayable) { this.totalPayable = totalPayable; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) {
        this.paid = paid;
        this.paymentStatus = paid ? PaymentStatus.PAID : PaymentStatus.UNPAID;
    }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
}
