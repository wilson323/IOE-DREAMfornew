package net.lab1024.sa.admin.module.smart.access.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.base.common.cache.RedisUtil;

/**
 * 门禁区域管理器
 * <p>
 * 严格遵循repowiki规范：
 * - Manager层负责缓存管理和复杂数据转换
 * - 使用Caffeine L1缓存 + Redis L2缓存的二级缓存架构
 * - 处理区域树形结构的缓存和懒加载
 * - 提供批量操作的性能优化
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class AccessAreaManager {

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private RedisUtil redisUtil;

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
    private static final String REDIS_AREA_PREFIX = "smart:access:area:";
    private static final String REDIS_AREA_TREE_PREFIX = "smart:access:area:tree:";
    private static final String REDIS_AREA_CHILDREN_PREFIX = "smart:access:area:children:";
    private static final String REDIS_AREA_STATISTICS_PREFIX = "smart:access:area:statistics:";

    /**
     * 获取区域信息（优先从缓存读取）
     */
    public AccessAreaEntity getAreaFromCache(Long areaId) {
        if (areaId == null) {
            return null;
        }

        String cacheKey = REDIS_AREA_PREFIX + areaId;

        try {
            // L1缓存查找
            AccessAreaEntity area = areaCache.getIfPresent(cacheKey);
            if (area != null) {
                log.debug("区域信息L1缓存命中: areaId={}", areaId);
                return area;
            }

            // L2缓存查找
            area = redisUtil.getBean(cacheKey, AccessAreaEntity.class);
            if (area != null) {
                log.debug("区域信息L2缓存命中: areaId={}", areaId);
                // 回填L1缓存
                areaCache.put(cacheKey, area);
                return area;
            }

            // 数据库查找
            area = accessAreaDao.selectById(areaId);
            if (area != null && area.getDeletedFlag() == 0) {
                log.debug("区域信息数据库查找: areaId={}", areaId);
                // 写入L1和L2缓存
                areaCache.put(cacheKey, area);
                redisUtil.setBean(cacheKey, area, 1800); // 30分钟过期
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
        String treeCacheKey = String.format("%s%d_%s", REDIS_AREA_TREE_PREFIX,
                parentId != null ? parentId : 0,
                includeChildren != null ? includeChildren : true);

        try {
            // L1缓存查找
            List<AccessAreaTreeVO> treeList = areaTreeCache.getIfPresent(treeCacheKey);
            if (treeList != null) {
                log.debug("区域树L1缓存命中: parentId={}, includeChildren={}", parentId, includeChildren);
                return treeList;
            }

            // L2缓存查找
            List<AccessAreaTreeVO> cachedList = redisUtil.getList(treeCacheKey, AccessAreaTreeVO.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                treeList = cachedList;
                log.debug("区域树L2缓存命中: parentId={}, includeChildren={}", parentId, includeChildren);
                // 回填L1缓存
                areaTreeCache.put(treeCacheKey, treeList);
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
        String treeCacheKey = String.format("%s%d_%s", REDIS_AREA_TREE_PREFIX,
                parentId != null ? parentId : 0,
                includeChildren != null ? includeChildren : true);

        try {
            // 写入L1缓存
            areaTreeCache.put(treeCacheKey, treeList);

            // 写入L2缓存
            redisUtil.setList(treeCacheKey, treeList, 900); // 15分钟过期

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

        String childrenCacheKey = REDIS_AREA_CHILDREN_PREFIX + parentId;

        try {
            // L1缓存查找
            List<Long> childrenIds = areaChildrenCache.getIfPresent(childrenCacheKey);
            if (childrenIds != null) {
                log.debug("子区域ID列表L1缓存命中: parentId={}", parentId);
                return childrenIds;
            }

            // L2缓存查找
            List<Long> cachedIds = redisUtil.getList(childrenCacheKey, Long.class);
            if (cachedIds != null && !cachedIds.isEmpty()) {
                log.debug("子区域ID列表L2缓存命中: parentId={}", parentId);
                // 回填L1缓存
                areaChildrenCache.put(childrenCacheKey, cachedIds);
                return cachedIds;
            }

            // 数据库查找
            List<Long> childrenIdsFromDb = accessAreaDao.selectChildrenIds(parentId);
            if (childrenIdsFromDb != null && !childrenIdsFromDb.isEmpty()) {
                log.debug("子区域ID列表数据库查找: parentId={}, count={}", parentId, childrenIdsFromDb.size());
                // 写入缓存
                areaChildrenCache.put(childrenCacheKey, childrenIdsFromDb);
                redisUtil.setList(childrenCacheKey, childrenIdsFromDb, 1200); // 20分钟过期
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
            String cacheKey = REDIS_AREA_PREFIX + areaId;

            // 清除L1缓存
            areaCache.invalidate(cacheKey);

            // 清除L2缓存
            redisUtil.delete(cacheKey);

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
            // 清除L1缓存中的所有区域树
            areaTreeCache.invalidateAll();

            // 清除Redis中的区域树缓存（使用通配符）
            Set<String> treeKeys = redisUtil.keys(REDIS_AREA_TREE_PREFIX + "*");
            if (treeKeys != null && !treeKeys.isEmpty()) {
                redisUtil.delete(treeKeys); // 使用Collection<String>参数的delete方法
                log.debug("已清除Redis区域树缓存，数量: {}", treeKeys.size());
            }

            // 清除子区域缓存
            Set<String> childrenKeys = redisUtil.keys(REDIS_AREA_CHILDREN_PREFIX + "*");
            if (childrenKeys != null && !childrenKeys.isEmpty()) {
                redisUtil.delete(childrenKeys); // 使用Collection<String>参数的delete方法
                log.debug("已清除Redis子区域缓存，数量: {}", childrenKeys.size());
            }

            // 清除L1子区域缓存
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

        Map<Long, AccessAreaEntity> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        // 先从缓存中获取
        for (Long areaId : areaIds) {
            AccessAreaEntity area = getAreaFromCache(areaId);
            if (area != null) {
                result.put(areaId, area);
            } else {
                missedIds.add(areaId);
            }
        }

        // 批量查询数据库中缺失的区域
        if (!missedIds.isEmpty()) {
            try {
                List<AccessAreaEntity> areasFromDb = accessAreaDao.selectBatchIds(missedIds);
                for (AccessAreaEntity area : areasFromDb) {
                    if (area != null && area.getDeletedFlag() == 0) {
                        result.put(area.getAreaId(), area);
                        // 缓存结果
                        cacheArea(area);
                    }
                }
                log.debug("批量获取区域信息: 总数={}, 缓存命中={}, 数据库查询={}",
                        areaIds.size(), result.size(), areasFromDb.size());

            } catch (Exception e) {
                log.error("批量获取区域信息失败: areaIds={}", areaIds, e);
            }
        }

        return result;
    }

    /**
     * 缓存单个区域信息
     */
    public void cacheArea(AccessAreaEntity area) {
        if (area == null || area.getAreaId() == null) {
            return;
        }

        try {
            String cacheKey = REDIS_AREA_PREFIX + area.getAreaId();

            // 写入L1缓存
            areaCache.put(cacheKey, area);

            // 写入L2缓存
            redisUtil.setBean(cacheKey, area, 1800); // 30分钟过期

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
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        // L1缓存统计
        stats.put("areaCache", areaCache.stats());
        stats.put("areaTreeCache", areaTreeCache.stats());
        stats.put("areaChildrenCache", areaChildrenCache.stats());

        // 缓存大小
        stats.put("areaCacheSize", areaCache.estimatedSize());
        stats.put("areaTreeCacheSize", areaTreeCache.estimatedSize());
        stats.put("areaChildrenCacheSize", areaChildrenCache.estimatedSize());

        return stats;
    }

    /**
     * 获取区域统计信息（优先从缓存读取）
     * <p>
     * 性能优化：使用多级缓存（Caffeine L1 + Redis L2）
     * 缓存时间：5分钟，适合统计信息的更新频率
     *
     * @param areaId 区域ID
     * @return 统计信息Map，如果缓存未命中返回null
     */
    public Map<String, Object> getAreaStatisticsFromCache(Long areaId) {
        if (areaId == null) {
            return null;
        }

        String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;

        try {
            // L1缓存查找
            Map<String, Object> statistics = areaStatisticsCache.getIfPresent(cacheKey);
            if (statistics != null) {
                log.debug("区域统计信息L1缓存命中: areaId={}", areaId);
                return statistics;
            }

            // L2缓存查找
            statistics = redisUtil.getBean(cacheKey, Map.class);
            if (statistics != null && !statistics.isEmpty()) {
                log.debug("区域统计信息L2缓存命中: areaId={}", areaId);
                // 回填L1缓存
                areaStatisticsCache.put(cacheKey, statistics);
                return statistics;
            }

            log.debug("区域统计信息缓存未命中: areaId={}", areaId);
            return null;

        } catch (Exception e) {
            log.error("获取区域统计信息缓存失败: areaId={}", areaId, e);
            return null;
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

        String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;

        try {
            // 写入L1缓存
            areaStatisticsCache.put(cacheKey, statistics);
            // 写入L2缓存（5分钟过期）
            redisUtil.setBean(cacheKey, statistics, 300);
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

        String cacheKey = REDIS_AREA_STATISTICS_PREFIX + areaId;

        try {
            // 清除L1缓存
            areaStatisticsCache.invalidate(cacheKey);
            // 清除L2缓存
            redisUtil.delete(cacheKey);
            log.debug("区域统计信息缓存已清除: areaId={}", areaId);

        } catch (Exception e) {
            log.error("清除区域统计信息缓存失败: areaId={}", areaId, e);
        }
    }

    /**
     * 预热缓存（可选，系统启动时调用）
     */
    public void warmupCache() {
        try {
            log.info("开始预热门禁区域缓存...");

            // 预加载根级区域
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