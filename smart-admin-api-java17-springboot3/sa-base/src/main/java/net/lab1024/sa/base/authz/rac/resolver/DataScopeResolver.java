package net.lab1024.sa.base.authz.rac.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lab1024.sa.base.authz.rac.AuthorizationContext;

/**
 * 数据域解析器类
 * 负责解析用户的数据权限范围，支持缓存机制提升性能
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-23
 */
public class DataScopeResolver {
    private static final Logger log = LoggerFactory.getLogger(DataScopeResolver.class);

    /**
     * 结果缓存，提升解析性能
     */
    private final Map<String, DataScopeResult> cache = new ConcurrentHashMap<>();

    /**
     * 缓存过期时间（毫秒）
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟

    /**
     * 解析授权上下文的数据域范围
     *
     * @param context 授权上下文
     * @return 数据域解析结果
     */
    public DataScopeResult resolve(AuthorizationContext context) {
        if (context == null) {
            log.warn("授权上下文为空，返回无权限结果");
            return createNoAccessResult();
        }

        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(context);

            // 检查缓存
            DataScopeResult cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                log.debug("命中数据域解析缓存: userId={}", context.getUserId());
                return cachedResult;
            }

            // 解析数据域
            long startTime = System.currentTimeMillis();
            DataScopeResult result = doResolve(context);
            long resolveTime = System.currentTimeMillis() - startTime;

            // 设置解析时间
            result.setResolveTime(resolveTime);
            result.setCacheHit(false);

            // 缓存结果
            cache.put(cacheKey, result);

            log.debug("数据域解析完成: userId={}, scope={},耗时={}ms",
                    context.getUserId(), context.getEffectiveDataScope(), resolveTime);

