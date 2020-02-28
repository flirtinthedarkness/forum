package com.delicate.forum.controller;

import com.delicate.forum.annotation.LoginRequired;
import com.delicate.forum.entity.User;
import com.delicate.forum.service.UserService;
import com.delicate.forum.util.ForumUtils;
import com.delicate.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/settings", method = RequestMethod.GET)
    public String getUserSettingsPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/uploadAvatar", method = RequestMethod.POST)
    public String uploadAvatar(MultipartFile avatarImage, Model model) {
        if (avatarImage == null) {
            model.addAttribute("error", "Image not selected");
            return "/site/setting";
        }

        String fileName = avatarImage.getOriginalFilename();
        String postfix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(postfix)) {
            model.addAttribute("error", "File format not supported");
            return "/site/setting";
        }

        fileName = ForumUtils.generateUUID() + postfix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            avatarImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("File upload failed: " + e.getMessage());
            throw new RuntimeException("Fileupload failed, server error!");
        }

        User user = hostHolder.getUser();
        String avatarUrl = domain + contextPath + "/user/avatar/" + fileName;
        userService.updateUserAvatar(user.getId(), avatarUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/avatar/{filename}", method = RequestMethod.GET)
    public void getAvatar(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        String postfix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/" + postfix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Load avatar failed");
        }
    }

    @LoginRequired
    @RequestMapping(path = "/alterPassword", method = RequestMethod.POST)
    public String alterPassword(String password, String newPassword,
                              Model model) {
        if (StringUtils.isBlank(password)) {
            model.addAttribute("passwordMsg", "Password cannot be empty");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "Password cannot be empty");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        if (user.getPassword().equals(ForumUtils.md5Encryption(password + user.getSalt()))) {
            userService.updateUserPassword(user.getId(), ForumUtils.md5Encryption(newPassword + user.getSalt()));
            return "redirect:/index";
        } else {
            model.addAttribute("passwordMsg", "Wrong password");
            return "/site/setting";
        }
    }
}
