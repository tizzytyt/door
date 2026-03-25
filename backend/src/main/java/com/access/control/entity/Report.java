package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private Long userId;
    private Integer type; // 1-晚归, 2-临时外出
    private LocalDateTime expectedTime;
    private String reason;
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝, 3-已撤销
    private String auditOpinion;
    private LocalDateTime createdAt;
}
