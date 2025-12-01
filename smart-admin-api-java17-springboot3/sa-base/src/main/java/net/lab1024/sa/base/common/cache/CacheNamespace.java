package net.lab1024.sa.base.common.cache;

import lombok.Getter;
import java.util.concurrent.TimeUnit;

/**
 * 缓存命名空间枚举
 * <p>
 * 严格遵循repowiki缓存架构规范：
 * - 统一缓存命名规范
 * - 模块化缓存隔离
 * - TTL配置标准化
 * - 本地缓存策略
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Getter
public enum CacheNamespace {

    /**
     * 系统缓存
     */
    SYSTEM("SYSTEM", 3600, 300),

    /**
     * 用户缓存
     */
    USER("USER", 1800, 120),

    /**
     * 权限缓存
     */
    PERMISSION("PERMISSION", 3600, 300),

    /**
     * 业务数据缓存
     */
    BUSINESS("BUSINESS", 1800, 180),

    /**
     * 配置缓存
     */
    CONFIG("CONFIG", 7200, 600),

    /**
     * 临时缓存
     */
    TEMP("TEMP", 300, 60),

    /**
     * 消费模块缓存
     */
    CONSUME("CONSUME", 1200, 180),

    /**
     * 门禁系统缓存
     */
    ACCESS("ACCESS", 600, 120),

    /**
     * 考勤系统缓存
     */
    ATTENDANCE("ATTENDANCE", 1800, 300),

    /**
     * 视频监控缓存
     */
    VIDEO("VIDEO", 900, 120),

    /**
     * 设备管理缓存
     */
    DEVICE("DEVICE", 3600, 300),

    /**
     * 文档管理缓存
     */
    DOCUMENT("DOCUMENT", 1800, 240);

    private final String prefix;
    private final long redisTtl;  // Redis缓存TTL（秒）
    private final long localTtl;  // 本地缓存TTL（毫秒）

    CacheNamespace(String prefix, long redisTtl, long localTtl) {
        this.prefix = prefix;
        this.redisTtl = redisTtl;
        this.localTtl = localTtl * 1000; // 转换为毫秒
    }

    /**
     * 根据前缀获取缓存命名空间
     */
    public static CacheNamespace valueOfPrefix(String prefix) {
        for (CacheNamespace namespace : values()) {
            if (namespace.getPrefix().equals(prefix)) {
                return namespace;
            }
        }
        throw new IllegalArgumentException("未知的缓存命名空间前缀: " + prefix);
    }

    /**
     * 获取缓存命名空间前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取Redis缓存TTL
     */
    public long getRedisTtl() {
        return redisTtl;
    }

    /**
     * 获取本地缓存TTL（毫秒）
     */
    public long getLocalTtl() {
        return localTtl;
    }

    /**
     * 获取默认TTL（秒）
     * 兼容UnifiedCacheManager的getDefaultTtl()方法
     */
    public long getDefaultTtl() {
        return redisTtl;
    }

    /**
     * 获取时间单位
     * 兼容UnifiedCacheManager的getTimeUnit()方法
     */
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    /**
     * 获取命名空间描述
     */
    public String getDescription() {
        switch (this) {
            case SYSTEM:
                return "系统级缓存，存储系统配置和全局数据";
            case USER:
                return "用户缓存，存储用户会话和个人信息";
            case PERMISSION:
                return "权限缓存，存储角色权限数据";
            case BUSINESS:
                return "业务数据缓存，存储常用业务数据";
            case CONFIG:
                return "配置缓存，存储系统配置信息";
            case TEMP:
                return "临时缓存，存储短期临时数据";
            case CONSUME:
                return "消费模块缓存，存储消费相关数据";
            case ACCESS:
                return "门禁系统缓存，存储门禁权限和记录";
            case ATTENDANCE:
                return "考勤系统缓存，存储考勤相关数据";
            case VIDEO:
                return "视频监控缓存，存储视频流和设备状态";
            case DEVICE:
                return "设备管理缓存，存储设备信息和状态";
            case DOCUMENT:
                return "文档管理缓存，存储文档元数据和权限";
            default:
                return "未知缓存类型";
        }
    }
}