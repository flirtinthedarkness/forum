package com.delicate.forum.controller;

import com.delicate.forum.entity.Message;
import com.delicate.forum.entity.Page;
import com.delicate.forum.entity.User;
import com.delicate.forum.service.MessageService;
import com.delicate.forum.service.UserService;
import com.delicate.forum.util.ForumUtils;
import com.delicate.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

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

    @RequestMapping(path = "/detail/{conversationId}", method = RequestMethod.GET)
    public String getConversationDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        page.setLimit(5);
        page.setPath("/conversation/detail/" + conversationId);
        page.setRows(messageService.findMessageCount(conversationId));

        List<Message> messageList = messageService.findMessagesByConversation(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> messages = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                messages.add(map);
            }
        }
        model.addAttribute("messages", messages);

        model.addAttribute("targetUser", getConversationTargetUser(conversationId));

        // set message status to read
        List<Integer> unreadMessageIds = getUnreadMessageIds(messageList);
        if (!unreadMessageIds.isEmpty()) {
            messageService.updateMessagesStatus(unreadMessageIds);
        }

        return "/site/letter-detail";
    }

    @RequestMapping(path = "/message/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String targetUsername, String content) {
        User targetUser = userService.findUserByUsername(targetUsername);
        if (targetUser == null) {
            return ForumUtils.getJSONString(1, "Message target user not exist");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return ForumUtils.getJSONString(0);
    }

    private List<Integer> getUnreadMessageIds(List<Message> messageList) {
        List<Integer> idList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    idList.add(message.getId());
                }
            }
        }
        return idList;
    }

    private User getConversationTargetUser(String conversationId) {
        String[] ids = conversationId.split("_");
        int id1 = Integer.parseInt(ids[0]);
        int id2 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id1) {
            return userService.findUserById(id2);
        } else {
            return userService.findUserById(id1);
        }
    }
}
