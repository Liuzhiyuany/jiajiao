package com.example.jiajiao.websocket;

import com.example.jiajiao.entity.Message;
import com.example.jiajiao.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 保存在线用户的连接
    private static final Map<Long, WebSocketSession> ONLINE = new ConcurrentHashMap<>();
    private final MessageRepository messageRepo;

    public ChatWebSocketHandler(MessageRepository messageRepo) {
        this.messageRepo = messageRepo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 URL 里获取 userId，比如 ws://localhost:8080/ws/chat?userId=1
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("=")[1];
            Long userId = Long.valueOf(userIdStr);
            ONLINE.put(userId, session);
            System.out.println("用户上线了, ID: " + userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 为了不报错，我们用最简单的竖线分割格式：发送者ID|发送者身份|接收者ID|接收者身份|内容
        // 比如：1|TEACHER|2|PARENT|你好啊
        String payload = message.getPayload();
        String[] parts = payload.split("\\|");

        if (parts.length == 5) {
            Message msg = new Message();
            msg.setFromUserId(Long.valueOf(parts[0]));
            msg.setFromRole(parts[1]);
            msg.setToUserId(Long.valueOf(parts[2]));
            msg.setToRole(parts[3]);
            msg.setContent(parts[4]);
            msg.setSendTime(LocalDateTime.now());
            messageRepo.save(msg); // 入库

            // 如果目标用户在线，就转发给他
            WebSocketSession targetSession = ONLINE.get(msg.getToUserId());
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage("收到来自" + msg.getFromRole() + "的消息: " + msg.getContent()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ONLINE.values().remove(session);
    }
}