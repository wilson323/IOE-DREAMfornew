package net.lab1024.sa.base.module.support.rbac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.Resource;
import net.lab1024.sa.base.module.support.auth.AuthorizationContext;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * 资源权限拦截器
 * <p>
 * 拦截带有@RequireResource注解的方法，执行统一的权限检查和审计记录
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
@Slf4j
@Component
public class ResourcePermissionInterceptor implements HandlerInterceptor {

    @Resource
    private PolicyEvaluator policyEvaluator;

    @Resource
    private DataScopeResolver dataScopeResolver;

    @Resource
    private ResourcePermissionService resourcePermissionService;

    private static final String AUTH_CONTEXT_ATTR = "authorizationContext";
    private static final String DATA_SCOPE_DECISION_ATTR = "dataScopeDecision";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查方法或类上是否有@RequireResource注解
        RequireResource requireResource = getRequireResourceAnnotation(method);
        if (requireResource == null) {
            return true; // 无注解则不需要权限检查
        }

        // 如果配置忽略权限检查
        if (requireResource.ignore()) {
            log.warn("权限检查已被忽略: method={}, resource={}", method.getName(), requireResource.code());
            return true;
        }

        try {
            // 1. 构建授权上下文
            AuthorizationContext context = buildAuthorizationContext(request, requireResource);

            // 2. 将上下文存入请求属性，供后续使用
            request.setAttribute(AUTH_CONTEXT_ATTR, context);

            // 3. 执行策略评估
            PolicyEvaluator.PolicyDecision decision = policyEvaluator.evaluate(context);
            if (!decision.isAllowed()) {
                logSecurityEvent(request, context, decision, false);
                throw createAuthException(decision.getReason(), requireResource.errorCode());
            }

            // 4. 解析数据域
            DataScopeResolver.DataScopeDecision dataScopeDecision = dataScopeResolver.resolve(context);
            request.setAttribute(DATA_SCOPE_DECISION_ATTR, dataScopeDecision);

            // 5. 记录审计日志
            if (requireResource.audit()) {
                logSecurityEvent(request, context, decision, true);
            }

            return true;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("权限检查异常: method={}, resource={}, error={}",
                    method.getName(), requireResource.code(), e.getMessage(), e);
            throw createAuthException("权限检查异常: " + e.getMessage(), requireResource.errorCode());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理请求属性
        request.removeAttribute(AUTH_CONTEXT_ATTR);
        request.removeAttribute(DATA_SCOPE_DECISION_ATTR);
    }

