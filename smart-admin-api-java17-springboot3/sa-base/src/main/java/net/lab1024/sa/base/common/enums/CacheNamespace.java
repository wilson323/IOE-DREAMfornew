package net.lab1024.sa.base.common.enums;

/**
 * 缓存命名空间枚举
 * 用于区分不同业务模块的缓存数据，避免缓存键冲突
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
public enum CacheNamespace {

    /**
     * 用户相关缓存
     */
    USER("user", "用户模块缓存"),

    /**
     * 会话相关缓存
     */
    SESSION("session", "会话模块缓存"),

    /**
     * 配置相关缓存
     */
    CONFIG("config", "配置模块缓存"),

    /**
     * 业务数据缓存
     */
    DATA("data", "业务数据缓存"),

    /**
     * 临时数据缓存
     */
    TEMP("temp", "临时数据缓存"),

    /**
     * 消费模块缓存
     */
    CONSUME("consume", "消费模块缓存"),

    /**
     * 门禁访问缓存
     */
    ACCESS("access", "门禁访问缓存"),

    /**
     * 考勤模块缓存
     */
    ATTENDANCE("attendance", "考勤模块缓存"),

    /**
     * 设备管理缓存
     */
    DEVICE("device", "设备管理缓存"),

    /**
     * 视频监控缓存
     */
    VIDEO("video", "视频监控缓存"),

    /**
     * 系统管理缓存
     */
    SYSTEM("system", "系统管理缓存"),

    /**
     * 文档管理缓存
     */
    DOCUMENT("document", "文档管理缓存");

    private final String code;
    private final String description;

    CacheNamespace(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static CacheNamespace getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (CacheNamespace namespace : values()) {
            if (namespace.getCode().equals(code)) {
                return namespace;
            }
        }
        return null;
    }

    /**
     * 获取前缀名称（兼容性方法）
     *
     * @return 前缀名称
     */
    public String getPrefix() {
        return code;
    }

    /**
     * 获取Redis TTL时间（秒）
     * 基于业务特性设置的默认TTL
     *
     * @return TTL时间（秒）
     */
    public long getRedisTtl() {
        // 根据业务模块设置不同的默认TTL
        switch (this) {
            case USER:
            case SESSION:
                return 1800; // 30分钟 - 用户相关数据
            case CONFIG:
            case SYSTEM:
                return 7200; // 2小时 - 配置和系统数据
            case CONSUME:
                return 900;  // 15分钟 - 消费数据，相对实时
            case ACCESS:
                return 600;  // 10分钟 - 门禁数据，实时性要求高
            case ATTENDANCE:
                return 1800; // 30分钟 - 考勤数据
            case DEVICE:
                return 900;  // 15分钟 - 设备状态数据
            case VIDEO:
                return 300;  // 5分钟 - 视频监控数据
            case DOCUMENT:
                return 3600; // 1小时 - 文档数据
            case DATA:
                return 1800; // 30分钟 - 一般业务数据
            case TEMP:
                return 300;  // 5分钟 - 临时数据
            default:
                return 1800; // 默认30分钟
        }
    }

    /**
     * 获取本地缓存TTL时间（毫秒）
     * 本地缓存TTL通常比Redis TTL短，以保证数据一致性
     *
     * @return TTL时间（毫秒）
     */
    public long getLocalTtl() {
        // 本地缓存TTL设置为Redis TTL的1/2到1/3，保证数据及时刷新
        return getRedisTtl() * 1000 / 3; // 转换为毫秒并取1/3
    }
}