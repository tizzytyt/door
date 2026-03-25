package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role; // student, admin, super_admin
    private String phone;
    private String avatar;
    private Integer status; // 1-正常, 0-禁用/黑名单
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
