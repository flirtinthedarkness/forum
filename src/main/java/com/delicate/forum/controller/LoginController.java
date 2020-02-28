package com.delicate.forum.controller;

import com.delicate.forum.entity.User;
import com.delicate.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
@RequestMapping("/account")
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> returnMap = userService.register(user);
        if (returnMap == null || returnMap.isEmpty()) {
            model.addAttribute("msg", "Register Succeeded, activation email sent. Activate ASAP.");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", returnMap.get("usernameMsg"));
            model.addAttribute("emailMsg", returnMap.get("emailMsg"));
            model.addAttribute("passwordMsg", returnMap.get("passwordMsg"));
            return "/site/register";
        }
    }
}
