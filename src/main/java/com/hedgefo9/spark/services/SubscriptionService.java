package com.hedgefo9.spark.services;

import com.hedgefo9.spark.models.Subscription;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SubscriptionService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SUBSCRIPTION_KEY_PREFIX = "subscription:";
    private static final String USER_SUBSCRIPTIONS_KEY_PREFIX = "user:subscriptions:";
    private static final String SUBSCRIPTION_ID_COUNTER = "subscription:id:counter";

    public SubscriptionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addSubscription(Subscription subscription) {

        Long newId = redisTemplate.opsForValue().increment(SUBSCRIPTION_ID_COUNTER);
        subscription.subscriptionId(newId);

        String subscriptionKey = SUBSCRIPTION_KEY_PREFIX + subscription.subscriptionId();
        String userSubscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + subscription.userId();

        redisTemplate.opsForValue().set(subscriptionKey, subscription);
        redisTemplate.opsForSet().add(userSubscriptionsKey, subscription.subscriptionId());
    }

    public List<Subscription> findByUserId(Long userId) {
        String userSubscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + userId;
        Set<Object> subscriptionIds = redisTemplate.opsForSet().members(userSubscriptionsKey);

        List<Subscription> subscriptions = new ArrayList<>();
        if (subscriptionIds != null) {
            for (Object id : subscriptionIds) {
                String subscriptionKey = SUBSCRIPTION_KEY_PREFIX + id;
                Subscription subscription = (Subscription) redisTemplate.opsForValue().get(subscriptionKey);
                if (subscription != null) {
                    subscriptions.add(subscription);
                }
            }
        }
        return subscriptions;
    }

    public List<Subscription> findAll() {
        Set<String> keys = redisTemplate.keys(SUBSCRIPTION_KEY_PREFIX + "*");
        List<Subscription> subscriptions = new ArrayList<>();

        if (keys != null) {

            keys.stream()
                    .filter(key -> !key.equals(SUBSCRIPTION_ID_COUNTER))
                    .forEach(key -> {
                        Subscription subscription = (Subscription) redisTemplate.opsForValue().get(key);
                        if (subscription != null) {
                            subscriptions.add(subscription);
                        }
                    });
        }
        return subscriptions;
    }

    public void deleteSubscription(Long subscriptionId) {
        String subscriptionKey = SUBSCRIPTION_KEY_PREFIX + subscriptionId;
        Subscription subscription = (Subscription) redisTemplate.opsForValue().get(subscriptionKey);

        if (subscription != null) {
            String userSubscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + subscription.userId();
            redisTemplate.opsForSet().remove(userSubscriptionsKey, subscriptionId);
            redisTemplate.delete(subscriptionKey);
        }
    }

    public Subscription findById(Long subscriptionId) {
        String subscriptionKey = SUBSCRIPTION_KEY_PREFIX + subscriptionId;
        return (Subscription) redisTemplate.opsForValue().get(subscriptionKey);
    }
}