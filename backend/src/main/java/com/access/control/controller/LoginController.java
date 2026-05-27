package com.access.control.controller;

import com.access.control.common.JwtUtils;
import com.access.control.common.Result;
import com.access.control.dto.LoginRequest;
import com.access.control.entity.User;
import com.access.control.service.AvatarFileResolver;
import com.access.control.service.CaptchaService;
import com.access.control.service.UserService;
import com.access.control.util.UserSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private AvatarFileResolver avatarFileResolver;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest req) {
        if (req == null) {
            return Result.error("参数错误");
        }
        if (!captchaService.verify(req.getCaptchaKey(), req.getCaptchaCode())) {
            return Result.error("验证码错误或已过期");
        }

        log.info("用户登录: {}, 角色: {}", req.getUsername(), req.getRole());
        User u = userService.login(req.getUsername(), req.getPassword(), req.getRole());

        // 登录成功, 生成令牌, 下发令牌
        if (u != null) {
            String avatar = avatarFileResolver.resolveDisplayAvatar(u);
            if (avatar != null) {
                u.setAvatar(avatar);
            }
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", u.getId());
            claims.put("username", u.getUsername());
            claims.put("role", u.getRole());
            String jwt = JwtUtils.generateJwt(claims);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("user", UserSanitizer.withoutPassword(u));
            return Result.success(data);
        }

        // 登录失败, 返回错误信息
        return Result.error("用户名或密码错误，或账号已被禁用");
    }
}
