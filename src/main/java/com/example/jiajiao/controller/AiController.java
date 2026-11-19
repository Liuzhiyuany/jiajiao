package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Parent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/parse-demand")
    public Parent parseDemand(@RequestParam String rawText) {
        // 告诉 AI：请把提取出来的信息，变成 Parent 这个类的格式
        BeanOutputConverter<Parent> converter = new BeanOutputConverter<>(Parent.class);

        // 给 AI 设定一个专业的角色
        String systemPrompt = """
                你是一个专业的家教平台数据提取助手。
                请仔细阅读用户的文本，提取出关键信息，并严格按照指定的 JSON 格式输出。
                如果用户没有提到某些信息，请保留默认值。
                只输出 JSON，不要输出任何解释性的废话。
                """ + converter.getFormat();

        // 调用 AI，并把结果直接转换成 Parent 对象
        Parent parsedParent = chatClient.prompt()
                .system(systemPrompt)
                .user(rawText)
                .call()
                .entity(converter);

        return parsedParent;
    }
}