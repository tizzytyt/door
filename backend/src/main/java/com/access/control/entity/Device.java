package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private Long id;
    private String name;
    private String location;
    private Integer status; // 1-正常, 2-维护中, 0-故障
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
