package com.community.service;

import com.community.entity.Conversation;
import com.community.entity.User;

import java.util.List;
import java.util.Optional;

public interface ConversationService {

    Conversation getOrCreateConversation(Long bookingId, User currentUser);

    List<Conversation> getUserConversations(User user);

    Optional<Conversation> getConversationBetween(User borrower, User owner);

    Optional<Conversation> getConversationByBookingId(Long bookingId);

    Conversation saveConversation(Conversation conversation);
}
