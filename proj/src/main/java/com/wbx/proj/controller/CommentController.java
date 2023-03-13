package com.wbx.proj.controller;

import com.wbx.proj.entity.Comment;
import com.wbx.proj.entity.DiscussPost;
import com.wbx.proj.entity.Event;
import com.wbx.proj.event.EventProducer;
import com.wbx.proj.service.CommentService;
import com.wbx.proj.service.DiscussPostService;
import com.wbx.proj.util.HostHolder;
import com.wbx.proj.util.ProjConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements ProjConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(value="/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost discussPost = discussPostService.selectDiscussPostsById(discussPostId);
            event.setEntityUserId(discussPost.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getTargetId());
        }
        eventProducer.sendEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
