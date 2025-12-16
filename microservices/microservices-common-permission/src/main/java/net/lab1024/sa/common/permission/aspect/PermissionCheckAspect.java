package net.lab1024.sa.common.permission.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.service.UnifiedPermissionService;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.domain.enums.PermissionCondition;
import net.lab1024.sa.common.permission.domain.enums.LogicOperator;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 权限验证AOP切面
 * <p>
 * 企业级声明式权限验证的核心实现，支持：
 * - 方法级权限验证
 * - 类级权限验证
 * - 参数解析和绑定
 * - 多种验证模式
 * - 缓存和审计
 * - 异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Aspect
@Component
public class PermissionCheckAspect {

    @Resource
    private UnifiedPermissionService unifiedPermissionService;

    @Resource
    private PermissionParameterResolver parameterResolver;

    @Resource
    private PermissionResultHandler resultHandler;

    /**
     * 权限验证切入点
     */
    @Pointcut("@annotation(net.lab1024.sa.common.permission.annotation.PermissionCheck) || " +
              "@within(net.lab1024.sa.common.permission.annotation.PermissionCheck)")
    public void permissionCheckPointcut() {
    }

    /**
     * 权限验证环绕通知
     */
    @Around("permissionCheckPointcut()")
    public Object aroundPermissionCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        try {
            // 1. 获取权限验证注解
            PermissionCheck permissionCheck = getPermissionCheckAnnotation(method, joinPoint.getTarget().getClass());
            if (permissionCheck == null) {
                log.debug("[权限验证] 未找到权限验证注解，直接执行方法: {}", method.getName());
                return joinPoint.proceed();
            }

            log.debug("[权限验证] 开始权限验证, method={}, description={}",
                method.getName(), permissionCheck.description());

            // 2. 解析方法参数
            Map<String, Object> parameters = parameterResolver.resolveParameters(joinPoint);

            // 3. 获取用户ID
            Long userId = parameterResolver.resolveUserId(parameters, permissionCheck);

            // 4. 执行权限验证
            PermissionValidationResult result = performPermissionValidation(userId, permissionCheck, parameters);

            // 5. 记录验证结果
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[权限验证] 验证完成, method={}, valid={}, duration={}ms",
                method.getName(), result.isValid(), duration);

            // 6. 处理验证失败
            if (!result.isValid()) {
                return handleValidationFailure(permissionCheck, result);
            }

            // 7. 执行前置处理
            executeBeforeHandler(permissionCheck, parameters);

            // 8. 执行目标方法
            Object methodResult = joinPoint.proceed();

            // 9. 执行后置处理
            executeAfterHandler(permissionCheck, parameters, methodResult);

