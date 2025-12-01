package net.lab1024.sa.base.common.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.annotation.cache.UnifiedCache;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 统一缓存切面
 * <p>
 * 严格遵循repowiki AOP规范：
 * - 统一缓存注解处理
 * - 支持SpEL表达式
 * - 异步缓存操作支持
 * - 异常处理和日志记录
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
public class UnifiedCacheAspect {

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 处理缓存注解
     */
    @Around("@annotation(unifiedCache)")
    public Object handleCache(ProceedingJoinPoint joinPoint, UnifiedCache unifiedCache) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            // 解析缓存键
            String cacheKey = parseCacheKey(joinPoint, unifiedCache);

            // 检查条件表达式
            if (!shouldCache(joinPoint, unifiedCache)) {
                log.debug("缓存条件不满足，直接执行方法: key={}", cacheKey);
                return joinPoint.proceed();
            }

            // 尝试从缓存获取
            UnifiedCacheManager.CacheResult<Object> cacheResult = getFromCache(joinPoint, unifiedCache, cacheKey);

            if (cacheResult.isSuccess()) {
                log.debug("缓存命中: namespace={}, key={}, hitTime={}ms",
                        unifiedCache.namespace().getPrefix(), cacheKey, System.currentTimeMillis() - startTime);
                return cacheResult.getData();
            }

            // 缓存未命中，执行方法
            Object result = joinPoint.proceed();

            // 检查是否应该缓存结果
            if (shouldCacheResult(result, unifiedCache)) {
                if (unifiedCache.async()) {
                    // 异步设置缓存
                    setCacheAsync(unifiedCache, cacheKey, result);
                } else {
                    // 同步设置缓存
                    setCache(unifiedCache, cacheKey, result);
                }
            }

