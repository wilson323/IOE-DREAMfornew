package net.lab1024.sa.admin.module.access.manager;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.constant.CacheKeyConst;
import net.lab1024.sa.base.common.util.SmartRedisUtil;

import java.util.concurrent.TimeUnit;

/**
 * 访客管理缓存管理器
 * <p>
 * 严格遵循repowiki规范：
 * - 使用Redis进行缓存管理
 * - 合理的缓存过期时间
 * - 缓存键命名规范
 * - 异常处理和日志记录
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class VisitorCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final long QR_CODE_EXPIRE_TIME = 24 * 60 * 60; // 24小时
    private static final long RESERVATION_CACHE_TIME = 30 * 60; // 30分钟
    private static final long STATISTICS_CACHE_TIME = 10 * 60; // 10分钟
    private static final long BLACKLIST_CACHE_TIME = 60 * 60; // 1小时

    /**
     * 缓存访客二维码
     *
     * @param reservationId 预约ID
     * @param qrCode        二维码内容
     * @param expireTime    过期时间（毫秒）
     */
    public void cacheQrCode(Long reservationId, String qrCode, long expireTime) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_QR_CODE + reservationId;
            SmartRedisUtil.set(key, qrCode, expireTime, TimeUnit.MILLISECONDS);

            // 建立反向索引，从二维码获取预约ID
            String reverseKey = CacheKeyConst.Visitor.VISITOR_QR_REVERSE + qrCode;
            SmartRedisUtil.set(reverseKey, reservationId.toString(), expireTime, TimeUnit.MILLISECONDS);

            log.debug("缓存访客二维码: reservationId={}", reservationId);
        } catch (Exception e) {
            log.error("缓存访客二维码失败: reservationId={}", reservationId, e);
        }
    }

    /**
     * 获取缓存的二维码
     *
     * @param reservationId 预约ID
     * @return 二维码内容
     */
    public String getQrCode(Long reservationId) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_QR_CODE + reservationId;
            String qrCode = SmartRedisUtil.get(key);
            log.debug("获取缓存的二维码: reservationId={}, result={}", reservationId, qrCode != null ? "success" : "not found");
            return qrCode;
        } catch (Exception e) {
            log.error("获取缓存的二维码失败: reservationId={}", reservationId, e);
            return null;
        }
    }

    /**
     * 从二维码获取预约ID
     *
     * @param qrCode 二维码内容
     * @return 预约ID
     */
    public Long getReservationIdFromQrCode(String qrCode) {
        try {
            String reverseKey = CacheKeyConst.Visitor.VISITOR_QR_REVERSE + qrCode;
            String reservationIdStr = SmartRedisUtil.get(reverseKey);
            if (reservationIdStr != null) {
                return Long.valueOf(reservationIdStr);
            }
            return null;
        } catch (Exception e) {
            log.error("从二维码获取预约ID失败: qrCode={}", qrCode, e);
            return null;
        }
    }

    /**
     * 删除二维码缓存
     *
     * @param reservationId 预约ID
     */
    public void removeQrCode(Long reservationId) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_QR_CODE + reservationId;
            String qrCode = SmartRedisUtil.get(key);

            if (qrCode != null) {
                // 删除正向索引
                SmartRedisUtil.delete(key);

                // 删除反向索引
                String reverseKey = CacheKeyConst.Visitor.VISITOR_QR_REVERSE + qrCode;
                SmartRedisUtil.delete(reverseKey);

                log.debug("删除二维码缓存: reservationId={}", reservationId);
            }
        } catch (Exception e) {
            log.error("删除二维码缓存失败: reservationId={}", reservationId, e);
        }
    }

    /**
     * 缓存访客预约信息
     *
     * @param reservationId 预约ID
     * @param reservationData 预约数据
     */
    public void cacheReservation(Long reservationId, Object reservationData) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_RESERVATION + reservationId;
            SmartRedisUtil.setObj(key, reservationData, RESERVATION_CACHE_TIME, TimeUnit.SECONDS);
            log.debug("缓存访客预约信息: reservationId={}", reservationId);
        } catch (Exception e) {
            log.error("缓存访客预约信息失败: reservationId={}", reservationId, e);
        }
    }

    /**
     * 获取缓存的访客预约信息
     *
     * @param reservationId 预约ID
     * @return 预约数据
     */
    public Object getCachedReservation(Long reservationId) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_RESERVATION + reservationId;
            Object data = SmartRedisUtil.getObj(key);
            log.debug("获取缓存的访客预约信息: reservationId={}, result={}", reservationId, data != null ? "success" : "not found");
            return data;
        } catch (Exception e) {
            log.error("获取缓存的访客预约信息失败: reservationId={}", reservationId, e);
            return null;
        }
    }

    /**
     * 删除预约缓存
     *
     * @param reservationId 预约ID
     */
    public void removeReservationCache(Long reservationId) {
        try {
            String key = CacheKeyConst.Visitor.VISITOR_RESERVATION + reservationId;
            SmartRedisUtil.delete(key);
            log.debug("删除预约缓存: reservationId={}", reservationId);
        } catch (Exception e) {
            log.error("删除预约缓存失败: reservationId={}", reservationId, e);
        }
    }

    /**
     * 清除所有访客预约缓存
     */
    public void clearReservationCache() {
        try {
            // 这里可以根据需要清除特定模式的缓存
            // 暂时不实现，避免误删其他缓存
            log.info("清除访客预约缓存（暂未实现具体逻辑）");
        } catch (Exception e) {
            log.error("清除访客预约缓存失败", e);
        }
    }

    /**
     * 缓存访客统计数据
     *
     * @param cacheKey     缓存键
     * @param statistics    统计数据
     */
    public void cacheStatistics(String cacheKey, Object statistics) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_STATISTICS + cacheKey;
            SmartRedisUtil.setObj(fullKey, statistics, STATISTICS_CACHE_TIME, TimeUnit.SECONDS);
            log.debug("缓存访客统计数据: cacheKey={}", cacheKey);
        } catch (Exception e) {
            log.error("缓存访客统计数据失败: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 获取缓存的访客统计数据
     *
     * @param cacheKey 缓存键
     * @return 统计数据
     */
    public Object getCachedStatistics(String cacheKey) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_STATISTICS + cacheKey;
            Object data = SmartRedisUtil.getObj(fullKey);
            log.debug("获取缓存的访客统计数据: cacheKey={}, result={}", cacheKey, data != null ? "success" : "not found");
            return data;
        } catch (Exception e) {
            log.error("获取缓存的访客统计数据失败: cacheKey={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 缓存访客黑名单信息
     *
     * @param visitorKey   访客标识（手机号或身份证号）
     * @param blacklistData 黑名单数据
     */
    public void cacheBlacklist(String visitorKey, Object blacklistData) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_BLACKLIST + visitorKey;
            SmartRedisUtil.setObj(fullKey, blacklistData, BLACKLIST_CACHE_TIME, TimeUnit.SECONDS);
            log.debug("缓存访客黑名单信息: visitorKey={}", visitorKey);
        } catch (Exception e) {
            log.error("缓存访客黑名单信息失败: visitorKey={}", visitorKey, e);
        }
    }

    /**
     * 获取缓存的访客黑名单信息
     *
     * @param visitorKey 访客标识（手机号或身份证号）
     * @return 黑名单数据
     */
    public Object getCachedBlacklist(String visitorKey) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_BLACKLIST + visitorKey;
            Object data = SmartRedisUtil.getObj(fullKey);
            log.debug("获取缓存的访客黑名单信息: visitorKey={}, result={}", visitorKey, data != null ? "found" : "not found");
            return data;
        } catch (Exception e) {
            log.error("获取缓存的访客黑名单信息失败: visitorKey={}", visitorKey, e);
            return null;
        }
    }

    /**
     * 删除黑名单缓存
     *
     * @param visitorKey 访客标识
     */
    public void removeBlacklistCache(String visitorKey) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_BLACKLIST + visitorKey;
            SmartRedisUtil.delete(fullKey);
            log.debug("删除黑名单缓存: visitorKey={}", visitorKey);
        } catch (Exception e) {
            log.error("删除黑名单缓存失败: visitorKey={}", visitorKey, e);
        }
    }

    /**
     * 缓存访客访问次数
     *
     * @param visitorKey 访客标识
     * @param count       访问次数
     * @param expireTime  过期时间（秒）
     */
    public void cacheVisitCount(String visitorKey, Integer count, long expireTime) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_COUNT + visitorKey;
            SmartRedisUtil.set(fullKey, count.toString(), expireTime, TimeUnit.SECONDS);
            log.debug("缓存访客访问次数: visitorKey={}, count={}", visitorKey, count);
        } catch (Exception e) {
            log.error("缓存访客访问次数失败: visitorKey={}, count={}", visitorKey, count, e);
        }
    }

    /**
     * 获取访客访问次数
     *
     * @param visitorKey 访客标识
     * @return 访问次数
     */
    public Integer getVisitCount(String visitorKey) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_COUNT + visitorKey;
            String countStr = SmartRedisUtil.get(fullKey);
            if (countStr != null) {
                return Integer.valueOf(countStr);
            }
            return 0;
        } catch (Exception e) {
            log.error("获取访客访问次数失败: visitorKey={}", visitorKey, e);
            return 0;
        }
    }

    /**
     * 增加访客访问次数
     *
     * @param visitorKey 访客标识
     * @param expireTime  过期时间（秒）
     * @return 增加后的访问次数
     */
    public Integer incrementVisitCount(String visitorKey, long expireTime) {
        try {
            String fullKey = CacheKeyConst.Visitor.VISITOR_COUNT + visitorKey;
            Long count = redisTemplate.opsForValue().increment(fullKey);

            // 设置过期时间（仅在第一次设置时）
            if (count == 1) {
                redisTemplate.expire(fullKey, expireTime, TimeUnit.SECONDS);
            }

            log.debug("增加访客访问次数: visitorKey={}, count={}", visitorKey, count);
            return count.intValue();
        } catch (Exception e) {
            log.error("增加访客访问次数失败: visitorKey={}", visitorKey, e);
            return 0;
        }
    }

    /**
     * 检查访客是否在缓存中
     *
     * @param visitorKey 访客标识
     * @return 是否存在
     */
    public boolean existsInCache(String visitorKey) {
        try {
            // 检查多个缓存键
            String[] cacheKeys = {
                CacheKeyConst.Visitor.VISITOR_BLACKLIST + visitorKey,
                CacheKeyConst.Visitor.VISITOR_COUNT + visitorKey,
                CacheKeyConst.Visitor.VISITOR_RESERVATION + visitorKey
            };

            for (String keySuffix : cacheKeys) {
                if (SmartRedisUtil.hasKey(keySuffix)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("检查访客缓存失败: visitorKey={}", visitorKey, e);
            return false;
        }
    }

    /**
     * 清理过期的访客缓存
     */
    public void cleanupExpiredCache() {
        try {
            // 这里可以实现定时清理过期缓存的逻辑
            // 暂时只记录日志
            log.info("清理过期的访客缓存（暂未实现具体逻辑）");
        } catch (Exception e) {
            log.error("清理过期的访客缓存失败", e);
        }
    }
}