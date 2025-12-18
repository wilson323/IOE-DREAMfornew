package net.lab1024.sa.access.strategy;

import net.lab1024.sa.access.domain.form.AccessRequest;

/**
 * 门禁权限策略接口
 * <p>
 * 使用策略模式实现不同的权限验证策略：
 * - 时间策略：基于时间段的权限验证
 * - 地理围栏策略：基于GPS位置的权限验证
 * - 角色策略：基于用户角色的权限验证
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用策略模式实现
 * - 接口化设计，支持依赖倒置
 * - 支持动态加载策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IAccessPermissionStrategy {

    /**
     * 检查是否有权限
     * <p>
     * 根据不同的策略实现不同的权限验证逻辑
     * </p>
     *
     * @param request 通行请求
     * @return 是否有权限
     */
    boolean hasPermission(AccessRequest request);

    /**
     * 获取策略优先级
     * <p>
     * 用于策略工厂排序，优先级高的策略优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取策略类型
     * <p>
     * 用于策略工厂识别策略类型
     * </p>
     *
     * @return 策略类型标识
     */
    String getStrategyType();
}
