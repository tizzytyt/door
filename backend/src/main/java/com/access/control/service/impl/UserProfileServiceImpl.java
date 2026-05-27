package com.access.control.service.impl;

import com.access.control.config.UploadStorageProperties;
import com.access.control.entity.User;
import com.access.control.mapper.UserMapper;
import com.access.control.service.AvatarFileResolver;
import com.access.control.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final long MAX_BYTES = 2 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UploadStorageProperties storage;
    @Autowired
    private AvatarFileResolver avatarFileResolver;

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.getById(userId);
        enrichAvatar(user);
        return user;
    }

    private void enrichAvatar(User user) {
        if (user == null) {
            return;
        }
        String display = avatarFileResolver.resolveDisplayAvatar(user);
        if (display != null) {
            user.setAvatar(display);
        }
    }

    @Override
    @Transactional
    public User updateAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("图片不能超过 2MB");
        }

        String ext = resolveExt(file.getOriginalFilename(), file.getContentType());
        if (ext == null) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp 格式");
        }

        User user = userMapper.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String filename = userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = storage.resolveAvatarFile(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("保存头像失败", e);
        }

        deleteOldAvatarFile(user.getAvatar());

        String avatarUrl = UploadStorageProperties.AVATAR_URL_PREFIX + filename;
        userMapper.updateAvatar(userId, avatarUrl);
        User updated = userMapper.getById(userId);
        enrichAvatar(updated);
        return updated;
    }

    private void deleteOldAvatarFile(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith(UploadStorageProperties.AVATAR_URL_PREFIX)) {
            return;
        }
        String name = avatarUrl.substring(UploadStorageProperties.AVATAR_URL_PREFIX.length());
        if (name.contains("..") || name.contains("/")) {
            return;
        }
        Path old = storage.resolveAvatarFile(name);
        if (!storage.isUnderAvatarsDir(old)) {
            return;
        }
        try {
            Files.deleteIfExists(old);
        } catch (IOException ignored) {
        }
    }

    private String resolveExt(String originalFilename, String contentType) {
        String fromName = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fromName = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (ALLOWED_EXT.contains(fromName)) {
            return "jpeg".equals(fromName) ? "jpg" : fromName;
        }
        if (contentType != null) {
            if (contentType.contains("jpeg")) return "jpg";
            if (contentType.contains("png")) return "png";
            if (contentType.contains("webp")) return "webp";
        }
        return null;
    }
}
