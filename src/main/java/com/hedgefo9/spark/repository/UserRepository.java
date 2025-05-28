package com.hedgefo9.spark.repository;

import com.hedgefo9.spark.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {
	private final JdbcTemplate jdbcTemplate;

	public List<User> findAll() {
		String sql = "SELECT * FROM users";
		return jdbcTemplate.query(sql, new UsersRowMapper());
	}

	public User findById(long id) {
		String sql = "SELECT * FROM users WHERE user_id = ?";
		return jdbcTemplate.query(sql, new UsersRowMapper(), id)
				.stream()
				.findAny()
				.orElse(null);
	}

	@Transactional
	public void save(User user) {
		String sql = """
                INSERT INTO users(first_name, last_name, gender,
                                  birth_date, phone_number, email, city,
                                  education, smokes, password_hash)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
		jdbcTemplate.update(sql,
				user.firstName(), user.lastName(), user.gender(),
				user.birthDate(), user.phoneNumber(), user.email(),
				user.city(), user.education(), user.smokes(), user.passwordHash());
	}

	@Transactional
	public boolean update(User user) {
		String sql = """
                UPDATE users
                SET
                    first_name = ?, last_name = ?, gender = ?,
                    birth_date = ?, phone_number = ?, email = ?, city = ?,
                    education = ?, smokes = ?, updated_at = ?
                WHERE user_id = ?
                """;

		return jdbcTemplate.update(sql,
				user.firstName(), user.lastName(), user.gender(),
				user.birthDate(), user.phoneNumber(), user.email(), user.city(),
				user.education(), user.smokes(), user.updatedAt(), user.userId()) > 0;
	}

	public boolean existsByEmail(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
		return count > 0;
	}

	public boolean existsByPhoneNumber(String phoneNumber) {
		String sql = "SELECT COUNT(*) FROM users WHERE phone_number = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phoneNumber);
		return count > 0;
	}

	public User findByEmail(String email) {
		String sql = "SELECT * FROM users WHERE email = ?";
		return jdbcTemplate.query(sql, new UsersRowMapper(), email)
				.stream()
				.findAny()
				.orElse(null);
	}
}
