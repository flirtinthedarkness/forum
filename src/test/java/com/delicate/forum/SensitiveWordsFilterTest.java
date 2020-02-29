package com.delicate.forum;

import com.delicate.forum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class SensitiveWordsFilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveWordsFilter() {
        String text = "这个群是正规群，不可嫖娼！ 不可赌博！不可吸毒！";
        System.out.println(sensitiveFilter.filterString(text));
        text = "这个群是🤣😅😆正规群，不可嫖😋娼！ 不可😆赌博！不可😇吸毒！";
        System.out.println(sensitiveFilter.filterString(text));
    }
}
