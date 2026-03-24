package com.community.dto;

import com.community.entity.Booking;
import com.community.entity.BookingStatus;
import com.community.entity.PaymentMethod;
import com.community.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingResponse {

    private Long bookingId;
    private Long resourceId;
    private String resourceName;
    private Long borrowerId;
    private List<String> borrowDates;
    private int quantity;
    private double rentPerDay;
    private double deposit;
    private double totalPayable;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private boolean paid;
    private boolean returned;
    private LocalDateTime createdAt;
    private LocalDateTime returnedAt;
    private ResourceResponse resource;
    private String borrowerEmail;

    public BookingResponse() {}
    

    public BookingResponse(Long bookingId, Long resourceId, String resourceName, Long borrowerId,
                           List<String> borrowDates, int quantity, double rentPerDay, double deposit,
                           double totalPayable, BookingStatus bookingStatus, PaymentStatus paymentStatus,
                           PaymentMethod paymentMethod, boolean paid, boolean returned, LocalDateTime createdAt,
                           LocalDateTime returnedAt, ResourceResponse resource, String borrowerEmail) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.borrowerId = borrowerId;
        this.borrowDates = borrowDates;
        this.quantity = quantity;
        this.rentPerDay = rentPerDay;
        this.deposit = deposit;
        this.totalPayable = totalPayable;
        this.bookingStatus = bookingStatus;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.returned = returned;
        this.createdAt = createdAt;
        this.returnedAt = returnedAt;
        this.resource = resource;
        this.borrowerEmail = borrowerEmail;
    }

    // =================== Factory Method ===================
    public static BookingResponse fromEntity(Booking booking, ResourceResponse resourceResponse) {
        // Extract borrow dates as strings if booking has a list of dates
        List<String> borrowDates = booking.getBorrowDates() != null
                ? booking.getBorrowDates().stream()
                        .map(date -> date.toString())
                        .collect(Collectors.toList())
                : List.of();

        double totalRent = borrowDates.size() * booking.getRentPerDay();
        double totalPayable = totalRent + booking.getDeposit();

        return new BookingResponse(
                booking.getBookingId(),
                booking.getResource().getResourceId(),
                booking.getResource().getTitle(),
                booking.getBorrower().getUserId(),
                borrowDates,
                booking.getQuantity(),
                booking.getRentPerDay(),
                booking.getDeposit(),
                totalPayable,
                booking.getBookingStatus(),
                booking.getPaymentStatus(),
                booking.getPaymentMethod(),
                booking.isPaid(),
                booking.isReturned(),
                booking.getCreatedAt(),
                booking.getReturnedAt(),
                resourceResponse,
                booking.getBorrower().getEmail()
        );
    }

    // =================== Getters & Setters ===================
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }

    public List<String> getBorrowDates() { return borrowDates; }
    public void setBorrowDates(List<String> borrowDates) { this.borrowDates = borrowDates; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(double rentPerDay) { this.rentPerDay = rentPerDay; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public double getTotalPayable() { return totalPayable; }
    public void setTotalPayable(double totalPayable) { this.totalPayable = totalPayable; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public ResourceResponse getResource() { return resource; }
    public void setResource(ResourceResponse resource) { this.resource = resource; }

    public String getBorrowerEmail() { return borrowerEmail; }
    public void setBorrowerEmail(String borrowerEmail) { this.borrowerEmail = borrowerEmail; }
}
