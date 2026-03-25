package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blacklist {
    private Long id;
    private Long userId;
    private String reason;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;

    // 辅助字段
    private String username;
    private String realName;
}
