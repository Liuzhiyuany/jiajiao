package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Message;
import com.example.jiajiao.entity.User;
import com.example.jiajiao.repository.MessageRepository;
import com.example.jiajiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

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

    // 2. 获取我的历史联系人：带名字、身份、带前缀的编号
    @GetMapping("/messages/contacts")
    public List<Map<String, Object>> getMyContacts(@RequestParam Long userId) {
        Set<Long> contactIds = new HashSet<>();
        for (Message msg : messageRepository.findAll()) {
            if (msg.getFromUserId().equals(userId)) {
                contactIds.add(msg.getToUserId());
            }
            if (msg.getToUserId().equals(userId)) {
                contactIds.add(msg.getFromUserId());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long id : contactIds) {
            User u = userRepository.findById(id).orElse(null);
            if (u == null) continue;

            String role = u.getRole();
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", role);
            // 老师显示 T-3，家长显示 P-2，界面上彻底分开
            m.put("displayId", ("TEACHER".equals(role) ? "T-" : "P-") + u.getId());
            result.add(m);
        }

        result.sort((a, b) -> Long.compare((Long) a.get("id"), (Long) b.get("id")));
        return result;
    }
}