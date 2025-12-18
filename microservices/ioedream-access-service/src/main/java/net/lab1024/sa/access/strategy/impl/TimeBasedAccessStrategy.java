package net.lab1024.sa.access.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.strategy.IAccessPermissionStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;
import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.time.LocalTime;
import java.util.List;

/**
 * 基于时间段的门禁权限策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否在允许的时间段内通行
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
@StrategyMarker(name = "TIME_BASED", type = "ACCESS_PERMISSION", priority = 100)
public class TimeBasedAccessStrategy implements IAccessPermissionStrategy {

    @Resource
    private UserAreaPermissionDao userAreaPermissionDao;

    @Override
    public boolean hasPermission(AccessRequest request) {
        // 查询用户在该区域的权限
        UserAreaPermissionEntity permission = userAreaPermissionDao.selectByUserIdAndAreaId(
                request.getUserId(), request.getAreaId());

        if (permission == null) {
            log.debug("[时间策略] 用户{}在区域{}无权限记录", request.getUserId(), request.getAreaId());
            return false;
        }

        // 检查权限是否生效
        if (permission.getStatus() == null || permission.getStatus() != 1) {
            log.debug("[时间策略] 用户{}在区域{}的权限状态为{}", request.getUserId(), request.getAreaId(), permission.getStatus());
            return false;
        }

        // 时间段验证
        LocalTime now = LocalTime.now();
        if (permission.getEffectiveTime() != null && now.isBefore(permission.getEffectiveTime().toLocalTime())) {
            log.debug("[时间策略] 当前时间{}早于权限生效时间{}", now, permission.getEffectiveTime());
            return false;
        }

        if (permission.getExpireTime() != null && now.isAfter(permission.getExpireTime().toLocalTime())) {
            log.debug("[时间策略] 当前时间{}晚于权限失效时间{}", now, permission.getExpireTime());
            return false;
        }

        log.debug("[时间策略] 用户{}在区域{}权限验证通过", request.getUserId(), request.getAreaId());
        return true;
    }

    @Override
    public int getPriority() {
        return 100; // 时间策略优先级最高
    }

    @Override
    public String getStrategyType() {
        return "TIME_BASED";
    }
}
