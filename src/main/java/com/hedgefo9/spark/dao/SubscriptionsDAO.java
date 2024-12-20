package com.hedgefo9.spark.dao;

import com.hedgefo9.spark.models.Subscription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubscriptionsDAO {

    private final JdbcTemplate jdbcTemplate;

    public SubscriptionsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Subscription> rowMapper = (rs, rowNum) -> {
        Subscription subscription = new Subscription();
        subscription.subscriptionId(rs.getLong("subscription_id"));
        subscription.userId(rs.getLong("user_id"));
        subscription.startAt(rs.getTimestamp("start_at"));
        subscription.endAt(rs.getTimestamp("end_at"));
        subscription.planType(rs.getString("plan_type"));
        return subscription;
    };

    public List<Subscription> findByUserId(Long userId) {
        String sql = "SELECT * FROM subscriptions WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public List<Subscription> findAll() {
        String sql = "SELECT * FROM subscriptions";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void addSubscription(Subscription subscription) {
        String sql = "INSERT INTO subscriptions (user_id, start_at, end_at, plan_type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, subscription.userId(), subscription.startAt(), subscription.endAt(), subscription.planType());
    }

    public void deleteSubscription(Long subscriptionId) {
        String sql = "DELETE FROM subscriptions WHERE subscription_id = ?";
        jdbcTemplate.update(sql, subscriptionId);
    }

    public Subscription findById(Long subscriptionId) {
        String sql = "SELECT * FROM subscriptions WHERE subscription_id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, subscriptionId);
    }
}

