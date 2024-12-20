package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Bio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class BiosDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiosDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addBio(Bio bio) {
        String sql = "INSERT INTO bios(user_id, about_me, looking_for) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,
                bio.userId(),
                bio.aboutMe(),
                bio.lookingFor());
    }

    public boolean updateBio(Bio bio) {
        String sql = "UPDATE bios SET about_me = ?, looking_for = ?, updated_at = ? WHERE user_id = ?";

        return jdbcTemplate.update(sql,
                bio.aboutMe(),
                bio.lookingFor(),
                bio.updatedAt(),
                bio.userId()) > 0;
    }

    public Bio getBioByUserId(Long userId) {
        String sql = "SELECT user_id, about_me, looking_for, updated_at FROM bios WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new RowMapper<>() {
            @Override
            public Bio mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Bio(
                        rs.getLong("user_id"),
                        rs.getString("about_me"),
                        rs.getString("looking_for"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        }, userId);
    }
}
