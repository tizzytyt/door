package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackMessage {
    private Long id;
    private Long feedbackId;
    private Long senderUserId;
    /** student/admin/super_admin 等 */
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;

    // 列表展示用
    private String senderRealName;
}

