package net.lab1024.sa.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存命名空间枚举
 * <p>
 * 统一管理所有缓存命名空间
 * 严格遵循CLAUDE.md规范：
 * - 统一的命名空间前缀
 * - 标准化的缓存键格式
 * - 完整的命名空间管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum CacheNamespace {

    /**
     * 用户缓存命名空间
     */
    USER("USER", "用户缓存", 3600L),

    /**
     * 菜单缓存命名空间
     */
    MENU("MENU", "菜单缓存", 7200L),

    /**
     * 部门缓存命名空间
     */
    DEPARTMENT("DEPARTMENT", "部门缓存", 3600L),

    /**
     * 角色缓存命名空间
     */
    ROLE("ROLE", "角色缓存", 3600L),

    /**
     * 权限缓存命名空间
     */
    PERMISSION("PERMISSION", "权限缓存", 7200L),

    /**
     * 字典缓存命名空间
     */
    DICT("DICT", "字典缓存", 86400L),

    /**
     * 配置缓存命名空间
     */
    CONFIG("CONFIG", "配置缓存", 3600L),

    /**
     * 员工缓存命名空间
     */
    EMPLOYEE("EMPLOYEE", "员工缓存", 1800L),

    /**
     * 访客缓存命名空间
     */
    VISITOR("VISITOR", "访客缓存", 1800L),

    /**
     * 门禁缓存命名空间
     */
    ACCESS("ACCESS", "门禁缓存", 600L),

    /**
     * 考勤缓存命名空间
     */
    ATTENDANCE("ATTENDANCE", "考勤缓存", 1800L),

    /**
     * 消费缓存命名空间
     */
    CONSUME("CONSUME", "消费缓存", 600L),

    /**
     * 视频缓存命名空间
     */
    VIDEO("VIDEO", "视频缓存", 300L),

    /**
     * 默认缓存命名空间
     */
    DEFAULT("DEFAULT", "默认缓存", 3600L);

    /**
     * 命名空间前缀
     */
    private final String prefix;

    /**
     * 命名空间描述
     */
    private final String description;

    /**
     * 默认过期时间（秒）
     */
    private final Long defaultTtl;

    /**
     * 根据前缀获取命名空间枚举
     *
     * @param prefix 命名空间前缀
     * @return 命名空间枚举，如果不存在则返回null
     */
    public static CacheNamespace valueOfPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        for (CacheNamespace namespace : values()) {
            if (namespace.getPrefix().equalsIgnoreCase(prefix)) {
                return namespace;
            }
        }
        return null;
    }

    /**
     * 获取完整的缓存键前缀
     * <p>
     * 格式：unified:cache:{prefix}:
     * </p>
     *
     * @return 完整的缓存键前缀
     */
    public String getFullPrefix() {
        return "unified:cache:" + prefix.toLowerCase() + ":";
    }

    /**
     * 构建完整的缓存键
     *
     * @param key 缓存键
     * @return 完整的缓存键
     */
    public String buildKey(String key) {
        return getFullPrefix() + key;
    }
}
