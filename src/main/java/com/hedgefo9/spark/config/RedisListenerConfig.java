package com.hedgefo9.spark.config;

import com.hedgefo9.spark.listener.MatchListener;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


@Configuration
public class RedisListenerConfig {
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ChannelTopic likeTopic;

    @Autowired
    private MatchListener matchListener;

    @PostConstruct
    public void setupListeners() {
        redisMessageListenerContainer.addMessageListener(matchListener, likeTopic);
    }
}