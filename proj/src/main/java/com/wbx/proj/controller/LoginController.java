package com.wbx.proj.controller;


import com.google.code.kaptcha.Producer;
import com.wbx.proj.config.KaptchaConfig;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.MailClient;
import com.wbx.proj.util.ProjConstant;
import com.wbx.proj.util.ProjUtil;
import com.wbx.proj.util.RedisKeyUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// 使用jakarta系列包，使用javax报错500
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements ProjConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("server.servlet.context-path")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.Register(user);
        System.out.println(map);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，请点击激活!");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/register";
        }
    }

    // http://localhost:8080/wbx/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，账户创建完成!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "重复激活!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，请检查激活码!");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*,HttpSession session*/) {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存入session
        //session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = ProjUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将验证码发给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:"+e.getMessage());
        }
    }

    @RequestMapping(path="/login",method=RequestMethod.POST)
    public String login(Model model, HttpServletResponse response, /*HttpSession session,*/
                      String username, String password, String code, boolean rememberme,
                        @CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner) {
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }

        // 验证账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", (String)map.get("ticket"));
            cookie.setPath("context");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    @RequestMapping(path="/forget", method=RequestMethod.GET)
    public String getForgetPage () {
        return "site/forget";
    }
//    email参数没办法传进来  在页面通过js传参
//    @RequestMapping(path="/sendVerifyCode",method=RequestMethod.GET)
//    public void getKaptcha(HttpSession session, String email) {
//        String text = kaptchaProducer.createText();
//        session.setAttribute("resetCode", text);
//
//        // 发送验证码
//        Context context = new Context();
//        context.setVariable("email", email);
//        context.setVariable("code", text);
//        String content = templateEngine.process("/mail/forget", context);
//        mailClient.sendMail(email, "忘记密码邮件", content);
//    }
//
//    @RequestMapping(path="/forget", method = RequestMethod.POST)
//    public String forgetPassword(HttpSession session, Model model, String email, String verifyCode, String newPassword) {
//        if (StringUtils.isBlank(email)) {
//            model.addAttribute("emailMsg", "邮箱不能为空！");
//            return "site/forget";
//        }
//
//        String resetCode = (String) session.getAttribute("resetCode");
//        if (StringUtils.isBlank(resetCode) || StringUtils.isBlank(verifyCode) || !resetCode.equalsIgnoreCase(verifyCode)) {
//            model.addAttribute("codeMsg", "验证码不正确！");
//            return "site/forget";
//        }
//
//        Map<String, Object> map = userService.forget(email, newPassword);
//        if (map.containsKey("emailMsg")) {
//            model.addAttribute("emailMsg", map.get("emailMsg"));
//            return "site/forget";
//        }
//        if (map.containsKey("passwordMsg")) {
//            model.addAttribute("passwordMsg", map.get("passwordMsg"));
//            return "site/forget";
//        }
//
//        return "redirect:/login";
//    }

    // 获取验证码
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        if (StringUtils.isBlank(email)) {
            return ProjUtil.getJSONString(1, "邮箱不能为空！");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = ProjUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码
        session.setAttribute("verifyCode", code);

        return ProjUtil.getJSONString(0);
    }

    // 重置密码
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

    @RequestMapping(path="/logout",method=RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
        // 重定向是浏览器的再次请求(默认get)，跳转是浏览器的刷新页面
    }


}
