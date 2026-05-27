package com.access.control.service.impl;

import com.access.control.service.CaptchaService;
import com.access.control.util.CaptchaImageUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final long TTL_MS = 5 * 60 * 1000L;
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LEN = 4;

    private final ConcurrentHashMap<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> createCaptcha() {
        cleanupExpired();
        String code = randomCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        store.put(key, new CaptchaEntry(code, System.currentTimeMillis() + TTL_MS));

        String base64 = CaptchaImageUtil.toBase64Png(code);
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", "data:image/png;base64," + base64);
        return result;
    }

    @Override
    public boolean verify(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.trim().isEmpty()) {
            return false;
        }
        if (captchaCode == null || captchaCode.trim().isEmpty()) {
            return false;
        }
        CaptchaEntry entry = store.remove(captchaKey.trim());
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            return false;
        }
        return entry.code.equalsIgnoreCase(captchaCode.trim());
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LEN);
        for (int i = 0; i < CODE_LEN; i++) {
            sb.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
        }
        return sb.toString();
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expireAt < now);
    }

    private static final class CaptchaEntry {
        final String code;
        final long expireAt;

        CaptchaEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
