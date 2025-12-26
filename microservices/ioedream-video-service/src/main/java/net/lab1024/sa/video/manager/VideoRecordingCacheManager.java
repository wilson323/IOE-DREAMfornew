package net.lab1024.sa.video.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoRecordingCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "video:recording:";
    private static final String STATISTICS_CACHE_PREFIX = "video:statistics:";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void cacheStatistics(String cacheKey, Object statistics, long ttl, TimeUnit timeUnit) {
        try {
            String key = STATISTICS_CACHE_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(key, statistics, ttl, timeUnit);
            log.debug("[缓存] 统计数据已缓存: key={}, ttl={} {}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.warn("[缓存] 缓存统计数据失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    public Object getCachedStatistics(String cacheKey) {
        try {
            String key = STATISTICS_CACHE_PREFIX + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("[缓存] 命中缓存: key={}", key);
            }
            return cached;
        } catch (Exception e) {
            log.warn("[缓存] 获取缓存失败: key={}, error={}", cacheKey, e.getMessage());
            return null;
        }
    }

    public void evictStatistics(String cacheKey) {
        try {
            String key = STATISTICS_CACHE_PREFIX + cacheKey;
            redisTemplate.delete(key);
            log.debug("[缓存] 清除缓存: key={}", key);
        } catch (Exception e) {
            log.warn("[缓存] 清除缓存失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    public void cacheDeviceInfo(String deviceId, Map<String, Object> deviceInfo) {
        String key = CACHE_PREFIX + "device:" + deviceId;
        redisTemplate.opsForValue().set(key, deviceInfo, 30, TimeUnit.MINUTES);
        log.debug("[缓存] 设备信息已缓存: key={}", key);
    }

    public Map<String, Object> getCachedDeviceInfo(String deviceId) {
        String key = CACHE_PREFIX + "device:" + deviceId;
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (Map<String, Object>) cached : null;
    }

    public void cachePlaybackSession(String sessionId, Map<String, Object> sessionInfo) {
        String key = CACHE_PREFIX + "session:" + sessionId;
        redisTemplate.opsForValue().set(key, sessionInfo, 1, TimeUnit.HOURS);
        log.debug("[缓存] 播放会话已缓存: key={}", key);
    }

    public Map<String, Object> getCachedPlaybackSession(String sessionId) {
        String key = CACHE_PREFIX + "session:" + sessionId;
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (Map<String, Object>) cached : null;
    }

    public void removePlaybackSession(String sessionId) {
        String key = CACHE_PREFIX + "session:" + sessionId;
        redisTemplate.delete(key);
        log.debug("[缓存] 播放会话已移除: key={}", key);
    }

    public String generateCacheKey(String type, LocalDateTime startTime, LocalDateTime endTime, Object... params) {
        StringBuilder key = new StringBuilder(type);
        key.append(":").append(startTime != null ? startTime.toString() : "null");
        key.append(":").append(endTime != null ? endTime.toString() : "null");
        for (Object param : params) {
            key.append(":").append(param != null ? param.toString() : "null");
        }
        return key.toString();
    }

    public void clearAllStatisticsCache() {
        try {
            String pattern = STATISTICS_CACHE_PREFIX + "*";
            redisTemplate.keys(pattern).forEach(key -> {
                redisTemplate.delete(key);
                log.debug("[缓存] 清除统计缓存: key={}", key);
            });
            log.info("[缓存] 已清除所有统计缓存");
        } catch (Exception e) {
            log.error("[缓存] 清除所有统计缓存失败: error={}", e.getMessage());
        }
    }
}
