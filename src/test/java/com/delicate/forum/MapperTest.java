package com.delicate.forum;

import com.delicate.forum.dao.DiscussPostMapper;
import com.delicate.forum.dao.UserMapper;
import com.delicate.forum.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

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
}
