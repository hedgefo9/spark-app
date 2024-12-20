package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UsersRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.userId(rs.getLong("user_id"));
        user.firstName(rs.getString("first_name"));
        user.lastName(rs.getString("last_name"));
        user.gender(rs.getInt("gender"));
        user.birthDate(rs.getDate("birth_date"));
        user.phoneNumber(rs.getString("phone_number"));
        user.email(rs.getString("email"));
        user.city(rs.getString("city"));
        user.education(rs.getInt("education"));
        user.smokes(rs.getBoolean("smokes"));
        user.passwordHash(rs.getString("password_hash"));
        user.createdAt(rs.getObject("created_at", LocalDateTime.class));
        user.updatedAt(rs.getObject("updated_at", LocalDateTime.class));

        return user;
    }
}
