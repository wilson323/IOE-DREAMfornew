package net.lab1024.sa.base.module.support.rbac.aspect;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.module.support.rbac.service.AreaPermissionService;
import net.lab1024.sa.base.module.support.auth.LoginHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 区域权限验证切面
 * <p>
 * 为Controller方法提供区域权限验证，支持DataScope.AREA数据权限控制
 * 严格遵循项目架构规范，作为权限控制层
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
@Slf4j
@Aspect
@Component
@Order(2) // 权限验证优先级高于其他切面
public class AreaPermissionAspect {

    @Resource
    private AreaPermissionService areaPermissionService;

    /**
     * 环绕通知：拦截带有@RequireAreaPermission注解的方法
     */
    @Around("@annotation(net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission)")
    public Object checkAreaPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("开始区域权限验证");

        try {
            // 获取当前用户ID
            Long userId = LoginHelper.getLoginUserId();
            if (userId == null) {
                log.warn("用户未登录，拒绝访问");
                return ResponseDTO.error("用户未登录");
            }

            // 获取目标方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method targetMethod = signature.getMethod();

            // 获取@RequireAreaPermission注解
            net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission annotation =
                targetMethod.getAnnotation(net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission.class);

            if (annotation == null) {
                log.warn("未找到@RequireAreaPermission注解，跳过权限验证");
                return joinPoint.proceed();
            }

            // 检查区域权限
            if (!checkUserAreaPermission(userId, annotation, joinPoint.getArgs(), signature)) {
                log.warn("用户 {} 区域权限验证失败", userId);
                return ResponseDTO.error("区域权限不足");
            }

            log.debug("用户 {} 区域权限验证通过", userId);
            return joinPoint.proceed();

        } catch (SmartException e) {
            log.error("区域权限验证过程中发生业务异常", e);
            throw e;
        } catch (Exception e) {
            log.error("区域权限验证过程中发生系统异常", e);
            return ResponseDTO.error("系统异常");
        }
    }

    /**
     * 检查用户区域权限
     *
     * @param userId 用户ID
     * @param annotation 权限注解
     * @param args 方法参数
     * @param signature 方法签名
     * @return 是否有权限
     */
    private boolean checkUserAreaPermission(Long userId,
                                           net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission annotation,
                                           Object[] args,
                                           MethodSignature signature) {
        try {
            // 获取需要验证的区域ID
            Long areaId = extractAreaId(annotation, args, signature);
            if (areaId == null && annotation.required()) {
                log.warn("缺少必需的区域ID参数");
                return false;
            }

            // 如果没有区域ID要求，直接通过
            if (areaId == null) {
                return true;
            }

            // 检查区域权限
            boolean hasPermission = areaPermissionService.hasAreaPermission(userId, areaId);

            if (!hasPermission) {
                log.info("用户 {} 无区域 {} 权限", userId, areaId);
            }

            return hasPermission;

        } catch (Exception e) {
            log.error("检查用户区域权限失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 从方法参数中提取区域ID
     *
     * @param annotation 权限注解
     * @param args 方法参数
     * @param signature 方法签名
     * @return 区域ID
     */
    private Long extractAreaId(net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission annotation,
                              Object[] args, MethodSignature signature) {
        try {
            // 如果注解指定了参数名称
            if (!annotation.paramName().isEmpty()) {
                return extractAreaIdByParamName(annotation.paramName(), args, signature);
            }

            // 如果注解指定了参数索引
            if (annotation.paramIndex() >= 0) {
                return extractAreaIdByParamIndex(annotation.paramIndex(), args);
            }

            // 如果注解指定了字段路径
            if (!annotation.fieldPath().isEmpty()) {
                return extractAreaIdByFieldPath(annotation.fieldPath(), args);
            }

            log.warn("未指定区域ID提取方式");
            return null;

        } catch (Exception e) {
            log.error("提取区域ID失败", e);
            return null;
        }
    }

    /**
     * 通过参数名称提取区域ID
     */
    private Long extractAreaIdByParamName(String paramName, Object[] args, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramName.equals(paramNames[i]) && i < args.length) {
                        Object param = args[i];
                        if (param instanceof Long) {
                            return (Long) param;
                        } else if (param instanceof Integer) {
                            return ((Integer) param).longValue();
                        } else if (param instanceof String) {
                            try {
                                return Long.parseLong((String) param);
                            } catch (NumberFormatException e) {
                                log.debug("无法解析区域ID: {}", param);
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("通过参数名称提取区域ID失败: paramName={}", paramName, e);
            return null;
        }
    }

    /**
     * 通过参数索引提取区域ID
     */
    private Long extractAreaIdByParamIndex(int paramIndex, Object[] args) {
        if (paramIndex >= 0 && paramIndex < args.length) {
            Object param = args[paramIndex];
            if (param instanceof Long) {
                return (Long) param;
            } else if (param instanceof Integer) {
                return ((Integer) param).longValue();
            }
        }
        return null;
    }

    /**
     * 通过字段路径提取区域ID
     */
    private Long extractAreaIdByFieldPath(String fieldPath, Object[] args) {
        try {
            if (fieldPath == null || fieldPath.isEmpty()) {
                return null;
            }

            String[] pathParts = fieldPath.split("\\.");

            // 如果是简单字段名，直接从参数中查找
            if (pathParts.length == 1) {
                String fieldName = pathParts[0];
                for (Object arg : args) {
                    if (arg != null) {
                        Long areaId = extractFieldFromObject(arg, fieldName);
                        if (areaId != null) {
                            return areaId;
                        }
                    }
                }
            } else {
                // 复杂路径，需要反射访问
                for (Object arg : args) {
                    if (arg != null) {
                        Object current = arg;
                        for (int i = 0; i < pathParts.length - 1; i++) {
                            current = getFieldValue(current, pathParts[i]);
                            if (current == null) {
                                break;
                            }
                        }

                        if (current != null) {
                            Long areaId = extractFieldFromObject(current, pathParts[pathParts.length - 1]);
                            if (areaId != null) {
                                return areaId;
                            }
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("通过字段路径提取区域ID失败: fieldPath={}", fieldPath, e);
            return null;
        }
    }

    /**
     * 从对象中提取字段值
     */
    private Long extractFieldFromObject(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }

        try {
            Object fieldValue = getFieldValue(obj, fieldName);
            if (fieldValue instanceof Long) {
                return (Long) fieldValue;
            } else if (fieldValue instanceof Integer) {
                return ((Integer) fieldValue).longValue();
            } else if (fieldValue instanceof String) {
                try {
                    return Long.parseLong((String) fieldValue);
                } catch (NumberFormatException e) {
                    log.debug("无法解析区域ID: {}", fieldValue);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("无法获取字段值: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return null;
        }
    }

    /**
     * 使用反射获取对象字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            // 尝试从父类查找
            Class<?> superClass = obj.getClass().getSuperclass();
            while (superClass != null) {
                try {
                    java.lang.reflect.Field field = superClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (NoSuchFieldException ex) {
                    superClass = superClass.getSuperclass();
                } catch (IllegalAccessException ex) {
                    superClass = superClass.getSuperclass();
                }
            }
            log.debug("字段不存在: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return null;
        } catch (IllegalAccessException e) {
            log.debug("无法访问字段: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return null;
        }
    }
}