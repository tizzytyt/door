package com.access.control.service;

import com.access.control.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    User getProfile(Long userId);

    User updateAvatar(Long userId, MultipartFile file);
}