    /**
     * 获取@RequireResource注解
     */
    private RequireResource getRequireResourceAnnotation(Method method) {
        // 优先检查方法上的注解
        RequireResource methodAnnotation = method.getAnnotation(RequireResource.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 检查类上的注解
        RequireResource classAnnotation = method.getDeclaringClass().getAnnotation(RequireResource.class);
        if (classAnnotation != null) {
            return classAnnotation;
        }

        return null;
    }

    /**
     * 构建授权上下文
     */
    private AuthorizationContext buildAuthorizationContext(HttpServletRequest request, RequireResource requireResource) {
        // TODO: 从Sa-Token获取登录用户信息
        // 暂时使用模拟数据
        Long userId = 1L;
        String userCode = "USER001";
        String userName = "测试用户";

        if (userId == null || userCode == null) {
            throw createAuthException("用户未登录或会话已过期", "NOT_LOGIN");
        }

        // 获取用户角色和权限信息
        Set<String> roleCodes = getUserRoles(userId);
        Set<Long> areaIds = getUserAreaPermissions(userId);
        Set<Long> deptIds = getUserDeptPermissions(userId);

        // 构建授权上下文
        AuthorizationContext context = AuthorizationContext.builder()
                .userId(userId)
                .userCode(userCode)
                .userName(userName)
                .roleCodes(roleCodes)
                .areaIds(new ArrayList<>(areaIds))
                .deptIds(new ArrayList<>(deptIds))
                .resourceCode(requireResource.code())
                .requestedAction(requireResource.action())
                .dataScope(requireResource.scope())
                .clientIp(getClientIp(request))
                .requestPath(request.getRequestURI())
                .requestTime(System.currentTimeMillis())
                .validStartTime(null)
                .validEndTime(null)
                .build();

        // 检查是否为超级管理员
        context.setIsSuperAdmin(checkSuperAdmin(roleCodes));

        return context;
    }

    /**
     * 获取用户角色
     */
    private Set<String> getUserRoles(Long userId) {
        try {
            // TODO: 查询 t_rbac_user_role 表获取用户角色
            // 暂时返回默认角色
            Set<String> roles = new HashSet<>();
            roles.add("USER");
            return roles;
        } catch (Exception e) {
            log.error("获取用户角色异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户区域权限
     */
    private Set<Long> getUserAreaPermissions(Long userId) {
        try {
            // TODO: 查询 t_area_person 表获取用户区域权限
            // 暂时返回空集合
            return new HashSet<>();
        } catch (Exception e) {
            log.error("获取用户区域权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户部门权限
     * <p>
     * 调用ResourcePermissionService获取用户部门权限，支持缓存
     * </p>
     *
     * @param userId 用户ID（对应员工ID）
     * @return 部门ID集合，如果员工不存在或未分配部门，返回空集合
     */
    private Set<Long> getUserDeptPermissions(Long userId) {
        try {
            return resourcePermissionService.getUserDeptPermissions(userId);
        } catch (Exception e) {
            log.error("获取用户部门权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 检查是否为超级管理员
     */
    private boolean checkSuperAdmin(Set<String> roleCodes) {
        return roleCodes.contains("SUPER_ADMIN");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 记录安全事件
     */
    private void logSecurityEvent(HttpServletRequest request, AuthorizationContext context,
                                 PolicyEvaluator.PolicyDecision decision, boolean success) {
        try {
            // 记录审计日志
            log.info("权限审计事件: {} - userId={}, resource={}, action={}, result={}, reason={}",
                    success ? "AUTH_SUCCESS" : "AUTH_DENIED",
                    context.getUserId(),
                    context.getResourceCode(),
                    context.getRequestedAction(),
                    success ? "通过" : "拒绝",
                    decision.getReason());

            // 如果是失败事件，检查是否需要告警
            if (!success && needSecurityAlert(decision.getReason())) {
                triggerSecurityAlert(context, decision.getReason());
            }

        } catch (Exception e) {
            log.error("记录安全事件失败", e);
        }
    }

    /**
     * 检查是否需要安全告警
     */
    private boolean needSecurityAlert(String reason) {
        // 敏感操作或多次失败需要告警
        return reason.contains("超级管理员") ||
               reason.contains("权限不在有效期内") ||
               reason.contains("IP不在白名单中");
    }

    /**
     * 触发安全告警
     */
    private void triggerSecurityAlert(AuthorizationContext context, String reason) {
        try {
            // TODO: 实现安全告警逻辑（发送邮件、短信、推送到监控系统等）
            log.warn("触发安全告警: userId={}, reason={}", context.getUserId(), reason);
        } catch (Exception e) {
            log.error("触发安全告警失败", e);
        }
    }

    /**
     * 创建认证异常
     */
    private RuntimeException createAuthException(String message, String errorCode) {
        // 这里可以根据errorCode返回不同的错误码
        return new RuntimeException(message);
    }

    /**
     * 从请求中获取授权上下文
     */
    public static AuthorizationContext getAuthorizationContext(HttpServletRequest request) {
        return (AuthorizationContext) request.getAttribute(AUTH_CONTEXT_ATTR);
    }

    /**
     * 从请求中获取数据域决策
     */
    public static DataScopeResolver.DataScopeDecision getDataScopeDecision(HttpServletRequest request) {
        return (DataScopeResolver.DataScopeDecision) request.getAttribute(DATA_SCOPE_DECISION_ATTR);
    }
}