package com.wbx.proj.controller;

import com.wbx.proj.entity.DiscussPost;
import com.wbx.proj.entity.Page;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.DiscussPostService;
import com.wbx.proj.service.LikeService;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.ProjConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class HomeController implements ProjConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/index", method = RequestMethod.GET)
    public String getIndexPage (Model model, Page page) {
        page.setPath("/index");
        page.setRows(discussPostService.selectDiscussPostsRows(0));

        List<HashMap<String,Object>> discussPosts = new ArrayList<>();
        List<DiscussPost> posts = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        for (DiscussPost post : posts) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likeCount", likeCount);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @RequestMapping(path="/error",method=RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

}
