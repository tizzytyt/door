package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.User;
import com.access.control.service.UserProfileService;
import com.access.control.util.UserSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserProfileController extends BaseController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/profile")
    public Result<User> profile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }
        User user = userProfileService.getProfile(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(UserSanitizer.withoutPassword(user));
    }

    @PostMapping("/avatar")
    public Result<User> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }
        try {
            User user = userProfileService.updateAvatar(userId, file);
            return Result.success(UserSanitizer.withoutPassword(user));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("上传头像失败");
        }
    }
}
