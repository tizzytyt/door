package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    private Long id;
    private Long userId;
    /** 报修时关联的门禁；建议等可为空 */
    private Long deviceId;
    private Integer type; // 1-报修, 2-投诉, 3-建议
    private String content;
    private String images; // 图片URL列表 (JSON或逗号分隔)
    private Integer status; // 0-待处理, 1-处理中, 2-已完成
    private String adminReply;
    private LocalDateTime createdAt;

    /** 列表展示：关联 device.name */
    private String deviceName;
}
