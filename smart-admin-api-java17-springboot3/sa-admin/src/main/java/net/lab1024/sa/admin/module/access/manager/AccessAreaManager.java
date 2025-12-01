package net.lab1024.sa.admin.module.access.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.access.service.AccessCacheService;

/**
 * 门禁区域管理器 - 统一缓存架构
 * <p>
 * 基于UnifiedCacheManager和AccessCacheService实现统一缓存架构
 * 遵循缓存架构统一化规范：
 * - 使用ACCESS命名空间
 * - 统一缓存键格式：iog:cache:ACCESS:{namespace}:{key}
 * - 标准TTL配置：默认10分钟
 * - 支持异步操作和批量操作
 * - Caffeine L1缓存 + Redis L2缓存的二级缓存架构
 *
 * 核心职责:
 * - 区域信息缓存管理
 * - 区域树形结构缓存和懒加载
 * - 子区域关系缓存
 * - 区域统计信息缓存
 * - 批量操作性能优化
 *
 * @author SmartAdmin Team
 * @since 2025-11-23
 */
@Slf4j
@Component
public class AccessAreaManager {

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private AccessCacheService accessCacheService;

    // Caffeine L1缓存配置
    private final Cache<String, AccessAreaEntity> areaCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final Cache<String, List<AccessAreaTreeVO>> areaTreeCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final Cache<String, List<Long>> areaChildrenCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final Cache<String, Map<String, Object>> areaStatisticsCache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    // Redis L2缓存Key前缀
    private static final String REDIS_AREA_PREFIX = "access:area:";
    private static final String REDIS_AREA_TREE_PREFIX = "access:area:tree:";
    private static final String REDIS_AREA_CHILDREN_PREFIX = "access:area:children:";
    private static final String REDIS_AREA_STATISTICS_PREFIX = "access:area:statistics:";

    /**
     * 获取区域信息（优先从缓存读取）
     */
    public AccessAreaEntity getAreaFromCache(Long areaId) {
        if (areaId == null) {
            return null;
        }

        try {
            // 先从统一缓存获取
            AccessAreaEntity area = accessCacheService.getArea(areaId);
            if (area != null) {
                log.debug("区域信息统一缓存命中: areaId={}", areaId);
                return area;
            }

            // 缓存未命中，从数据库查询
            area = accessAreaDao.selectById(areaId);
            if (area != null && area.getDeletedFlag() == 0) {
                log.debug("区域信息数据库查找: areaId={}", areaId);
                // 写入统一缓存
                accessCacheService.cacheArea(area);
                return area;
            }

            log.debug("区域信息不存在: areaId={}", areaId);
            return null;

        } catch (Exception e) {
            log.error("获取区域缓存失败: areaId={}", areaId, e);
            // 降级到直接数据库查询
            return accessAreaDao.selectById(areaId);
        }
    }

    /**
     * 获取区域树形结构（支持懒加载和缓存）
     */
    public List<AccessAreaTreeVO> getAreaTreeFromCache(Long parentId, Boolean includeChildren) {
        try {
            // 先从统一缓存获取
            List<AccessAreaTreeVO> treeList = accessCacheService.getAreaTree(parentId, includeChildren);
            if (!treeList.isEmpty()) {
                log.debug("区域树统一缓存命中: parentId={}, includeChildren={}", parentId, includeChildren);
                return treeList;
            }

            log.debug("区域树需要从数据库构建: parentId={}, includeChildren={}", parentId, includeChildren);
            return null; // 告诉Service层需要从数据库构建

        } catch (Exception e) {
            log.error("获取区域树缓存失败: parentId={}, includeChildren={}", parentId, includeChildren, e);
            return null;
        }
    }

    /**
     * 缓存区域树形结构
     */
    public void cacheAreaTree(Long parentId, Boolean includeChildren, List<AccessAreaTreeVO> treeList) {
        try {
            // 写入统一缓存
            accessCacheService.cacheAreaTree(parentId, includeChildren, treeList);

            log.debug("区域树已缓存: parentId={}, includeChildren={}, size={}",
                    parentId, includeChildren, treeList.size());

        } catch (Exception e) {
            log.error("缓存区域树失败: parentId={}, includeChildren={}", parentId, includeChildren, e);
        }
    }

