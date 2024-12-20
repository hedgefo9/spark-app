package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.UserPhoto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserPhotosDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserPhotosDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserPhoto> rowMapper = (rs, rowNum) -> {
        UserPhoto userPhoto = new UserPhoto();
        userPhoto.photoId(rs.getLong("photo_id"));
        userPhoto.userId(rs.getLong("user_id"));
        userPhoto.fileName(rs.getString("file_name"));
        userPhoto.uploadedAt(rs.getTimestamp("uploaded_at"));
        userPhoto.isPrimary(rs.getBoolean("is_primary"));
        return userPhoto;
    };

    public List<UserPhoto> findByUserId(Long userId) {
        String sql = "SELECT * FROM user_photos WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public void addPhoto(UserPhoto userPhoto) {
        String sql = "INSERT INTO user_photos (user_id, file_name, is_primary) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userPhoto.userId(), userPhoto.fileName(), userPhoto.isPrimary());
    }

    public void deletePhoto(Long photoId) {
        String sql = "DELETE FROM user_photos WHERE photo_id = ?";
        jdbcTemplate.update(sql, photoId);
    }

    public void setPrimaryPhoto(Long userId, Long photoId) {
        String unsetPrimarySql = "UPDATE user_photos SET is_primary = FALSE WHERE user_id = ?";
        String setPrimarySql = "UPDATE user_photos SET is_primary = TRUE WHERE photo_id = ?";
        jdbcTemplate.update(unsetPrimarySql, userId);
        jdbcTemplate.update(setPrimarySql, photoId);
    }

    public Optional<UserPhoto> findPrimaryPhotoByUserId(Long userId) {
        String sql = "SELECT * FROM user_photos WHERE user_id = ? AND is_primary = TRUE LIMIT 1";
        return jdbcTemplate.query(sql, rowMapper, userId).stream().findFirst();
    }

    public Optional<UserPhoto> findById(Long photoId) {
        String sql = "SELECT * FROM user_photos WHERE photo_id = ?";
        return jdbcTemplate.query(sql, rowMapper, photoId).stream().findFirst();
    }
}

