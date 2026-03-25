package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessRecord {
    private Long id;
    private Long userId;
    private Long deviceId;
    private LocalDateTime accessTime;
    private Integer type; // 1-进入, 2-离开
    private Long reservationId;
    private Integer status; // 1-正常, 0-异常

    // 辅助展示字段
    private String deviceName;
    private String location;
}
