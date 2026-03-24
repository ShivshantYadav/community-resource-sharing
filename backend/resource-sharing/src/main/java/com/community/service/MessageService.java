package com.community.service;

import com.community.entity.Conversation;
import com.community.entity.Message;

import java.util.List;

public interface MessageService {

    List<Message> getMessagesByConversation(Long conversationId);

    Message sendMessage(Long conversationId, Long senderId, String content);

    List<Conversation> getUserConversations(Long userId);
}
