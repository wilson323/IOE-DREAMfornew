package net.lab1024.sa.common.permission.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.service.UnifiedPermissionService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限参数解析器
 * <p>
 * 企业级权限验证的参数解析组件，提供：
 * - 方法参数解析和绑定
 * - 用户ID解析
 * - 请求参数提取
 * - 路径变量解析
 * - 头信息解析
 * - 表达式解析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionParameterResolver {

    @Resource
    private UnifiedPermissionService unifiedPermissionService;

    /**
     * 参数名发现器
     */
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 解析方法参数
     *
     * @param joinPoint 连接点
     * @return 参数映射表
     */
    public Map<String, Object> resolveParameters(JoinPoint joinPoint) {
        Map<String, Object> parameters = new HashMap<>();

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            if (parameterNames != null && args != null) {
                for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                    String paramName = parameterNames[i];
                    Object paramValue = args[i];

                    parameters.put(paramName, paramValue);

                    // 如果是基础类型，也添加类型信息
                    if (paramValue != null) {
                        parameters.put(paramName + "_type", paramValue.getClass().getSimpleName());
                    }
                }
            }

            // 添加请求相关信息
            addRequestInfo(parameters);

            log.debug("[参数解析] 解析方法参数完成, method={}, paramCount={}",
                method.getName(), parameters.size());

        } catch (Exception e) {
            log.error("[参数解析] 解析方法参数异常, error={}", e.getMessage(), e);
        }

        return parameters;
    }

    /**
     * 解析用户ID
     *
     * @param parameters 参数映射表
     * @param permissionCheck 权限验证注解
     * @return 用户ID
     */
    public Long resolveUserId(Map<String, Object> parameters, PermissionCheck permissionCheck) {
        try {
            // 1. 检查是否指定了用户ID参数
            if (!permissionCheck.userParam().isEmpty()) {
                Object userIdValue = parameters.get(permissionCheck.userParam());
                if (userIdValue != null) {
                    Long userId = convertToLong(userIdValue);
                    if (userId != null) {
                        log.debug("[用户ID解析] 从参数解析用户ID, param={}, value={}",
                            permissionCheck.userParam(), userId);
                        return userId;
                    }
                }
            }

            // 2. 从请求上下文获取用户ID
            Long userId = getCurrentUserId();
            if (userId != null) {
                log.debug("[用户ID解析] 从请求上下文解析用户ID, userId={}", userId);
                return userId;
            }

            // 3. 从参数中查找userId参数
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if ("userId".equals(entry.getKey()) || "user_id".equals(entry.getKey())
                    || "id".equals(entry.getKey())) {
                    Long userId = convertToLong(entry.getValue());
                    if (userId != null) {
                        log.debug("[用户ID解析] 从参数查找用户ID, param={}, value={}",
                            entry.getKey(), userId);
                        return userId;
                    }
                }
            }

            log.warn("[用户ID解析] 无法解析用户ID");
            return null;

        } catch (Exception e) {
            log.error("[用户ID解析] 解析用户ID异常, error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 从请求头获取用户ID
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    return Long.parseLong(userIdHeader);
                }

                // 从请求属性获取用户ID
                Object userIdAttribute = request.getAttribute("userId");
                if (userIdAttribute != null) {
                    return convertToLong(userIdAttribute);
                }

                // 从Session获取用户ID
                Object sessionUserId = request.getSession().getAttribute("userId");
                if (sessionUserId != null) {
                    return convertToLong(sessionUserId);
                }

                // 从Token解析用户ID（这里可以根据实际的JWT实现进行解析）
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    return parseUserIdFromToken(token.substring(7));
                }
            }
        } catch (Exception e) {
            log.debug("[用户ID解析] 从请求上下文获取用户ID失败", e);
        }

        return null;
    }

    /**
     * 从Token解析用户ID
     */
    private Long parseUserIdFromToken(String token) {
        // 这里应该实现具体的JWT解析逻辑
        // 简化实现，实际项目中应该使用JWT库进行解析
        try {
            // 示例：假设Token包含用户ID信息
            // 实际应该解析JWT Token的claims
            if (token.length() > 10) {
                return Long.parseLong(token.substring(0, 8));
            }
        } catch (Exception e) {
            log.debug("[Token解析] 解析用户ID失败, token={}", token);
        }
        return null;
    }

    /**
     * 添加请求相关信息
     */
    private void addRequestInfo(Map<String, Object> parameters) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 添加请求信息
                parameters.put("_request_method", request.getMethod());
                parameters.put("_request_uri", request.getRequestURI());
                parameters.put("_request_url", request.getRequestURL().toString());
                parameters.put("_remote_addr", request.getRemoteAddr());
                parameters.put("_user_agent", request.getHeader("User-Agent"));
                parameters.put("_session_id", request.getSession().getId());

                // 添加请求参数
                Map<String, String[]> requestParams = request.getParameterMap();
                for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        if (values.length == 1) {
                            parameters.put("_param_" + entry.getKey(), values[0]);
                        } else {
                            parameters.put("_param_" + entry.getKey(), String.join(",", values));
                        }
                    }
                }

                // 添加请求头信息
                for (String headerName : request.getHeaderNames()) {
                    String headerValue = request.getHeader(headerName);
                    parameters.put("_header_" + headerName.toLowerCase(), headerValue);
                }

                log.debug("[请求信息] 添加请求信息完成, paramCount={}", parameters.size());
            }
        } catch (Exception e) {
            log.debug("[请求信息] 添加请求信息异常", e);
        }
    }

    /**
     * 转换为Long类型
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return null;
    }

    /**
     * 获取参数值
     *
     * @param parameters 参数映射表
     * @param paramName 参数名
     * @param paramType 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(Map<String, Object> parameters, String paramName, Class<T> paramType) {
        Object value = parameters.get(paramName);
        if (value != null && paramType.isInstance(value)) {
            return (T) value;
        }

        // 尝试类型转换
        if (value != null) {
            try {
                if (paramType == Long.class) {
                    return (T) convertToLong(value);
                } else if (paramType == String.class) {
                    return (T) value.toString();
                } else if (paramType == Integer.class) {
                    return (T) Integer.valueOf(value.toString());
                } else if (paramType == Boolean.class) {
                    return (T) Boolean.valueOf(value.toString());
                }
            } catch (Exception e) {
                log.debug("[参数转换] 类型转换失败, param={}, type={}, value={}",
                    paramName, paramType.getSimpleName(), value, e);
            }
        }

        return null;
    }

    /**
     * 检查参数是否存在
     *
     * @param parameters 参数映射表
     * @param paramName 参数名
     * @return 是否存在
     */
    public boolean hasParameter(Map<String, Object> parameters, String paramName) {
        return parameters.containsKey(paramName) && parameters.get(paramName) != null;
    }

    /**
     * 获取参数类型
     *
     * @param parameters 参数映射表
     * @param paramName 参数名
     * @return 参数类型
     */
    public String getParameterType(Map<String, Object> parameters, String paramName) {
        Object value = parameters.get(paramName);
        return value != null ? value.getClass().getSimpleName() : null;
    }

    /**
     * 获取所有参数名
     *
     * @param parameters 参数映射表
     * @return 参数名列表
     */
    public Set<String> getParameterNames(Map<String, Object> parameters) {
        return parameters.keySet();
    }

    /**
     * 过滤系统参数
     *
     * @param parameters 参数映射表
     * @return 过滤后的参数映射表
     */
    public Map<String, Object> filterSystemParameters(Map<String, Object> parameters) {
        Map<String, Object> filtered = new HashMap<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("_") && !key.endsWith("_type")) {
                filtered.put(key, entry.getValue());
            }
        }

        return filtered;
    }

    /**
     * 获取系统参数
     *
     * @param parameters 参数映射表
     * @return 系统参数映射表
     */
    public Map<String, Object> getSystemParameters(Map<String, Object> parameters) {
        Map<String, Object> systemParams = new HashMap<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_")) {
                systemParams.put(key, entry.getValue());
            }
        }

        return systemParams;
    }
}