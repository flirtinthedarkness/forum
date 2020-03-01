package com.delicate.forum.service;

import com.delicate.forum.dao.MessageMapper;
import com.delicate.forum.entity.Message;
import com.delicate.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

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

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filterString(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int updateMessagesStatus(List<Integer> idList) {
        return messageMapper.updateMessageStatus(idList, 1);
    }
}
