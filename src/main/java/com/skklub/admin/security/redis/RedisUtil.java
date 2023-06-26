package com.skklub.admin.security.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, String> redisBlackListTemplate;

    public void setRefreshToken(String key, String token, Long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, token, timeout, timeUnit);
    }

    public String getRefreshToken(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKeyRefreshToken(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setBlackList(String token, String username, Long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(token, username, timeout, timeUnit);
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }

//    public String getBlackList(String key) {
//        return redisBlackListTemplate.opsForValue().get(key);
//    }
//
//    public void deleteBlackList(String key) {
//        redisBlackListTemplate.delete(key);
//    }

}

