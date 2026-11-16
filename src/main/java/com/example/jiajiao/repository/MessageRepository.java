package com.example.jiajiao.repository;

import com.example.jiajiao.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 查历史聊天记录用：找出两人之间互发的消息，并按时间顺序排好
    List<Message> findByFromUserIdAndToUserIdOrFromUserIdAndToUserIdOrderBySendTime(
            Long fromId1, Long toId1, Long fromId2, Long toId2);
}