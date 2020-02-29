package com.delicate.forum.service;

import com.delicate.forum.dao.MessageMapper;
import com.delicate.forum.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public List<Message> findLatestConversations(int userId, int offset, int limit) {
        return messageMapper.selectLatestConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findMessagesByConversation(String conversationId, int offset, int limit) {
        return messageMapper.selectMessageByConversation(conversationId, offset, limit);
    }

    public int findMessageCount(String conversationId) {
        return messageMapper.selectMessageCountByConversation(conversationId);
    }

    public int findUnreadMessagesCountByConversation(int userId, String conversationId) {
        return messageMapper.selectMessageUnreadCount(userId, conversationId);
    }
}
