package com.wbx.proj.dao;

import com.wbx.proj.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    public User selectById(int id);

    public User selectByName(String username);

    public User selectByEmail(String email);

    public int insertUser(User user);

    public int updateStatus(int id, int status);

    public int updateHeader(int id, String headerUrl);

    public int updatePassword(int id, String password);
}
