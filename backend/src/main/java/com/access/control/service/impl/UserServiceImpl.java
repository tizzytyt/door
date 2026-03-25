package com.access.control.service.impl;

import com.access.control.entity.User;
import com.access.control.mapper.UserMapper;
import com.access.control.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password, String role) {
        User user = userMapper.getByUsername(username);

        if (user == null) {
            log.info("登录失败: 用户不存在 - username: {}", username);
            return null;
        }

        if (!user.getPassword().equals(password)) {
            log.info("登录失败: 密码错误 - username: {}, inputPwd: {}, dbPwd: {}", username, password, user.getPassword());
            return null;
        }

        if (user.getStatus() != 1) {
            log.info("登录失败: 账号状态异常 - username: {}, status: {}", username, user.getStatus());
            return null;
        }

        if (!user.getRole().equals(role)) {
            log.info("登录失败: 角色不匹配 - username: {}, inputRole: {}, dbRole: {}", username, role, user.getRole());
            return null;
        }

        return user;
    }
}
