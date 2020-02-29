package com.delicate.forum.controller;

import com.delicate.forum.entity.Comment;
import com.delicate.forum.entity.DiscussPost;
import com.delicate.forum.entity.Page;
import com.delicate.forum.entity.User;
import com.delicate.forum.service.CommentService;
import com.delicate.forum.service.DiscussPostService;
import com.delicate.forum.service.UserService;
import com.delicate.forum.util.ForumConstant;
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
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/addPost", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return ForumUtils.getJSONString(403, "Operation requires login");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return ForumUtils.getJSONString(200, "Post Succeed");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        // get comments data
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        List<Comment> commentsList = commentService.findCommentsByEntity(ForumConstant.ENTITY_POST,
                discussPost.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentsList != null) {
            for (Comment comment : commentsList) {
                Map<String, Object> commentVOMap = new HashMap<>();
                commentVOMap.put("comment", comment);
                commentVOMap.put("user", userService.findUserById(comment.getUserId()));
                List<Comment> replyList = commentService.findCommentsByEntity(ForumConstant.ENTITY_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVOMap = new HashMap<>();
                        replyVOMap.put("reply", reply);
                        replyVOMap.put("user", userService.findUserById(reply.getUserId()));
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVOMap.put("target", targetUser);
                        replyVOList.add(replyVOMap);
                    }
                }
                commentVOMap.put("replys", replyVOList);
                int replyCount = commentService.getCommentCount(ForumConstant.ENTITY_COMMENT, comment.getId());
                commentVOMap.put("replyCount", replyCount);
                commentVOList.add(commentVOMap);
            }
        }
        model.addAttribute("comments", commentVOList);
        return "/site/discuss-detail";
    }
}
