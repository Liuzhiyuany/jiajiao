package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Parent;
import com.example.jiajiao.entity.Teacher;
import com.example.jiajiao.security.AiRateLimiter;
import com.example.jiajiao.security.CurrentUser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private AiRateLimiter aiRateLimiter;

    public AiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 家长：大白话 → 家教需求
    @PostMapping("/parse-demand")
    public Parent parseDemand(@RequestParam String rawText) {
        aiRateLimiter.check(currentUser.id());   // 限流

        BeanOutputConverter<Parent> converter = new BeanOutputConverter<>(Parent.class);
        String systemPrompt = """
                你是一个专业的家教平台数据提取助手。
                请仔细阅读用户的文本，提取出关键信息，并严格按照指定的 JSON 格式输出。
                如果用户没有提到某些信息，请保留默认值。
                只输出 JSON，不要输出任何解释性的废话。
                """ + converter.getFormat();

        return chatClient.prompt()
                .system(systemPrompt)
                .user(rawText)
                .call()
                .entity(converter);
    }

    // 老师：大白话 → 老师资料
    @PostMapping("/parse-teacher")
    public Teacher parseTeacher(@RequestParam String rawText) {
        aiRateLimiter.check(currentUser.id());   // 限流

        BeanOutputConverter<Teacher> converter = new BeanOutputConverter<>(Teacher.class);
        String systemPrompt = """
                你是一个专业的家教平台资料整理助手，负责把老师的口语化自我介绍整理成结构化资料。
                请仔细阅读用户文本，提取关键信息，并严格按照指定的 JSON 格式输出。
                规则：
                1. subject 字段：提取能教的科目，多个科目用顿号分隔，例如「初中数学、高中物理」。
                2. grade 字段：提取能教的年级，可以用区间表达，例如「初一至初三」。
                3. pricePerHour 字段：只输出数字，例如 150。
                4. location 字段：提取所在区域或可上门区域。
                5. teachMode 字段：只能是「线上」「线下」「不限」三者之一。
                6. school 字段：提取就读学校，没有提到就留空。
                7. intro 字段：把用户提到的自身情况整理成一段通顺、专业、有亲和力的自我介绍，不超过80字。
                8. 用户没有提到的字段，保留默认值。
                只输出 JSON，不要输出任何解释性的废话。
                """ + converter.getFormat();

        return chatClient.prompt()
                .system(systemPrompt)
                .user(rawText)
                .call()
                .entity(converter);
    }
}