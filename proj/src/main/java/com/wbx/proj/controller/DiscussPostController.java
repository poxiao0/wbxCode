package com.wbx.proj.controller;

import com.wbx.proj.entity.Comment;
import com.wbx.proj.entity.DiscussPost;
import com.wbx.proj.entity.Page;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.CommentService;
import com.wbx.proj.service.DiscussPostService;
import com.wbx.proj.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements ProjConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return ProjUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的情况,将来统一处理.
        return ProjUtil.getJSONString(0, "发布成功!");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.selectDiscussPostsById(discussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        // 帖子评论
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST,
                post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentPostList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentPost = new HashMap<>();
                commentPost.put("comment", comment);
                commentPost.put("user", userService.findUserById(comment.getUserId()));
                long likeCount1 = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentPost.put("likeCount", likeCount1);
                int likeStatus1 = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentPost.put("likeStatus", likeStatus1);

                // 评论回复
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> commentReplyList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> commentReply = new HashMap<>();
                        commentReply.put("reply", reply);
                        commentReply.put("user", userService.findUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ?
                                null : userService.findUserById(reply.getTargetId());
                        commentReply.put("target", target);
                        long likeCount2 = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        commentReply.put("likeCount", likeCount2);
                        int likeStatus2 = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        commentReply.put("likeStatus", likeStatus2);

                        commentReplyList.add(commentReply);
                    }
                }
                commentPost.put("replys", commentReplyList);

                // 回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentPost.put("replyCount", replyCount);

                commentPostList.add(commentPost);
            }
        }

        model.addAttribute("comments", commentPostList);
        return "/site/discuss-detail";
    }

}
