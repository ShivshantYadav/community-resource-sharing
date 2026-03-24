package com.community.service;

import com.community.entity.Booking;
import com.community.entity.Conversation;
import com.community.entity.User;
import com.community.exception.ResourceNotFoundException;
import com.community.repository.BookingRepository;
import com.community.repository.ConversationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final BookingRepository bookingRepository;

    public ConversationServiceImpl(
            ConversationRepository conversationRepository,
            BookingRepository bookingRepository) {
        this.conversationRepository = conversationRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Fetch existing conversation for a booking or create a new one if it doesn't exist.
     * Ensures proper users are set to avoid foreign key errors when inserting messages.
     */
    @Override
    public Conversation getOrCreateConversation(Long bookingId, User currentUser) {

        // Fetch booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Only borrower or owner can access
        boolean allowed =
                currentUser.getUserId().equals(booking.getBorrower().getUserId()) ||
                currentUser.getUserId().equals(booking.getResource().getOwner().getUserId());

        if (!allowed) {
            throw new RuntimeException("Not allowed to access this conversation");
        }

        // Return existing conversation if found
        Optional<Conversation> existingConv = conversationRepository.findByBookingIdWithUsers(bookingId);
        if (existingConv.isPresent()) {
            return existingConv.get();
        }

        // Create new conversation
        Conversation conv = new Conversation();
        conv.setBooking(booking);
        conv.setBorrower(booking.getBorrower());
        conv.setOwner(booking.getResource().getOwner());
        conv.setCreatedAt(java.time.LocalDateTime.now());

        return conversationRepository.save(conv);
    }

    @Override
    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findAllByUser(user);
    }

    @Override
    public Optional<Conversation> getConversationBetween(User borrower, User owner) {
        return conversationRepository.findBetweenUsers(borrower, owner);
    }

    @Override
    public Optional<Conversation> getConversationByBookingId(Long bookingId) {
        return conversationRepository.findByBookingIdWithUsers(bookingId);
    }

    @Override
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }
}
