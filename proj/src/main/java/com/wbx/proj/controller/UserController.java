package com.wbx.proj.controller;

import com.wbx.proj.annotation.LoginRequired;
import com.wbx.proj.entity.User;
import com.wbx.proj.service.FollowService;
import com.wbx.proj.service.LikeService;
import com.wbx.proj.service.UserService;
import com.wbx.proj.util.HostHolder;
import com.wbx.proj.util.ProjConstant;
import com.wbx.proj.util.ProjUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements ProjConstant{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${proj.path.domain}")
    private String domain;

    @Value("${proj.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path="/setting", method = RequestMethod.GET)
    public String setUser() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headImg, Model model) {
        if (headImg == null) {
            model.addAttribute("error", "图片输入为空！");
            return "site/setting";
        }

        String fileName = headImg.getOriginalFilename();
        String suffix = fileName.substring(fileName.indexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "图片格式不对！");
            return "site/setting";
        }

        fileName = ProjUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" +fileName);
        try {
            headImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("图片保存失败: " + e.getMessage());
            throw new RuntimeException("图片上传失败",e);
        }

        // 服务器地址
        // http://localhost:8080/wbx/user/header/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    @RequestMapping(path="/header/{headImg}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("headImg") String headImg, HttpServletResponse response) {
        headImg = uploadPath + "/" + headImg;
        String suffix = headImg.substring(headImg.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(headImg);
                OutputStream os = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.debug("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path="/resetting",method=RequestMethod.POST)
    public String resetPassword(Model model, String oldPassword, String newPassword, String confirmPassword) {
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword, confirmPassword);
        if (map.get("oldPasswordMsg") != null) {
            model.addAttribute("oldError", map.get("oldPasswordMsg"));
            return "site/setting";
        }
        if (map.get("newPasswordMsg") != null) {
            model.addAttribute("newError", map.get("newPasswordMsg"));
            return "site/setting";
        }
        if (map.get("confirmPasswordMsg") != null) {
            model.addAttribute("conError", map.get("confirmPasswordMsg"));
            return "site/setting";
        }
        return "redirect:/index";
    }

    @RequestMapping(path="/profile/{userId}",method=RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        // 点赞
        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已经关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "site/profile";
    }

}
