package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    private Long id;
    private Long userId;
    private Long deviceId;
    private LocalDateTime createdAt;

    // 辅助字段
    private String deviceName;
    private String location;
}
