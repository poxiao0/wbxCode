package com.wbx.proj.controller;

import com.alibaba.fastjson.JSONObject;
import com.wbx.proj.entity.Message;
import com.wbx.proj.entity.Page;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.MessageService;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.HostHolder;
import com.wbx.proj.util.ProjConstant;
import com.wbx.proj.util.ProjUtil;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController implements ProjConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/conversationList",method = RequestMethod.GET)
    public String getConversations(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页
        page.setLimit(5);
        page.setPath("/message/conversationList");
        page.setRows(messageService.selectConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationList = messageService.selectConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (list != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadFrom", messageService.selectLetterUnreadCount(
                        user.getId(), message.getConversationId()));

                int fromId = user.getId() == message.getToId() ? message.getFromId() : message.getToId();
                map.put("fromId", fromId);
                User userFrom = userService.findUserById(fromId);
                map.put("imgUserFrom", userFrom.getHeaderUrl());
                map.put("usernameFrom", userFrom.getUsername());

                list.add(map);
            }
        }
        model.addAttribute("conversations", list);

        // 所有未读消息
        int letterUnreadCount =  messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("unreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnread", noticeUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path="/detail/{conversationId}",method = RequestMethod.GET)
    public String getConversations(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        // 分页
        page.setLimit(5);
        page.setPath("/message/detail/" + conversationId);
        page.setRows(messageService.selectLetterCount(conversationId));

        // 消息列表
        List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                User user = userService.findUserById(letter.getFromId());
                map.put("user", user);

                list.add(map);
            }
        }
        model.addAttribute("letters", list);

        // 确定发送消息方的名字
        User user = hostHolder.getUser();
        String[] ss = conversationId.split("_");
        int a = Integer.parseInt(ss[0]);
        int b = Integer.parseInt(ss[1]);
        int userIdFrom = user.getId() == a ? b : a;
        String usernameFrom = userService.findUserById(userIdFrom).getUsername();
        model.addAttribute("usernameFrom", usernameFrom);

        // 未读消息标记已读
        List<Integer> unreadList = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (message.getToId() == user.getId() && message.getStatus() == 0) {
                    unreadList.add(message.getId());
                }
            }
        }

        if (!unreadList.isEmpty()) {
            messageService.updateMessage(unreadList);
        }

        return "/site/letter-detail";
    }

    @RequestMapping(path="/add",method = RequestMethod.POST)
    @ResponseBody
    public String addLetter(String usernameTo, String content) {
        User target = userService.findUserByName(usernameTo);
        if (target == null) {
            return ProjUtil.getJSONString(1,"目标用户不存在");
        }
        // 添加信息设置
        User user = hostHolder.getUser();
        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        String conversationId = user.getId() > target.getId() ? target.getId() + "_" + user.getId() :
                user.getId() + "_" + target.getId();
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.insertLetter(message);

        return ProjUtil.getJSONString(0);
    }

    @RequestMapping(path="/notice",method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.selectLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> commentMap = new HashMap<>();
        if (message != null) {
            commentMap.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            commentMap.put("user", userService.findUserById((Integer)data.get("userId")));
            commentMap.put("entityType", data.get("entityType"));
            commentMap.put("entityId", data.get("entityId"));
            commentMap.put("postId", data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(),TOPIC_COMMENT);
            commentMap.put("count", count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            commentMap.put("unread", unread);
            model.addAttribute("commentNotice", commentMap);
        }

        // 查询点赞类通知
        Message message1 = messageService.selectLatestNotice(user.getId(), TOPIC_LIKE);
        Map<String, Object> likeMap = new HashMap<>();
        if (message1 != null) {
            likeMap.put("message", message1);

            String content = HtmlUtils.htmlUnescape(message1.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            likeMap.put("user", userService.findUserById((Integer)data.get("userId")));
            likeMap.put("entityType", data.get("entityType"));
            likeMap.put("entityId", data.get("entityId"));
            likeMap.put("postId", data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(),TOPIC_LIKE);
            likeMap.put("count", count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeMap.put("unread", unread);
            model.addAttribute("likeNotice", likeMap);
        }


        // 查询关注类通知
        Message message2 = messageService.selectLatestNotice(user.getId(), TOPIC_FOLLOW);
        Map<String, Object> followMap = new HashMap<>();
        if (message2 != null) {
            followMap.put("message", message2);

            String content = HtmlUtils.htmlUnescape(message2.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            followMap.put("user", userService.findUserById((Integer)data.get("userId")));
            followMap.put("entityType", data.get("entityType"));
            followMap.put("entityId", data.get("entityId"));

            int count = messageService.selectNoticeCount(user.getId(),TOPIC_FOLLOW);
            followMap.put("count", count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followMap.put("unread", unread);
            model.addAttribute("followNotice", followMap);
        }


        // 查询消息数
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnread", letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnread", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path="/notice/detail/{topic}",method=RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/message/notice/detail/" + topic);
        page.setRows(messageService.selectNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.selectNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                list.add(map);
            }
        }
        model.addAttribute("notices", list);

        // 设置已读
        List<Integer> unreadList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                if (notice.getToId() == user.getId() && notice.getStatus() == 0) {
                    unreadList.add(notice.getId());
                }
            }
        }
        if (!unreadList.isEmpty()) {
            messageService.updateMessage(unreadList);
        }

        return "/site/notice-detail";
    }



}
