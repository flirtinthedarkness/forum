package com.delicate.forum.service;

import com.delicate.forum.dao.UserMapper;
import com.delicate.forum.entity.User;
import com.delicate.forum.util.ForumUtils;
import com.delicate.forum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> returnMap = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("Param user cannot be null");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            returnMap.put("username", "Username cannot be empty");
            return returnMap;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            returnMap.put("password", "Password cannot be empty");
            return returnMap;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            returnMap.put("email", "Email cannot be empty");
            return returnMap;
        }

        // check account availability
        User inUser = userMapper.selectByName(user.getUsername());
        if (inUser != null) {
            returnMap.put("usernameMsg", "Account name existed!");
            return returnMap;
        }
        inUser = userMapper.selectByEmail(user.getEmail());
        if (inUser != null) {
            returnMap.put("emailMsg", "Email existed!");
            return returnMap;
        }

        // register user
        user.setSalt(ForumUtils.generateUUID().substring(0, 5));
        user.setPassword(ForumUtils.md5Encryption(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(ForumUtils.generateUUID());
        user.setAvatarUrl(String.format("http://images/nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // send activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/account/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Account Activation", content);

        return returnMap;
    }
}
