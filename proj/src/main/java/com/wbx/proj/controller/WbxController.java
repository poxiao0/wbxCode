package com.wbx.proj.controller;

import com.wbx.proj.service.WbxService;
import com.wbx.proj.service.WbxServiceImpl;
import com.wbx.proj.util.ProjUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/proj")
public class WbxController {

    @Autowired
    private WbxServiceImpl wbxService;

    // hello
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello spring boot!";
    }

    // controller、service和dao
    @RequestMapping("/find")
    @ResponseBody
    public String findHello() {
        return wbxService.findHello();
    }

    //获取request,返回reponse
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse reponse) throws IOException {
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+" : "+value);
        }
        System.out.println(request.getParameter("code"));

        reponse.setContentType("text/html;charset=utf-8");
        PrintWriter writer = reponse.getWriter();
        writer.write("<h1>wbx proj</h1>");
    }

    // get方法
    // /student?id=1&name=wbx
    @RequestMapping(path="/student",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(
            @RequestParam(name = "id", required = false, defaultValue = "1") int id,
            @RequestParam(name = "name", required = false, defaultValue = "hhh") String name
    ) {
        System.out.println(id);
        System.out.println(name);
        return "OK! student";
    }

    // /student/1/wbx
    @RequestMapping(path="/student1/{id}/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent1(@PathVariable("id") int id, @PathVariable("name") String name) {
        System.out.println(id);
        System.out.println(name);
        return "ok! student1";
    }

    // post
    @RequestMapping(path="/student2", method=RequestMethod.POST)
    @ResponseBody
    public String postStudent(int id, String name) {
        System.out.println(id);
        System.out.println(name);
        return "submit success!";
    }

    // 动态html
    // ModelAndView
    @RequestMapping(path="/teacher", method=RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "影");
        modelAndView.addObject("age", "25");
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    // model
    @RequestMapping(path="/teacher1", method = RequestMethod.GET)
    public String getTeacher1(Model model) {
        model.addAttribute("name", "雷电将军");
        model.addAttribute("age", "22");
        return "/demo/view";
    }

    // 异步通信 java对象 -> json流 -> js对象
    @RequestMapping(path="/teacher2",method=RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getTeacher2() {
        Map<String,Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "雷电真");
        map.put("location", "稻妻");
        return map;
    }

    // Cookie
    @RequestMapping(path="/cookie/set",method=RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("code", ProjUtil.generateUUID());
        cookie.setPath("/wbx/proj");
        cookie.setMaxAge(10 * 60);
        response.addCookie(cookie);
        return "set cookie ok!";
    }

    @RequestMapping(path="/cookie/get", method=RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String cookie) {
        System.out.println(cookie);
        return cookie;
    }

    // session
    @RequestMapping(path="/session/set",method=RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id",1);
        session.setAttribute("name","session1");
        return "set session ok!";
    }

    @RequestMapping(path="/session/get",method=RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session ok!";
    }

}
