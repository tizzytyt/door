package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.User;
import com.access.control.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student/user")
public class StudentUserController extends BaseController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, Object> params) {
        if (!"student".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }

        String oldPassword = params.get("oldPassword") == null ? "" : params.get("oldPassword").toString();
        String newPassword = params.get("newPassword") == null ? "" : params.get("newPassword").toString();

        oldPassword = oldPassword.trim();
        newPassword = newPassword.trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            return Result.error("旧密码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }
        if (oldPassword.equals(newPassword)) {
            return Result.error("新密码不能与旧密码相同");
        }

        User u = userMapper.getById(userId);
        if (u == null) {
            return Result.error("用户不存在");
        }
        if (!oldPassword.equals(u.getPassword())) {
            return Result.error("旧密码不正确");
        }

        boolean ok = userMapper.updatePassword(userId, newPassword) > 0;
        return ok ? Result.success() : Result.error("修改密码失败");
    }
}

