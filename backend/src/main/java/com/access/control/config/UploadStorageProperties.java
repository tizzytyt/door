package com.access.control.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 头像等上传文件的本地持久化目录。
 * 配置项 app.upload-dir 支持绝对路径或相对路径（相对路径以 JVM 启动时的工作目录为基准）。
 */
@Slf4j
@Component
@Getter
public class UploadStorageProperties {

    public static final String AVATAR_URL_PREFIX = "/uploads/avatars/";

    @Value("${app.upload-dir:../data/uploads}")
    private String uploadDir;

    /** 解析后的上传根目录（绝对路径） */
    private Path uploadRoot;

    /** 头像子目录：{uploadRoot}/avatars */
    private Path avatarsDir;

    @PostConstruct
    public void init() throws IOException {
        uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        avatarsDir = uploadRoot.resolve("avatars");
        Files.createDirectories(avatarsDir);
        log.info("头像持久化目录: {}", avatarsDir);
        log.info("HTTP 访问前缀: {}{}", "http://localhost:8080", AVATAR_URL_PREFIX);
    }

    public Path resolveAvatarFile(String filename) {
        return avatarsDir.resolve(filename).normalize();
    }

    public boolean isUnderAvatarsDir(Path file) {
        return file.startsWith(avatarsDir);
    }
}
