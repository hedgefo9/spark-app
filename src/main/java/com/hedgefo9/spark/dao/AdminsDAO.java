package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminsDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AdminsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Admin> findAll() {
        String query = "SELECT * FROM admins";
        return jdbcTemplate.query(query, new AdminRowMapper());
    }

    public Admin findById(int id) {
        String query = "SELECT * FROM admins WHERE admin_id = ?";
        return jdbcTemplate.query(query, new AdminRowMapper(), id).getFirst();
    }

    public Admin findByEmail(String email) {
        String query = "SELECT * FROM admins WHERE email = ?";
        return jdbcTemplate.query(query, new AdminRowMapper(), email).getFirst();
    }

    public void save(Admin admin) {
        String query = "INSERT INTO admins (name, email, password_hash) VALUES (?, ?, ?)";

        jdbcTemplate.update(query, admin.name(), admin.email(), admin.passwordHash());
    }

    public void deleteById(int id) {
        String query = "DELETE FROM admins WHERE admin_id = ?";
        jdbcTemplate.update(query, id);
    }

    private static class AdminRowMapper implements RowMapper<Admin> {
        @Override
        public Admin mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            Admin admin = new Admin();
            admin.adminId(rs.getInt("admin_id"));
            admin.name(rs.getString("name"));
            admin.email(rs.getString("email"));
            admin.passwordHash(rs.getString("password_hash"));
            admin.createdAt(rs.getTimestamp("created_at"));
            return admin;
        }
    }
}
