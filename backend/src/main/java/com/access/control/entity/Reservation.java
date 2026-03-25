package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long id;
    private Long userId;
    private Long deviceId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝, 3-已使用, 4-已取消, 5-已失效
    private String auditOpinion;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 辅助字段 (用于前端展示)
    private String deviceName;
    private String realName;
}
