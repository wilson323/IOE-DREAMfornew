package net.lab1024.sa.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存命名空间枚举
 * <p>
 * 定义所有缓存命名空间，提供统一的缓存键前缀和TTL管理
 * 严格遵循CLAUDE.md规范：纯Java枚举，无框架依赖
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-21
 */
@Getter
@AllArgsConstructor
public enum CacheNamespace {

    USER("USER", "用户缓存", 3600L),
    MENU("MENU", "菜单缓存", 7200L),
    DEPARTMENT("DEPARTMENT", "部门缓存", 3600L),
    ROLE("ROLE", "角色缓存", 3600L),
    PERMISSION("PERMISSION", "权限缓存", 7200L),
    DICT("DICT", "字典缓存", 86400L),
    CONFIG("CONFIG", "配置缓存", 3600L),
    EMPLOYEE("EMPLOYEE", "员工缓存", 1800L),
    VISITOR("VISITOR", "访客缓存", 1800L),
    ACCESS("ACCESS", "门禁缓存", 600L),
    ATTENDANCE("ATTENDANCE", "考勤缓存", 1800L),
    CONSUME("CONSUME", "消费缓存", 600L),
    VIDEO("VIDEO", "视频缓存", 300L),
    DEFAULT("DEFAULT", "默认缓存", 3600L);

    private final String prefix;
    private final String description;
    private final Long defaultTtl;

    /**
     * 获取缓存键前缀（用于Spring Cache CacheManager）
     *
     * @return 缓存键前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取完整的缓存键前缀（用于Redis）
     * 格式：unified:cache:{prefix}:
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

    /**
     * 根据前缀获取命名空间枚举
     *
     * @param prefix 前缀
     * @return 命名空间枚举，不存在返回null
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
}

