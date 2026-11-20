package com.example.jiajiao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理：所有没被捕获的异常，统一返回 {success:false, message:"..."}
 * 好处：前端永远拿到结构化 JSON，不会出现 500 白页
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 我们自己主动抛的业务校验错误
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        return result;
    }

    // 参数类型不对（比如 id 传了 null 或字母）
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> handleTypeMismatch(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "参数格式不正确");
        return result;
    }

    // 兜底：任何没预料到的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> handleAll(Exception e) {
        e.printStackTrace();   // 控制台保留完整堆栈，方便你自己排查
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "服务器开小差了，请稍后再试");
        return result;
    }
}