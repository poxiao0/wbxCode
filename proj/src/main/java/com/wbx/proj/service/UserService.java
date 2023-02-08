package com.wbx.proj.service;

import com.wbx.proj.dao.LoginTicketMapper;
import com.wbx.proj.dao.UserMapper;
import com.wbx.proj.entity.LoginTicket;
import com.wbx.proj.entity.User;
import com.wbx.proj.util.MailClient;
import com.wbx.proj.util.ProjConstant;
import com.wbx.proj.util.ProjUtil;
import io.micrometer.common.util.StringUtils;
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
public class UserService implements ProjConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("proj.path.domain")
    private String domain;

    @Value("server.servlet.context-path")
    private String contextPath;

    public User findUserById (int id) {
        return userMapper.selectById(id);
    }

    public Map<String,Object> Register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 验证不空
        if (user == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        // 验证已被注册
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg","账号已被使用！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg","邮箱已被使用！");
            return map;
        }

        // 添加用户
        user.setSalt(ProjUtil.generateUUID().substring(0, 5));
        user.setPassword(ProjUtil.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(ProjUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活账户
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = "http://127.0.0.1:8080/wbx" + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username, String password, int expiredSeconds) {
        HashMap<String,Object> map = new HashMap<>();

        // 验证为空
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg","用户名为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码为空！");
            return map;
        }

        // 验证账户
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg","用户不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg","用户未激活！");
            return map;
        }

        // 验证密码
        String pssd = ProjUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(pssd)) {
            map.put("passwordMsg","密码错误！");
            return map;
        }

        // 注册ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(ProjUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket selectByTicket (String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int id, String headerUrl) {
        return userMapper.updateHeader(id, headerUrl);
    }

    public Map<String, Object> updatePassword (int id, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectById(id);

        String s = ProjUtil.md5(oldPassword + user.getSalt());
        if (!s.equals(user.getPassword())) {
            map.put("oldPasswordMsg", "旧密码错误！");
            return map;
        }

        if (!newPassword.equals(confirmPassword)) {
            map.put("confirmPasswordMsg", "两次输入的密码不一致");
            return map;
        }

        String pssd = ProjUtil.md5(newPassword + user.getSalt());
        if (oldPassword.equals(pssd)) {
            map.put("newPasswordMsg", "新密码不能与旧密码相同!");
            return map;
        }

        userMapper.updatePassword(id, pssd);
        return map;
    }

}
