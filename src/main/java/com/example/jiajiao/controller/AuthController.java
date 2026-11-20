package com.example.jiajiao.controller;

import com.example.jiajiao.entity.User;
import com.example.jiajiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private com.example.jiajiao.security.JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.put("success", false);
            result.put("message", "用户名已被注册！");
            return result;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 密码加密
        userRepository.save(user);
        result.put("success", true);
        result.put("message", "注册成功，请登录！");
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        User dbUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (dbUser == null) {
            result.put("success", false);
            result.put("message", "用户不存在！");
            return result;
        }
        // 密码比对
        if (passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            result.put("success", true);
            result.put("token", jwtUtil.generate(dbUser.getId(), dbUser.getUsername(), dbUser.getRole()));
            result.put("message", "登录成功！");
            result.put("role", dbUser.getRole());
            result.put("userId", dbUser.getId());
            result.put("username", dbUser.getUsername());
        } else {
            result.put("success", false);
            result.put("message", "密码错误！");
        }
        return result;
    }
}