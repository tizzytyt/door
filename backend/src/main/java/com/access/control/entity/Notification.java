package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer isRead;
    /** 1-预约提醒, 2-审核结果, 4-管理员新报修（系统公告走 announcement 表） */
    private Integer type;
    private LocalDateTime createdAt;

    /** 前端展示 */
    private String createdAtText;
    private String typeText;
}
