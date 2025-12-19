package net.lab1024.sa.access.strategy.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessCacheConstants;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.manager.AntiPassbackManager;
import net.lab1024.sa.access.strategy.VerificationModeStrategy;
import net.lab1024.sa.access.util.AccessRecordIdempotencyUtil;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 设备端验证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现VerificationModeStrategy接口
 * - 使用@Resource注入依赖
 * - 使用Resilience4j进行容错处理
 * </p>
 * <p>
 * 核心职责：
 * - 处理设备端验证后的记录接收
 * - 验证记录的有效性
 * - 支持离线验证记录缓存
 * - 存储记录到数据库
 * - 异常处理和重试机制
 * </p>
 * <p>
 * 工作流程：
 * 1. 设备端已完成识别+验证+开门
 * 2. 设备批量上传通行记录
 * 3. 软件端接收记录并存储
 * 4. 进行异常检测和统计分析
 * </p>
 * <p>
 * 离线支持机制：
 * - 网络故障时，记录缓存到Redis
 * - 网络恢复后，自动补录缓存记录
 * - 支持记录去重（幂等性检查）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class EdgeVerificationStrategy implements VerificationModeStrategy {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AntiPassbackManager antiPassbackManager;

    /**
     * 缓存键前缀和过期时间统一使用AccessCacheConstants
     */

    /**
     * 执行验证（设备端验证模式）
     * <p>
     * 设备端已完成验证和开门，软件端只负责接收和存储记录
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @Override
    @Retry(name = "edge-verification-retry", fallbackMethod = "verifyFallback")
    @CircuitBreaker(name = "edge-verification-circuitbreaker", fallbackMethod = "verifyFallback")
    @Transactional(rollbackFor = Exception.class)
    public VerificationResult verify(AccessVerificationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("[设备端验证] 开始接收通行记录: userId={}, deviceId={}, areaId={}, event={}, verifyType={}, verifyTime={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId(),
                request.getEvent(), request.getVerifyType(), request.getVerifyTime());

        try {
            // 1. 验证记录有效性
            ValidationResult validation = validateRecord(request);
            if (!validation.isValid()) {
                log.warn("[设备端验证] 记录验证失败: userId={}, deviceId={}, reason={}",
                        request.getUserId(), request.getDeviceId(), validation.getReason());
                return VerificationResult.failed("INVALID_RECORD", validation.getReason());
            }

            // 2. 生成记录唯一标识（使用统一工具类）
            LocalDateTime verifyTime = request.getVerifyTime() != null ?
                    request.getVerifyTime() : LocalDateTime.now();
            String recordUniqueId = AccessRecordIdempotencyUtil.generateRecordUniqueId(
                    request.getUserId(), request.getDeviceId(), verifyTime);

            // 3. 幂等性检查（使用统一工具类，防止重复记录）
            if (AccessRecordIdempotencyUtil.isDuplicateRecord(
                    recordUniqueId, request.getUserId(), request.getDeviceId(), verifyTime,
                    redisTemplate, accessRecordDao)) {
                log.warn("[设备端验证] 记录重复（幂等性检查）: userId={}, deviceId={}, recordUniqueId={}",
                        request.getUserId(), request.getDeviceId(), recordUniqueId);
                return VerificationResult.success("记录已存在（重复）", null, "edge");
            }

            // 4. 尝试存储记录到数据库
            boolean stored = storeRecord(request, recordUniqueId);

            if (stored) {
                // 5. 更新记录唯一标识缓存（使用统一工具类）
                AccessRecordIdempotencyUtil.updateRecordUniqueIdCache(recordUniqueId, redisTemplate);

                long duration = System.currentTimeMillis() - startTime;
                log.info("[设备端验证] 记录接收成功: userId={}, deviceId={}, recordUniqueId={}, duration={}ms",
                        request.getUserId(), request.getDeviceId(), recordUniqueId, duration);
                return VerificationResult.success("记录接收成功", null, "edge");
            } else {
                // 6. 数据库存储失败，缓存到Redis（离线支持）
                cacheOfflineRecord(request, recordUniqueId);

                long duration = System.currentTimeMillis() - startTime;
                log.warn("[设备端验证] 数据库存储失败，已缓存到Redis: userId={}, deviceId={}, recordUniqueId={}, duration={}ms",
                        request.getUserId(), request.getDeviceId(), recordUniqueId, duration);
                return VerificationResult.success("记录已缓存（离线模式）", null, "edge");
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[设备端验证] 记录处理异常: userId={}, deviceId={}, duration={}ms, error={}",
                    request.getUserId(), request.getDeviceId(), duration, e.getMessage(), e);

            // 异常时也尝试缓存到Redis（离线支持）
            try {
                LocalDateTime verifyTime = request.getVerifyTime() != null ?
                        request.getVerifyTime() : LocalDateTime.now();
                String recordUniqueId = AccessRecordIdempotencyUtil.generateRecordUniqueId(
                        request.getUserId(), request.getDeviceId(), verifyTime);
                cacheOfflineRecord(request, recordUniqueId);
                log.info("[设备端验证] 异常时已缓存到Redis: userId={}, deviceId={}, recordUniqueId={}",
                        request.getUserId(), request.getDeviceId(), recordUniqueId);
            } catch (Exception cacheException) {
                log.error("[设备端验证] 缓存离线记录失败: userId={}, deviceId={}, error={}",
                        request.getUserId(), request.getDeviceId(), cacheException.getMessage(), cacheException);
            }

            return VerificationResult.failed("SYSTEM_ERROR", "系统异常，记录已缓存，请稍后重试");
        }
    }

    /**
     * 验证记录有效性
     *
     * @param request 验证请求
     * @return 验证结果
     */
    private ValidationResult validateRecord(AccessVerificationRequest request) {
        // 1. 必填字段检查
        if (request.getUserId() == null) {
            return ValidationResult.invalid("用户ID不能为空");
        }
        if (request.getDeviceId() == null) {
            return ValidationResult.invalid("设备ID不能为空");
        }
        if (request.getAreaId() == null) {
            return ValidationResult.invalid("区域ID不能为空");
        }

        // 2. 时间有效性检查
        LocalDateTime verifyTime = request.getVerifyTime();
        if (verifyTime == null) {
            verifyTime = LocalDateTime.now();
        }

        // 检查时间是否在未来（允许5分钟误差）
        LocalDateTime now = LocalDateTime.now();
        if (verifyTime.isAfter(now.plusMinutes(5))) {
            return ValidationResult.invalid("验证时间不能在未来");
        }

        // 检查时间是否过于久远（超过7天的记录视为无效）
        if (verifyTime.isBefore(now.minusDays(7))) {
            return ValidationResult.invalid("验证时间过于久远（超过7天）");
        }

        return ValidationResult.valid();
    }


    /**
     * 存储记录到数据库
     *
     * @param request 验证请求
     * @param recordUniqueId 记录唯一标识
     * @return 是否存储成功
     */
    private boolean storeRecord(AccessVerificationRequest request, String recordUniqueId) {
        try {
            // 1. 转换为Entity
            AccessRecordEntity entity = convertToEntity(request, recordUniqueId);

            // 2. 再次检查数据库（防止并发重复）
            AccessRecordEntity existing = accessRecordDao.selectByCompositeKey(
                    entity.getUserId(), entity.getDeviceId(), entity.getAccessTime());
            if (existing != null) {
                log.warn("[设备端验证] 记录重复（数据库）: userId={}, deviceId={}, accessTime={}",
                        entity.getUserId(), entity.getDeviceId(), entity.getAccessTime());
                // 更新缓存（使用统一工具类）
                AccessRecordIdempotencyUtil.updateRecordUniqueIdCache(recordUniqueId, redisTemplate);
                return true; // 视为成功（记录已存在）
            }

            // 3. 插入数据库
            int insertCount = accessRecordDao.insert(entity);
            if (insertCount > 0) {
                log.debug("[设备端验证] 记录已存储到数据库: recordId={}, userId={}, deviceId={}",
                        entity.getRecordId(), entity.getUserId(), entity.getDeviceId());

                // 4. ⚠️ 边缘验证模式：只记录反潜回信息，不验证
                // 原因：设备端已完成单设备内反潜回验证，软件端无法跨设备验证
                // 用途：用于统计、分析和审计
                if (request.getInOutStatus() != null) {
                    try {
                        antiPassbackManager.recordAntiPassback(
                                request.getUserId(),
                                request.getDeviceId(),
                                request.getAreaId(),
                                request.getInOutStatus(),
                                request.getVerifyType()
                        );
                        log.debug("[设备端验证] 反潜回信息已记录: userId={}, deviceId={}, inOutStatus={}",
                                request.getUserId(), request.getDeviceId(), request.getInOutStatus());
                    } catch (Exception e) {
                        // 反潜回记录失败不影响主流程，只记录警告日志
                        log.warn("[设备端验证] 记录反潜回信息失败: userId={}, deviceId={}, error={}",
                                request.getUserId(), request.getDeviceId(), e.getMessage());
                    }
                }

                return true;
            } else {
                log.warn("[设备端验证] 数据库插入失败: userId={}, deviceId={}",
                        entity.getUserId(), entity.getDeviceId());
                return false;
            }

        } catch (Exception e) {
            log.error("[设备端验证] 存储记录异常: userId={}, deviceId={}, error={}",
                    request.getUserId(), request.getDeviceId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 转换为Entity
     *
     * @param request 验证请求
     * @param recordUniqueId 记录唯一标识
     * @return 实体对象
     */
    private AccessRecordEntity convertToEntity(AccessVerificationRequest request, String recordUniqueId) {
        AccessRecordEntity entity = new AccessRecordEntity();
        entity.setUserId(request.getUserId());
        entity.setDeviceId(request.getDeviceId());
        entity.setAreaId(request.getAreaId());

        // 通行结果（设备端验证模式下，设备已通过验证，默认为成功）
        entity.setAccessResult(1); // 1=成功

        // 通行时间
        entity.setAccessTime(request.getVerifyTime() != null ?
                request.getVerifyTime() : LocalDateTime.now());

        // 通行类型（根据inOutStatus判断）
        if (request.getInOutStatus() != null) {
            entity.setAccessType(request.getInOutStatus() == 1 ? "IN" : "OUT");
        } else {
            entity.setAccessType("IN"); // 默认进入
        }

        // 验证方式（使用VerifyTypeEnum统一管理）
        if (request.getVerifyType() != null) {
            VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.getByCode(request.getVerifyType());
            if (verifyTypeEnum != null) {
                entity.setVerifyMethod(verifyTypeEnum.getName());
            } else {
                log.warn("[设备端验证] 不支持的认证方式: verifyType={}, 使用默认值CARD", request.getVerifyType());
                entity.setVerifyMethod("CARD"); // 默认卡片
            }
        } else {
            entity.setVerifyMethod("CARD"); // 默认刷卡
        }

        // 记录唯一标识（用于幂等性检查）
        entity.setRecordUniqueId(recordUniqueId);

        return entity;
    }

    /**
     * 缓存离线记录到Redis
     * <p>
     * 当数据库存储失败时，缓存到Redis，等待后续补录
     * </p>
     *
     * @param request 验证请求
     * @param recordUniqueId 记录唯一标识
     */
    private void cacheOfflineRecord(AccessVerificationRequest request, String recordUniqueId) {
        try {
            // 1. 转换为Entity
            AccessRecordEntity entity = convertToEntity(request, recordUniqueId);

            // 2. 缓存记录
            String cacheKey = AccessCacheConstants.buildOfflineRecordKey(recordUniqueId);
            redisTemplate.opsForValue().set(cacheKey, entity, AccessCacheConstants.CACHE_EXPIRE_OFFLINE_RECORD);

            // 3. 添加到离线队列（用于批量补录）
            redisTemplate.opsForList().rightPush(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, recordUniqueId);

            // 设置队列过期时间（7天）
            redisTemplate.expire(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, AccessCacheConstants.CACHE_EXPIRE_OFFLINE_RECORD);

            log.info("[设备端验证] 离线记录已缓存: recordUniqueId={}, userId={}, deviceId={}",
                    recordUniqueId, request.getUserId(), request.getDeviceId());

        } catch (Exception e) {
            log.error("[设备端验证] 缓存离线记录失败: recordUniqueId={}, userId={}, deviceId={}, error={}",
                    recordUniqueId, request.getUserId(), request.getDeviceId(), e.getMessage(), e);
            throw e; // 重新抛出异常，触发降级处理
        }
    }


    /**
     * 降级处理方法（Resilience4j Fallback）
     * <p>
     * 当重试或熔断触发时，使用此方法处理
     * </p>
     *
     * @param request 验证请求
     * @param exception 异常
     * @return 验证结果
     */
    private VerificationResult verifyFallback(AccessVerificationRequest request, Exception exception) {
        log.warn("[设备端验证] 触发降级处理: userId={}, deviceId={}, error={}",
                request.getUserId(), request.getDeviceId(), exception.getMessage());

        try {
            // 降级时缓存到Redis
            LocalDateTime verifyTime = request.getVerifyTime() != null ?
                    request.getVerifyTime() : LocalDateTime.now();
            String recordUniqueId = AccessRecordIdempotencyUtil.generateRecordUniqueId(
                    request.getUserId(), request.getDeviceId(), verifyTime);
            cacheOfflineRecord(request, recordUniqueId);

            log.info("[设备端验证] 降级处理：记录已缓存到Redis: recordUniqueId={}", recordUniqueId);
            return VerificationResult.success("记录已缓存（降级模式）", null, "edge");
        } catch (Exception e) {
            log.error("[设备端验证] 降级处理失败: userId={}, deviceId={}, error={}",
                    request.getUserId(), request.getDeviceId(), e.getMessage(), e);
            return VerificationResult.failed("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public boolean supports(String mode) {
        return "edge".equals(mode);
    }

    @Override
    public String getStrategyName() {
        return "EdgeVerificationStrategy";
    }

    /**
     * 验证结果内部类
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }
}
