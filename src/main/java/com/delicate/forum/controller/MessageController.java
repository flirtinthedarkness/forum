package com.delicate.forum.controller;

import com.delicate.forum.entity.Message;
import com.delicate.forum.entity.Page;
import com.delicate.forum.entity.User;
import com.delicate.forum.service.MessageService;
import com.delicate.forum.service.UserService;
import com.delicate.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/conversation")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String getMessageList(Model model, Page page) {
        page.setLimit(5);
        page.setPath("/conversation/list");
        User user = hostHolder.getUser();
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversationList =
                messageService.findLatestConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                // each conversation's view object
                Map<String, Object> voMap = new HashMap<>();
                voMap.put("conversation", message);
                voMap.put("unreadCount",
                        messageService.findUnreadMessagesCountByConversation(user.getId(),
                                message.getConversationId()));
                voMap.put("messageCount", messageService.findMessageCount(message.getConversationId()));

                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                voMap.put("targetUser", userService.findUserById(targetId));
                conversations.add(voMap);
            }
        }
        model.addAttribute("conversations", conversations);

        // find total unread message
        int messageUnreadCount = messageService.findUnreadMessagesCountByConversation(user.getId(), null);
        model.addAttribute("totalUnread", messageUnreadCount);

        return "/site/letter";
    }
}
