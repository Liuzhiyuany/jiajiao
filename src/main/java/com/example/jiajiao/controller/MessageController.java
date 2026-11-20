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
            m.put("displayId", ("TEACHER".equals(role) ? "T-" : "P-") + u.getId());
            result.add(m);
        }

        result.sort((a, b) -> Long.compare((Long) a.get("id"), (Long) b.get("id")));
        return result;
    }

    // 3. 未读消息统计：返回 [{contactId, count}, ...]
    @GetMapping("/messages/unread")
    public List<Map<String, Object>> unreadCount(@RequestParam Long userId) {
        Map<Long, Long> counts = new HashMap<>();
        for (Message m : messageRepository.findAll()) {
            if (userId.equals(m.getToUserId()) && !Boolean.TRUE.equals(m.getIsRead())) {
                counts.merge(m.getFromUserId(), 1L, Long::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((contactId, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("contactId", contactId);
            map.put("count", count);
            result.add(map);
        });
        return result;
    }

    // 4. 打开对话时，把对方发给我的消息全部标记为已读
    @PutMapping("/messages/read")
    public int markRead(@RequestParam Long userId, @RequestParam Long contactId) {
        int n = 0;
        for (Message m : messageRepository.findAll()) {
            if (userId.equals(m.getToUserId())
                    && contactId.equals(m.getFromUserId())
                    && !Boolean.TRUE.equals(m.getIsRead())) {
                m.setIsRead(true);
                messageRepository.save(m);
                n++;
            }
        }
        return n;
    }
}