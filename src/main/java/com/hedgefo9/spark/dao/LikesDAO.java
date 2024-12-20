package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Like;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LikesDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LikesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Like> getAllBySenderId(Long sender_id) {
        String sql = """
                SELECT sender_id, receiver_id, timestamp FROM likes
                WHERE sender_id = ?
                """;
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(Like.class), sender_id);
    }

    public boolean check(Like like) {
        String sql = """
                SELECT COUNT(*) FROM likes
                WHERE sender_id = ? AND receiver_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, like.senderId(), like.receiverId()) > 0;
    }

    public boolean add(Like like) {
        String sql = """
                INSERT INTO likes(sender_id, receiver_id, timestamp)
                VALUES (?, ?, ?)
                """;
        return jdbcTemplate.update(sql, like.senderId(), like.receiverId(), like.timestamp()) != 0;
    }

    public boolean remove(Like like) {
        System.out.println(like.toString());
        String sql = "DELETE FROM likes WHERE sender_id = ? AND receiver_id = ?";
        return jdbcTemplate.update(sql, like.senderId().longValue(), like.receiverId().longValue()) != 0;
    }
}