            log.debug("方法执行完成并缓存结果: namespace={}, key={}, executionTime={}ms",
                    unifiedCache.namespace().getPrefix(), cacheKey, System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("缓存处理失败", e);
            throw e;
        }
    }

    /**
     * 解析缓存键
     */
    private String parseCacheKey(ProceedingJoinPoint joinPoint, UnifiedCache unifiedCache) {
        String keyExpression = unifiedCache.key();

        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            // 生成默认缓存键
            return generateDefaultKey(joinPoint);
        }

        try {
            // 解析SpEL表达式
            EvaluationContext context = createEvaluationContext(joinPoint);
            Expression expression = expressionParser.parseExpression(keyExpression);
            Object keyValue = expression.getValue(context);

            if (keyValue == null) {
                return generateDefaultKey(joinPoint);
            }

            return String.valueOf(keyValue);

        } catch (Exception e) {
            log.warn("解析缓存键表达式失败: {}, 使用默认键", keyExpression, e);
            return generateDefaultKey(joinPoint);
        }
    }

    /**
     * 生成默认缓存键
     */
    private String generateDefaultKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(className).append(":").append(methodName);

        if (args != null && args.length > 0) {
            keyBuilder.append(":");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    keyBuilder.append(",");
                }
                keyBuilder.append(args[i] != null ? args[i].toString() : "null");
            }
        }

        return keyBuilder.toString();
    }

    /**
     * 创建SpEL表达式上下文
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        StandardEvaluationContext context = new StandardEvaluationContext();

        // 添加方法参数
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 添加参数数组
        context.setVariable("args", args);
        context.setVariable("method", method);
        context.setVariable("target", joinPoint.getTarget());

        return context;
    }

    /**
     * 检查是否应该缓存
     */
    private boolean shouldCache(ProceedingJoinPoint joinPoint, UnifiedCache unifiedCache) {
        // 检查条件表达式
        String condition = unifiedCache.condition();
        if (condition != null && !condition.trim().isEmpty()) {
            try {
                EvaluationContext context = createEvaluationContext(joinPoint);
                Expression expression = expressionParser.parseExpression(condition);
                Boolean result = expression.getValue(context, Boolean.class);
                if (result != null && !result) {
                    return false;
                }
            } catch (Exception e) {
                log.warn("解析缓存条件表达式失败: {}", condition, e);
            }
        }

        // 检查排除条件
        String unless = unifiedCache.unless();
        if (unless != null && !unless.trim().isEmpty()) {
            try {
                EvaluationContext context = createEvaluationContext(joinPoint);
                Expression expression = expressionParser.parseExpression(unless);
                Boolean result = expression.getValue(context, Boolean.class);
                if (result != null && result) {
                    return false;
                }
            } catch (Exception e) {
                log.warn("解析缓存排除条件表达式失败: {}", unless, e);
            }
        }

        return true;
    }

    /**
     * 检查是否应该缓存结果
     */
    private boolean shouldCacheResult(Object result, UnifiedCache unifiedCache) {
        // 检查null值缓存设置
        if (result == null && !unifiedCache.cacheNull()) {
            return false;
        }

        return true;
    }

    /**
     * 从缓存获取数据
     */
    private UnifiedCacheManager.CacheResult<Object> getFromCache(ProceedingJoinPoint joinPoint,
            UnifiedCache unifiedCache, String cacheKey) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Type returnType = method.getGenericReturnType();

            // 处理泛型返回类型
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                Type rawType = parameterizedType.getRawType();

                // 处理CompletableFuture异步返回类型
                if (rawType.equals(CompletableFuture.class)) {
                    // 异步类型暂不直接支持缓存
                    return UnifiedCacheManager.CacheResult.failure(cacheKey, unifiedCache.namespace(),
                            "Async type not supported for direct cache");
                }

                // 其他泛型类型可以使用TypeReference
                // 使用Object.class作为类型参数，避免泛型类型约束问题
                UnifiedCacheManager.CacheResult<?> result = unifiedCacheManager.get(unifiedCache.namespace(), cacheKey,
                        Object.class);
                @SuppressWarnings("unchecked")
                UnifiedCacheManager.CacheResult<Object> castResult = (UnifiedCacheManager.CacheResult<Object>) result;
                return castResult;
            } else {
                // 普通类型
                Class<?> returnClass = (Class<?>) returnType;
                UnifiedCacheManager.CacheResult<?> result = unifiedCacheManager.get(unifiedCache.namespace(), cacheKey,
                        returnClass);
                // 转换为CacheResult<Object>以匹配返回类型
                @SuppressWarnings("unchecked")
                UnifiedCacheManager.CacheResult<Object> castResult = (UnifiedCacheManager.CacheResult<Object>) result;
                return castResult;
            }

        } catch (Exception e) {
            log.error("从缓存获取数据失败: namespace={}, key={}", unifiedCache.namespace().getPrefix(), cacheKey, e);
            return UnifiedCacheManager.CacheResult.failure(cacheKey, unifiedCache.namespace(), e.getMessage());
        }
    }

    /**
     * 设置缓存
     */
    private void setCache(UnifiedCache unifiedCache, String cacheKey, Object result) {
        try {
            long ttl = unifiedCache.ttl();
            if (ttl > 0) {
                unifiedCacheManager.set(unifiedCache.namespace(), cacheKey, result, ttl, unifiedCache.timeUnit());
            } else {
                unifiedCacheManager.set(unifiedCache.namespace(), cacheKey, result);
            }

        } catch (Exception e) {
            log.error("设置缓存失败: namespace={}, key={}", unifiedCache.namespace().getPrefix(), cacheKey, e);
        }
    }

    /**
     * 异步设置缓存
     */
    private void setCacheAsync(UnifiedCache unifiedCache, String cacheKey, Object result) {
        CompletableFuture.runAsync(() -> {
            try {
                long ttl = unifiedCache.ttl();
                if (ttl > 0) {
                    unifiedCacheManager.set(unifiedCache.namespace(), cacheKey, result, ttl, unifiedCache.timeUnit());
                } else {
                    unifiedCacheManager.set(unifiedCache.namespace(), cacheKey, result);
                }

                log.debug("异步缓存设置完成: namespace={}, key={}", unifiedCache.namespace().getPrefix(), cacheKey);

            } catch (Exception e) {
                log.error("异步设置缓存失败: namespace={}, key={}", unifiedCache.namespace().getPrefix(), cacheKey, e);
            }
        });
    }

}
