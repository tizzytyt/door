package com.access.control.service;

import com.access.control.entity.User;

public interface UserService {
    /**
     * 用户登录
     * @param username
     * @param password
     * @param role
     * @return
     */
    User login(String username, String password, String role);
}
