package com.hedgefo9.spark.repository;

import com.hedgefo9.spark.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MessageRepository {
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public MessageRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Message> findMessagesInDialogue(Long userId1, Long userId2) {
		String sql = """
                SELECT message_id, sender_id, receiver_id, content, sent_at, is_read
                FROM messages
                WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)""";
		return jdbcTemplate.query(sql, new DataClassRowMapper<>(Message.class), userId1, userId2, userId2, userId1);
	}

	@Transactional
	public boolean saveMessage(Message message) {
		String sql = """
                INSERT INTO messages(sender_id, receiver_id, content)
                VALUES (?, ?, ?)
                """;
		return jdbcTemplate.update(sql, message.senderId(), message.receiverId(), message.content()) != 0;
	}
}
