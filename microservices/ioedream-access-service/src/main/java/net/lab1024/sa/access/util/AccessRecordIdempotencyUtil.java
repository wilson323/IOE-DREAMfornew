package net.lab1024.sa.access.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessCacheConstants;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 门禁记录幂等性工具类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 工具类，不依赖Spring框架（但可以注入Spring Bean）
 * - 统一处理记录唯一标识生成和幂等性检查
 * - 避免代码冗余，提高复用性
 * </p>
 * <p>
 * 核心功能：
 * - 生成记录唯一标识（统一格式）
 * - 幂等性检查（Redis + 数据库双重检查）
 * - 更新记录唯一标识缓存
 * </p>
 * <p>
 * 使用场景：
 * - EdgeVerificationStrategy（单条记录）
 * - AccessRecordBatchServiceImpl（批量记录）
 * - EdgeOfflineRecordReplayService（离线记录补录）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AccessRecordIdempotencyUtil {

    /**
     * 缓存键前缀和过期时间统一使用AccessCacheConstants
     * 保持向后兼容，保留常量定义（标记为@Deprecated）
     */
    @Deprecated
    public static final String CACHE_KEY_RECORD_UNIQUE = AccessCacheConstants.CACHE_KEY_RECORD_UNIQUE;

    @Deprecated
    public static final Duration CACHE_EXPIRE_RECORD_UNIQUE = AccessCacheConstants.CACHE_EXPIRE_RECORD_UNIQUE;

    /**
     * 时间格式化器（统一格式）
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 生成记录唯一标识
     * <p>
     * 统一格式：REC_{userId}_{deviceId}_{accessTime}_{hash}
     * 使用 userId + deviceId + accessTime 的组合作为唯一标识
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param accessTime 通行时间
     * @return 记录唯一标识
     */
    public static String generateRecordUniqueId(Long userId, Long deviceId, LocalDateTime accessTime) {
        if (userId == null || deviceId == null) {
            throw new IllegalArgumentException("userId和deviceId不能为空");
        }

        LocalDateTime time = accessTime != null ? accessTime : LocalDateTime.now();
        String timeStr = time.format(TIME_FORMATTER);

        // 使用hashCode生成短哈希（避免过长）
        String hash = String.valueOf((userId + deviceId + timeStr).hashCode());
        return String.format("REC_%d_%d_%s_%s", userId, deviceId, timeStr, hash);
    }

    /**
     * 检查记录是否重复（幂等性检查）
     * <p>
     * 双重检查机制：
     * 1. Redis缓存快速检查
     * 2. 数据库精确检查
     * </p>
     *
     * @param recordUniqueId 记录唯一标识
     * @param userId 用户ID（用于数据库检查）
     * @param deviceId 设备ID（用于数据库检查）
     * @param accessTime 通行时间（用于数据库检查）
     * @param redisTemplate Redis模板
     * @param accessRecordDao 记录DAO
     * @return 是否重复
     */
    public static boolean isDuplicateRecord(
            String recordUniqueId,
            Long userId,
            Long deviceId,
            LocalDateTime accessTime,
            RedisTemplate<String, Object> redisTemplate,
            AccessRecordDao accessRecordDao) {

        try {
            // 1. 检查Redis缓存（快速检查）
            String cacheKey = AccessCacheConstants.buildRecordUniqueKey(recordUniqueId);
            Boolean exists = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(exists)) {
                log.debug("[幂等性检查] 记录重复（Redis缓存）: recordUniqueId={}", recordUniqueId);
                return true;
            }

            // 2. 检查数据库（确保准确性）
            if (accessRecordDao != null && userId != null && deviceId != null && accessTime != null) {
                AccessRecordEntity existing = accessRecordDao.selectByCompositeKey(userId, deviceId, accessTime);
                if (existing != null) {
                    // 更新Redis缓存
                    redisTemplate.opsForValue().set(cacheKey, "1", AccessCacheConstants.CACHE_EXPIRE_RECORD_UNIQUE);
                    log.debug("[幂等性检查] 记录重复（数据库）: recordUniqueId={}", recordUniqueId);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.warn("[幂等性检查] 检查异常: recordUniqueId={}, error={}", recordUniqueId, e.getMessage());
            // 异常时返回false，继续处理（避免因检查异常而丢失记录）
            return false;
        }
    }

    /**
     * 更新记录唯一标识缓存
     * <p>
     * 记录存储成功后，更新缓存，用于后续幂等性检查
     * </p>
     *
     * @param recordUniqueId 记录唯一标识
     * @param redisTemplate Redis模板
     */
    public static void updateRecordUniqueIdCache(String recordUniqueId, RedisTemplate<String, Object> redisTemplate) {
        try {
            String cacheKey = AccessCacheConstants.buildRecordUniqueKey(recordUniqueId);
            redisTemplate.opsForValue().set(cacheKey, "1", AccessCacheConstants.CACHE_EXPIRE_RECORD_UNIQUE);
            log.debug("[幂等性检查] 记录唯一标识已缓存: recordUniqueId={}", recordUniqueId);
        } catch (Exception e) {
            log.warn("[幂等性检查] 更新缓存失败: recordUniqueId={}, error={}", recordUniqueId, e.getMessage());
            // 不抛出异常，缓存失败不影响主流程
        }
    }
}
