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
public class VisitorReservation {
    private Long id;
    private Long userId;
    private String visitorName;
    private String visitorPhone;
    private String visitReason;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝, 3-已取消
    private String auditOpinion;
    private LocalDateTime createdAt;
}
