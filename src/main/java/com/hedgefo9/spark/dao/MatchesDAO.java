package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Like;
import com.hedgefo9.spark.models.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchesDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MatchesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Match> getAllByUserId(Long user_id) {
        String sql = """
                SELECT user_id1, user_id2, matched_at FROM matches
                WHERE user_id1 = ? OR user_id2 = ?
                """;
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(Match.class), user_id, user_id);
    }

    public boolean check(Long sender_id, Long receiver_id) {
        String sql = """
                SELECT COUNT(*) FROM matches
                WHERE user_id1 = ? AND user_id2 = ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class,
                Long.min(sender_id, receiver_id),
                Long.max(sender_id, receiver_id)) > 0;
    }

}
