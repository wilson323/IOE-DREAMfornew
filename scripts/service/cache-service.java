package {{package}};

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.BusinessDataType;
import net.lab1024.sa.base.common.cache.CacheModule;
import net.lab1024.sa.base.common.cache.UnifiedCacheService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.code.SystemErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * {{EntityName}}缓存服务类
 * <p>
 * 统一缓存访问接口，支持多种缓存策略和模式
 * 严格遵循repowiki缓存架构规范
 * </p>
 *
 * @author {{author}}
 * @since {{date}}
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class {{EntityName}}CacheService {

    @Resource
    private UnifiedCacheService unifiedCacheService;

    @Resource
    private {{EntityName}}Service {{entityName}}Service;

    // ==================== 缓存常量定义 ====================

    /** 缓存模块 */
    private static final CacheModule CACHE_MODULE = CacheModule.{{CACHE_MODULE}};

    /** 缓存命名空间 */
    private static final String NAMESPACE = "{{entityName}}";

    /** 缓存键前缀 */
    private static final String CACHE_KEY_PREFIX = "{{entityName}}:";

    // ==================== 基础缓存操作 ====================

    /**
     * 获取{{EntityName}}详情（从缓存）
     *
     * @param id 主键ID
     * @return {{EntityName}}VO对象
     */
    public {{EntityName}}VO getDetail(Long id) {
        try {
            if (id == null || id <= 0) {
                log.warn("获取{{EntityName}}详情失败：ID不能为空");
                return null;
            }

            String cacheKey = buildCacheKey("detail", String.valueOf(id));
            {{EntityName}}VO cachedResult = unifiedCacheService.get(
                CACHE_MODULE, NAMESPACE, cacheKey, {{EntityName}}VO.class
            );

            if (cachedResult != null) {
                log.debug("从缓存获取{{EntityName}}详情成功，ID: {}", id);
                return cachedResult;
            }

            // 缓存未命中，从数据库获取
            ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(id);
            if (response != null && response.getOk() && response.getData() != null) {
                // 将结果放入缓存
                unifiedCacheService.set(
                    CACHE_MODULE, NAMESPACE, cacheKey,
                    response.getData(), BusinessDataType.USER_SESSION
                );
                log.debug("{{EntityName}}详情已缓存，ID: {}", id);
                return response.getData();
            }

            return null;

        } catch (Exception e) {
            log.error("获取{{EntityName}}详情缓存失败，ID: {}", id, e);
            // 缓存异常时直接从数据库获取
            try {
                ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(id);
                return response != null && response.getOk() ? response.getData() : null;
            } catch (Exception dbException) {
                log.error("从数据库获取{{EntityName}}详情也失败，ID: {}", id, dbException);
                return null;
            }
        }
    }

    /**
     * 获取{{EntityName}}详情（Cache-Aside模式）
     *
     * @param id 主键ID
     * @return {{EntityName}}VO对象
     */
    public {{EntityName}}VO getDetailWithCacheAside(Long id) {
        try {
            if (id == null || id <= 0) {
                return null;
            }

            String cacheKey = buildCacheKey("detail", String.valueOf(id));

            return unifiedCacheService.getOrSet(
                CACHE_MODULE, NAMESPACE, cacheKey, {{EntityName}}VO.class,
                () -> {
                    ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(id);
                    return response != null && response.getOk() ? response.getData() : null;
                },
                BusinessDataType.USER_SESSION
            );

        } catch (Exception e) {
            log.error("Cache-Aside模式获取{{EntityName}}详情失败，ID: {}", id, e);
            return null;
        }
    }

    /**
     * 设置{{EntityName}}详情缓存
     *
     * @param id 主键ID
     * @param vo {{EntityName}}VO对象
     */
    public void setDetail(Long id, {{EntityName}}VO vo) {
        try {
            if (id == null || id <= 0 || vo == null) {
                log.warn("设置{{EntityName}}缓存失败：参数无效");
                return;
            }

            String cacheKey = buildCacheKey("detail", String.valueOf(id));
            unifiedCacheService.set(
                CACHE_MODULE, NAMESPACE, cacheKey, vo, BusinessDataType.USER_SESSION
            );

            log.debug("设置{{EntityName}}缓存成功，ID: {}", id);

        } catch (Exception e) {
            log.error("设置{{EntityName}}缓存失败，ID: {}", id, e);
        }
    }

    /**
     * 删除{{EntityName}}详情缓存
     *
     * @param id 主键ID
     */
    public void deleteDetail(Long id) {
        try {
            if (id == null || id <= 0) {
                return;
            }

            String cacheKey = buildCacheKey("detail", String.valueOf(id));
            unifiedCacheService.delete(CACHE_MODULE, NAMESPACE, cacheKey);

            log.debug("删除{{EntityName}}缓存成功，ID: {}", id);

        } catch (Exception e) {
            log.error("删除{{EntityName}}缓存失败，ID: {}", id, e);
        }
    }

    // ==================== 批量缓存操作 ====================

    /**
     * 批量获取{{EntityName}}详情
     *
     * @param ids ID列表
     * @return {{EntityName}}VO映射
     */
    public Map<Long, {{EntityName}}VO> batchGetDetail(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, {{EntityName}}VO> result = new HashMap<>();
        List<String> cacheKeys = new ArrayList<>();

        // 构建缓存键列表
        for (Long id : ids) {
            if (id != null && id > 0) {
                cacheKeys.add(buildCacheKey("detail", String.valueOf(id)));
            }
        }

        try {
            // 批量获取缓存
            Map<String, {{EntityName}}VO> cachedResults = unifiedCacheService.getBatch(
                CACHE_MODULE, NAMESPACE, cacheKeys, {{EntityName}}VO.class
            );

            // 解析缓存结果
            for (String cacheKey : cacheKeys) {
                {{EntityName}}VO vo = cachedResults.get(cacheKey);
                if (vo != null) {
                    Long id = extractIdFromCacheKey(cacheKey);
                    if (id != null) {
                        result.put(id, vo);
                    }
                }
            }

            // 找出缓存未命中的ID
            List<Long> missedIds = ids.stream()
                .filter(id -> id != null && id > 0 && !result.containsKey(id))
                .collect(Collectors.toList());

            if (!missedIds.isEmpty()) {
                log.debug("缓存未命中{}个{{EntityName}}，从数据库获取", missedIds.size());
                // 从数据库获取未命中的数据
                for (Long id : missedIds) {
                    try {
                        ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(id);
                        if (response != null && response.getOk() && response.getData() != null) {
                            result.put(id, response.getData());
                            // 异步更新缓存
                            CompletableFuture.runAsync(() -> {
                                try {
                                    setDetail(id, response.getData());
                                } catch (Exception e) {
                                    log.warn("异步缓存{{EntityName}}详情失败，ID: {}", id, e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.error("批量获取{{EntityName}}详情时单个失败，ID: {}", id, e);
                    }
                }
            }

            log.debug("批量获取{{EntityName}}详情完成，总计: {}，缓存命中: {}", ids.size(), result.size());

        } catch (Exception e) {
            log.error("批量获取{{EntityName}}详情缓存失败", e);
            // 缓存异常时直接从数据库获取
            result.clear();
            for (Long id : ids) {
                try {
                    {{EntityName}}VO vo = getDetail(id);
                    if (vo != null) {
                        result.put(id, vo);
                    }
                } catch (Exception e2) {
                    log.error("从数据库获取{{EntityName}}详情失败，ID: {}", id, e2);
                }
            }
        }

        return result;
    }

    /**
     * 批量设置{{EntityName}}详情缓存
     *
     * @param dataMap 数据映射
     */
    public void batchSetDetail(Map<Long, {{EntityName}}VO> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }

        try {
            Map<String, {{EntityName}}VO> cacheDataMap = new HashMap<>();

            for (Map.Entry<Long, {{EntityName}}VO> entry : dataMap.entrySet()) {
                Long id = entry.getKey();
                {{EntityName}}VO vo = entry.getValue();

                if (id != null && id > 0 && vo != null) {
                    String cacheKey = buildCacheKey("detail", String.valueOf(id));
                    cacheDataMap.put(cacheKey, vo);
                }
            }

            if (!cacheDataMap.isEmpty()) {
                unifiedCacheService.setBatch(
                    CACHE_MODULE, NAMESPACE, cacheDataMap, BusinessDataType.USER_SESSION
                );
                log.debug("批量设置{{EntityName}}缓存成功，数量: {}", cacheDataMap.size());
            }

        } catch (Exception e) {
            log.error("批量设置{{EntityName}}缓存失败", e);
        }
    }

    /**
     * 批量删除{{EntityName}}详情缓存
     *
     * @param ids ID列表
     */
    public void batchDeleteDetail(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        try {
            for (Long id : ids) {
                if (id != null && id > 0) {
                    deleteDetail(id);
                }
            }

            log.debug("批量删除{{EntityName}}缓存成功，数量: {}", ids.size());

        } catch (Exception e) {
            log.error("批量删除{{EntityName}}缓存失败", e);
        }
    }

    // ==================== 高级缓存操作 ====================

    /**
     * 获取或计算缓存数据
     *
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param dataType 数据类型
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getOrCompute(String key, Supplier<T> dataLoader, BusinessDataType dataType) {
        try {
            String cacheKey = buildCacheKey(key);

            return unifiedCacheService.getOrSet(
                CACHE_MODULE, NAMESPACE, cacheKey,
                (Class<T>) Object.class, // 这里需要根据实际情况调整
                dataLoader,
                dataType
            );

        } catch (Exception e) {
            log.error("获取或计算缓存失败，key: {}", key, e);
            return dataLoader.get();
        }
    }

    /**
     * 缓存存在性检查
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            String cacheKey = buildCacheKey(key);
            return unifiedCacheService.exists(CACHE_MODULE, NAMESPACE, cacheKey);

        } catch (Exception e) {
            log.error("检查缓存存在性失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 清除命名空间下的所有缓存
     */
    public void clearAll() {
        try {
            unifiedCacheService.clearNamespace(CACHE_MODULE, NAMESPACE);
            log.info("清除{{EntityName}}所有缓存成功");

        } catch (Exception e) {
            log.error("清除{{EntityName}}所有缓存失败", e);
        }
    }

    /**
     * 按模式清除缓存
     *
     * @param pattern 匹配模式
     * @return 清除的数量
     */
    public int deleteByPattern(String pattern) {
        try {
            return unifiedCacheService.deleteByPattern(CACHE_MODULE, NAMESPACE, pattern);

        } catch (Exception e) {
            log.error("按模式清除缓存失败，pattern: {}", pattern, e);
            return 0;
        }
    }

    // ==================== 异步缓存操作 ====================

    /**
     * 异步获取{{EntityName}}详情
     *
     * @param id 主键ID
     * @return CompletableFuture
     */
    public CompletableFuture<{{EntityName}}VO> getDetailAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> getDetail(id));
    }

    /**
     * 异步设置{{EntityName}}详情缓存
     *
     * @param id 主键ID
     * @param vo {{EntityName}}VO对象
     * @return CompletableFuture
     */
    public CompletableFuture<Void> setDetailAsync(Long id, {{EntityName}}VO vo) {
        return CompletableFuture.runAsync(() -> setDetail(id, vo));
    }

    /**
     * 异步删除{{EntityName}}详情缓存
     *
     * @param id 主键ID
     * @return CompletableFuture
     */
    public CompletableFuture<Void> deleteDetailAsync(Long id) {
        return CompletableFuture.runAsync(() -> deleteDetail(id));
    }

    /**
     * 异步批量获取{{EntityName}}详情
     *
     * @param ids ID列表
     * @return CompletableFuture
     */
    public CompletableFuture<Map<Long, {{EntityName}}VO>> batchGetDetailAsync(List<Long> ids) {
        return CompletableFuture.supplyAsync(() -> batchGetDetail(ids));
    }

    // ==================== 缓存统计和监控 ====================

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        try {
            return unifiedCacheService.getCacheStatistics(CACHE_MODULE);

        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取缓存健康度评估
     *
     * @return 健康度信息
     */
    public Map<String, Object> getHealthAssessment() {
        try {
            Map<String, Object> health = unifiedCacheService.getHealthAssessment();
            health.put("module", "{{EntityName}}");
            health.put("namespace", NAMESPACE);
            return health;

        } catch (Exception e) {
            log.error("获取缓存健康度评估失败", e);
            Map<String, Object> health = new HashMap<>();
            health.put("healthy", false);
            health.put("module", "{{EntityName}}");
            health.put("error", e.getMessage());
            return health;
        }
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        try {
            unifiedCacheService.cleanExpiredCache();
            log.debug("清理过期{{EntityName}}缓存完成");

        } catch (Exception e) {
            log.error("清理过期{{EntityName}}缓存失败", e);
        }
    }

    // ==================== 缓存预热 ====================

    /**
     * 预热缓存
     *
     * @param ids 需要预热的ID列表
     */
    public void warmupCache(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        try {
            log.info("开始预热{{EntityName}}缓存，数量: {}", ids.size());

            Map<Long, {{EntityName}}VO> cacheData = batchGetDetail(ids);
            log.info("{{EntityName}}缓存预热完成，成功: {}, 失败: {}",
                    cacheData.size(), ids.size() - cacheData.size());

        } catch (Exception e) {
            log.error("{{EntityName}}缓存预热失败", e);
        }
    }

    /**
     * 异步预热缓存
     *
     * @param ids 需要预热的ID列表
     * @return CompletableFuture
     */
    public CompletableFuture<Void> warmupCacheAsync(List<Long> ids) {
        return CompletableFuture.runAsync(() -> warmupCache(ids));
    }

    // ==================== 私有工具方法 ====================

    /**
     * 构建缓存键
     *
     * @param parts 键的组成部分
     * @return 完整缓存键
     */
    private String buildCacheKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return CACHE_KEY_PREFIX;
        }

        StringBuilder keyBuilder = new StringBuilder(CACHE_KEY_PREFIX);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                keyBuilder.append(":");
            }
            keyBuilder.append(parts[i]);
        }

        return keyBuilder.toString();
    }

    /**
     * 从缓存键中提取ID
     *
     * @param cacheKey 缓存键
     * @return ID值
     */
    private Long extractIdFromCacheKey(String cacheKey) {
        try {
            if (cacheKey == null || cacheKey.isEmpty()) {
                return null;
            }

            // 解析格式：{{entityName}}:detail:{id}
            String[] parts = cacheKey.split(":");
            if (parts.length >= 3 && "detail".equals(parts[1])) {
                return Long.parseLong(parts[2]);
            }

            return null;

        } catch (Exception e) {
            log.warn("从缓存键提取ID失败，key: {}", cacheKey, e);
            return null;
        }
    }

    /**
     * 验证ID有效性
     *
     * @param id ID值
     * @return 是否有效
     */
    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 记录缓存操作日志
     *
     * @param operation 操作类型
     * @param key 缓存键
     * @param success 是否成功
     * @param duration 耗时（毫秒）
     */
    private void logCacheOperation(String operation, String key, boolean success, long duration) {
        if (log.isDebugEnabled()) {
            log.debug("{{EntityName}}缓存操作: {}, key: {}, success: {}, duration: {}ms",
                    operation, key, success, duration);
        }
    }

    // ==================== 缓存策略方法 ====================

    /**
     * 获取带过期时间的缓存数据
     *
     * @param key 缓存键
     * @param ttlSeconds 过期时间（秒）
     * @param dataLoader 数据加载器
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getWithTtl(String key, long ttlSeconds, Supplier<T> dataLoader) {
        try {
            String cacheKey = buildCacheKey(key);

            T cachedData = unifiedCacheService.get(CACHE_MODULE, NAMESPACE, cacheKey, (Class<T>) Object.class);
            if (cachedData != null) {
                return cachedData;
            }

            T data = dataLoader.get();
            if (data != null) {
                unifiedCacheService.set(CACHE_MODULE, NAMESPACE, cacheKey, data, ttlSeconds);
            }

            return data;

        } catch (Exception e) {
            log.error("获取带过期时间的缓存失败，key: {}", key, e);
            return dataLoader.get();
        }
    }

    /**
     * 刷新缓存（更新数据并重新缓存）
     *
     * @param id 主键ID
     * @return 刷新后的数据
     */
    public {{EntityName}}VO refreshCache(Long id) {
        try {
            // 先删除旧缓存
            deleteDetail(id);

            // 重新获取数据（会自动缓存）
            {{EntityName}}VO vo = getDetail(id);

            log.debug("刷新{{EntityName}}缓存成功，ID: {}", id);
            return vo;

        } catch (Exception e) {
            log.error("刷新{{EntityName}}缓存失败，ID: {}", id, e);
            return null;
        }
    }

    /**
     * 批量刷新缓存
     *
     * @param ids ID列表
     * @return 刷新结果统计
     */
    public Map<String, Integer> batchRefreshCache(List<Long> ids) {
        Map<String, Integer> result = new HashMap<>();
        result.put("total", ids != null ? ids.size() : 0);
        result.put("success", 0);
        result.put("failed", 0);

        if (ids == null || ids.isEmpty()) {
            return result;
        }

        for (Long id : ids) {
            try {
                if (refreshCache(id) != null) {
                    result.put("success", result.get("success") + 1);
                } else {
                    result.put("failed", result.get("failed") + 1);
                }
            } catch (Exception e) {
                log.error("批量刷新缓存单个失败，ID: {}", id, e);
                result.put("failed", result.get("failed") + 1);
            }
        }

        return result;
    }
}