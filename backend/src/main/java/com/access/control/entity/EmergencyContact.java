package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    /** 关系：父母/配偶/亲友/其他 */
    private String relation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
