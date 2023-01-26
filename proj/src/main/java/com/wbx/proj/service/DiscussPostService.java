package com.wbx.proj.service;

import com.wbx.proj.dao.DiscussPostMapper;
import com.wbx.proj.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int selectDiscussPostsRows(int userId) {
        return discussPostMapper.selectDiscussPostsRows(userId);
    }
}
