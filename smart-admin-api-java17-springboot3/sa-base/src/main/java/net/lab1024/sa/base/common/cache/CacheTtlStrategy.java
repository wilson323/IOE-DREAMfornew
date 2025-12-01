package net.lab1024.sa.base.common.cache;

import lombok.Getter;
import net.lab1024.sa.base.common.cache.CacheNamespace;

import java.util.concurrent.TimeUnit;

/**
 * 缓存TTL策略定义
 * <p>
 * 基于业务特性的TTL分类策略，严格遵循repowiki缓存规范：
 * - 实时性要求高: 5分钟 (账户余额、权限信息)
 * - 准实时性要求: 15分钟 (设备状态、考勤记录)
 * - 一般性要求: 30分钟 (用户信息、基础配置)
 * - 稳定性要求: 60分钟 (系统配置、权限模板)
 * - 长期缓存: 120分钟 (字典数据、静态配置)
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
@Getter
public enum CacheTtlStrategy {

    /**
     * 实时性要求高
     * 适用场景: 账户余额、用户权限、设备在线状态等
     * 特点: 数据变化频繁，需要实时更新
     */
    REALTIME(5, TimeUnit.MINUTES, CacheNamespace.CONSUME, "实时数据 - 5分钟"),

    /**
     * 准实时性要求
     * 适用场景: 设备状态、考勤记录、最近消费等
     * 特点: 数据变化较频繁，允许短时间延迟
     */
    NEAR_REALTIME(15, TimeUnit.MINUTES, CacheNamespace.DEVICE, "准实时数据 - 15分钟"),

    /**
     * 一般性要求
     * 适用场景: 用户信息、基础配置、部门信息等
     * 特点: 数据相对稳定，变化不频繁
     */
    NORMAL(30, TimeUnit.MINUTES, CacheNamespace.USER, "一般数据 - 30分钟"),

    /**
     * 稳定性要求
     * 适用场景: 系统配置、权限模板、业务规则等
     * 特点: 数据稳定，变化很少，可以长时间缓存
     */
    STABLE(60, TimeUnit.MINUTES, CacheNamespace.SYSTEM, "稳定数据 - 60分钟"),

    /**
     * 长期缓存
     * 适用场景: 字典数据、静态配置、菜单配置等
     * 特点: 基本不变，可以长期缓存
     */
    LONG_TERM(120, TimeUnit.MINUTES, CacheNamespace.CONFIG, "长期数据 - 120分钟");

    private final long ttl;
    private final TimeUnit timeUnit;
    private final CacheNamespace defaultNamespace;
    private final String description;

    CacheTtlStrategy(long ttl, TimeUnit timeUnit, CacheNamespace defaultNamespace, String description) {
        this.ttl = ttl;
        this.timeUnit = timeUnit;
        this.defaultNamespace = defaultNamespace;
        this.description = description;
    }

    /**
     * 获取TTL时间（秒）
     */
    public long getTtlInSeconds() {
        return timeUnit.toSeconds(ttl);
    }

    /**
     * 获取TTL时间（毫秒）
     */
    public long getTtlInMillis() {
        return timeUnit.toMillis(ttl);
    }

    /**
     * 获取TTL时间（分钟）
     */
    public long getTtlInMinutes() {
        return timeUnit.toMinutes(ttl);
    }

    /**
     * 根据业务特性获取推荐TTL策略
     *
     * @param updateFrequency 更新频率 (HIGH/MEDIUM/LOW)
     * @param businessCriticality 业务关键性 (CRITICAL/NORMAL/LOW)
     * @param consistencyRequirement 一致性要求 (STRONG/MEDIUM/WEAK)
     * @return 推荐的TTL策略
     */
    public static CacheTtlStrategy getRecommendedStrategy(String updateFrequency, String businessCriticality, String consistencyRequirement) {
        // 高更新频率 + 高业务关键性 + 强一致性要求 = 实时
        if ("HIGH".equalsIgnoreCase(updateFrequency) &&
            "CRITICAL".equalsIgnoreCase(businessCriticality) &&
            "STRONG".equalsIgnoreCase(consistencyRequirement)) {
            return REALTIME;
        }

        // 高更新频率 + 高业务关键性 = 准实时
        if ("HIGH".equalsIgnoreCase(updateFrequency) &&
            "CRITICAL".equalsIgnoreCase(businessCriticality)) {
            return NEAR_REALTIME;
        }

        // 中等更新频率 + 高业务关键性 = 准实时
        if ("MEDIUM".equalsIgnoreCase(updateFrequency) &&
            "CRITICAL".equalsIgnoreCase(businessCriticality)) {
            return NEAR_REALTIME;
        }

        // 低更新频率 + 中等业务关键性 = 一般
        if ("LOW".equalsIgnoreCase(updateFrequency) &&
            "NORMAL".equalsIgnoreCase(businessCriticality)) {
            return NORMAL;
        }

        // 低更新频率 + 低业务关键性 = 稳定
        if ("LOW".equalsIgnoreCase(updateFrequency) &&
            "LOW".equalsIgnoreCase(businessCriticality)) {
            return STABLE;
        }

        // 弱一致性要求 = 稳定或长期
        if ("WEAK".equalsIgnoreCase(consistencyRequirement)) {
            return LONG_TERM;
        }

        // 默认返回一般策略
        return NORMAL;
    }

    /**
     * 获取策略的性能等级
     * 1-5级，1级最高性能，5级最低性能
     */
    public int getPerformanceLevel() {
        switch (this) {
            case REALTIME: return 1;    // 最高性能要求
            case NEAR_REALTIME: return 2; // 高性能要求
            case NORMAL: return 3;      // 中等性能要求
            case STABLE: return 4;      // 低性能要求
            case LONG_TERM: return 5;   // 最低性能要求
            default: return 3;
        }
    }

    /**
     * 获取策略的缓存成本等级
     * 1-5级，1级最高成本，5级最低成本
     */
    public int getCacheCostLevel() {
        switch (this) {
            case REALTIME: return 5;    // 最高缓存成本
            case NEAR_REALTIME: return 4; // 高缓存成本
            case NORMAL: return 3;      // 中等缓存成本
            case STABLE: return 2;      // 低缓存成本
            case LONG_TERM: return 1;   // 最低缓存成本
            default: return 3;
        }
    }
}