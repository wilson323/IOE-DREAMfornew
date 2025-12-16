package net.lab1024.sa.common.permission.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限结果处理器
 * <p>
 * 企业级权限验证结果处理组件，提供：
 * - 默认值返回
 * - 自定义失败处理
 * - 前置和后置处理
 * - 异常处理
 * - 结果转换
 * - 性能监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionResultHandler {

    /**
     * 处理器缓存
     */
    private final Map<String, Method> handlerMethodCache = new ConcurrentHashMap<>();

    /**
     * 获取默认值
     *
     * @param permissionCheck 权限验证注解
     * @param result 验证结果
     * @return 默认值
     */
    public Object getDefaultValue(PermissionCheck permissionCheck, PermissionValidationResult result) {
        try {
            // 根据方法返回类型确定默认值
            // 这里需要获取原方法的返回类型，但在AOP中难以直接获取
            // 可以通过反射或其他方式获取

            // 基本类型的默认值
            return switch (getDefaultReturnType()) {
                case "boolean", "Boolean" -> false;
                case "int", "Integer" -> 0;
                case "long", "Long" -> 0L;
                case "double", "Double" -> 0.0;
                case "float", "Float" -> 0.0f;
                case "String" -> "";
                case "void" -> null;
                default -> null;
            };

        } catch (Exception e) {
            log.error("[权限结果处理] 获取默认值异常", e);
            return null;
        }
    }

    /**
     * 处理自定义失败
     *
     * @param permissionCheck 权限验证注解
     * @param result 验证结果
     * @return 处理结果
     */
    public Object handleCustomFailure(PermissionCheck permissionCheck, PermissionValidationResult result) {
        try {
            log.debug("[权限结果处理] 执行自定义失败处理, statusCode={}, message={}",
                result.getStatusCode(), result.getMessage());

            // 这里可以实现自定义的失败处理逻辑
            // 例如：返回特定的错误响应、重定向到登录页面等

            // 示例：返回错误响应对象
            Map<String, Object> errorResponse = new ConcurrentHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", result.getStatusCode());
            errorResponse.put("message", result.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return errorResponse;

        } catch (Exception e) {
            log.error("[权限结果处理] 自定义失败处理异常", e);
            return null;
        }
    }

    /**
     * 执行前置处理
     *
     * @param handlerName 处理器名称
     * @param parameters 参数映射表
     */
    public void executeBeforeHandler(String handlerName, Map<String, Object> parameters) {
        try {
            if (handlerName.isEmpty()) {
                return;
            }

            log.debug("[权限结果处理] 执行前置处理, handler={}", handlerName);

            // 解析处理器名称：className.methodName
            String[] parts = handlerName.split("\\.");
            if (parts.length < 2) {
                log.warn("[权限结果处理] 无效的处理器名称: {}", handlerName);
                return;
            }

            String className = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
            String methodName = parts[parts.length - 1];

            // 获取处理器方法
            Method handlerMethod = getHandlerMethod(className, methodName, parameters.getClass());
            if (handlerMethod == null) {
                log.warn("[权限结果处理] 未找到处理器方法: {}", handlerName);
                return;
            }

            // 执行处理器方法
            Object handlerInstance = getHandlerInstance(className);
            if (handlerInstance != null) {
                handlerMethod.invoke(handlerInstance, parameters);
                log.debug("[权限结果处理] 前置处理执行完成, handler={}", handlerName);
            }

        } catch (Exception e) {
            log.error("[权限结果处理] 前置处理执行异常, handler={}, error={}", handlerName, e.getMessage(), e);
        }
    }

    /**
     * 执行后置处理
     *
     * @param handlerName 处理器名称
     * @param parameters 参数映射表
     * @param methodResult 方法执行结果
     */
    public void executeAfterHandler(String handlerName, Map<String, Object> parameters, Object methodResult) {
        try {
            if (handlerName.isEmpty()) {
                return;
            }

            log.debug("[权限结果处理] 执行后置处理, handler={}", handlerName);

            // 解析处理器名称：className.methodName
            String[] parts = handlerName.split("\\.");
            if (parts.length < 2) {
                log.warn("[权限结果处理] 无效的处理器名称: {}", handlerName);
                return;
            }

            String className = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
            String methodName = parts[parts.length - 1];

            // 获取处理器方法
            Method handlerMethod = getHandlerMethod(className, methodName, parameters.getClass(), Object.class);
            if (handlerMethod == null) {
                log.warn("[权限结果处理] 未找到处理器方法: {}", handlerName);
                return;
            }

            // 执行处理器方法
            Object handlerInstance = getHandlerInstance(className);
            if (handlerInstance != null) {
                handlerMethod.invoke(handlerInstance, parameters, methodResult);
                log.debug("[权限结果处理] 后置处理执行完成, handler={}", handlerName);
            }

        } catch (Exception e) {
            log.error("[权限结果处理] 后置处理执行异常, handler={}, error={}", handlerName, e.getMessage(), e);
        }
    }

    /**
     * 获取处理器方法
     */
    private Method getHandlerMethod(String className, String methodName, Class<?>... parameterTypes) {
        String cacheKey = className + "." + methodName + "." + java.util.Arrays.toString(parameterTypes);

        Method cachedMethod = handlerMethodCache.get(cacheKey);
        if (cachedMethod != null) {
            return cachedMethod;
        }

        try {
            Class<?> handlerClass = Class.forName(className);
            Method method = handlerClass.getMethod(methodName, parameterTypes);
            handlerMethodCache.put(cacheKey, method);
            return method;
        } catch (Exception e) {
            log.error("[权限结果处理] 获取处理器方法失败, className={}, methodName={}, error={}",
                className, methodName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取处理器实例
     */
    private Object getHandlerInstance(String className) {
        try {
            Class<?> handlerClass = Class.forName(className);

            // 尝试获取Spring管理的实例
            // 这里可以通过ApplicationContext获取Bean
            // 简化实现：直接创建实例
            return handlerClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            log.error("[权限结果处理] 获取处理器实例失败, className={}, error={}", className, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取默认返回类型
     */
    private String getDefaultReturnType() {
        // 这里应该从实际的方法签名中获取返回类型
        // 由于在结果处理器中难以获取原方法，这里返回一个默认值
        // 实际项目中可以通过其他方式传递方法信息
        return "Object";
    }

    /**
     * 转换结果类型
     *
     * @param result 原始结果
     * @param targetType 目标类型
     * @return 转换后的结果
     */
    @SuppressWarnings("unchecked")
    public <T> T convertResult(Object result, Class<T> targetType) {
        if (result == null) {
            return null;
        }

        if (targetType.isInstance(result)) {
            return (T) result;
        }

        try {
            // 基本类型转换
            if (targetType == String.class) {
                return (T) result.toString();
            } else if (targetType == Long.class && result instanceof Number) {
                return (T) Long.valueOf(((Number) result).longValue());
            } else if (targetType == Integer.class && result instanceof Number) {
                return (T) Integer.valueOf(((Number) result).intValue());
            } else if (targetType == Boolean.class && result instanceof String) {
                return (T) Boolean.valueOf((String) result);
            }

            // 其他转换逻辑
            log.warn("[权限结果处理] 不支持的类型转换, from={}, to={}", result.getClass(), targetType);
            return null;

        } catch (Exception e) {
            log.error("[权限结果处理] 结果类型转换异常, result={}, target={}, error={}",
                result, targetType.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 包装结果
     *
     * @param result 原始结果
     * @param success 是否成功
     * @param message 消息
     * @return 包装后的结果
     */
    public Map<String, Object> wrapResult(Object result, boolean success, String message) {
        Map<String, Object> wrappedResult = new ConcurrentHashMap<>();
        wrappedResult.put("success", success);
        wrappedResult.put("message", message);
        wrappedResult.put("timestamp", System.currentTimeMillis());

        if (result != null) {
            wrappedResult.put("data", result);
        }

        return wrappedResult;
    }

    /**
     * 包装成功结果
     *
     * @param result 原始结果
     * @return 包装后的结果
     */
    public Map<String, Object> wrapSuccessResult(Object result) {
        return wrapResult(result, true, "操作成功");
    }

    /**
     * 包装失败结果
     *
     * @param message 错误消息
     * @return 包装后的结果
     */
    public Map<String, Object> wrapFailureResult(String message) {
        return wrapResult(null, false, message);
    }

    /**
     * 包装异常结果
     *
     * @param exception 异常
     * @return 包装后的结果
     */
    public Map<String, Object> wrapExceptionResult(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            message = exception.getClass().getSimpleName();
        }

        return wrapFailureResult(message);
    }

    /**
     * 记录处理结果
     *
     * @param handlerName 处理器名称
     * @param success 是否成功
     * @param duration 处理耗时
     * @param error 错误信息
     */
    public void recordHandlerResult(String handlerName, boolean success, long duration, String error) {
        if (success) {
            log.debug("[权限结果处理] 处理器执行成功, handler={}, duration={}ms", handlerName, duration);
        } else {
            log.warn("[权限结果处理] 处理器执行失败, handler={}, duration={}ms, error={}",
                handlerName, duration, error);
        }

        // 这里可以添加性能统计和监控逻辑
        // 例如：记录到监控系统、发送告警等
    }

    /**
     * 清理处理器缓存
     */
    public void clearHandlerCache() {
        handlerMethodCache.clear();
        log.info("[权限结果处理] 处理器缓存已清理");
    }

    /**
     * 获取处理器缓存大小
     */
    public int getHandlerCacheSize() {
        return handlerMethodCache.size();
    }
}