            return result;

        } catch (Exception e) {
            log.error("数据域解析失败: userId={}", context.getUserId(), e);
            return createNoAccessResult();
        }
    }

    /**
     * 合并两个数据域结果
     *
     * @param result1 结果1
     * @param result2 结果2
     * @return 合并后的结果
     */
    public DataScopeResult merge(DataScopeResult result1, DataScopeResult result2) {
        if (result1 == null)
            return result2;
        if (result2 == null)
            return result1;

        // 如果任一结果有全部数据权限，则合并结果也有全部数据权限
        if (result1.isAllDataAccess() || result2.isAllDataAccess()) {
            return DataScopeResult.builder()
                    .allDataAccess(true)
                    .accessibleAreaIds(Set.of())
                    .accessibleDeptIds(Set.of())
                    .accessibleUserIds(Set.of())
                    .customRules(new HashMap<>())
                    .build();
        }

        // 合并区域权限
        Set<Long> mergedAreaIds = new java.util.HashSet<>(result1.getAccessibleAreaIds());
        mergedAreaIds.addAll(result2.getAccessibleAreaIds());

        // 合并部门权限
        Set<Long> mergedDeptIds = new java.util.HashSet<>(result1.getAccessibleDeptIds());
        mergedDeptIds.addAll(result2.getAccessibleDeptIds());

        // 合并用户权限
        Set<Long> mergedUserIds = new java.util.HashSet<>(result1.getAccessibleUserIds());
        mergedUserIds.addAll(result2.getAccessibleUserIds());

        // 合并自定义规则
        Map<String, Object> mergedCustomRules = new HashMap<>(result1.getCustomRules());
        mergedCustomRules.putAll(result2.getCustomRules());

        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(mergedAreaIds)
                .accessibleDeptIds(mergedDeptIds)
                .accessibleUserIds(mergedUserIds)
                .customRules(mergedCustomRules)
                .build();
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        // 这里简化实现，实际项目中可以使用定时任务清理过期缓存
        if (cache.size() > 1000) {
            cache.clear();
            log.info("数据域解析缓存已清理");
        }
    }

    /**
     * 清理所有缓存
     */
    public void clearCache() {
        cache.clear();
        log.info("数据域解析缓存已清空");
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "cacheSize", cache.size(),
                "maxSize", 1000,
                "expireTime", CACHE_EXPIRE_TIME);
    }

    /**
     * 执行实际的数据域解析逻辑
     *
     * @param context 授权上下文
     * @return 解析结果
     */
    private DataScopeResult doResolve(AuthorizationContext context) {
        AuthorizationContext.DataScope dataScope = context.getEffectiveDataScope();

        switch (dataScope) {
            case ALL:
                return createAllAccessResult();
            case AREA:
                return createAreaAccessResult(context);
            case DEPT:
                return createDeptAccessResult(context);
            case SELF:
                return createSelfAccessResult(context);
            case CUSTOM:
                return createCustomAccessResult(context);
            case NONE:
            default:
                return createNoAccessResult();
        }
    }

    /**
     * 创建全部数据权限结果
     */
    private DataScopeResult createAllAccessResult() {
        return DataScopeResult.builder()
                .allDataAccess(true)
                .accessibleAreaIds(Set.of())
                .accessibleDeptIds(Set.of())
                .accessibleUserIds(Set.of())
                .customRules(Map.of())
                .build();
    }

    /**
     * 创建区域数据权限结果
     */
    private DataScopeResult createAreaAccessResult(AuthorizationContext context) {
        Set<Long> areaIds = context.getAreaIds() != null ? context.getAreaIds() : Set.of();

        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(areaIds)
                .accessibleDeptIds(Set.of())
                .accessibleUserIds(Set.of())
                .customRules(context.getCustomRules() != null ? context.getCustomRules() : Map.of())
                .build();
    }

    /**
     * 创建部门数据权限结果
     */
    private DataScopeResult createDeptAccessResult(AuthorizationContext context) {
        Set<Long> deptIds = context.getDeptIds() != null ? context.getDeptIds() : Set.of();

        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(Set.of())
                .accessibleDeptIds(deptIds)
                .accessibleUserIds(Set.of())
                .customRules(context.getCustomRules() != null ? context.getCustomRules() : Map.of())
                .build();
    }

    /**
     * 创建个人数据权限结果
     */
    private DataScopeResult createSelfAccessResult(AuthorizationContext context) {
        Set<Long> userIds = Set.of(context.getUserId());

        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(Set.of())
                .accessibleDeptIds(Set.of())
                .accessibleUserIds(userIds)
                .customRules(context.getCustomRules() != null ? context.getCustomRules() : Map.of())
                .build();
    }

    /**
     * 创建自定义数据权限结果
     */
    private DataScopeResult createCustomAccessResult(AuthorizationContext context) {
        Set<Long> areaIds = context.getAreaIds() != null ? context.getAreaIds() : Set.of();
        Set<Long> deptIds = context.getDeptIds() != null ? context.getDeptIds() : Set.of();
        Set<Long> userIds = context.getUserIds() != null ? context.getUserIds() : Set.of();

        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(areaIds)
                .accessibleDeptIds(deptIds)
                .accessibleUserIds(userIds)
                .customRules(context.getCustomRules() != null ? context.getCustomRules() : Map.of())
                .build();
    }

    /**
     * 创建无数据权限结果
     */
    private DataScopeResult createNoAccessResult() {
        return DataScopeResult.builder()
                .allDataAccess(false)
                .accessibleAreaIds(Set.of())
                .accessibleDeptIds(Set.of())
                .accessibleUserIds(Set.of())
                .customRules(Map.of())
                .build();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(AuthorizationContext context) {
        return String.format("%d_%s_%s_%s_%s_%s_%s",
                context.getUserId(),
                context.getEffectiveDataScope().name(),
                context.getAreaIds(),
                context.getDeptIds(),
                context.getUserIds(),
                context.isSuperAdmin(),
                context.getCustomRules());
    }

    /**
     * 获取缓存结果
     */
    private DataScopeResult getCachedResult(String cacheKey) {
        // 简化实现，实际项目中需要检查缓存过期时间
        DataScopeResult result = cache.get(cacheKey);
        if (result != null) {
            result.setCacheHit(true);
        }
        return result;
    }
}
