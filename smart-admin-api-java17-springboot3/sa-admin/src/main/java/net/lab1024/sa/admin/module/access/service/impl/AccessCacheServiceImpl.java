package net.lab1024.sa.admin.module.access.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;

import net.lab1024.sa.admin.module.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.access.service.AccessCacheService;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 门禁系统统一缓存服务实现
 * <p>
 * 基于UnifiedCacheManager实现的门禁模块缓存服务
 * 遵循缓存架构统一化规范，使用ACCESS命名空间
 * 缓存键格式：iog:cache:ACCESS:{namespace}:{key}
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-23
 */
@Slf4j
@Service
public class AccessCacheServiceImpl implements AccessCacheService {

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    // 缓存键命名空间定义
    private static final class CacheKey {
        // 门禁记录
        public static final String ACCESS_RECORD = "record";
        public static final String USER_TODAY_ACCESS = "user:today:access";
        public static final String DEVICE_TODAY_ACCESS = "device:today:access";

        // 区域信息
        public static final String AREA_INFO = "area:info";
        public static final String AREA_TREE = "area:tree";
        public static final String AREA_CHILDREN = "area:children";

        // 统计数据
        public static final String ACCESS_STATS = "stats:access";
        public static final String REAL_TIME_STATS = "stats:realtime";
        public static final String AREA_STATISTICS = "stats:area";

        // 权限信息
        public static final String USER_PERMISSION = "permission:user";
    }

    // ========== 门禁记录缓存 ==========

