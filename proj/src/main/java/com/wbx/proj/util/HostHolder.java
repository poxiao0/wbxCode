package com.wbx.proj.util;

import com.wbx.proj.entity.User;
import org.springframework.stereotype.Component;

/**
 *  服务器端存储User,代替Session
 */
@Component
public class HostHolder {

    private ThreadLocal<User> threadlocal = new ThreadLocal<>();

    public void setUser(User user) {
        threadlocal.set(user);
    }

    public User getUser() {
        return threadlocal.get();
    }

    public void clear() {
        threadlocal.remove();
    }
}