            return methodResult;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[权限验证] 执行异常, method={}, duration={}ms, error={}",
                method.getName(), duration, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取权限验证注解
     */
    private PermissionCheck getPermissionCheckAnnotation(Method method, Class<?> targetClass) {
        // 优先获取方法上的注解
        PermissionCheck methodAnnotation = AnnotationUtils.findAnnotation(method, PermissionCheck.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 获取类上的注解
        PermissionCheck classAnnotation = AnnotationUtils.findAnnotation(targetClass, PermissionCheck.class);
        if (classAnnotation != null) {
            log.debug("[权限验证] 使用类级别权限验证注解, class={}", targetClass.getSimpleName());
            return classAnnotation;
        }

        return null;
    }

    /**
     * 执行权限验证
     */
    private PermissionValidationResult performPermissionValidation(Long userId, PermissionCheck permissionCheck,
                                                                 Map<String, Object> parameters) {
        try {
            // 构建权限验证条件
            List<PermissionCondition> conditions = new ArrayList<>();

            // 1. 权限条件
            if (permissionCheck.value().length > 0) {
                for (String permission : permissionCheck.value()) {
                    String resource = resolveResource(permissionCheck.resource(), parameters);
                    conditions.add(PermissionCondition.ofPermission(permission, resource));
                }
            }

            // 2. 角色条件
            if (permissionCheck.roles().length > 0) {
                for (String role : permissionCheck.roles()) {
                    String resource = resolveResource(permissionCheck.resource(), parameters);
                    conditions.add(PermissionCondition.ofRole(role, resource));
                }
            }

            // 3. 数据权限条件
            if (permissionCheck.dataScope() != PermissionCheck.DataScopeType.NONE) {
                addDataScopeCondition(conditions, permissionCheck, parameters);
            }

            // 4. 区域权限条件
            if (!permissionCheck.areaParam().isEmpty()) {
                Object areaId = parameters.get(permissionCheck.areaParam());
                if (areaId != null) {
                    conditions.add(PermissionCondition.ofAreaPermission(
                        Long.parseLong(areaId.toString()), "ACCESS"));
                }
            }

            // 5. 设备权限条件
            if (!permissionCheck.deviceParam().isEmpty()) {
                Object deviceId = parameters.get(permissionCheck.deviceParam());
                if (deviceId != null) {
                    conditions.add(PermissionCondition.ofDevicePermission(
                        deviceId.toString(), "ACCESS"));
                }
            }

            // 6. 模块权限条件
            if (!permissionCheck.moduleParam().isEmpty()) {
                Object moduleCode = parameters.get(permissionCheck.moduleParam());
                if (moduleCode != null) {
                    conditions.add(PermissionCondition.ofModulePermission(
                        moduleCode.toString(), "ACCESS"));
                }
            }

            // 执行验证
            if (conditions.isEmpty()) {
                log.warn("[权限验证] 未配置任何权限条件");
                return PermissionValidationResult.success("未配置权限条件，默认通过");
            }

            return unifiedPermissionService.validateConditions(
                userId,
                conditions.toArray(new PermissionCondition[0]),
                convertLogicOperator(permissionCheck.operator())
            );

        } catch (Exception e) {
            log.error("[权限验证] 验证过程异常, userId={}, error={}", userId, e.getMessage(), e);
            return PermissionValidationResult.systemError("权限验证异常: " + e.getMessage());
        }
    }

    /**
     * 添加数据权限条件
     */
    private void addDataScopeCondition(List<PermissionCondition> conditions, PermissionCheck permissionCheck,
                                      Map<String, Object> parameters) {
        String dataScopeParam = permissionCheck.dataScopeParam();
        if (dataScopeParam.isEmpty()) {
            return;
        }

        Object resourceId = parameters.get(dataScopeParam);
        if (resourceId == null) {
            return;
        }

        String dataType = switch (permissionCheck.dataScope()) {
            case DEPARTMENT -> "DEPARTMENT";
            case AREA -> "AREA";
            case DEVICE -> "DEVICE";
            default -> "CUSTOM";
        };

        conditions.add(PermissionCondition.ofDataScope(dataType, resourceId));
    }

    /**
     * 解析资源标识
     */
    private String resolveResource(String resource, Map<String, Object> parameters) {
        if (resource.isEmpty()) {
            return null;
        }

        // 简单的SpEL表达式解析（#{#paramName}格式）
        if (resource.startsWith("#{") && resource.endsWith("}")) {
            String paramName = resource.substring(2, resource.length() - 1);
            Object value = parameters.get(paramName);
            return value != null ? value.toString() : null;
        }

        return resource;
    }

    /**
     * 转换逻辑操作符
     */
    private LogicOperator convertLogicOperator(PermissionCheck.LogicOperator operator) {
        return switch (operator) {
            case AND -> LogicOperator.AND;
            case OR -> LogicOperator.OR;
        };
    }

    /**
     * 处理验证失败
     */
    private Object handleValidationFailure(PermissionCheck permissionCheck, PermissionValidationResult result) {
        switch (permissionCheck.failureStrategy()) {
            case EXCEPTION:
                throw createPermissionException(permissionCheck, result);
            case RETURN_NULL:
                return null;
            case RETURN_DEFAULT:
                return resultHandler.getDefaultValue(permissionCheck, result);
            case CUSTOM:
                return resultHandler.handleCustomFailure(permissionCheck, result);
            default:
                throw createPermissionException(permissionCheck, result);
        }
    }

    /**
     * 创建权限异常
     */
    private Exception createPermissionException(PermissionCheck permissionCheck, PermissionValidationResult result) {
        String message = resolveMessage(permissionCheck.message(), result);

        try {
            return permissionCheck.exceptionType()
                .getConstructor(String.class)
                .newInstance(message);
        } catch (Exception e) {
            log.error("[权限验证] 创建权限异常失败", e);
            return new PermissionCheck.PermissionDeniedException(message);
        }
    }

    /**
     * 解析错误消息
     */
    private String resolveMessage(String template, PermissionValidationResult result) {
        if (template.contains("{") && template.contains("}")) {
            return template
                .replace("{permission}", result.getPermission() != null ? result.getPermission() : "")
                .replace("{resource}", result.getResource() != null ? result.getResource() : "")
                .replace("{message}", result.getMessage() != null ? result.getMessage() : "");
        }
        return template;
    }

    /**
     * 执行前置处理
     */
    private void executeBeforeHandler(PermissionCheck permissionCheck, Map<String, Object> parameters) {
        if (permissionCheck.beforeHandler().isEmpty()) {
            return;
        }

        try {
            resultHandler.executeBeforeHandler(permissionCheck.beforeHandler(), parameters);
        } catch (Exception e) {
            log.warn("[权限验证] 前置处理执行失败, handler={}, error={}",
                permissionCheck.beforeHandler(), e.getMessage(), e);
        }
    }

    /**
     * 执行后置处理
     */
    private void executeAfterHandler(PermissionCheck permissionCheck, Map<String, Object> parameters, Object methodResult) {
        if (permissionCheck.afterHandler().isEmpty()) {
            return;
        }

        try {
            resultHandler.executeAfterHandler(permissionCheck.afterHandler(), parameters, methodResult);
        } catch (Exception e) {
            log.warn("[权限验证] 后置处理执行失败, handler={}, error={}",
                permissionCheck.afterHandler(), e.getMessage(), e);
        }
    }
}