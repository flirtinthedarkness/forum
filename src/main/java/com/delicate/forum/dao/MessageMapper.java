package com.delicate.forum.dao;

import com.delicate.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    List<Message> selectLatestConversations(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectMessageByConversation(String conversationId, int offset, int limit);

    int selectMessageCountByConversation(String conversationId);

    int selectMessageUnreadCount(int userId, String conversationId);
}
