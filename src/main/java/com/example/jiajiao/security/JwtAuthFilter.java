package com.example.jiajiao.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析 Authorization: Bearer <token>
 * 兼容模式：没带 token 也放行（不破坏现有功能），带了就校验并注入 userId
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        // 没带 token 直接拒绝
        if (auth == null || !auth.startsWith("Bearer ")) {
            reject(response, "请先登录");
            return;
        }

        try {
            Claims claims = jwtUtil.parse(auth.substring(7));
            request.setAttribute("tokenUserId", Long.valueOf(claims.getSubject()));
            request.setAttribute("tokenRole", claims.get("role"));
        } catch (Exception e) {
            reject(response, "登录已过期，请重新登录");
            return;
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 登录注册、WebSocket 握手、静态页面都不拦
        return uri.startsWith("/api/auth") || uri.startsWith("/ws") || !uri.startsWith("/api");
    }
}