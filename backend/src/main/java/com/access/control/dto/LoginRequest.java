package com.access.control.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String role;
    private String captchaKey;
    private String captchaCode;
}
