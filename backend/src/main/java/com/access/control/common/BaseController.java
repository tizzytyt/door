package com.access.control.common;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;

public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    /**
     * 获取当前登录用户的ID
     */
    protected Long getCurrentUserId() {
        Claims claims = (Claims) request.getAttribute("user_info");
        if (claims != null) {
            return Long.valueOf(claims.get("id").toString());
        }
        return null;
    }

    /**
     * 获取当前登录用户的角色
     */
    protected String getCurrentUserRole() {
        Claims claims = (Claims) request.getAttribute("user_info");
        if (claims != null) {
            return claims.get("role").toString();
        }
        return null;
    }
}
