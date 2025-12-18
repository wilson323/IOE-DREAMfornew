package net.lab1024.sa.access.config;

import java.time.Duration;

/**
 * 门禁模块缓存常量类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 统一管理所有缓存键前缀和过期时间
 * - 提高代码可维护性和一致性
 * - 避免缓存键分散在各处
 * </p>
 * <p>
 * 使用说明：
 * - 所有缓存键前缀统一在此定义
 * - 所有缓存过期时间统一在此定义
 * - 新增缓存键时，请在此类中添加常量
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public final class AccessCacheConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private AccessCacheConstants() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 缓存键前缀 ====================

    /**
     * 反潜记录缓存键前缀
     * 格式：access:anti-passback:record:{userId}:{deviceId}
     */
    public static final String CACHE_KEY_ANTI_PASSBACK_RECORD = "access:anti-passback:record:";

    /**
     * 区域配置缓存键前缀
     * 格式：access:area:config:{areaId}
     */
    public static final String CACHE_KEY_AREA_CONFIG = "access:area:config:";

    /**
     * 黑名单缓存键前缀
     * 格式：access:blacklist:user:{userId}
     */
    public static final String CACHE_KEY_BLACKLIST = "access:blacklist:user:";

    /**
     * 多人验证会话缓存键前缀
     * 格式：access:multi-person:session:{areaId}:{deviceId}
     */
    public static final String CACHE_KEY_MULTI_PERSON_SESSION = "access:multi-person:session:";

    /**
     * 设备序列号缓存键前缀
     * 格式：access:device:sn:{serialNumber}
     */
    public static final String CACHE_KEY_DEVICE_SN = "access:device:sn:";

    /**
     * 设备区域缓存键前缀
     * 格式：access:device:area:{deviceId}
     */
    public static final String CACHE_KEY_DEVICE_AREA = "access:device:area:";

    /**
     * 离线记录缓存键前缀
     * 格式：access:edge:offline:record:{recordUniqueId}
     */
    public static final String CACHE_KEY_OFFLINE_RECORD = "access:edge:offline:record:";

    /**
     * 离线记录队列键
     * 格式：access:edge:offline:queue
     */
    public static final String CACHE_KEY_OFFLINE_QUEUE = "access:edge:offline:queue";

    /**
     * 记录唯一标识缓存键前缀
     * 格式：access:record:unique:{recordUniqueId}
     */
    public static final String CACHE_KEY_RECORD_UNIQUE = "access:record:unique:";

    /**
     * 批量上传状态缓存键前缀
     * 格式：access:batch:status:{batchId}
     */
    public static final String CACHE_KEY_BATCH_STATUS = "access:batch:status:";

    // ==================== 缓存过期时间 ====================

    /**
     * 反潜记录缓存过期时间：10分钟
     */
    public static final Duration CACHE_EXPIRE_ANTI_PASSBACK_RECORD = Duration.ofMinutes(10);

    /**
     * 区域配置缓存过期时间：1小时
     */
    public static final Duration CACHE_EXPIRE_AREA_CONFIG = Duration.ofHours(1);

    /**
     * 黑名单缓存过期时间：1小时
     */
    public static final Duration CACHE_EXPIRE_BLACKLIST = Duration.ofHours(1);

    /**
     * 多人验证会话缓存过期时间：5分钟
     */
    public static final Duration CACHE_EXPIRE_MULTI_PERSON_SESSION = Duration.ofMinutes(5);

    /**
     * 设备信息缓存过期时间：1小时
     */
    public static final Duration CACHE_EXPIRE_DEVICE = Duration.ofHours(1);

    /**
     * 离线记录缓存过期时间：7天
     */
    public static final Duration CACHE_EXPIRE_OFFLINE_RECORD = Duration.ofDays(7);

    /**
     * 记录唯一标识缓存过期时间：7天
     */
    public static final Duration CACHE_EXPIRE_RECORD_UNIQUE = Duration.ofDays(7);

    /**
     * 批量上传状态缓存过期时间：24小时
     */
    public static final Duration CACHE_EXPIRE_BATCH_STATUS = Duration.ofHours(24);

    // ==================== 工具方法 ====================

    /**
     * 构建反潜记录缓存键
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 缓存键
     */
    public static String buildAntiPassbackRecordKey(Long userId, Long deviceId) {
        return CACHE_KEY_ANTI_PASSBACK_RECORD + userId + ":" + deviceId;
    }

    /**
     * 构建区域配置缓存键
     *
     * @param areaId 区域ID
     * @return 缓存键
     */
    public static String buildAreaConfigKey(Long areaId) {
        return CACHE_KEY_AREA_CONFIG + areaId;
    }

    /**
     * 构建黑名单缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String buildBlacklistKey(Long userId) {
        return CACHE_KEY_BLACKLIST + userId;
    }

    /**
     * 构建多人验证会话缓存键
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 缓存键
     */
    public static String buildMultiPersonSessionKey(Long areaId, Long deviceId) {
        return CACHE_KEY_MULTI_PERSON_SESSION + areaId + ":" + deviceId;
    }

    /**
     * 构建设备序列号缓存键
     *
     * @param serialNumber 设备序列号
     * @return 缓存键
     */
    public static String buildDeviceSnKey(String serialNumber) {
        return CACHE_KEY_DEVICE_SN + serialNumber;
    }

    /**
     * 构建设备区域缓存键
     *
     * @param deviceId 设备ID
     * @return 缓存键
     */
    public static String buildDeviceAreaKey(String deviceId) {
        return CACHE_KEY_DEVICE_AREA + deviceId;
    }

    /**
     * 构建离线记录缓存键
     *
     * @param recordUniqueId 记录唯一标识
     * @return 缓存键
     */
    public static String buildOfflineRecordKey(String recordUniqueId) {
        return CACHE_KEY_OFFLINE_RECORD + recordUniqueId;
    }

    /**
     * 构建记录唯一标识缓存键
     *
     * @param recordUniqueId 记录唯一标识
     * @return 缓存键
     */
    public static String buildRecordUniqueKey(String recordUniqueId) {
        return CACHE_KEY_RECORD_UNIQUE + recordUniqueId;
    }

    /**
     * 构建批量上传状态缓存键
     *
     * @param batchId 批次ID
     * @return 缓存键
     */
    public static String buildBatchStatusKey(String batchId) {
        return CACHE_KEY_BATCH_STATUS + batchId;
    }
}