    @Override
    public AccessRecordEntity getAccessRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }

        String key = buildKey(CacheKey.ACCESS_RECORD, recordId.toString());
        UnifiedCacheManager.CacheResult<AccessRecordEntity> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, AccessRecordEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public CompletableFuture<AccessRecordEntity> getAccessRecordAsync(Long recordId) {
        if (recordId == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.ACCESS_RECORD, recordId.toString());
        return unifiedCacheManager.getAsync(CacheNamespace.ACCESS, key, AccessRecordEntity.class)
            .thenApply(result -> result.isSuccess() ? result.getData() : null);
    }

    @Override
    public void cacheAccessRecord(AccessRecordEntity record) {
        if (record == null || record.getRecordId() == null) {
            return;
        }

        String key = buildKey(CacheKey.ACCESS_RECORD, record.getRecordId().toString());
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, record);

        log.debug("缓存门禁记录: recordId={}", record.getRecordId());
    }

    @Override
    public CompletableFuture<Void> cacheAccessRecordAsync(AccessRecordEntity record) {
        if (record == null || record.getRecordId() == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.ACCESS_RECORD, record.getRecordId().toString());
        return unifiedCacheManager.setAsync(CacheNamespace.ACCESS, key, record)
            .thenRun(() -> log.debug("异步缓存门禁记录: recordId={}", record.getRecordId()));
    }

    @Override
    public List<AccessRecordEntity> getUserTodayAccess(Long userId, String date) {
        if (userId == null || date == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.USER_TODAY_ACCESS, userId.toString(), date);
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<List<AccessRecordEntity>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> rawList = (List<?>) data;
                    if (!rawList.isEmpty() && rawList.get(0) instanceof AccessRecordEntity) {
                        @SuppressWarnings("unchecked")
                        List<AccessRecordEntity> castList = (List<AccessRecordEntity>) rawList;
                        return castList;
                    }
                }
            } catch (Exception e) {
                log.warn("缓存数据类型转换失败，返回空列表", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void cacheUserTodayAccess(Long userId, String date, List<AccessRecordEntity> records) {
        if (userId == null || date == null || records == null) {
            return;
        }

        String key = buildKey(CacheKey.USER_TODAY_ACCESS, userId.toString(), date);
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, records);

        log.debug("缓存用户当日访问记录: userId={}, date={}, count={}", userId, date, records.size());
    }

    @Override
    public List<AccessRecordEntity> getDeviceTodayAccess(Long deviceId, String date) {
        if (deviceId == null || date == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_TODAY_ACCESS, deviceId.toString(), date);
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<List<AccessRecordEntity>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> rawList = (List<?>) data;
                    if (!rawList.isEmpty() && rawList.get(0) instanceof AccessRecordEntity) {
                        @SuppressWarnings("unchecked")
                        List<AccessRecordEntity> castList = (List<AccessRecordEntity>) rawList;
                        return castList;
                    }
                }
            } catch (Exception e) {
                log.warn("缓存数据类型转换失败，返回空列表", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void cacheDeviceTodayAccess(Long deviceId, String date, List<AccessRecordEntity> records) {
        if (deviceId == null || date == null || records == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_TODAY_ACCESS, deviceId.toString(), date);
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, records);

        log.debug("缓存设备当日访问记录: deviceId={}, date={}, count={}", deviceId, date, records.size());
    }

    // ========== 区域信息缓存 ==========

    @Override
    public AccessAreaEntity getArea(Long areaId) {
        if (areaId == null) {
            return null;
        }

        String key = buildKey(CacheKey.AREA_INFO, areaId.toString());
        UnifiedCacheManager.CacheResult<AccessAreaEntity> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, AccessAreaEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public void cacheArea(AccessAreaEntity area) {
        if (area == null || area.getAreaId() == null) {
            return;
        }

        String key = buildKey(CacheKey.AREA_INFO, area.getAreaId().toString());
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, area);

        log.debug("缓存区域信息: areaId={}", area.getAreaId());
    }

    @Override
    public Map<Long, AccessAreaEntity> batchGetAreas(List<Long> areaIds) {
        Map<Long, AccessAreaEntity> result = new HashMap<>();

        if (CollectionUtils.isEmpty(areaIds)) {
            return result;
        }

        // 批量获取缓存
        List<String> keys = areaIds.stream()
            .map(id -> buildKey(CacheKey.AREA_INFO, id.toString()))
            .collect(Collectors.toList());

        UnifiedCacheManager.BatchCacheResult<AccessAreaEntity> batchResult =
            unifiedCacheManager.mGet(CacheNamespace.ACCESS, keys, AccessAreaEntity.class);

        // 整理结果
        for (int i = 0; i < areaIds.size() && i < batchResult.getResults().size(); i++) {
            Long areaId = areaIds.get(i);
            UnifiedCacheManager.CacheResult<AccessAreaEntity> cacheResult = batchResult.getResults().get(i);

            if (cacheResult.isSuccess() && cacheResult.getData() != null) {
                result.put(areaId, cacheResult.getData());
            }
        }

        log.debug("批量获取区域信息: 总数={}, 命中={}", areaIds.size(), result.size());
        return result;
    }

    @Override
    public void batchCacheAreas(List<AccessAreaEntity> areas) {
        if (CollectionUtils.isEmpty(areas)) {
            return;
        }

        Map<String, AccessAreaEntity> keyValues = new HashMap<>();
        for (AccessAreaEntity area : areas) {
            if (area != null && area.getAreaId() != null) {
                String key = buildKey(CacheKey.AREA_INFO, area.getAreaId().toString());
                keyValues.put(key, area);
            }
        }

        unifiedCacheManager.mSet(CacheNamespace.ACCESS, keyValues);

        log.debug("批量缓存区域信息: 数量={}", areas.size());
    }

    @Override
    public List<AccessAreaTreeVO> getAreaTree(Long parentId, Boolean includeChildren) {
        String key = buildKey(CacheKey.AREA_TREE,
            parentId != null ? parentId.toString() : "0",
            includeChildren != null ? includeChildren.toString() : "true");

        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<List<AccessAreaTreeVO>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> rawList = (List<?>) data;
                    if (!rawList.isEmpty() && rawList.get(0) instanceof AccessAreaTreeVO) {
                        @SuppressWarnings("unchecked")
                        List<AccessAreaTreeVO> castList = (List<AccessAreaTreeVO>) rawList;
                        return castList;
                    }
                }
            } catch (Exception e) {
                log.warn("缓存区域树数据类型转换失败，返回空列表", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void cacheAreaTree(Long parentId, Boolean includeChildren, List<AccessAreaTreeVO> treeList) {
        if (treeList == null) {
            return;
        }

        String key = buildKey(CacheKey.AREA_TREE,
            parentId != null ? parentId.toString() : "0",
            includeChildren != null ? includeChildren.toString() : "true");

        unifiedCacheManager.set(CacheNamespace.ACCESS, key, treeList);

        log.debug("缓存区域树: parentId={}, includeChildren={}, size={}", parentId, includeChildren, treeList.size());
    }

    @Override
    public List<Long> getChildrenIds(Long parentId) {
        if (parentId == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.AREA_CHILDREN, parentId.toString());
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<List<Long>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> rawList = (List<?>) data;
                    if (!rawList.isEmpty() && rawList.get(0) instanceof Long) {
                        @SuppressWarnings("unchecked")
                        List<Long> castList = (List<Long>) rawList;
                        return castList;
                    }
                }
            } catch (Exception e) {
                log.warn("缓存子区域ID数据类型转换失败，返回空列表", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void cacheChildrenIds(Long parentId, List<Long> childrenIds) {
        if (parentId == null || childrenIds == null) {
            return;
        }

        String key = buildKey(CacheKey.AREA_CHILDREN, parentId.toString());
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, childrenIds);

        log.debug("缓存子区域ID: parentId={}, count={}", parentId, childrenIds.size());
    }

    // ========== 统计数据缓存 ==========

    @Override
    public Map<String, Object> getAccessStats(String date) {
        if (date == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.ACCESS_STATS, date);
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<Map<String, Object>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castMap = (Map<String, Object>) data;
                    return castMap;
                }
            } catch (Exception e) {
                log.warn("缓存Map数据类型转换失败，返回空Map", e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public void cacheAccessStats(String date, Map<String, Object> stats) {
        if (date == null || stats == null) {
            return;
        }

        String key = buildKey(CacheKey.ACCESS_STATS, date);
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, stats);

        log.debug("缓存访问统计数据: date={}", date);
    }

    @Override
    public Map<String, Object> getRealTimeStats() {
        String key = CacheKey.REAL_TIME_STATS;
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<Map<String, Object>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castMap = (Map<String, Object>) data;
                    return castMap;
                }
            } catch (Exception e) {
                log.warn("缓存Map数据类型转换失败，返回空Map", e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public void updateRealTimeStats(Map<String, Object> stats) {
        if (stats == null) {
            return;
        }

        String key = CacheKey.REAL_TIME_STATS;
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, stats);

        log.debug("更新实时统计: stats={}", stats);
    }

    @Override
    public Map<String, Object> getAreaStatistics(Long areaId) {
        if (areaId == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.AREA_STATISTICS, areaId.toString());
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<Map<String, Object>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castMap = (Map<String, Object>) data;
                    return castMap;
                }
            } catch (Exception e) {
                log.warn("缓存Map数据类型转换失败，返回空Map", e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public void cacheAreaStatistics(Long areaId, Map<String, Object> stats) {
        if (areaId == null || stats == null) {
            return;
        }

        String key = buildKey(CacheKey.AREA_STATISTICS, areaId.toString());
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, stats);

        log.debug("缓存区域统计: areaId={}", areaId);
    }

    // ========== 权限缓存 ==========

    @Override
    public Map<String, Object> checkUserPermission(Long userId, Long deviceId) {
        if (userId == null || deviceId == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.USER_PERMISSION, userId.toString(), deviceId.toString());
        UnifiedCacheManager.CacheResult<?> result =
            unifiedCacheManager.get(CacheNamespace.ACCESS, key, new TypeReference<Map<String, Object>>() {});

        if (result.isSuccess() && result.getData() != null) {
            try {
                Object data = result.getData();
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castMap = (Map<String, Object>) data;
                    return castMap;
                }
            } catch (Exception e) {
                log.warn("缓存Map数据类型转换失败，返回空Map", e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public void cacheUserPermission(Long userId, Map<String, Object> permission) {
        if (userId == null || permission == null) {
            return;
        }

        // 简化实现：缓存用户权限信息
        String key = buildKey(CacheKey.USER_PERMISSION, userId.toString(), "general");
        unifiedCacheManager.set(CacheNamespace.ACCESS, key, permission);

        log.debug("缓存用户权限: userId={}", userId);
    }

    // ========== 缓存管理操作 ==========

    @Override
    public void clearUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        // 清除用户相关的所有缓存
        String userPattern = "*:" + userId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, userPattern);

        log.debug("清除用户缓存: userId={}", userId);
    }

    @Override
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 清除设备相关的所有缓存
        String devicePattern = "*:" + deviceId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, devicePattern);

        log.debug("清除设备缓存: deviceId={}", deviceId);
    }

    @Override
    public void clearAreaCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        // 清除区域相关的所有缓存
        String areaPattern = CacheKey.AREA_INFO + ":" + areaId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, areaPattern);

        log.debug("清除区域缓存: areaId={}", areaId);
    }

    @Override
    public void clearAreaTreeCache() {
        // 清除所有区域树缓存
        String treePattern = CacheKey.AREA_TREE + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, treePattern);

        // 清除所有子区域缓存
        String childrenPattern = CacheKey.AREA_CHILDREN + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, childrenPattern);

        log.debug("清除区域树相关缓存");
    }

    @Override
    public void clearDateCache(String date) {
        if (date == null) {
            return;
        }

        // 清除日期相关的统计缓存
        String statsPattern = CacheKey.ACCESS_STATS + ":" + date + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, statsPattern);

        // 清除日期相关的访问记录缓存
        String accessPattern = "*:*:" + date;
        unifiedCacheManager.deleteByPattern(CacheNamespace.ACCESS, accessPattern);

        log.debug("清除日期相关缓存: date={}", date);
    }

    @Override
    public void clearRealTimeStatsCache() {
        unifiedCacheManager.delete(CacheNamespace.ACCESS, CacheKey.REAL_TIME_STATS);
        log.debug("清除实时统计缓存");
    }

    @Override
    public void clearAreaStatisticsCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        String key = buildKey(CacheKey.AREA_STATISTICS, areaId.toString());
        unifiedCacheManager.delete(CacheNamespace.ACCESS, key);

        log.debug("清除区域统计缓存: areaId={}", areaId);
    }

    @Override
    public void warmupCache() {
        log.info("开始预热门禁缓存...");
        // 预热逻辑由具体的Manager类实现
        log.info("门禁缓存预热完成");
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        return unifiedCacheManager.getCacheStatistics(CacheNamespace.ACCESS);
    }

    @Override
    public void clearAllAccessCache() {
        unifiedCacheManager.clearNamespace(CacheNamespace.ACCESS);
        log.info("清除ACCESS命名空间下的所有缓存");
    }

    @Override
    public void removeCache(String cacheKey) {
        unifiedCacheManager.remove(cacheKey);
        log.debug("移除缓存项: {}", cacheKey);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建缓存键
     *
     * @param parts 键的部分
     * @return 完整的缓存键
     */
    private String buildKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        return String.join(":", parts);
    }
}