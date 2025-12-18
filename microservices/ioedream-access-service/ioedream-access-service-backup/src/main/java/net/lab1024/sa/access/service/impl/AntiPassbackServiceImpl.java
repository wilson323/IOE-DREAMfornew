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
import net.lab1024.sa.access.manager.AntiPassbackManager;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;

/**
 * 门禁反潜回服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识业务层实现类
 * - 使用@Transactional管理事务
 * - 集成@CircuitBreaker熔断和@Retry重试容错机制
 * - 使用Redis缓存提供高性能响应
 * - 实现多种反潜回模式：硬反潜回、软反潜回、区域反潜回、全局反潜回
 * </p>
 * <p>
 * P0级性能优化：使用Redis存储用户访问状态，避免频繁数据库查询
 * 内存优化：使用合理的缓存过期时间，避免内存泄漏
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
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private AntiPassbackManager antiPassbackManager;

    // Redis缓存键值前缀
    private static final String ANTI_PASSBACK_PREFIX = "anti_passback:";
    private static final String USER_LAST_ACCESS_PREFIX = "user_last_access:";
    private static final String AREA_USER_COUNT_PREFIX = "area_user_count:";
    private static final String GLOBAL_USER_ACCESS_PREFIX = "global_user_access:";

    // 反潜回时间窗口配置（分钟）
    private static final int ANTI_PASSBACK_TIME_WINDOW = 5;
    private static final int AREA_ANTI_PASSBACK_TIME_WINDOW = 10;
    private static final int GLOBAL_ANTI_PASSBACK_TIME_WINDOW = 15;

    // 缓存过期时间配置（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "performAntiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    @RateLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<AntiPassbackResult>> performAntiPassbackCheck(
            Long userId, Long deviceId, Long areaId, String verificationData) {

        log.info("[反潜回检查] 开始检查:userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取设备和区域信息
                DeviceEntity device = getDeviceById(deviceId);
                AreaEntity area = getAreaById(areaId);

                if (device == null || area == null) {
                    return ResponseDTO.error("DEVICE_OR_AREA_NOT_FOUND", "设备或区域不存在");
                }

                // 获取反潜回策略配置
                String antiPassbackType = area.getAntiPassbackType();
                if (antiPassbackType == null) {
                    antiPassbackType = "NONE";
                }

                // 根据策略执行不同的反潜回检查
                AntiPassbackResult result = switch (antiPassbackType) {
                    case "HARD" -> checkHardAntiPassback(userId, deviceId, areaId, device, area);
                    case "SOFT" -> checkSoftAntiPassback(userId, deviceId, areaId, device, area);
                    case "AREA" -> checkAreaAntiPassback(userId, deviceId, areaId, device, area);
                    case "GLOBAL" -> checkGlobalAntiPassback(userId, deviceId, areaId, device, area);
                    default -> AntiPassbackResult.success("未启用反潜回限制");
                };

                log.info("[反潜回检查] 检查完成:userId={}, result={}", userId, result.isPassed());
                return ResponseDTO.ok(result);

            } catch (Exception e) {
                log.error("[反潜回检查] 检查异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("ANTI_PASSBACK_CHECK_ERROR", "系统异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "checkAreaAntiPassbackFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<AntiPassbackResult>> checkAreaAntiPassback(
            Long userId, Long entryAreaId, Long exitAreaId, String direction) {
        log.info("[区域反潜回] 开始检查:userId={}, entryAreaId={}, exitAreaId={}, direction={}", userId, entryAreaId, exitAreaId, direction);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证参数
                if (userId == null || entryAreaId == null || direction == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层处理复杂逻辑
                AntiPassbackResult result = antiPassbackManager.checkAreaAntiPassback(userId, entryAreaId, exitAreaId, direction);

                log.info("[区域反潜回] 检查完成:userId={}, result={}", userId, result.isPassed());
                return ResponseDTO.ok(result);

            } catch (Exception e) {
                log.error("[区域反潜回] 检查异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("AREA_ANTI_PASSBACK_ERROR", "区域反潜回检查异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Void>> setAntiPassbackPolicy(
            Long deviceId, String antiPassbackType, Map<String, Object> config) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回策略] 设置策略:deviceId={}, type={}", deviceId, antiPassbackType);

                // 验证参数
                if (deviceId == null || antiPassbackType == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "设备ID和反潜回类型不能为空");
                }

                // 使用Manager层处理策略设置
                antiPassbackManager.setAntiPassbackPolicy(deviceId, antiPassbackType, config);

                log.info("[反潜回策略] 策略设置成功:deviceId={}, type={}", deviceId, antiPassbackType);
                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回策略] 设置策略异常:deviceId={}, error={}", deviceId, e.getMessage(), e);
                return ResponseDTO.error("ANTI_PASSBACK_POLICY_ERROR", "设置策略异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackPolicy(Long deviceId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[反潜回策略] 获取策略:deviceId={}", deviceId);

                // 使用Manager层获取策略
                Object policy = antiPassbackManager.getAntiPassbackPolicy(deviceId);

                if (policy == null) {
                    return ResponseDTO.error("POLICY_NOT_FOUND", "反潜回策略不存在");
                }

                return ResponseDTO.ok(policy);

            } catch (Exception e) {
                log.error("[反潜回策略] 获取策略异常:deviceId={}, error={}", deviceId, e.getMessage(), e);
                return ResponseDTO.error("GET_POLICY_ERROR", "获取策略异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Void>> updateAntiPassbackConfig(
            Long deviceId, Map<String, Object> config) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回配置] 更新配置:deviceId={}", deviceId);

                // 验证参数
                if (deviceId == null || config == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层更新配置
                antiPassbackManager.updateAntiPassbackConfig(deviceId, config);

                log.info("[反潜回配置] 配置更新成功:deviceId={}", deviceId);
                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回配置] 更新配置异常:deviceId={}, error={}", deviceId, e.getMessage(), e);
                return ResponseDTO.error("UPDATE_CONFIG_ERROR", "更新配置异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    @Retry(name = "antiPassbackService", fallbackMethod = "recordAccessEventFallback")
    public CompletableFuture<ResponseDTO<Void>> recordAccessEvent(
            Long userId, Long deviceId, Long areaId, String direction, String verificationData, Boolean result) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[反潜回记录] 记录事件:userId={}, deviceId={}, areaId={}, allowed={}",
                        userId, deviceId, areaId, result);

                // 验证参数
                if (userId == null || deviceId == null || areaId == null || direction == null || result == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层记录访问事件
                antiPassbackManager.recordAccessEvent(userId, deviceId, areaId, direction, verificationData, result);

                // 如果访问被拒绝，记录违规事件
                if (!result) {
                    antiPassbackManager.recordViolation(userId, deviceId, areaId, "ACCESS_DENIED", "反潜回检查拒绝访问");
                }

                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回记录] 记录异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("RECORD_ACCESS_ERROR", "记录访问事件异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Object>> getUserAntiPassbackStatus(Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[反潜回状态] 获取状态:userId={}", userId);

                // 使用Manager层获取用户状态
                Object status = antiPassbackManager.getUserAntiPassbackStatus(userId);

                return ResponseDTO.ok(status);

            } catch (Exception e) {
                log.error("[反潜回状态] 获取状态异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("GET_STATUS_ERROR", "获取状态异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            Long userId, Long deviceId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回清理] 清理记录:userId={}, deviceId={}", userId, deviceId);

                // 验证参数
                if (userId == null || deviceId == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层清理用户记录
                antiPassbackManager.clearUserAntiPassbackRecords(userId, deviceId);

                log.info("[反潜回清理] 清理完成:userId={}, deviceId={}", userId, deviceId);
                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回清理] 清理异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("CLEAR_RECORDS_ERROR", "清理记录异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Void>> handleAntiPassbackViolation(
            Long userId, Long deviceId, AntiPassbackResult antiPassbackResult) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回违规处理] 处理违规:userId={}, deviceId={}", userId, deviceId);

                // 验证参数
                if (userId == null || deviceId == null || antiPassbackResult == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层处理违规
                antiPassbackManager.handleAntiPassbackViolation(userId, deviceId, antiPassbackResult);

                log.info("[反潜回违规处理] 处理完成:userId={}, deviceId={}", userId, deviceId);
                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回违规处理] 处理异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("HANDLE_VIOLATION_ERROR", "处理违规异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Void>> resetUserAntiPassbackStatus(
            Long userId, Long operatorId, String reason) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回重置] 重置状态:userId={}, operatorId={}, reason={}", userId, operatorId, reason);

                // 验证参数
                if (userId == null || operatorId == null) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "参数不完整");
                }

                // 使用Manager层重置状态
                antiPassbackManager.resetUserAntiPassbackStatus(userId, operatorId, reason);

                log.info("[反潜回重置] 重置完成:userId={}, operatorId={}", userId, operatorId);
                return ResponseDTO.ok();

            } catch (Exception e) {
                log.error("[反潜回重置] 重置异常:userId={}, error={}", userId, e.getMessage(), e);
                return ResponseDTO.error("RESET_STATUS_ERROR", "重置状态异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackStatistics(
            String startTime, String endTime) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[反潜回统计] 获取统计:startTime={}, endTime={}", startTime, endTime);

                // 使用Manager层获取统计数据
                Object statistics = antiPassbackManager.getAntiPassbackStatistics(
                        LocalDateTime.parse(startTime),
                        LocalDateTime.parse(endTime)
                );

                return ResponseDTO.ok(statistics);

            } catch (Exception e) {
                log.error("[反潜回统计] 获取统计异常:startTime={}, endTime={}, error={}", startTime, endTime, e.getMessage(), e);
                return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计数据异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackViolationReport(
            Long deviceId, String startTime, String endTime) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[反潜回报告] 获取报告:deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

                // 使用Manager层获取违规报告
                Object report = antiPassbackManager.getAntiPassbackViolationReport(
                        deviceId,
                        LocalDateTime.parse(startTime),
                        LocalDateTime.parse(endTime)
                );

                return ResponseDTO.ok(report);

            } catch (Exception e) {
                log.error("[反潜回报告] 获取报告异常:deviceId={}, error={}", deviceId, e.getMessage(), e);
                return ResponseDTO.error("GET_VIOLATION_REPORT_ERROR", "获取违规报告异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchCheckAntiPassbackStatus(
            String userIds) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回批量检查] 批量检查:userIds={}", userIds);

                // 验证参数
                if (userIds == null || userIds.trim().isEmpty()) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "用户ID列表不能为空");
                }

                // 解析用户ID列表
                List<Long> userIdList = Arrays.stream(userIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                // 使用Manager层进行批量检查
                Map<Long, Object> results = new HashMap<>();
                for (Long userId : userIdList) {
                    try {
                        AntiPassbackResult result = checkHardAntiPassback(userId, null, null, null, null);
                        results.put(userId, result);
                    } catch (Exception e) {
                        results.put(userId, AntiPassbackResult.failure("批量检查异常: " + e.getMessage()));
                    }
                }

                return ResponseDTO.ok(results);

            } catch (Exception e) {
                log.error("[反潜回批量检查] 批量检查异常:userIds={}, error={}", userIds, e.getMessage(), e);
                return ResponseDTO.error("BATCH_CHECK_ERROR", "批量检查异常: " + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchClearAntiPassbackRecords(
            String userIds) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[反潜回批量清理] 批量清理:userIds={}", userIds);

                // 验证参数
                if (userIds == null || userIds.trim().isEmpty()) {
                    return ResponseDTO.error("INVALID_PARAMETERS", "用户ID列表不能为空");
                }

                // 解析用户ID列表
                List<Long> userIdList = Arrays.stream(userIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                Map<Long, Object> results = new HashMap<>();
                for (Long userId : userIdList) {
                    try {
                        clearUserAntiPassbackRecords(userId, 0L);
                        results.put(userId, "清理成功");
                    } catch (Exception e) {
                        results.put(userId, "清理异常: " + e.getMessage());
                    }
                }

                return ResponseDTO.ok(results);

            } catch (Exception e) {
                log.error("[反潜回批量清理] 批量清理异常:userIds={}, error={}", userIds, e.getMessage(), e);
                return ResponseDTO.error("BATCH_CLEAR_ERROR", "批量清理异常: " + e.getMessage());
            }
        });
    }

    // ==================== 私有方法：硬反潜回实现 ====================

    /**
     * 硬反潜回检查
     * 严格禁止用户在时间窗口内重复进入
     */
    private AntiPassbackResult checkHardAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "hard:" + userId;

        // 检查用户最近访问记录
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                return AntiPassbackResult.failure("硬反潜回：检测到时间窗口内的重复访问");
            }
        }

        // 记录当前访问
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        return AntiPassbackResult.success("硬反潜回：检查通过");
    }

    // ==================== 私有方法：软反潜回实现 ====================

    /**
     * 软反潜回检查
     * 允许重复访问但记录异常
     */
    private AntiPassbackResult checkSoftAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "soft:" + userId;

        // 检查用户最近访问记录
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        boolean isException = false;

        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                isException = true;
                log.warn("[软反潜回] 检测到异常访问:userId={}, deviceId={}, lastTime={}",
                        userId, deviceId, lastAccessTime);

                // 记录异常事件
                recordSoftException(userId, deviceId, areaId);
            }
        }

        // 记录当前访问
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        String message = isException ? "软反潜回：检测到异常访问但已记录" : "软反潜回：检查通过";
        return AntiPassbackResult.success(message);
    }

    // ==================== 私有方法：全局反潜回实现 ====================

    /**
     * 全局反潜回检查
     * 跨设备检查用户访问状态
     */
    private AntiPassbackResult checkGlobalAntiPassback(Long userId, Long deviceId, Long areaId,
                                                     DeviceEntity device, AreaEntity area) {
        String cacheKey = GLOBAL_USER_ACCESS_PREFIX + userId;

        // 获取用户全局访问记录
        List<String> recentAccesses = (List<String>) redisTemplate.opsForValue().get(cacheKey);

        if (recentAccesses != null && !recentAccesses.isEmpty()) {
            // 检查用户是否在时间窗口内在多个设备上访问
            LocalDateTime now = LocalDateTime.now();
            for (String accessStr : recentAccesses) {
                String[] accessInfo = accessStr.split(":");
                LocalDateTime accessTime = LocalDateTime.parse(accessInfo[0]);

                if (Duration.between(accessTime, now).toMinutes() < GLOBAL_ANTI_PASSBACK_TIME_WINDOW) {
                    return AntiPassbackResult.failure("全局反潜回：检测到跨设备并发访问");
                }
            }
        }

        // 记录当前访问
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

        return AntiPassbackResult.success("全局反潜回：检查通过");
    }

    // ==================== 私有方法：访问记录缓存管理 ====================

    /**
     * 通过网关获取设备信息
     */
    private DeviceEntity getDeviceById(Long deviceId) {
        try {
            // 使用Manager层获取设备信息
            return antiPassbackManager.getDeviceById(String.valueOf(deviceId));
        } catch (Exception e) {
            log.error("[设备信息获取] 获取设备信息失败:deviceId={}, error={}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过网关获取区域信息
     */
    private AreaEntity getAreaById(Long areaId) {
        try {
            // 使用Manager层获取区域信息
            return antiPassbackManager.getAreaById(areaId);
        } catch (Exception e) {
            log.error("[区域信息获取] 获取区域信息失败:areaId={}, error={}", areaId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 记录软反潜回异常
     */
    private void recordSoftException(Long userId, Long deviceId, Long areaId) {
        log.warn("[软反潜回异常] userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        // 记录异常事件到Redis
        String exceptionKey = ANTI_PASSBACK_PREFIX + "soft_exception:" + userId;
        String exceptionData = LocalDateTime.now() + ":" + deviceId + ":" + areaId;

        redisTemplate.opsForList().rightPush(exceptionKey, exceptionData);
        redisTemplate.expire(exceptionKey, Duration.ofHours(24));
    }

    // ==================== 降级处理方法 ====================

    /**
     * 反潜回检查降级处理
     */
    default CompletableFuture<ResponseDTO<AntiPassbackResult>> performAntiPassbackCheckFallback(
            Long userId, Long deviceId, Long areaId, String verificationData, Exception e) {
        log.warn("[反潜回检查] 服务降级: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.ok(AntiPassbackResult.success("反潜回检查服务暂时不可用，允许通行"))
        );
    }

    /**
     * 区域反潜回检查降级处理
     */
    default CompletableFuture<ResponseDTO<AntiPassbackResult>> checkAreaAntiPassbackFallback(
            Long userId, Long entryAreaId, Long exitAreaId, String direction, Exception e) {
        log.warn("[区域反潜回检查] 服务降级: userId={}, areaId={}, error={}", userId, entryAreaId, e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.ok(AntiPassbackResult.success("区域反潜回检查服务暂时不可用，允许通行"))
        );
    }

    /**
     * 反潜回操作降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> antiPassbackOperationFallback(
            Exception exception, Object... params) {
        log.warn("[反潜回操作] 服务降级: params={}, error={}", Arrays.toString(params), exception.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTI_PASSBACK_SERVICE_UNAVAILABLE", "反潜回服务暂时不可用，操作稍后重试")
        );
    }

    /**
     * 批量操作降级处理
     */
    default CompletableFuture<ResponseDTO<Map<Long, Object>>> antiPassbackBatchOperationFallback(
            String params, Exception exception) {
        log.warn("[反潜回批量操作] 服务降级: params={}, error={}", params, exception.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTI_PASSBACK_BATCH_SERVICE_UNAVAILABLE", "批量反潜回服务暂时不可用，稍后重试")
        );
    }
}