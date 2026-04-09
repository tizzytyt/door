package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {
    private Long id;
    private Long announcementId;
    private Long userId;
    private LocalDateTime readAt;

    /** 管理端明细展示：关联 user 表 */
    private String username;
    private String realName;
}

