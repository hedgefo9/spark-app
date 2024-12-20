package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsersDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UsersDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> index() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UsersRowMapper());
    }

    public User show(long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.query(sql, new UsersRowMapper(), id)
                .stream()
                .findAny()
                .orElse(null);
    }

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
