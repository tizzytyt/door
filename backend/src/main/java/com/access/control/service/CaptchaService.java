package com.access.control.service;

import java.util.Map;

public interface CaptchaService {

    /**
     * 生成验证码，返回 captchaKey 与 Base64 图片（data:image/png;base64,...）
     */
    Map<String, String> createCaptcha();

    /**
     * 校验验证码（一次性，校验后无论成败均失效该 key）
     */
    boolean verify(String captchaKey, String captchaCode);
}
