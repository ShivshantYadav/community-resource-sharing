package com.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.community.entity.Booking;
import com.community.entity.Review;
import com.community.exception.ResourceNotFoundException;
import com.community.repository.BookingRepository;
import com.community.service.ReviewService;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/{bookingId}")
    public Review review(@PathVariable Long bookingId,
                         @RequestParam int rating,
                         @RequestParam String comment) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return reviewService.addReview(booking, rating, comment);
    }
}
