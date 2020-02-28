package com.delicate.forum.controller;

import com.delicate.forum.entity.User;
import com.delicate.forum.service.UserService;
import com.delicate.forum.util.ForumConstant;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
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

    @RequestMapping(path = "/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activateAccount(Model model,
                                  @PathVariable("userId") int userId,
                                  @PathVariable("activationCode") String activationCode) {
        int result = userService.activateAccount(userId, activationCode);
        if (result == ForumConstant.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "Your account has been activated successfully!");
            model.addAttribute("target", "/login");
        } else if (result == ForumConstant.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "Account was activated before.");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "Activation failed, wrong activation code.");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // generate kaptcha verification code
        String codeText = kaptchaProducer.createText();
        BufferedImage codeImage = kaptchaProducer.createImage(codeText);

        session.setAttribute("kaptcha", codeText);

        response.setContentType("image/png");
        try {
            OutputStream os =  response.getOutputStream();
            ImageIO.write(codeImage, "png", os);
        } catch (IOException e) {
            logger.error("Response verification code failed: " + e.getMessage());
        }

    }
}
