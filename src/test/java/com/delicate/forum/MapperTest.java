package com.delicate.forum;

import com.delicate.forum.dao.DiscussPostMapper;
import com.delicate.forum.dao.LoginTicketMapper;
import com.delicate.forum.dao.UserMapper;
import com.delicate.forum.entity.DiscussPost;
import com.delicate.forum.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectUser() {
        System.out.println(userMapper.selectById(101));
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost post : postList) {
            System.out.println(post);
        }
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1234341);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
}
