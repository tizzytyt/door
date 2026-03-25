package com.access.control.controller;

import com.access.control.common.Result;
import com.access.control.entity.User;
import com.access.control.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, Object> params) {
        String username = params.get("username") == null ? "" : params.get("username").toString().trim();
        String password = params.get("password") == null ? "" : params.get("password").toString();
        String realName = params.get("realName") == null ? "" : params.get("realName").toString().trim();
        String phone = params.get("phone") == null ? "" : params.get("phone").toString().trim();
        String role = params.get("role") == null ? "" : params.get("role").toString().trim();

        if (username.isEmpty()) return Result.error("账号不能为空");
        if (password.isEmpty()) return Result.error("密码不能为空");
        if (password.length() < 6) return Result.error("密码至少6位");
        if (realName.isEmpty()) return Result.error("真实姓名不能为空");

        // 允许注册身份：student/admin/super_admin
        if (role.isEmpty()) {
            role = "student";
        }
        if (!("student".equals(role) || "admin".equals(role) || "super_admin".equals(role))) {
            return Result.error("无效的身份类型");
        }

        User exist = userMapper.getByUsername(username);
        if (exist != null) {
            return Result.error("该账号已存在");
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRealName(realName);
        u.setRole(role);
        u.setPhone(phone);
        u.setAvatar(null);
        u.setStatus(1);

        try {
            int ok = userMapper.insert(u);
            return ok > 0 ? Result.success() : Result.error("注册失败");
        } catch (Exception e) {
            return Result.error("注册失败，账号可能已存在");
        }
    }
}

