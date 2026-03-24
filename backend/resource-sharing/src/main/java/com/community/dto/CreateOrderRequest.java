package com.community.dto;


public class CreateOrderRequest {

    private Long bookingId;
    private String paymentMethod;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // =================== Getters & Setters ===================
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    // =================== Convert to BookingRequest ===================
    public BookingRequest toBookingRequest() {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setFullName(this.fullName);
        bookingRequest.setPhone(this.phone);
        bookingRequest.setEmail(this.email);
        bookingRequest.setAddress(this.address);
        bookingRequest.setCity(this.city);
        bookingRequest.setState(this.state);
        bookingRequest.setPostalCode(this.postalCode);
        bookingRequest.setCountry(this.country);
        return bookingRequest;
    }
}
