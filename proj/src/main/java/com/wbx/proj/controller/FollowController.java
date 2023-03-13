package com.wbx.proj.controller;

import com.wbx.proj.entity.Event;
import com.wbx.proj.entity.Page;
import com.wbx.proj.entity.User;
import com.wbx.proj.event.EventProducer;
import com.wbx.proj.service.FollowService;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.HostHolder;
import com.wbx.proj.util.ProjConstant;
import com.wbx.proj.util.ProjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements ProjConstant{

    @Autowired
    private HostHolder hostholder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path="/follow/{isFollow}",method= RequestMethod.POST)
    @ResponseBody
    public String follow(@PathVariable("isFollow") int isFollow, int entityType, int entityId) {
        User user = hostholder.getUser();

        followService.follow(isFollow, user.getId(), entityType, entityId);
        String msg = isFollow == 0 ? "已关注" : "已取消关注";

        // 触发关注事件
        if (isFollow == 0) {
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
            eventProducer.sendEvent(event);
        }

        return ProjUtil.getJSONString(0, msg);
    }

    @RequestMapping(path="/followees/{userId}",method=RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String,Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("userList", userList);

        return "/site/followee";
    }

    public boolean hasFollowed(int userId) {
        if (hostholder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostholder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

    @RequestMapping(path="/followers/{userId}",method=RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String,Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("userList", userList);

        return "/site/follower";
    }

}
