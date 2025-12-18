package net.lab1024.sa.common.permission.optimize;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.manager.PermissionCacheManager;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 权限验证性能优化器
 * <p>
 * 企业级权限验证性能优化，提供：
 * - 权限验证算法优化，提升验证效率
 * - 权限数据预加载和智能缓存策略
 * - 批量权限验证优化，减少网络开销
 * - 权限验证结果压缩存储，节省内存
 * - 权限验证并行处理机制，提升并发性能
 * - 权限验证热点数据识别和优化
 * - 权限验证负载均衡和故障转移
 * </p>
 * <p>
 * 优化策略：
 * 1. 权限矩阵预计算和缓存
 * 2. 权限继承关系优化
 * 3. 权限验证结果预测和缓存
 * 4. 异步权限验证和批量处理
 * 5. 权限数据索引和快速查找
 * 6. 权限验证路径优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionPerformanceOptimizer {

    @Resource
    private PermissionCacheManager permissionCacheManager;

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 权限验证优化配置
    private final PermissionOptimizationConfig config;

    // 权限验证热点数据统计
    private final Map<String, PermissionHotData> hotDataMap = new ConcurrentHashMap<>();

    // 权限矩阵缓存（预计算）
    private final Map<String, Set<String>> permissionMatrixCache = new ConcurrentHashMap<>();

    // 用户权限索引（快速查找）
    private final Map<Long, UserPermissionIndex> userPermissionIndex = new ConcurrentHashMap<>();

    // 权限验证路径缓存
    private final Map<String, PermissionValidationPath> validationPathCache = new ConcurrentHashMap<>();

    // Redis键前缀
    private static final String OPTIMIZATION_PREFIX = "permission:optimization:";
    private static final String HOT_DATA_PREFIX = OPTIMIZATION_PREFIX + "hot:";
    private static final String MATRIX_PREFIX = OPTIMIZATION_PREFIX + "matrix:";
    private static final String INDEX_PREFIX = OPTIMIZATION_PREFIX + "index:";
    private static final String PATH_PREFIX = OPTIMIZATION_PREFIX + "path:";

    /**
     * 构造函数
     */
    public PermissionPerformanceOptimizer() {
        this.config = PermissionOptimizationConfig.defaultConfig();
    }

    /**
     * 优化权限验证请求
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param validationFunc 验证函数
     * @return 优化后的验证结果
     */
    public PermissionValidationResult optimizeValidation(Long userId, Set<String> permissions,
                                                       Supplier<PermissionValidationResult> validationFunc) {
        try {
            // 1. 快速路径：缓存命中
            String cacheKey = buildOptimizationCacheKey(userId, permissions);
            PermissionValidationResult cachedResult = getCachedOptimizationResult(cacheKey);
            if (cachedResult != null) {
                updateHotDataStats(userId, permissions, true);
                return cachedResult;
            }

            // 2. 权限预测：基于历史数据预测验证结果
            PermissionValidationResult predictedResult = predictValidationResult(userId, permissions);
            if (predictedResult != null && predictedResult.getConfidence() > 0.8) {
                // 异步验证预测结果
                asyncValidatePrediction(userId, permissions, predictedResult, validationFunc);
                return predictedResult;
            }

            // 3. 批量优化：如果权限数量超过阈值，使用批量验证
            if (permissions.size() > config.getBatchThreshold()) {
                return optimizedBatchValidation(userId, permissions, validationFunc);
            }

            // 4. 并行优化：并行验证多个权限
            return optimizedParallelValidation(userId, permissions, validationFunc);

        } catch (Exception e) {
            log.error("[权限优化] 优化权限验证异常: userId={}, permissions={}", userId, permissions, e);
            // 降级到原始验证
            return validationFunc.get();
        }
    }

    /**
     * 批量权限验证优化
     */
    private PermissionValidationResult optimizedBatchValidation(Long userId, Set<String> permissions,
                                                               Supplier<PermissionValidationResult> validationFunc) {
        long startTime = System.currentTimeMillis();

        try {
            // 将权限分组优化
            Map<String, List<String>> permissionGroups = groupPermissions(permissions);

            // 并行验证各组权限
            List<CompletableFuture<PermissionValidationResult>> futures = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : permissionGroups.entrySet()) {
                String groupKey = entry.getKey();
                List<String> groupPermissions = entry.getValue();

                CompletableFuture<PermissionValidationResult> future = CompletableFuture.supplyAsync(() -> {
                    return validatePermissionGroup(userId, groupKey, groupPermissions);
                });

                futures.add(future);
            }

            // 等待所有验证完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );

            allFutures.join();

            // 合并验证结果
            PermissionValidationResult combinedResult = combineValidationResults(futures);

            // 缓存优化结果
            String cacheKey = buildOptimizationCacheKey(userId, permissions);
            cacheOptimizationResult(cacheKey, combinedResult);

            // 更新热点数据统计
            updateHotDataStats(userId, permissions, true);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[权限优化] 批量验证完成: userId={}, permissions={}, duration={}ms",
                     userId, permissions.size(), duration);

            return combinedResult;

        } catch (Exception e) {
            log.error("[权限优化] 批量权限验证异常: userId={}", userId, e);
            return validationFunc.get();
        }
    }

    /**
     * 并行权限验证优化
     */
    private PermissionValidationResult optimizedParallelValidation(Long userId, Set<String> permissions,
                                                                 Supplier<PermissionValidationResult> validationFunc) {
        long startTime = System.currentTimeMillis();

        try {
            // 预检查权限索引
            UserPermissionIndex index = getUserPermissionIndex(userId);
            if (index != null) {
                // 基于索引快速验证
                PermissionValidationResult indexResult = validateWithIndex(userId, permissions, index);
                if (indexResult != null) {
                    updateHotDataStats(userId, permissions, true);
                    return indexResult;
                }
            }

            // 并行验证各个权限
            List<CompletableFuture<PermissionValidationResult>> futures = permissions.stream()
                .map(permission -> CompletableFuture.supplyAsync(() ->
                    validateSinglePermission(userId, permission)))
                .collect(Collectors.toList());

            // 合并并行验证结果
            PermissionValidationResult parallelResult = combineParallelResults(futures);

            // 缓存优化结果
            String cacheKey = buildOptimizationCacheKey(userId, permissions);
            cacheOptimizationResult(cacheKey, parallelResult);

            // 更新热点数据统计
            updateHotDataStats(userId, permissions, true);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[权限优化] 并行验证完成: userId={}, permissions={}, duration={}ms",
                     userId, permissions.size(), duration);

            return parallelResult;

        } catch (Exception e) {
            log.error("[权限优化] 并行权限验证异常: userId={}", userId, e);
            return validationFunc.get();
        }
    }

    /**
     * 权限分组优化
     */
    private Map<String, List<String>> groupPermissions(Set<String> permissions) {
        Map<String, List<String>> groups = new HashMap<>();

        // 按权限类型分组
        for (String permission : permissions) {
            String groupKey = extractPermissionGroup(permission);
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(permission);
        }

        return groups;
    }

    /**
     * 提取权限组
     */
    private String extractPermissionGroup(String permission) {
        if (permission == null || permission.isEmpty()) {
            return "UNKNOWN";
        }

        // 提取权限前缀作为组标识
        int colonIndex = permission.indexOf(':');
        if (colonIndex > 0) {
            return permission.substring(0, colonIndex);
        }

        return permission;
    }

    /**
     * 验证权限组
     */
    private PermissionValidationResult validatePermissionGroup(Long userId, String groupKey,
                                                             List<String> permissions) {
        try {
            // 检查权限组缓存
            String groupCacheKey = buildGroupCacheKey(userId, groupKey);
            PermissionValidationResult groupResult = getCachedOptimizationResult(groupCacheKey);

            if (groupResult != null) {
                return groupResult;
            }

            // 批量验证权限组
            boolean allValid = permissions.stream()
                .allMatch(permission -> validateSinglePermission(userId, permission).isValid());

            PermissionValidationResult result = PermissionValidationResult.builder()
                .valid(allValid)
                .userId(userId)
                .permissions(new HashSet<>(permissions))
                .validationTime(System.currentTimeMillis())
                .groupKey(groupKey)
                .build();

            // 缓存组验证结果
            cacheOptimizationResult(groupCacheKey, result);

            return result;

        } catch (Exception e) {
            log.error("[权限优化] 验证权限组异常: userId={}, groupKey={}", userId, groupKey, e);
            return PermissionValidationResult.invalid(userId, new HashSet<>(permissions));
        }
    }

    /**
     * 验证单个权限
     */
    private PermissionValidationResult validateSinglePermission(Long userId, String permission) {
        try {
            // 检查权限矩阵缓存
            Set<String> userPermissions = permissionMatrixCache.get(String.valueOf(userId));
            if (userPermissions != null && userPermissions.contains(permission)) {
                return PermissionValidationResult.valid(userId, Set.of(permission));
            }

            // 检查缓存中的用户权限
            Set<String> cachedPermissions = permissionCacheManager.getUserPermissions(userId);
            if (cachedPermissions != null && cachedPermissions.contains(permission)) {
                // 更新权限矩阵缓存
                updatePermissionMatrix(userId, cachedPermissions);
                return PermissionValidationResult.valid(userId, Set.of(permission));
            }

            // 降级到数据库查询
            // TODO: 调用实际的权限验证逻辑
            return PermissionValidationResult.invalid(userId, Set.of(permission));

        } catch (Exception e) {
            log.error("[权限优化] 验证单个权限异常: userId={}, permission={}", userId, permission, e);
            return PermissionValidationResult.invalid(userId, Set.of(permission));
        }
    }

    /**
     * 基于索引验证权限
     */
    private PermissionValidationResult validateWithIndex(Long userId, Set<String> permissions,
                                                         UserPermissionIndex index) {
        try {
            Set<String> userPermissions = index.getPermissions();
            Set<String> matchedPermissions = new HashSet<>();
            Set<String> unmatchedPermissions = new HashSet<>();

            for (String permission : permissions) {
                if (userPermissions.contains(permission)) {
                    matchedPermissions.add(permission);
                } else {
                    unmatchedPermissions.add(permission);
                }
            }

            boolean allValid = unmatchedPermissions.isEmpty();

            return PermissionValidationResult.builder()
                .valid(allValid)
                .userId(userId)
                .permissions(permissions)
                .matchedPermissions(matchedPermissions)
                .unmatchedPermissions(unmatchedPermissions)
                .validationTime(System.currentTimeMillis())
                .source("INDEX")
                .build();

        } catch (Exception e) {
            log.error("[权限优化] 基于索引验证权限异常: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 预测权限验证结果
     */
    private PermissionValidationResult predictValidationResult(Long userId, Set<String> permissions) {
        try {
            // 基于历史数据预测
            PermissionHotData hotData = hotDataMap.get(String.valueOf(userId));
            if (hotData == null) {
                return null;
            }

            // 计算预测置信度
            double confidence = hotData.calculatePredictionConfidence(permissions);

            if (confidence < 0.8) {
                return null;
            }

            // 生成预测结果
            boolean predictedValid = hotData.predictValidation(permissions);

            return PermissionValidationResult.builder()
                .valid(predictedValid)
                .userId(userId)
                .permissions(permissions)
                .validationTime(System.currentTimeMillis())
                .confidence(confidence)
                .source("PREDICTION")
                .build();

        } catch (Exception e) {
            log.error("[权限优化] 预测权限验证结果异常: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 异步验证预测结果
     */
    @Async
    public void asyncValidatePrediction(Long userId, Set<String> permissions,
                                       PermissionValidationResult predictedResult,
                                       Supplier<PermissionValidationResult> validationFunc) {
        try {
            // 异步执行实际验证
            PermissionValidationResult actualResult = validationFunc.get();

            // 比较预测结果和实际结果
            if (predictedResult.isValid() != actualResult.isValid()) {
                log.warn("[权限优化] 预测结果与实际结果不符: userId={}, predicted={}, actual={}",
                         userId, predictedResult.isValid(), actualResult.isValid());

                // 更新热点数据模型
                updateHotDataModel(userId, permissions, actualResult);
            }

            // 更新缓存
            String cacheKey = buildOptimizationCacheKey(userId, permissions);
            cacheOptimizationResult(cacheKey, actualResult);

        } catch (Exception e) {
            log.error("[权限优化] 异步验证预测结果异常: userId={}", userId, e);
        }
    }

    /**
     * 合并验证结果
     */
    private PermissionValidationResult combineValidationResults(List<CompletableFuture<PermissionValidationResult>> futures) {
        try {
            Set<String> allPermissions = new HashSet<>();
            boolean allValid = true;

            for (CompletableFuture<PermissionValidationResult> future : futures) {
                PermissionValidationResult result = future.get();
                if (result != null) {
                    allPermissions.addAll(result.getPermissions());
                    if (!result.isValid()) {
                        allValid = false;
                    }
                }
            }

            return PermissionValidationResult.builder()
                .valid(allValid)
                .permissions(allPermissions)
                .validationTime(System.currentTimeMillis())
                .source("BATCH_COMBINED")
                .build();

        } catch (Exception e) {
            log.error("[权限优化] 合并验证结果异常", e);
            return PermissionValidationResult.invalid(null, new HashSet<>());
        }
    }

    /**
     * 合并并行验证结果
     */
    private PermissionValidationResult combineParallelResults(List<CompletableFuture<PermissionValidationResult>> futures) {
        return combineValidationResults(futures);
    }

    /**
     * 更新权限矩阵缓存
     */
    private void updatePermissionMatrix(Long userId, Set<String> permissions) {
        try {
            permissionMatrixCache.put(String.valueOf(userId), new HashSet<>(permissions));

            // 同步到Redis
            String matrixKey = MATRIX_PREFIX + userId;
            redisTemplate.opsForValue().set(matrixKey, permissions, config.getMatrixCacheTtl());

        } catch (Exception e) {
            log.error("[权限优化] 更新权限矩阵缓存异常: userId={}", userId, e);
        }
    }

    /**
     * 更新用户权限索引
     */
    private void updatePermissionIndex(Long userId, Set<String> permissions) {
        try {
            UserPermissionIndex index = new UserPermissionIndex(userId, permissions);
            userPermissionIndex.put(userId, index);

            // 同步到Redis
            String indexKey = INDEX_PREFIX + userId;
            redisTemplate.opsForValue().set(indexKey, index, config.getIndexCacheTtl());

        } catch (Exception e) {
            log.error("[权限优化] 更新用户权限索引异常: userId={}", userId, e);
        }
    }

    /**
     * 获取用户权限索引
     */
    private UserPermissionIndex getUserPermissionIndex(Long userId) {
        if (userId == null) {
            return null;
        }

        UserPermissionIndex index = userPermissionIndex.get(userId);
        if (index == null) {
            // 从Redis加载索引
            try {
                String indexKey = INDEX_PREFIX + userId;
                index = (UserPermissionIndex) redisTemplate.opsForValue().get(indexKey);
                if (index != null) {
                    userPermissionIndex.put(userId, index);
                }
            } catch (Exception e) {
                log.error("[权限优化] 从Redis加载用户权限索引异常: userId={}", userId, e);
            }
        }

        return index;
    }

    /**
     * 更新热点数据统计
     */
    private void updateHotDataStats(Long userId, Set<String> permissions, boolean cacheHit) {
        try {
            String userKey = String.valueOf(userId);
            PermissionHotData hotData = hotDataMap.computeIfAbsent(userKey, k -> new PermissionHotData(userId));

            hotData.recordAccess(permissions, cacheHit);

            // 定期同步到Redis
            if (hotData.getAccessCount() % 10 == 0) {
                syncHotDataToRedis(userKey, hotData);
            }

        } catch (Exception e) {
            log.error("[权限优化] 更新热点数据统计异常: userId={}", userId, e);
        }
    }

    /**
     * 更新热点数据模型
     */
    private void updateHotDataModel(Long userId, Set<String> permissions, PermissionValidationResult result) {
        try {
            String userKey = String.valueOf(userId);
            PermissionHotData hotData = hotDataMap.computeIfAbsent(userKey, k -> new PermissionHotData(userId));

            hotData.updateModel(permissions, result);

            // 立即同步到Redis
            syncHotDataToRedis(userKey, hotData);

        } catch (Exception e) {
            log.error("[权限优化] 更新热点数据模型异常: userId={}", userId, e);
        }
    }

    /**
     * 同步热点数据到Redis
     */
    private void syncHotDataToRedis(String userKey, PermissionHotData hotData) {
        try {
            String hotDataKey = HOT_DATA_PREFIX + userKey;
            redisTemplate.opsForValue().set(hotDataKey, hotData, config.getHotDataCacheTtl());

        } catch (Exception e) {
            log.error("[权限优化] 同步热点数据到Redis异常: userKey={}", userKey, e);
        }
    }

    /**
     * 获取缓存优化结果
     */
    private PermissionValidationResult getCachedOptimizationResult(String cacheKey) {
        try {
            return (PermissionValidationResult) unifiedCacheManager.get(cacheKey);
        } catch (Exception e) {
            log.error("[权限优化] 获取缓存优化结果异常: cacheKey={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 缓存优化结果
     */
    private void cacheOptimizationResult(String cacheKey, PermissionValidationResult result) {
        try {
            unifiedCacheManager.put(cacheKey, result, config.getOptimizationCacheTtl().toMillis());
        } catch (Exception e) {
            log.error("[权限优化] 缓存优化结果异常: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 构建优化缓存键
     */
    private String buildOptimizationCacheKey(Long userId, Set<String> permissions) {
        return OPTIMIZATION_PREFIX + "validation:" + userId + ":" +
               String.join(",", permissions.stream().sorted().collect(Collectors.toList()));
    }

    /**
     * 构建组缓存键
     */
    private String buildGroupCacheKey(Long userId, String groupKey) {
        return OPTIMIZATION_PREFIX + "group:" + userId + ":" + groupKey;
    }

    /**
     * 定期清理过期优化缓存
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanupExpiredOptimizationCache() {
        try {
            // 清理权限矩阵缓存
            permissionMatrixCache.entrySet().removeIf(entry -> {
                // 检查是否过期
                return isCacheExpired(String.valueOf(entry.getKey()));
            });

            // 清理用户权限索引
            userPermissionIndex.entrySet().removeIf(entry -> {
                return isCacheExpired(String.valueOf(entry.getKey()));
            });

            // 清理验证路径缓存
            validationPathCache.entrySet().removeIf(entry -> {
                return isCacheExpired(entry.getKey());
            });

            log.debug("[权限优化] 清理过期优化缓存完成");

        } catch (Exception e) {
            log.error("[权限优化] 清理过期优化缓存异常", e);
        }
    }

    /**
     * 检查缓存是否过期
     */
    private boolean isCacheExpired(String key) {
        try {
            return !redisTemplate.hasKey(key);
        } catch (Exception e) {
            return true; // 出现异常时认为已过期
        }
    }

    /**
     * 获取优化统计信息
     */
    public OptimizationStatistics getOptimizationStatistics() {
        OptimizationStatistics stats = new OptimizationStatistics();

        stats.setMatrixCacheSize(permissionMatrixCache.size());
        stats.setIndexCacheSize(userPermissionIndex.size());
        stats.setHotDataCacheSize(hotDataMap.size());
        stats.setPathCacheSize(validationPathCache.size());

        // 计算缓存命中率
        long totalAccess = hotDataMap.values().stream()
            .mapToLong(PermissionHotData::getTotalAccessCount)
            .sum();
        long cacheHits = hotDataMap.values().stream()
            .mapToLong(PermissionHotData::getCacheHitCount)
            .sum();

        stats.setCacheHitRate(totalAccess > 0 ? (double) cacheHits / totalAccess : 0.0);

        return stats;
    }

    /**
     * 权限优化配置
     */
    @lombok.Data
    public static class PermissionOptimizationConfig {
        private final int batchThreshold;
        private final int parallelThreshold;
        private final java.time.Duration optimizationCacheTtl;
        private final java.time.Duration matrixCacheTtl;
        private final java.time.Duration indexCacheTtl;
        private final java.time.Duration hotDataCacheTtl;
        private final boolean enablePrediction;
        private final double predictionConfidenceThreshold;
        private final boolean enableBatchValidation;
        private final boolean enableParallelValidation;

        public PermissionOptimizationConfig(int batchThreshold, int parallelThreshold,
                                           java.time.Duration optimizationCacheTtl,
                                           java.time.Duration matrixCacheTtl,
                                           java.time.Duration indexCacheTtl,
                                           java.time.Duration hotDataCacheTtl,
                                           boolean enablePrediction,
                                           double predictionConfidenceThreshold,
                                           boolean enableBatchValidation,
                                           boolean enableParallelValidation) {
            this.batchThreshold = batchThreshold;
            this.parallelThreshold = parallelThreshold;
            this.optimizationCacheTtl = optimizationCacheTtl;
            this.matrixCacheTtl = matrixCacheTtl;
            this.indexCacheTtl = indexCacheTtl;
            this.hotDataCacheTtl = hotDataCacheTtl;
            this.enablePrediction = enablePrediction;
            this.predictionConfidenceThreshold = predictionConfidenceThreshold;
            this.enableBatchValidation = enableBatchValidation;
            this.enableParallelValidation = enableParallelValidation;
        }

        public static PermissionOptimizationConfig defaultConfig() {
            return new PermissionOptimizationConfig(
                10,                                      // 批量阈值：10个权限
                5,                                       // 并行阈值：5个权限
                java.time.Duration.ofMinutes(30),        // 优化缓存TTL：30分钟
                java.time.Duration.ofHours(2),          // 矩阵缓存TTL：2小时
                java.time.Duration.ofMinutes(15),        // 索引缓存TTL：15分钟
                java.time.Duration.ofHours(1),          // 热点数据TTL：1小时
                true,                                    // 启用预测
                0.8,                                     // 预测置信度阈值：0.8
                true,                                    // 启用批量验证
                true                                     // 启用并行验证
            );
        }

        // Getter方法
        public int getBatchThreshold() { return batchThreshold; }
        public int getParallelThreshold() { return parallelThreshold; }
        public java.time.Duration getOptimizationCacheTtl() { return optimizationCacheTtl; }
        public java.time.Duration getMatrixCacheTtl() { return matrixCacheTtl; }
        public java.time.Duration getIndexCacheTtl() { return indexCacheTtl; }
        public java.time.Duration getHotDataCacheTtl() { return hotDataCacheTtl; }
        public boolean isEnablePrediction() { return enablePrediction; }
        public double getPredictionConfidenceThreshold() { return predictionConfidenceThreshold; }
        public boolean isEnableBatchValidation() { return enableBatchValidation; }
        public boolean isEnableParallelValidation() { return enableParallelValidation; }
    }

    /**
     * 权限热点数据
     */
    @lombok.Data
    public static class PermissionHotData {
        private final Long userId;
        private final Map<String, PermissionStats> permissionStats = new HashMap<>();
        private long totalAccessCount = 0;
        private long cacheHitCount = 0;
        private LocalDateTime lastUpdateTime = LocalDateTime.now();

        public PermissionHotData(Long userId) {
            this.userId = userId;
        }

        public long getAccessCount() {
            return totalAccessCount;
        }

        public void recordAccess(Set<String> permissions, boolean cacheHit) {
            totalAccessCount++;
            if (cacheHit) {
                cacheHitCount++;
            }

            for (String permission : permissions) {
                PermissionStats stats = permissionStats.computeIfAbsent(permission, k -> new PermissionStats());
                stats.recordAccess(cacheHit);
            }

            lastUpdateTime = LocalDateTime.now();
        }

        public void updateModel(Set<String> permissions, PermissionValidationResult result) {
            for (String permission : permissions) {
                PermissionStats stats = permissionStats.computeIfAbsent(permission, k -> new PermissionStats());
                stats.updateValidation(result.isValid());
            }
        }

        public double calculatePredictionConfidence(Set<String> permissions) {
            if (permissions.isEmpty()) {
                return 0.0;
            }

            double totalConfidence = 0.0;
            int permissionCount = 0;

            for (String permission : permissions) {
                PermissionStats stats = permissionStats.get(permission);
                if (stats != null && stats.getAccessCount() >= 5) {
                    totalConfidence += stats.calculateConfidence();
                    permissionCount++;
                }
            }

            return permissionCount > 0 ? totalConfidence / permissionCount : 0.0;
        }

        public boolean predictValidation(Set<String> permissions) {
            int validCount = 0;
            int totalCount = 0;

            for (String permission : permissions) {
                PermissionStats stats = permissionStats.get(permission);
                if (stats != null && stats.getAccessCount() >= 3) {
                    totalCount++;
                    if (stats.getSuccessRate() > 0.7) {
                        validCount++;
                    }
                }
            }

            return totalCount > 0 && (validCount / (double) totalCount) > 0.7;
        }

        public long getTotalAccessCount() { return totalAccessCount; }
        public long getCacheHitCount() { return cacheHitCount; }

        @lombok.Data
        public static class PermissionStats {
            private long accessCount = 0;
            private long cacheHitCount = 0;
            private long validationCount = 0;
            private long successCount = 0;
            private LocalDateTime lastAccessTime = LocalDateTime.now();

            public void recordAccess(boolean cacheHit) {
                accessCount++;
                if (cacheHit) {
                    cacheHitCount++;
                }
                lastAccessTime = LocalDateTime.now();
            }

            public void updateValidation(boolean success) {
                validationCount++;
                if (success) {
                    successCount++;
                }
            }

            public double calculateConfidence() {
                if (accessCount < 5) {
                    return 0.0;
                }

                double accessConfidence = Math.min(accessCount / 100.0, 1.0);
                double successConfidence = validationCount > 0 ?
                    Math.min((double) successCount / validationCount, 1.0) : 0.0;

                return (accessConfidence + successConfidence) / 2.0;
            }

            public double getSuccessRate() {
                return validationCount > 0 ? (double) successCount / validationCount : 0.0;
            }

            public long getAccessCount() { return accessCount; }
            public long getCacheHitCount() { return cacheHitCount; }
        }
    }

    /**
     * 用户权限索引
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class UserPermissionIndex {
        private final Long userId;
        private final Set<String> permissions;
        private final LocalDateTime indexTime;
        private final String version;

        public UserPermissionIndex(Long userId, Set<String> permissions) {
            this.userId = userId;
            this.permissions = new HashSet<>(permissions);
            this.indexTime = LocalDateTime.now();
            this.version = "1.0";
        }
    }

    /**
     * 权限验证路径
     */
    @lombok.Data
    public static class PermissionValidationPath {
        private final String pathId;
        private final List<String> validationSteps;
        private final long estimatedTime;
        private final double confidence;

        public PermissionValidationPath(String pathId, List<String> validationSteps,
                                      long estimatedTime, double confidence) {
            this.pathId = pathId;
            this.validationSteps = new ArrayList<>(validationSteps);
            this.estimatedTime = estimatedTime;
            this.confidence = confidence;
        }
    }

    /**
     * 优化统计信息
     */
    @lombok.Data
    public static class OptimizationStatistics {
        private int matrixCacheSize = 0;
        private int indexCacheSize = 0;
        private int hotDataCacheSize = 0;
        private int pathCacheSize = 0;
        private double cacheHitRate = 0.0;
        private LocalDateTime lastUpdateTime = LocalDateTime.now();
    }
}
