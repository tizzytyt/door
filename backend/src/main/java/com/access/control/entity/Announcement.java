package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 管理端展示：发布人信息 */
    private String publisherName;

    /** 管理端展示：已读/未读统计 */
    private Long readCount;
    private Long totalStudents;
    private Long unreadCount;

    /** 学生端展示：当前用户是否已读 */
    private Integer isRead; // 0/1
    private LocalDateTime readAt;
}

