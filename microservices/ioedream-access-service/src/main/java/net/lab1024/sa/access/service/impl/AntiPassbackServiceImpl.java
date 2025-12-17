package net.lab1024.sa.access.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.service.DeviceService;
import net.lab1024.sa.common.organization.service.AreaService;

/**
 * 门禁反潜回服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务层
 * - 使用@Transactional管理事务
 * - 实现@CircuitBreaker熔断和@Retry重试机制
 * - 使用Redis缓存提升性能
 * - 支持四种反潜回算法：硬反潜回、软反潜回、区域反潜回、全局反潜回
 * </p>
 * <p>
 * P0级安全功能：防止同一个人在短时间内在多个门禁点重复进出
 * 企业级实现：高并发、高性能、高可用
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AntiPassbackServiceImpl implements AntiPassbackService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DeviceService deviceService;

    @Resource
    private AreaService areaService;

    // Redis缓存键前缀
    private static final String ANTI_PASSBACK_PREFIX = "anti_passback:";
    private static final String USER_LAST_ACCESS_PREFIX = "user_last_access:";
    private static final String AREA_USER_COUNT_PREFIX = "area_user_count:";
    private static final String GLOBAL_USER_ACCESS_PREFIX = "global_user_access:";

    // 反潜回时间窗口（分钟）
    private static final int ANTI_PASSBACK_TIME_WINDOW = 5;
    private static final int AREA_ANTI_PASSBACK_TIME_WINDOW = 10;
    private static final int GLOBAL_ANTI_PASSBACK_TIME_WINDOW = 15;

    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "performAntiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    @RateLimiter(name = "antiPassbackService")
    public CompletableFuture<AntiPassbackResult> performAntiPassbackCheck(
            Long userId, Long deviceId, Long areaId, String verificationData) {

        log.info("[反潜回检查] 开始检查 userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取设备和区域信息
                DeviceEntity device = deviceService.getById(deviceId);
                AreaEntity area = areaService.getById(areaId);

                if (device == null || area == null) {
                    return AntiPassbackResult.failure("设备或区域不存在");
                }

                // 获取反潜回策略
                String antiPassbackType = area.getAntiPassbackType();
                if (antiPassbackType == null) {
                    antiPassbackType = "NONE";
                }

                // 执行对应类型的反潜回检查
                AntiPassbackResult result = switch (antiPassbackType) {
                    case "HARD" -> checkHardAntiPassback(userId, deviceId, areaId, device, area);
                    case "SOFT" -> checkSoftAntiPassback(userId, deviceId, areaId, device, area);
                    case "AREA" -> checkAreaAntiPassback(userId, deviceId, areaId, device, area);
                    case "GLOBAL" -> checkGlobalAntiPassback(userId, deviceId, areaId, device, area);
                    default -> AntiPassbackResult.success("无反潜回限制");
                };

                log.info("[反潜回检查] 检查完成 userId={}, result={}", userId, result.isAllowed());
                return result;

            } catch (Exception e) {
                log.error("[反潜回检查] 检查异常 userId={}, error={}", userId, e.getMessage(), e);
                return AntiPassbackResult.failure("系统异常：" + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "checkAreaAntiPassbackFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<AntiPassbackResult> checkAreaAntiPassback(Long userId, Long areaId, String accessType) {
        log.info("[区域反潜回检查] 开始检查 userId={}, areaId={}, accessType={}", userId, areaId, accessType);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String cacheKey = ANTI_PASSBACK_PREFIX + "area:" + areaId + ":" + userId;

                // 获取用户在区域内的最后通行记录
                String lastAccessKey = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
                String lastAccessStr = (String) redisTemplate.opsForValue().get(lastAccessKey);

                if (lastAccessStr == null) {
                    // 首次进入区域，记录并允许
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("首次进入区域");
                }

                // 解析最后通行信息
                String[] lastAccessInfo = lastAccessStr.split(":");
                String lastAccessType = lastAccessInfo[0];
                LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessInfo[1]);

                // 检查时间窗口
                if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() > AREA_ANTI_PASSBACK_TIME_WINDOW) {
                    // 超时，允许通行
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("超时重新进入");
                }

                // 检查通行类型是否匹配
                if (isAccessTypeValid(lastAccessType, accessType)) {
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("通行类型匹配");
                } else {
                    return AntiPassbackResult.failure("反潜回违规：通行类型不匹配。上一次：" + lastAccessType + "，本次：" + accessType);
                }

            } catch (Exception e) {
                log.error("[区域反潜回检查] 检查异常 userId={}, areaId={}, error={}", userId, areaId, e.getMessage(), e);
                return AntiPassbackResult.failure("系统异常：" + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "updatePolicyFallback")
    public AntiPassbackResult updatePolicy(String deviceId, String policy) {
        try {
            log.info("[反潜回策略更新] 开始更新 deviceId={}, policy={}", deviceId, policy);

            DeviceEntity device = deviceService.getById(Long.parseLong(deviceId));
            if (device == null) {
                return AntiPassbackResult.failure("设备不存在");
            }

            // 更新设备反潜回策略配置
            Map<String, Object> extendedAttributes = new HashMap<>();
            if (device.getExtendedAttributes() != null) {
                // 解析现有扩展属性
                extendedAttributes = parseExtendedAttributes(device.getExtendedAttributes());
            }

            extendedAttributes.put("antiPassbackPolicy", policy);
            device.setExtendedAttributes(serializeExtendedAttributes(extendedAttributes));

            deviceService.updateById(device);

            // 清除相关缓存
            clearDeviceCache(Long.parseLong(deviceId));

            log.info("[反潜回策略更新] 更新成功 deviceId={}", deviceId);
            return AntiPassbackResult.success("策略更新成功");

        } catch (Exception e) {
            log.error("[反潜回策略更新] 更新失败 deviceId={}, error={}", deviceId, e.getMessage(), e);
            return AntiPassbackResult.failure("更新失败：" + e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "recordAccessEventFallback")
    @Retry(name = "antiPassbackService", fallbackMethod = "recordAccessEventFallback")
    public void recordAccessEvent(Long userId, Long deviceId, Long areaId, boolean allowed, String reason) {
        try {
            log.debug("[反潜回事件记录] 记录事件 userId={}, deviceId={}, areaId={}, allowed={}",
                     userId, deviceId, areaId, allowed);

            // 创建通行记录
            AccessRecordEntity record = new AccessRecordEntity();
            record.setUserId(userId);
            record.setDeviceId(deviceId);
            record.setAreaId(areaId);
            record.setAccessResult(allowed ? 1 : 2);
            record.setAccessTime(LocalDateTime.now());

            accessRecordDao.insert(record);

            // 更新缓存
            updateAccessCache(userId, deviceId, areaId, allowed);

        } catch (Exception e) {
            log.error("[反潜回事件记录] 记录失败 userId={}, deviceId={}, error={}",
                     userId, deviceId, e.getMessage(), e);
            // 记录失败不应该影响主要业务流程，所以这里只记录日志
        }
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "resetAntiPassbackFallback")
    public AntiPassbackResult resetAntiPassback(Long userId, Long deviceId, Long areaId) {
        try {
            log.info("[反潜回重置] 开始重置 userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

            // 清除相关缓存
            clearUserCache(userId, deviceId, areaId);

            // 记录重置事件
            recordResetEvent(userId, deviceId, areaId);

            log.info("[反潜回重置] 重置成功 userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);
            return AntiPassbackResult.success("重置成功");

        } catch (Exception e) {
            log.error("[反潜回重置] 重置失败 userId={}, deviceId={}, areaId={}, error={}",
                     userId, deviceId, areaId, e.getMessage(), e);
            return AntiPassbackResult.failure("重置失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法：硬反潜回 ====================

    /**
     * 硬反潜回检查
     * 严格禁止在时间窗口内重复通行
     */
    private AntiPassbackResult checkHardAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "hard:" + userId;

        // 检查最近通行记录
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                return AntiPassbackResult.failure("硬反潜回违规：在时间窗口内禁止重复通行");
            }
        }

        // 记录当前通行
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        return AntiPassbackResult.success("硬反潜回检查通过");
    }

    // ==================== 私有方法：软反潜回 ====================

    /**
     * 软反潜回检查
     * 允许通行但记录异常
     */
    private AntiPassbackResult checkSoftAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "soft:" + userId;

        // 检查最近通行记录
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        boolean isException = false;

        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                isException = true;
                log.warn("[软反潜回] 检测到重复通行 userId={}, deviceId={}, lastTime={}",
                        userId, deviceId, lastAccessTime);

                // 记录异常事件
                recordSoftException(userId, deviceId, areaId);
            }
        }

        // 记录当前通行
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        String message = isException ? "软反潜回：检测到重复通行但允许通过" : "软反潜回检查通过";
        return AntiPassbackResult.success(message);
    }

    // ==================== 私有方法：全局反潜回 ====================

    /**
     * 全局反潜回检查
     * 跨区域、跨设备的全局反潜回检查
     */
    private AntiPassbackResult checkGlobalAntiPassback(Long userId, Long deviceId, Long areaId,
                                                     DeviceEntity device, AreaEntity area) {
        String cacheKey = GLOBAL_USER_ACCESS_PREFIX + userId;

        // 获取用户全局最近通行记录
        List<String> recentAccesses = (List<String>) redisTemplate.opsForValue().get(cacheKey);

        if (recentAccesses != null && !recentAccesses.isEmpty()) {
            // 检查是否有在时间窗口内的通行记录
            LocalDateTime now = LocalDateTime.now();
            for (String accessStr : recentAccesses) {
                String[] accessInfo = accessStr.split(":");
                LocalDateTime accessTime = LocalDateTime.parse(accessInfo[0]);

                if (Duration.between(accessTime, now).toMinutes() < GLOBAL_ANTI_PASSBACK_TIME_WINDOW) {
                    return AntiPassbackResult.failure("全局反潜回违规：在全局时间窗口内禁止多区域通行");
                }
            }
        }

        // 记录当前通行
        String currentAccess = LocalDateTime.now() + ":" + areaId + ":" + deviceId;
        if (recentAccesses == null) {
            recentAccesses = new ArrayList<>();
        }
        recentAccesses.add(currentAccess);

        // 保持最近10条记录
        if (recentAccesses.size() > 10) {
            recentAccesses = recentAccesses.subList(recentAccesses.size() - 10, recentAccesses.size());
        }

        redisTemplate.opsForValue().set(cacheKey, recentAccesses,
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        return AntiPassbackResult.success("全局反潜回检查通过");
    }

    // ==================== 私有方法：辅助功能 ====================

    /**
     * 检查通行类型是否有效
     */
    private boolean isAccessTypeValid(String lastType, String currentType) {
        // 实现进出类型匹配逻辑
        // 例如：IN和OUT应该交替出现
        if ("IN".equals(lastType) && "OUT".equals(currentType)) {
            return true;
        }
        if ("OUT".equals(lastType) && "IN".equals(currentType)) {
            return true;
        }
        return false;
    }

    /**
     * 记录区域通行
     */
    private void recordAreaAccess(Long userId, Long areaId, String accessType) {
        String key = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
        String value = accessType + ":" + LocalDateTime.now();

        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        // 更新区域人数统计
        updateUserCountInArea(areaId, accessType);
    }

    /**
     * 更新区域用户人数
     */
    private void updateUserCountInArea(Long areaId, String accessType) {
        String countKey = AREA_USER_COUNT_PREFIX + areaId;

        if ("IN".equals(accessType)) {
            redisTemplate.opsForValue().increment(countKey);
        } else if ("OUT".equals(accessType)) {
            redisTemplate.opsForValue().decrement(countKey);
        }

        redisTemplate.expire(countKey, Duration.ofHours(24));
    }

    /**
     * 清除设备缓存
     */
    private void clearDeviceCache(Long deviceId) {
        Set<String> keys = redisTemplate.keys(ANTI_PASSBACK_PREFIX + "*:" + deviceId + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除用户缓存
     */
    private void clearUserCache(Long userId, Long deviceId, Long areaId) {
        Set<String> keys = redisTemplate.keys(ANTI_PASSBACK_PREFIX + "*:" + userId + "*");
        keys.addAll(redisTemplate.keys(USER_LAST_ACCESS_PREFIX + "*:" + userId));
        keys.addAll(redisTemplate.keys(GLOBAL_USER_ACCESS_PREFIX + userId));

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 更新访问缓存
     */
    private void updateAccessCache(Long userId, Long deviceId, Long areaId, boolean allowed) {
        if (allowed) {
            // 更新成功访问的缓存
            String key = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
            String value = "IN:" + LocalDateTime.now();
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(CACHE_EXPIRE_MINUTES));
        }
    }

    /**
     * 记录软反潜回异常
     */
    private void recordSoftException(Long userId, Long deviceId, Long areaId) {
        log.warn("[软反潜回异常] userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        // 可以添加到异常表或发送告警
        String exceptionKey = ANTI_PASSBACK_PREFIX + "soft_exception:" + userId;
        String exceptionData = LocalDateTime.now() + ":" + deviceId + ":" + areaId;

        redisTemplate.opsForList().rightPush(exceptionKey, exceptionData);
        redisTemplate.expire(exceptionKey, Duration.ofHours(24));
    }

    /**
     * 记录重置事件
     */
    private void recordResetEvent(Long userId, Long deviceId, Long areaId) {
        String resetKey = ANTI_PASSBACK_PREFIX + "reset:" + userId;
        String resetData = LocalDateTime.now() + ":" + deviceId + ":" + areaId;

        redisTemplate.opsForList().rightPush(resetKey, resetData);
        redisTemplate.expire(resetKey, Duration.ofHours(24));
    }

    /**
     * 解析扩展属性
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseExtendedAttributes(String extendedAttributes) {
        try {
            // 这里应该使用JSON解析，简化实现
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 序列化扩展属性
     */
    private String serializeExtendedAttributes(Map<String, Object> attributes) {
        try {
            // 这里应该使用JSON序列化，简化实现
            return attributes.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    // ==================== 熔断降级方法 ====================

    public CompletableFuture<AntiPassbackResult> performAntiPassbackCheckFallback(
            Long userId, Long deviceId, Long areaId, String verificationData, Exception e) {
        log.warn("[反潜回检查] 熔断降级 userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage());
        return CompletableFuture.completedFuture(AntiPassbackResult.failure("服务暂时不可用，已降级处理"));
    }

    public CompletableFuture<AntiPassbackResult> checkAreaAntiPassbackFallback(
            Long userId, Long areaId, String accessType, Exception e) {
        log.warn("[区域反潜回检查] 熔断降级 userId={}, areaId={}, error={}", userId, areaId, e.getMessage());
        return CompletableFuture.completedFuture(AntiPassbackResult.failure("区域反潜回检查服务暂时不可用"));
    }

    public AntiPassbackResult updatePolicyFallback(String deviceId, String policy, Exception e) {
        log.warn("[反潜回策略更新] 熔断降级 deviceId={}, error={}", deviceId, e.getMessage());
        return AntiPassbackResult.failure("策略更新服务暂时不可用");
    }

    public void recordAccessEventFallback(Long userId, Long deviceId, Long areaId, boolean allowed, String reason, Exception e) {
        log.error("[反潜回事件记录] 熔断降级 userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage());
        // 事件记录失败不应该影响主流程
    }

    public AntiPassbackResult resetAntiPassbackFallback(Long userId, Long deviceId, Long areaId, Exception e) {
        log.warn("[反潜回重置] 熔断降级 userId={}, deviceId={}, areaId={}, error={}", userId, deviceId, areaId, e.getMessage());
        return AntiPassbackResult.failure("重置服务暂时不可用");
    }

    // ==================== 预留方法 ====================

    @Override
    public Map<String, Object> getAntiPassbackStatistics() {
        // TODO: 实现统计功能
        return new HashMap<>();
    }

    @Override
    public AntiPassbackResult checkTimeWindowViolation(Long userId, Long deviceId, LocalDateTime accessTime) {
        // TODO: 实现时间窗口违规检查
        return AntiPassbackResult.success("功能待实现");
    }
}
