package net.lab1024.sa.base.module.area.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.module.area.dao.AreaDao;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.base.module.area.domain.vo.AreaVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.area.enums.AreaTypeEnum;
import com.alibaba.fastjson2.JSON;

/**
 * 区域缓存管理器
 * 负责区域数据的缓存管理，提升查询性能
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Component
public class AreaCacheManager {

    @Resource
    private AreaDao areaDao;

    @Resource
    private AreaManager areaManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "area:";
    private static final String CACHE_PREFIX_ENTITY = CACHE_PREFIX + "entity:";
    private static final String CACHE_PREFIX_VO = CACHE_PREFIX + "vo:";
    private static final String CACHE_PREFIX_TREE = CACHE_PREFIX + "tree:";
    private static final String CACHE_PREFIX_CHILDREN = CACHE_PREFIX + "children:";
    private static final String CACHE_PREFIX_PARENTS = CACHE_PREFIX + "parents:";
    private static final String CACHE_PREFIX_BY_TYPE = CACHE_PREFIX + "by_type:";
    private static final String CACHE_PREFIX_BY_PARENT = CACHE_PREFIX + "by_parent:";
    private static final String CACHE_PREFIX_STATS = CACHE_PREFIX + "stats";

    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_TIME = TimeUnit.HOURS.toSeconds(2); // 2小时
    private static final long CACHE_EXPIRE_TIME_SHORT = TimeUnit.MINUTES.toSeconds(30); // 30分钟

    /**
     * 获取区域实体（带缓存）
     */
    public AreaEntity getEntity(Long areaId) {
        if (areaId == null) {
            return null;
        }

        String cacheKey = CACHE_PREFIX_ENTITY + areaId;
        try {
            // 先从缓存获取
            AreaEntity cachedEntity = (AreaEntity) redisTemplate.opsForValue().get(cacheKey);
            if (cachedEntity != null) {
                log.debug("从缓存获取区域实体成功, areaId: {}", areaId);
                return cachedEntity;
            }

            // 缓存中没有，从数据库查询
            AreaEntity entity = areaDao.selectById(areaId);
            if (entity != null) {
                // 存入缓存
                redisTemplate.opsForValue().set(cacheKey, entity, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                log.debug("从数据库查询区域实体并存入缓存, areaId: {}", areaId);
            }

            return entity;
        } catch (Exception e) {
            log.error("获取区域实体缓存失败, areaId: {}", areaId, e);
            // 缓存异常时直接查询数据库
            return areaDao.selectById(areaId);
        }
    }

    /**
     * 获取区域VO（带缓存）
     */
    public AreaVO getVO(Long areaId) {
        if (areaId == null) {
            return null;
        }

        String cacheKey = CACHE_PREFIX_VO + areaId;
        try {
            // 先从缓存获取
            AreaVO cachedVO = (AreaVO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedVO != null) {
                log.debug("从缓存获取区域VO成功, areaId: {}", areaId);
                return cachedVO;
            }

            // 缓存中没有，从数据库查询
            AreaEntity entity = areaDao.selectById(areaId);
            if (entity != null) {
                AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                // 设置区域类型名称
                AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                vo.setAreaTypeName(typeEnum.getDescription());
                vo.setAreaTypeEnglishName(typeEnum.getEnglishName());

                // 存入缓存
                redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                log.debug("从数据库查询区域VO并存入缓存, areaId: {}", areaId);
            }

            return null;
        } catch (Exception e) {
            log.error("获取区域VO缓存失败, areaId: {}", areaId, e);
            // 缓存异常时直接查询数据库
            AreaEntity entity = areaDao.selectById(areaId);
            if (entity != null) {
                AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                vo.setAreaTypeName(typeEnum.getDescription());
                vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                return vo;
            }
            return null;
        }
    }

    /**
     * 获取区域树（带缓存）
     */
    public List<AreaTreeVO> getAreaTree() {
        String cacheKey = CACHE_PREFIX_TREE + "all";
        try {
            // 先从缓存获取
            List<AreaTreeVO> cachedTree = (List<AreaTreeVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedTree != null) {
                log.debug("从缓存获取区域树成功");
                return cachedTree;
            }

            // 缓存中没有，构建区域树
            List<AreaTreeVO> areaTree = areaManager.buildAreaTreeWithStats();

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, areaTree, CACHE_EXPIRE_TIME_SHORT, TimeUnit.SECONDS);
            log.debug("构建区域树并存入缓存");

            return areaTree;
        } catch (Exception e) {
            log.error("获取区域树缓存失败", e);
            // 缓存异常时直接构建
            return areaManager.buildAreaTreeWithStats();
        }
    }

    /**
     * 获取子区域ID列表（带缓存）
     */
    public List<Long> getChildrenIds(Long areaId) {
        if (areaId == null) {
            return new ArrayList<>();
        }

        String cacheKey = CACHE_PREFIX_CHILDREN + areaId;
        try {
            // 先从缓存获取
            List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedIds != null) {
                log.debug("从缓存获取子区域ID列表成功, areaId: {}", areaId);
                return cachedIds;
            }

            // 缓存中没有，从数据库查询
            List<Long> childrenIds = areaDao.selectSelfAndAllChildrenIds(areaId);

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, childrenIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("从数据库查询子区域ID列表并存入缓存, areaId: {}", areaId);

            return childrenIds;
        } catch (Exception e) {
            log.error("获取子区域ID列表缓存失败, areaId: {}", areaId, e);
            // 缓存异常时直接查询数据库
            return areaDao.selectSelfAndAllChildrenIds(areaId);
        }
    }

    /**
     * 获取父区域ID列表（带缓存）
     */
    public List<Long> getParentIds(Long areaId) {
        if (areaId == null) {
            return new ArrayList<>();
        }

        String cacheKey = CACHE_PREFIX_PARENTS + areaId;
        try {
            // 先从缓存获取
            List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedIds != null) {
                log.debug("从缓存获取父区域ID列表成功, areaId: {}", areaId);
                return cachedIds;
            }

            // 缓存中没有，从数据库查询
            List<Long> parentIds = areaDao.selectAllParentIds(areaId);

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, parentIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("从数据库查询父区域ID列表并存入缓存, areaId: {}", areaId);

            return parentIds;
        } catch (Exception e) {
            log.error("获取父区域ID列表缓存失败, areaId: {}", areaId, e);
            // 缓存异常时直接查询数据库
            return areaDao.selectAllParentIds(areaId);
        }
    }

    /**
     * 根据区域类型获取区域列表（带缓存）
     */
    public List<AreaVO> getByAreaType(Integer areaType) {
        if (areaType == null) {
            return new ArrayList<>();
        }

        String cacheKey = CACHE_PREFIX_BY_TYPE + areaType;
        try {
            // 先从缓存获取
            List<AreaVO> cachedList = (List<AreaVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null) {
                log.debug("从缓存获取按类型查询的区域列表成功, areaType: {}", areaType);
                return cachedList;
            }

            // 缓存中没有，从数据库查询
            List<AreaEntity> entityList = areaDao.selectByAreaType(areaType);
            List<AreaVO> voList = convertToVOList(entityList);

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, voList, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("从数据库查询按类型查询的区域列表并存入缓存, areaType: {}", areaType);

            return voList;
        } catch (Exception e) {
            log.error("获取按类型查询的区域列表缓存失败, areaType: {}", areaType, e);
            // 缓存异常时直接查询数据库
            List<AreaEntity> entityList = areaDao.selectByAreaType(areaType);
            return convertToVOList(entityList);
        }
    }

    /**
     * 根据父区域ID获取子区域列表（带缓存）
     */
    public List<AreaVO> getByParentId(Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }

        String cacheKey = CACHE_PREFIX_BY_PARENT + parentId;
        try {
            // 先从缓存获取
            List<AreaVO> cachedList = (List<AreaVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null) {
                log.debug("从缓存获取按父区域ID查询的区域列表成功, parentId: {}", parentId);
                return cachedList;
            }

            // 缓存中没有，从数据库查询
            List<AreaEntity> entityList = areaDao.selectByParentId(parentId);
            List<AreaVO> voList = convertToVOList(entityList);

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, voList, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("从数据库查询按父区域ID查询的区域列表并存入缓存, parentId: {}", parentId);

            return voList;
        } catch (Exception e) {
            log.error("获取按父区域ID查询的区域列表缓存失败, parentId: {}", parentId, e);
            // 缓存异常时直接查询数据库
            List<AreaEntity> entityList = areaDao.selectByParentId(parentId);
            return convertToVOList(entityList);
        }
    }

    /**
     * 获取所有区域（带缓存）
     */
    public List<AreaVO> getAllAreas() {
        String cacheKey = CACHE_PREFIX_VO + "all";
        try {
            // 先从缓存获取
            List<AreaVO> cachedList = (List<AreaVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null) {
                log.debug("从缓存获取所有区域列表成功");
                return cachedList;
            }

            // 缓存中没有，从数据库查询
            List<AreaEntity> entityList = areaDao.selectAll();
            List<AreaVO> voList = convertToVOList(entityList);

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, voList, CACHE_EXPIRE_TIME_SHORT, TimeUnit.SECONDS);
            log.debug("从数据库查询所有区域列表并存入缓存");

            return voList;
        } catch (Exception e) {
            log.error("获取所有区域列表缓存失败", e);
            // 缓存异常时直接查询数据库
            List<AreaEntity> entityList = areaDao.selectAll();
            return convertToVOList(entityList);
        }
    }

    /**
     * 获取区域统计信息（带缓存）
     */
    public Map<String, Object> getStatistics() {
        String cacheKey = CACHE_PREFIX_STATS;
        try {
            // 先从缓存获取
            Map<String, Object> cachedStats = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedStats != null) {
                log.debug("从缓存获取区域统计信息成功");
                return cachedStats;
            }

            // 缓存中没有，构建统计信息
            Map<String, Object> statistics = new HashMap<>();

            // 总区域数
            long totalCount = areaDao.countByCondition(null, null, null);
            statistics.put("totalCount", totalCount);

            // 按类型统计
            Map<Integer, Long> typeStatistics = new HashMap<>();
            for (AreaTypeEnum typeEnum : AreaTypeEnum.values()) {
                long count = areaDao.countByCondition(typeEnum.getValue(), null, null);
                typeStatistics.put(typeEnum.getValue(), count);
            }
            statistics.put("typeStatistics", typeStatistics);

            // 按状态统计
            long enabledCount = areaDao.countByCondition(null, 1, null);
            long disabledCount = areaDao.countByCondition(null, 0, null);
            long maintenanceCount = areaDao.countByCondition(null, 2, null);

            Map<String, Long> statusStatistics = new HashMap<>();
            statusStatistics.put("enabled", enabledCount);
            statusStatistics.put("disabled", disabledCount);
            statusStatistics.put("maintenance", maintenanceCount);
            statistics.put("statusStatistics", statusStatistics);

            // 根区域数量
            List<AreaEntity> rootAreas = areaDao.selectRootAreas();
            statistics.put("rootCount", rootAreas.size());

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, statistics, CACHE_EXPIRE_TIME_SHORT, TimeUnit.SECONDS);
            log.debug("构建区域统计信息并存入缓存");

            return statistics;
        } catch (Exception e) {
            log.error("获取区域统计信息缓存失败", e);
            // 缓存异常时返回空统计
            return new HashMap<>();
        }
    }

    /**
     * 删除区域缓存
     */
    public void evictCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        try {
            List<String> keysToDelete = new ArrayList<>();

            // 删除具体区域的缓存
            keysToDelete.add(CACHE_PREFIX_ENTITY + areaId);
            keysToDelete.add(CACHE_PREFIX_VO + areaId);
            keysToDelete.add(CACHE_PREFIX_CHILDREN + areaId);
            keysToDelete.add(CACHE_PREFIX_PARENTS + areaId);

            // 删除相关列表缓存
            keysToDelete.add(CACHE_PREFIX_TREE + "all");
            keysToDelete.add(CACHE_PREFIX_VO + "all");
            keysToDelete.add(CACHE_PREFIX_STATS);

            // 批量删除
            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("删除区域缓存成功, areaId: {}, keys: {}", areaId, keysToDelete);
            }
        } catch (Exception e) {
            log.error("删除区域缓存失败, areaId: {}", areaId, e);
        }
    }

    /**
     * 清除所有区域相关缓存
     */
    public void evictAllCache() {
        try {
            // 获取所有区域相关的缓存键
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
                log.info("清除所有区域缓存成功, key count: {}", keys.size());
            }
        } catch (Exception e) {
            log.error("清除所有区域缓存失败", e);
        }
    }

    /**
     * 删除按类型查询的缓存
     */
    public void evictByTypeCache(Integer areaType) {
        if (areaType == null) {
            return;
        }

        try {
            String cacheKey = CACHE_PREFIX_BY_TYPE + areaType;
            redisTemplate.delete(cacheKey);
            log.debug("删除按类型查询的区域缓存成功, areaType: {}", areaType);
        } catch (Exception e) {
            log.error("删除按类型查询的区域缓存失败, areaType: {}", areaType, e);
        }
    }

    /**
     * 删除按父区域ID查询的缓存
     */
    public void evictByParentCache(Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }

        try {
            String cacheKey = CACHE_PREFIX_BY_PARENT + parentId;
            redisTemplate.delete(cacheKey);
            log.debug("删除按父区域ID查询的区域缓存成功, parentId: {}", parentId);
        } catch (Exception e) {
            log.error("删除按父区域ID查询的区域缓存失败, parentId: {}", parentId, e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 获取所有区域相关的缓存键
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            stats.put("totalKeys", keys != null ? keys.size() : 0);

            // 按类型统计
            Map<String, Integer> typeStats = new HashMap<>();
            if (keys != null) {
                for (String key : keys) {
                    if (key.startsWith(CACHE_PREFIX_ENTITY)) {
                        typeStats.put("entity", typeStats.getOrDefault("entity", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_VO)) {
                        typeStats.put("vo", typeStats.getOrDefault("vo", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_TREE)) {
                        typeStats.put("tree", typeStats.getOrDefault("tree", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_CHILDREN)) {
                        typeStats.put("children", typeStats.getOrDefault("children", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_PARENTS)) {
                        typeStats.put("parents", typeStats.getOrDefault("parents", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_BY_TYPE)) {
                        typeStats.put("by_type", typeStats.getOrDefault("by_type", 0) + 1);
                    } else if (key.startsWith(CACHE_PREFIX_BY_PARENT)) {
                        typeStats.put("by_parent", typeStats.getOrDefault("by_parent", 0) + 1);
                    } else if (key.equals(CACHE_PREFIX_STATS)) {
                        typeStats.put("stats", typeStats.getOrDefault("stats", 0) + 1);
                    }
                }
            }
            stats.put("typeStatistics", typeStats);

            return stats;
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return stats;
        }
    }

    /**
     * 预热缓存
     */
    public void warmUpCache() {
        try {
            log.info("开始预热区域缓存...");

            // 预热所有区域数据
            List<AreaEntity> allAreas = areaDao.selectAll();
            log.info("预热区域实体缓存, count: {}", allAreas.size());

            // 预热区域树
            getAreaTree();

            // 预热统计信息
            getStatistics();

            // 预热按类型分组的数据
            for (AreaTypeEnum typeEnum : AreaTypeEnum.values()) {
                getByAreaType(typeEnum.getValue());
            }

            // 预热根区域的子区域
            List<AreaEntity> rootAreas = areaDao.selectRootAreas();
            for (AreaEntity rootArea : rootAreas) {
                getByParentId(rootArea.getAreaId());
            }

            log.info("区域缓存预热完成");
        } catch (Exception e) {
            log.error("区域缓存预热失败", e);
        }
    }

    /**
     * 转换实体列表为VO列表
     */
    private List<AreaVO> convertToVOList(List<AreaEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        return entityList.stream()
            .map(entity -> {
                AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                vo.setAreaTypeName(typeEnum.getDescription());
                vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                return vo;
            })
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}