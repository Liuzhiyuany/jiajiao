package com.example.jiajiao.security;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的 AI 接口限流：每个账号每分钟最多 LIMIT 次
 * 内存实现，重启清零；生产环境可换成 Redis 计数
 */
@Component
public class AiRateLimiter {

    private static final int LIMIT = 10;              // 每分钟最多 10 次
    private static final long WINDOW_MS = 60_000L;    // 窗口 1 分钟

    private final Map<Long, Deque<Long>> records = new ConcurrentHashMap<>();

    public void check(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        long now = System.currentTimeMillis();
        Deque<Long> queue = records.computeIfAbsent(userId, k -> new ArrayDeque<>());

        synchronized (queue) {
            // 扔掉一分钟之前的记录
            while (!queue.isEmpty() && now - queue.peekFirst() > WINDOW_MS) {
                queue.pollFirst();
            }
            if (queue.size() >= LIMIT) {
                throw new IllegalArgumentException("AI 使用太频繁了，请稍后再试（每分钟最多 " + LIMIT + " 次）");
            }
            queue.addLast(now);
        }
    }
}