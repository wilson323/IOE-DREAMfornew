package net.lab1024.sa.common.permission.audit;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.audit.PermissionAuditLogger;
import net.lab1024.sa.common.permission.domain.dto.PermissionAuditDTO;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.manager.PermissionCacheManager;
import net.lab1024.sa.common.service.AuthService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限操作审计切面
 * <p>
 * 基于AOP的权限审计日志自动记录，提供：
 * - 自动拦截权限验证注解，记录审计信息
 * - 权限验证前后状态对比，记录详细过程
 * - 异常情况自动捕获和记录
 * - 性能指标自动采集（耗时、调用次数等）
 * - 敏感操作自动识别和强化审计
 * - 审计日志格式标准化和结构化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Aspect
@Component
public class PermissionAuditAspect {

    @Resource
    private PermissionAuditLogger permissionAuditLogger;

    @Resource
    private PermissionCacheManager permissionCacheManager;

    @Resource
    private AuthService authService;

    /**
     * 权限验证切点 - 拦截所有带有@PermissionCheck注解的方法
     */
    @Pointcut("@annotation(net.lab1024.sa.common.permission.annotation.PermissionCheck)")
    public void permissionCheckPointcut() {
    }

    /**
     * 权限验证切点 - 拦截权限验证方法
     */
    @Pointcut("execution(* net.lab1024.sa.common.permission.service.impl.*.validatePermission(..))")
    public void permissionValidationPointcut() {
    }

    /**
     * 权限验证切点 - 拦截权限检查方法
     */
    @Pointcut("execution(* net.lab1024.sa.common.permission.service.impl.*.checkPermission(..))")
    public void permissionCheckPointcut2() {
    }

    /**
     * 权限验证切面通知
     */
    @Around("permissionCheckPointcut() || permissionValidationPointcut() || permissionCheckPointcut2()")
    public Object auditPermissionValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 获取权限检查注解
        PermissionCheck permissionCheck = method.getAnnotation(PermissionCheck.class);

        // 构建基础审计信息
        PermissionAuditDTO auditDTO = buildBaseAuditInfo(joinPoint, method, targetClass, permissionCheck);

        // 记录权限验证开始
        log.debug("[权限审计] 开始权限验证: {}.{}", targetClass.getSimpleName(), method.getName());

        Object result = null;
        Exception exception = null;
        boolean permissionGranted = false;

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 分析权限验证结果
            permissionGranted = analyzePermissionResult(result);

            // 更新审计信息
            long validationTime = System.currentTimeMillis() - startTime;
            auditDTO.setResult(permissionGranted ? "GRANTED" : "DENIED");
            auditDTO.setValidationTime(validationTime);
            auditDTO.setSuccess(true);

            // 记录成功审计
            permissionAuditLogger.logPermissionValidation(auditDTO);

            log.debug("[权限审计] 权限验证完成: {}={},耗时={}ms",
                     auditDTO.getResource(), auditDTO.getResult(), validationTime);

