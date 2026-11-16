package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Message;
import com.example.jiajiao.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // 获取两人之间的历史聊天记录
    @GetMapping("/messages")
    public List<Message> getHistory(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        // 查出 A 发给 B，或者 B 发给 A 的所有消息，并按时间排序
        return messageRepository.findAll().stream()
                .filter(m ->
                        (m.getFromUserId().equals(userId1) && m.getToUserId().equals(userId2)) ||
                                (m.getFromUserId().equals(userId2) && m.getToUserId().equals(userId1))
                )
                .sorted((m1, m2) -> m1.getSendTime().compareTo(m2.getSendTime()))
                .toList();
    }
}