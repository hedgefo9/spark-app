package com.hedgefo9.spark.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgefo9.spark.models.Like;
import com.hedgefo9.spark.models.LikeEvent;
import com.hedgefo9.spark.services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchListener implements MessageListener {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchService matchService;
    private static final String MATCH_KEY_PREFIX = "matches:";
    @Qualifier("myObjectMapper")
    private final ObjectMapper objectMapper;

    @Autowired
    public MatchListener(RedisTemplate<String, Object> redisTemplate,
                         MatchService matchService,
                         ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.matchService = matchService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            System.out.println("Received message: " + messageBody);
            LikeEvent event = objectMapper.readValue(messageBody, LikeEvent.class);
            Like like = event.like();

            switch (event.type()) {
                case ADD -> handleLikeAdd(like);
                case REMOVE -> handleLikeRemove(like);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLikeAdd(Like like) {

        String key = "likes:" + like.receiverId();
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, like.senderId()))) {

            String matchKey1 = MATCH_KEY_PREFIX + like.senderId();
            String matchKey2 = MATCH_KEY_PREFIX + like.receiverId();

            redisTemplate.opsForSet().add(matchKey1, like.receiverId());
            redisTemplate.opsForSet().add(matchKey2, like.senderId());
        }
    }

    private void handleLikeRemove(Like like) {

        String matchKey1 = MATCH_KEY_PREFIX + like.senderId();
        String matchKey2 = MATCH_KEY_PREFIX + like.receiverId();

        redisTemplate.opsForSet().remove(matchKey1, like.receiverId());
        redisTemplate.opsForSet().remove(matchKey2, like.senderId());
    }
}