    /**
     * 获取子区域ID列表（从缓存读取）
     */
    public List<Long> getChildrenIdsFromCache(Long parentId) {
        if (parentId == null) {
            return new ArrayList<>();
        }

        try {
            // 先从统一缓存获取
            List<Long> childrenIds = accessCacheService.getChildrenIds(parentId);
            if (!childrenIds.isEmpty()) {
                log.debug("子区域ID列表统一缓存命中: parentId={}", parentId);
                return childrenIds;
            }

            // 缓存未命中，从数据库查询
            List<Long> childrenIdsFromDb = accessAreaDao.selectChildrenIds(parentId);
            if (childrenIdsFromDb != null && !childrenIdsFromDb.isEmpty()) {
                log.debug("子区域ID列表数据库查找: parentId={}, count={}", parentId, childrenIdsFromDb.size());
                // 写入统一缓存
                accessCacheService.cacheChildrenIds(parentId, childrenIdsFromDb);
                return childrenIdsFromDb;
            }

            log.debug("无子区域: parentId={}", parentId);
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("获取子区域ID缓存失败: parentId={}", parentId, e);
            // 降级到数据库查询
            return accessAreaDao.selectChildrenIds(parentId);
        }
    }

    /**
     * 清除区域相关缓存
     */
    public void clearAreaCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        try {
            // 清除统一缓存
            accessCacheService.clearAreaCache(areaId);

            // 清除L1缓存（保持兼容性）
            String cacheKey = REDIS_AREA_PREFIX + areaId;
            areaCache.invalidate(cacheKey);

            log.debug("区域缓存已清除: areaId={}", areaId);

        } catch (Exception e) {
            log.error("清除区域缓存失败: areaId={}", areaId, e);
        }
    }

    /**
     * 清除区域树相关缓存
     */
    public void clearAreaTreeCache() {
        try {
            // 清除统一缓存
            accessCacheService.clearAreaTreeCache();

            // 清除L1缓存（保持兼容性）
            areaTreeCache.invalidateAll();
            areaChildrenCache.invalidateAll();

            log.info("区域树相关缓存已全部清除");

        } catch (Exception e) {
            log.error("清除区域树缓存失败", e);
        }
    }

    /**
     * 批量获取区域信息（优化数据库查询）
     */
    public Map<Long, AccessAreaEntity> batchGetAreas(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // 先从统一缓存批量获取
            Map<Long, AccessAreaEntity> cachedAreas = accessCacheService.batchGetAreas(areaIds);

            // 识别缓存未命中的区域ID
            List<Long> missedIds = areaIds.stream()
                .filter(areaId -> !cachedAreas.containsKey(areaId))
                .collect(Collectors.toList());

            Map<Long, AccessAreaEntity> result = new HashMap<>(cachedAreas);

            // 批量查询数据库中缺失的区域
            if (!missedIds.isEmpty()) {
                List<AccessAreaEntity> areasFromDb = accessAreaDao.selectBatchIds(missedIds);
                for (AccessAreaEntity area : areasFromDb) {
                    if (area != null && area.getDeletedFlag() == 0) {
                        result.put(area.getAreaId(), area);
                    }
                }

                // 批量缓存新查询的区域
                if (!areasFromDb.isEmpty()) {
                    accessCacheService.batchCacheAreas(areasFromDb);
                }

                log.debug("批量获取区域信息: 总数={}, 缓存命中={}, 数据库查询={}",
                        areaIds.size(), cachedAreas.size(), areasFromDb.size());
            }

            return result;

        } catch (Exception e) {
            log.error("批量获取区域信息失败: areaIds={}", areaIds, e);
            return new HashMap<>();
        }
    }

    /**
     * 缓存单个区域信息
     */
    public void cacheArea(AccessAreaEntity area) {
        if (area == null || area.getAreaId() == null) {
            return;
        }

        try {
            // 写入统一缓存
            accessCacheService.cacheArea(area);

            // 保持L1缓存兼容性
            String cacheKey = REDIS_AREA_PREFIX + area.getAreaId();
            areaCache.put(cacheKey, area);

            log.debug("区域信息已缓存: areaId={}", area.getAreaId());

        } catch (Exception e) {
            log.error("缓存区域信息失败: areaId={}", area.getAreaId(), e);
        }
    }

    /**
     * 检查区域是否为指定区域的子区域
     */
    public boolean isChildArea(Long parentAreaId, Long childAreaId) {
        if (parentAreaId == null || childAreaId == null) {
            return false;
        }

        if (parentAreaId.equals(childAreaId)) {
            return true;
        }

        AccessAreaEntity childArea = getAreaFromCache(childAreaId);
        if (childArea == null || childArea.getPath() == null) {
            return false;
        }

        // 通过路径字符串判断是否为父子关系
        String path = childArea.getPath();
        return path.contains("," + parentAreaId + ",");
    }

    /**
     * 获取区域路径上的所有区域ID
     */
    public List<Long> getAreaPath(Long areaId) {
        AccessAreaEntity area = getAreaFromCache(areaId);
        if (area == null || area.getPath() == null) {
            return new ArrayList<>();
        }

        String[] pathIds = area.getPath().split(",");
        return Arrays.stream(pathIds)
                .filter(id -> !id.equals("0"))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 获取缓存统计信息
     */

    /**
     * 获取区域统计信息（优先从缓存读取）
     * <p>
     * 性能优化：使用统一缓存架构（ACCESS命名空间）
     * 缓存时间：10分钟（ACCESS命名空间默认TTL）
     *
     * @param areaId 区域ID
     * @return 统计信息Map，如果缓存未命中返回空Map
     */
    public Map<String, Object> getAreaStatisticsFromCache(Long areaId) {
        if (areaId == null) {
            return new HashMap<>();
        }

        try {
            // 先从统一缓存获取
            Map<String, Object> statistics = accessCacheService.getAreaStatistics(areaId);
            if (!statistics.isEmpty()) {
                log.debug("区域统计信息统一缓存命中: areaId={}", areaId);
                return statistics;
            }

            // 保持L1缓存兼容性
            String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;
            Map<String, Object> l1Stats = areaStatisticsCache.getIfPresent(cacheKey);
            if (l1Stats != null && !l1Stats.isEmpty()) {
                log.debug("区域统计信息L1缓存命中: areaId={}", areaId);
                return l1Stats;
            }

            log.debug("区域统计信息缓存未命中: areaId={}", areaId);
            return new HashMap<>();

        } catch (Exception e) {
            log.error("获取区域统计信息缓存失败: areaId={}", areaId, e);
            return new HashMap<>();
        }
    }

    /**
     * 缓存区域统计信息
     *
     * @param areaId 区域ID
     * @param statistics 统计信息Map
     */
    public void cacheAreaStatistics(Long areaId, Map<String, Object> statistics) {
        if (areaId == null || statistics == null) {
            return;
        }

        try {
            // 写入统一缓存
            accessCacheService.cacheAreaStatistics(areaId, statistics);

            // 保持L1缓存兼容性
            String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;
            areaStatisticsCache.put(cacheKey, statistics);
            log.debug("区域统计信息已缓存: areaId={}", areaId);

        } catch (Exception e) {
            log.error("缓存区域统计信息失败: areaId={}", areaId, e);
        }
    }

    /**
     * 清除区域统计信息缓存
     *
     * @param areaId 区域ID
     */
    public void evictAreaStatisticsCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        try {
            // 清除统一缓存
            accessCacheService.clearAreaStatisticsCache(areaId);

            // 清除L1缓存
            String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;
            areaStatisticsCache.invalidate(cacheKey);

            log.debug("区域统计信息缓存已清除: areaId={}", areaId);

        } catch (Exception e) {
            log.error("清除区域统计信息缓存失败: areaId={}", areaId, e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return accessCacheService.getCacheStatistics();
    }

    /**
     * 预热缓存（可选，系统启动时调用）
     */
    public void warmupCache() {
        try {
            log.info("开始预热门禁区域缓存...");

            // 调用统一缓存服务预热
            accessCacheService.warmupCache();

            // 预加载根级区域（保持兼容性）
            List<AccessAreaEntity> rootAreas = accessAreaDao.selectRootAreas();
            for (AccessAreaEntity area : rootAreas) {
                cacheArea(area);
            }

            log.info("门禁区域缓存预热完成，根级区域数量: {}", rootAreas.size());

        } catch (Exception e) {
            log.error("门禁区域缓存预热失败", e);
        }
    }
}