package net.lab1024.sa.base.common.cache;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 业务数据类型和TTL策略定义
 * <p>
 * 严格遵循repowiki缓存规范：
 * - 基于业务特性的TTL分类
 * - 统一的缓存命名空间映射
 * - 明确的数据类型定义和用途
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
@Getter
public enum BusinessDataType {

    // ========== 实时性要求高 (5分钟) ==========
    ACCOUNT_BALANCE(CacheTtlStrategy.REALTIME, "账户余额", "用户账户实时余额信息"),
    USER_PERMISSION(CacheTtlStrategy.REALTIME, "用户权限", "用户实时权限和角色信息"),
    DEVICE_ONLINE_STATUS(CacheTtlStrategy.REALTIME, "设备在线状态", "设备实时在线状态信息"),
    ACCESS_CONTROL(CacheTtlStrategy.REALTIME, "门禁控制", "实时门禁权限和状态信息"),
    CONSUME_QUOTA(CacheTtlStrategy.REALTIME, "消费配额", "用户消费配额和限额信息"),

    // ========== 准实时性要求 (15分钟) ==========
    DEVICE_STATUS(CacheTtlStrategy.NEAR_REALTIME, "设备状态", "设备运行状态和配置信息"),
    ATTENDANCE_RECORD(CacheTtlStrategy.NEAR_REALTIME, "考勤记录", "当日考勤记录数据"),
    CONSUME_RECENT(CacheTtlStrategy.NEAR_REALTIME, "最近消费", "最近消费记录摘要信息"),
    USER_LOCATION(CacheTtlStrategy.NEAR_REALTIME, "用户位置", "用户最近位置信息"),
    NOTIFICATION_STATUS(CacheTtlStrategy.NEAR_REALTIME, "通知状态", "通知发送状态信息"),

    // ========== 一般性要求 (30分钟) ==========
    USER_INFO(CacheTtlStrategy.NORMAL, "用户信息", "用户基本信息和档案"),
    DEVICE_BASIC(CacheTtlStrategy.NORMAL, "设备基础", "设备基础信息和配置"),
    CONSUME_CONFIG(CacheTtlStrategy.NORMAL, "消费配置", "消费模式和配置信息"),
    DEPARTMENT_INFO(CacheTtlStrategy.NORMAL, "部门信息", "部门组织结构信息"),
    ROLE_PERMISSION(CacheTtlStrategy.NORMAL, "角色权限", "角色权限模板信息"),

    // ========== 稳定性要求 (60分钟) ==========
    SYSTEM_CONFIG(CacheTtlStrategy.STABLE, "系统配置", "系统运行参数和配置"),
    PERMISSION_TEMPLATE(CacheTtlStrategy.STABLE, "权限模板", "权限模板和规则配置"),
    BUSINESS_RULE(CacheTtlStrategy.STABLE, "业务规则", "业务规则引擎配置"),
    WORKFLOW_CONFIG(CacheTtlStrategy.STABLE, "工作流配置", "工作流流程和节点配置"),
    REPORT_TEMPLATE(CacheTtlStrategy.STABLE, "报表模板", "报表模板和格式配置"),

    // ========== 长期缓存 (120分钟) ==========
    DICTIONARY_DATA(CacheTtlStrategy.LONG_TERM, "字典数据", "系统字典和枚举数据"),
    REGION_INFO(CacheTtlStrategy.LONG_TERM, "区域信息", "地理区域和分区信息"),
    MENU_CONFIG(CacheTtlStrategy.LONG_TERM, "菜单配置", "系统菜单和导航配置"),
    STATIC_RESOURCE(CacheTtlStrategy.LONG_TERM, "静态资源", "静态资源配置和映射"),
    CACHED_QUERY(CacheTtlStrategy.LONG_TERM, "缓存查询", "复杂查询结果缓存");

    private final CacheTtlStrategy ttlStrategy;
    private final String displayName;
    private final String description;

    BusinessDataType(CacheTtlStrategy ttlStrategy, String displayName, String description) {
        this.ttlStrategy = ttlStrategy;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 获取对应的缓存命名空间
     */
    public CacheNamespace getCacheNamespace() {
        return ttlStrategy.getDefaultNamespace();
    }

    /**
     * 获取TTL时间（秒）
     */
    public long getTtlInSeconds() {
        return ttlStrategy.getTtlInSeconds();
    }

    /**
     * 获取默认TTL时间（秒）- 为了兼容性
     */
    public long getDefaultTtl() {
        return getTtlInSeconds();
    }

    /**
     * 是否为实时数据类型
     */
    public boolean isRealtime() {
        return ttlStrategy == CacheTtlStrategy.REALTIME;
    }

    /**
     * 是否为稳定数据类型
     */
    public boolean isStable() {
        return ttlStrategy == CacheTtlStrategy.STABLE || ttlStrategy == CacheTtlStrategy.LONG_TERM;
    }
}