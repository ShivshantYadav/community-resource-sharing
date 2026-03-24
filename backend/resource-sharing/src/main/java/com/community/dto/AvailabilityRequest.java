package com.community.dto;

import java.time.LocalDate;

public class AvailabilityRequest {
    private LocalDate availableDate;

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }
}
