package com.hedgefo9.spark.services;

import com.hedgefo9.spark.models.Like;
import com.hedgefo9.spark.models.LikeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LikeService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic likeTopic;
    private static final String LIKE_KEY_PREFIX = "likes:";

    @Autowired
    public LikeService(RedisTemplate<String, Object> redisTemplate, ChannelTopic likeTopic) {
        this.redisTemplate = redisTemplate;
        this.likeTopic = likeTopic;
    }

    public List<Like> getAllBySenderId(Long senderId) {
        String key = LIKE_KEY_PREFIX + senderId;
        Set<Object> receiverIds = redisTemplate.opsForSet().members(key);

        if (receiverIds == null) {
            return List.of();
        }

        return receiverIds.stream()
                .map(id -> new Like(senderId, Long.parseLong(id.toString()), System.currentTimeMillis()))
                .collect(Collectors.toList());
    }

    public boolean check(Like like) {
        String key = LIKE_KEY_PREFIX + like.senderId();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, like.receiverId()));
    }

    public boolean add(Like like) {
        String key = LIKE_KEY_PREFIX + like.senderId();
        redisTemplate.opsForSet().add(key, like.receiverId());

        Like likeWithTimestamp = new Like(
                like.senderId(),
                like.receiverId(),
                System.currentTimeMillis()
        );
        redisTemplate.convertAndSend(likeTopic.getTopic(), new LikeEvent(likeWithTimestamp, LikeEvent.LikeEventType.ADD));
        return true;
    }

    public boolean remove(Like like) {
        String key = LIKE_KEY_PREFIX + like.senderId();
        redisTemplate.opsForSet().remove(key, like.receiverId());

        Like likeWithTimestamp = new Like(
                like.senderId(),
                like.receiverId(),
                System.currentTimeMillis()
        );
        redisTemplate.convertAndSend(likeTopic.getTopic(), new LikeEvent(likeWithTimestamp, LikeEvent.LikeEventType.REMOVE));
        return true;
    }
}