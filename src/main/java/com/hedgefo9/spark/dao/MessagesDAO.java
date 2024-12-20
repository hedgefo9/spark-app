package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessagesDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MessagesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Message> getMessagesInDialogue(Long userId1, Long userId2) {
        String sql = """
                SELECT message_id, sender_id, receiver_id, content, sent_at, is_read
                FROM messages
                WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)""";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(Message.class), userId1, userId2, userId2, userId1);
    }

    public boolean addMessage(Message message) {
        String sql = """
                INSERT INTO messages(sender_id, receiver_id, content)
                VALUES (?, ?, ?)
                """;
        return jdbcTemplate.update(sql, message.senderId(), message.receiverId(), message.content()) != 0;
    }
}
