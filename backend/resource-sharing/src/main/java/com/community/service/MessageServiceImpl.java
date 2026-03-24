package com.community.service;

import com.community.entity.Conversation;
import com.community.entity.Message;
import com.community.entity.User;
import com.community.exception.ResourceNotFoundException;
import com.community.repository.ConversationRepository;
import com.community.repository.MessageRepository;
import com.community.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageServiceImpl(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Message> getMessagesByConversation(Long conversationId) {
        // Ensure conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    @Override
    public Message sendMessage(Long conversationId, Long senderId, String content) {
        // Validate conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Validate sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create and save message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);

        return messageRepository.save(message);
    }

    @Override
    public List<Conversation> getUserConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return conversationRepository.findAllByUser(user);
    }
}
