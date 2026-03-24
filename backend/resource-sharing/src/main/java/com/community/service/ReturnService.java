package com.community.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.community.entity.Booking;
import com.community.entity.BookingStatus;
import com.community.entity.ResourceStatus;
import com.community.entity.ReturnEntity;
import com.community.exception.ResourceNotFoundException;
import com.community.repository.BookingRepository;
import com.community.repository.ReturnRepository;

@Service
public class ReturnService {

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public ReturnEntity returnItem(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.COMPLETED);
        booking.getResource().setStatus(ResourceStatus.AVAILABLE);

        ReturnEntity re = new ReturnEntity();
        re.setBooking(booking);
        re.setReturnDate(LocalDateTime.now());
        re.setDamageReport(false);

        return returnRepository.save(re);
    }
}

