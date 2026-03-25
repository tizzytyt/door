package com.access.control.config;

import com.access.control.common.JwtUtils;
import com.access.control.common.Result;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求url
        String url = request.getRequestURL().toString();
        log.info("请求的url: {}", url);

        // 2. 判断请求url中是否包含login或register，如果包含，说明是登录/注册操作，放行
        if (url.contains("login") || url.contains("register")) {
            log.info("登录操作，放行...");
            return true;
        }

        // 3. 获取请求头中的令牌（token）
        String jwt = request.getHeader("token");

        // 4. 判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(jwt)) {
            log.info("请求头token为空,返回未登录的信息");
            Result<?> error = Result.error(401, "NOT_LOGIN");
            // 手动转换 对象--json --------> fastjson
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }

        // 5. 解析token，如果解析失败，返回错误结果（未登录）
        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            log.info("令牌解析成功，放行...");
            // 将用户信息存入 request 中方便后续获取
            request.setAttribute("user_info", claims);
        } catch (Exception e) { // 解析失败
            e.printStackTrace();
            log.info("解析令牌失败, 返回未登录错误信息");
            Result<?> error = Result.error(401, "NOT_LOGIN");
            // 手动转换 对象--json --------> fastjson
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }

        // 6. 放行
        return true;
    }
}
