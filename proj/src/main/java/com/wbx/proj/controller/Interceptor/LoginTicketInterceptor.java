package com.wbx.proj.controller.Interceptor;

import com.wbx.proj.entity.LoginTicket;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.CookieUtil;
import com.wbx.proj.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) throws Exception{
        String ticket = CookieUtil.getTicket(request, "ticket");

        if (ticket != null) {
            LoginTicket loginTicket = userService.selectByTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                // ThreadLocal多线程隔离User对象
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handle, ModelAndView modelAndView) throws Exception{
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handle, Exception ex) {
        hostHolder.clear();
    }
}
