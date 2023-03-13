package com.wbx.proj.controller.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.Handle;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class WbxInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WbxInterceptor.class);

    // Controller之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,  Object handle) throws Exception {
        logger.debug("preHandle" + handle.toString());
        return true;
    }

    // Controller之后
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handle, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle" + handle.toString());
    }

    // TemplateEngine之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handle, Exception ex) throws Exception {
        logger.debug("afterCompletion" + handle.toString());
    }


}
