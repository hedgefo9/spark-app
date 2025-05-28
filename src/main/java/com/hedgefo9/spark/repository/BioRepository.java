package com.hedgefo9.spark.repository;

import com.hedgefo9.spark.models.Bio;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class BioRepository {
	private final JdbcTemplate jdbcTemplate;

	@Transactional
	public void save(Bio bio) {
		String sql = "INSERT INTO bios(user_id, about_me, looking_for) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql,
				bio.userId(),
				bio.aboutMe(),
				bio.lookingFor());
	}

	@Transactional
	public boolean update(Bio bio) {
		String sql = "UPDATE bios SET about_me = ?, looking_for = ?, updated_at = ? WHERE user_id = ?";
		return jdbcTemplate.update(sql,
				bio.aboutMe(),
				bio.lookingFor(),
				bio.updatedAt(),
				bio.userId()) > 0;
	}

	public Bio findByUserId(Long userId) {
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
