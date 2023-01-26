package com.wbx.proj.service;

import com.wbx.proj.dao.UserMapper;
import com.wbx.proj.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById (int id) {
        return userMapper.selectById(id);
    }
}
