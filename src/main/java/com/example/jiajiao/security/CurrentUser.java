package com.example.jiajiao.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从当前请求里取出 JwtAuthFilter 解析出来的登录用户 ID
 * 有 token 时返回真实 userId；没 token 时返回 null
 */
@Component
public class CurrentUser {

    public Long id() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        Object v = attrs.getRequest().getAttribute("tokenUserId");
        return v == null ? null : (Long) v;
    }
}