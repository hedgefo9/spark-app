package com.hedgefo9.spark.services.security;

import com.hedgefo9.spark.config.TokenConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenConfig tokenConfig;

    public TokenService(RedisTemplate<String, String> redisTemplate, TokenConfig tokenConfig) {
        this.redisTemplate = redisTemplate;
        this.tokenConfig = tokenConfig;
    }

    public String createToken(String username) {
        String token = UUID.randomUUID().toString();
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, username, tokenConfig.getExpirationTime(), TimeUnit.MILLISECONDS);
        return token;
    }

    public String getUsernameFromToken(String token) {
        String key = "token:" + token;
        return redisTemplate.opsForValue().get(key);
    }

    public void removeToken(String token) {
        String key = "token:" + token;
        System.out.println("Removing token: " + key);
        redisTemplate.delete(key);
    }

    public boolean validateToken(String token) {
        String key = "token:" + token;
        return redisTemplate.hasKey(key);
    }
}