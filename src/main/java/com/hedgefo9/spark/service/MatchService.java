package com.hedgefo9.spark.service;

import com.hedgefo9.spark.models.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MATCH_KEY_PREFIX = "matches:";

    public List<Match> getAllByUserId(Long userId) {
        String key = MATCH_KEY_PREFIX + userId;
        Set<Object> matchedUserIds = redisTemplate.opsForSet().members(key);

        if (matchedUserIds == null) {
            return List.of();
        }

        return matchedUserIds.stream()
                .map(id -> new Match(userId, Long.parseLong(id.toString()), System.currentTimeMillis()))
                .collect(Collectors.toList());
    }

    public boolean check(Long userId1, Long userId2) {
        String key = MATCH_KEY_PREFIX + Math.min(userId1, userId2);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, Math.max(userId1, userId2)));
    }
}
