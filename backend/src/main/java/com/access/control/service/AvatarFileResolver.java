package com.access.control.service;

import com.access.control.config.UploadStorageProperties;
import com.access.control.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 从本地 avatars 目录解析头像 URL（数据库无记录时按约定文件名查找）。
 */
@Component
public class AvatarFileResolver {

    private static final List<String> EXT_ORDER = Arrays.asList("jpg", "jpeg", "png", "webp");

    @Autowired
    private UploadStorageProperties storage;

    /**
     * 返回应展示的头像 URL：优先数据库；否则在 avatars 目录查找 {userId}.{ext} 或 {username}.{ext}
     */
    public String resolveDisplayAvatar(User user) {
        if (user == null) {
            return null;
        }
        if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            return user.getAvatar().trim();
        }
        String fromDisk = findAvatarOnDisk(user.getId(), user.getUsername());
        return fromDisk;
    }

    private String findAvatarOnDisk(Long userId, String username) {
        if (userId != null) {
            String url = tryNames(String.valueOf(userId));
            if (url != null) {
                return url;
            }
        }
        if (username != null && !username.trim().isEmpty()) {
            return tryNames(username.trim());
        }
        return null;
    }

    private String tryNames(String baseName) {
        for (String ext : EXT_ORDER) {
            Path file = storage.resolveAvatarFile(baseName + "." + ext);
            if (Files.isRegularFile(file)) {
                return UploadStorageProperties.AVATAR_URL_PREFIX + baseName + "." + ext;
            }
        }
        return null;
    }
}