            return result;

        } catch (Exception e) {
            exception = e;

            // 记录异常审计
            long validationTime = System.currentTimeMillis() - startTime;
            auditDTO.setResult("ERROR");
            auditDTO.setValidationTime(validationTime);
            auditDTO.setSuccess(false);
            auditDTO.setFailureReason(e.getMessage());

            permissionAuditLogger.logPermissionValidation(auditDTO);

            log.error("[权限审计] 权限验证异常: {}.{}={}",
                     targetClass.getSimpleName(), method.getName(), e.getMessage(), e);

            throw e;
        }
    }

    /**
     * 构建基础审计信息
     */
    private PermissionAuditDTO buildBaseAuditInfo(ProceedingJoinPoint joinPoint, Method method,
                                                 Class<?> targetClass, PermissionCheck permissionCheck) {
        try {
            // 获取当前用户信息
            Long userId = getCurrentUserId();

            // 获取HTTP请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 构建审计DTO
            PermissionAuditDTO.AuditDTOBuilder builder = PermissionAuditDTO.builder()
                .userId(userId)
                .className(targetClass.getName())
                .methodName(method.getName())
                .auditTime(LocalDateTime.now());

            // 设置权限相关信息
            if (permissionCheck != null) {
                builder.operation(Arrays.toString(permissionCheck.value()))
                       .resource(Arrays.toString(permissionCheck.resource()))
                       .description(permissionCheck.description());
            }

            // 设置请求信息
            if (request != null) {
                builder.clientIp(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"))
                       .requestMethod(request.getMethod())
                       .requestUrl(request.getRequestURL().toString());
            }

            // 设置方法参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                builder.methodParameters(Arrays.stream(args)
                    .map(this::maskSensitiveParameter)
                    .collect(Collectors.toList()));
            }

            return builder.build();

        } catch (Exception e) {
            log.error("[权限审计] 构建审计信息异常", e);
            return PermissionAuditDTO.builder()
                .auditTime(LocalDateTime.now())
                .result("ERROR")
                .failureReason("构建审计信息失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 分析权限验证结果
     */
    private boolean analyzePermissionResult(Object result) {
        if (result == null) {
            return false;
        }

        // 如果返回的是权限验证结果对象
        if (result instanceof PermissionValidationResult) {
            PermissionValidationResult validationResult = (PermissionValidationResult) result;
            return validationResult.isValid();
        }

        // 如果返回的是布尔值
        if (result instanceof Boolean) {
            return (Boolean) result;
        }

        // 如果返回的是ResponseDTO
        if (result instanceof net.lab1024.sa.common.dto.ResponseDTO) {
            net.lab1024.sa.common.dto.ResponseDTO<?> response = (net.lab1024.sa.common.dto.ResponseDTO<?>) result;
            return response.getOk();
        }

        // 默认认为有返回值即为成功
        return true;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUserId();
        } catch (Exception e) {
            log.debug("[权限审计] 获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 脱敏敏感参数
     */
    private Object maskSensitiveParameter(Object param) {
        if (param == null) {
            return null;
        }

        // 脱敏密码参数
        if (param instanceof String) {
            String str = (String) param;
            if (str.length() > 100) {
                return str.substring(0, 100) + "...[截断]";
            }
            // 脱敏可能的密码字段
            if (str.toLowerCase().contains("password") ||
                str.toLowerCase().contains("token") ||
                str.toLowerCase().contains("secret")) {
                return "***[敏感信息已脱敏]***";
            }
        }

        return param;
    }

    /**
     * 权限验证性能监控切面
     */
    @Around("permissionCheckPointcut() || permissionValidationPointcut() || permissionCheckPointcut2()")
    public Object monitorPermissionPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String key = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // 记录性能指标
            recordPerformanceMetrics(key, executionTime, true, null);

            // 检查性能阈值
            checkPerformanceThreshold(key, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录性能指标（失败）
            recordPerformanceMetrics(key, executionTime, false, e);

            throw e;
        }
    }

    /**
     * 记录性能指标
     */
    private void recordPerformanceMetrics(String key, long executionTime, boolean success, Exception exception) {
        try {
            // 使用缓存管理器记录性能数据
            String perfKey = "permission:performance:" + key;

            // 更新统计信息
            // 这里应该调用缓存管理器的方法来更新性能统计
            // permissionCacheManager.recordPermissionMetrics(key, executionTime, success);

            // 记录慢查询
            if (executionTime > 1000) { // 超过1秒认为是慢查询
                log.warn("[权限审计-性能] 权限验证耗时较长: {}ms, key={}", executionTime, key);
            }

        } catch (Exception e) {
            log.error("[权限审计] 记录性能指标异常", e);
        }
    }

    /**
     * 检查性能阈值
     */
    private void checkPerformanceThreshold(String key, long executionTime) {
        // 定义性能阈值
        long warningThreshold = 500;  // 警告阈值：500ms
        long criticalThreshold = 1000; // 严重阈值：1000ms

        if (executionTime > criticalThreshold) {
            log.error("[权限审计-性能] 权限验证严重超时: {}ms, key={}", executionTime, key);
            // 触发性能告警
            triggerPerformanceAlert(key, executionTime, "CRITICAL");
        } else if (executionTime > warningThreshold) {
            log.warn("[权限审计-性能] 权限验证警告超时: {}ms, key={}", executionTime, key);
            // 触发性能告警
            triggerPerformanceAlert(key, executionTime, "WARNING");
        }
    }

    /**
     * 触发性能告警
     */
    private void triggerPerformanceAlert(String key, long executionTime, String level) {
        try {
            // 创建性能告警
            PermissionAlert alert = PermissionAlert.builder()
                .userId(getCurrentUserId())
                .operation("PERFORMANCE_MONITOR")
                .alertType("PERFORMANCE_" + level)
                .message(String.format("权限验证性能告警: key=%s, 耗时=%dms, 级别=%s", key, executionTime, level))
                .createTime(LocalDateTime.now())
                .severity(level)
                .status("ACTIVE")
                .build();

            // 这里应该调用审计日志器记录告警
            // permissionAuditLogger.logPermissionAlert(alert);

            log.warn("[权限审计-性能告警] {}", alert.getMessage());

        } catch (Exception e) {
            log.error("[权限审计] 触发性能告警异常", e);
        }
    }

    /**
     * 权限告警信息
     */
    @lombok.Data
    @lombok.Builder
    public static class PermissionAlert {
        private Long userId;
        private String operation;
        private String alertType;
        private String message;
        private LocalDateTime createTime;
        private String severity;
        private String status;
        private String resolvedTime;
        private String resolvedBy;
    }
}