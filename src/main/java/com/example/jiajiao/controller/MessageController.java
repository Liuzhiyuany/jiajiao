package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Message;
import com.example.jiajiao.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // 1. 获取两人之间的历史聊天记录
    @GetMapping("/messages")
    public List<Message> getHistory(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        return messageRepository.findAll().stream()
                .filter(m ->
                        (m.getFromUserId().equals(userId1) && m.getToUserId().equals(userId2)) ||
                                (m.getFromUserId().equals(userId2) && m.getToUserId().equals(userId1))
                )
                .sorted((m1, m2) -> m1.getSendTime().compareTo(m2.getSendTime()))
                .toList();
    }

    // 2. 【新增】获取我的所有历史联系人 (去重)
    @GetMapping("/messages/contacts")
    public List<Long> getMyContacts(@RequestParam Long userId) {
        List<Message> allMessages = messageRepository.findAll();
        Set<Long> contactIds = new HashSet<>();

        for (Message msg : allMessages) {
            // 如果是我发的，收件人是我的联系人
            if (msg.getFromUserId().equals(userId)) {
                contactIds.add(msg.getToUserId());
            }
            // 如果是发给我的，发件人是我的联系人
            if (msg.getToUserId().equals(userId)) {
                contactIds.add(msg.getFromUserId());
            }
        }
        return new ArrayList<>(contactIds);
    }
}