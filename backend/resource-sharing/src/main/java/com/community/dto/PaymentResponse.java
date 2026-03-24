package com.community.dto;

public class PaymentResponse {
    private String message;
    private Long paymentId;
    private Double amount;

    public PaymentResponse(String message, Long paymentId) {
        this.message = message;
        this.paymentId = paymentId;
    }

    public PaymentResponse(String razorpayOrderId, Double amount) {
        this.message = razorpayOrderId;
        this.amount = amount;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

 
}
