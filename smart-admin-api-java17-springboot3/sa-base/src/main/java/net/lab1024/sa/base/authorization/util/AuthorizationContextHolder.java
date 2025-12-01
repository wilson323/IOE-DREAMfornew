/*
 * 权限上下文持有者
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.base.authorization.util;

import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * 权限上下文持有者
 * 提供权限验证的上下文管理和便捷方法
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
@Slf4j
@Component
public class AuthorizationContextHolder {

    /**
     * 检查当前用户是否可以访问指定区域
     *
     * @param regionId 区域ID
     * @return 是否可以访问
     */
    public static boolean canAccessArea(Long regionId) {
        try {
            // 检查区域访问权限
            boolean hasAreaPermission = StpUtil.hasPermission("AREA_" + regionId);
            if (hasAreaPermission) {
                log.debug("用户具有区域访问权限: regionId={}", regionId);
                return true;
            }

            // 检查通用区域权限
            boolean hasGeneralPermission = StpUtil.hasPermission("CONSUME_REGION_ALL")
                || StpUtil.hasPermission("AREA_ALL");
            if (hasGeneralPermission) {
                log.debug("用户具有通用区域访问权限: regionId={}", regionId);
                return true;
            }

            log.debug("用户无区域访问权限: regionId={}", regionId);
            return false;

        } catch (Exception e) {
            log.error("检查区域访问权限异常: regionId={}", regionId, e);
            return false;
        }
    }

    /**
     * 获取当前权限上下文
     *
     * @return 权限上下文
     */
    public static AuthorizationContext getCurrentContext() {
        try {
            if (!StpUtil.isLogin()) {
                return AuthorizationContext.anonymous();
            }

            Long userId = StpUtil.getLoginIdAsLong();
            return AuthorizationContext.of(userId);

        } catch (Exception e) {
            log.error("获取权限上下文异常", e);
            return AuthorizationContext.anonymous();
        }
    }
}

/**
 * 权限上下文
 */
class AuthorizationContext {

    private final Long userId;
    private final boolean authenticated;

    private AuthorizationContext(Long userId, boolean authenticated) {
        this.userId = userId;
        this.authenticated = authenticated;
    }

    public static AuthorizationContext of(Long userId) {
        return new AuthorizationContext(userId, true);
    }

    public static AuthorizationContext anonymous() {
        return new AuthorizationContext(null, false);
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}