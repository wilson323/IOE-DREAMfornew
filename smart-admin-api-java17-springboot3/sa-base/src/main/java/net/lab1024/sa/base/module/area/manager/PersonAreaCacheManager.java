package net.lab1024.sa.base.module.area.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.module.area.dao.PersonAreaRelationDao;
import net.lab1024.sa.base.module.area.domain.entity.PersonAreaRelationEntity;
import net.lab1024.sa.base.module.area.domain.vo.PersonAreaRelationVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 人员区域缓存管理器
 * 负责人员区域关联数据的缓存管理，提升查询性能
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Component
public class PersonAreaCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PersonAreaRelationDao personAreaRelationDao;

    private static final String CACHE_PREFIX = "person_area:";
    private static final long CACHE_TTL = 30L; // 30分钟
    private static final long CACHE_TTL_HOURS = 30L; // 30分钟，兼容性常量

    /**
     * 构建缓存键
     */
    private String buildCacheKey(Long personId) {
        return CACHE_PREFIX + personId;
    }

    /**
     * 缓存人员区域关联数据
     *
     * @param personId 人员ID
     */
    public void cachePersonAreaRelation(Long personId) {
        try {
            String cacheKey = CACHE_PREFIX + personId;
            List<PersonAreaRelationEntity> relations = personAreaRelationDao.selectList(
                new LambdaQueryWrapper<PersonAreaRelationEntity>()
                    .eq(PersonAreaRelationEntity::getPersonId, personId)
            );

            if (!CollectionUtils.isEmpty(relations)) {
                List<PersonAreaRelationVO> voList = relations.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                    .collect(Collectors.toList());

                redisTemplate.opsForValue().set(cacheKey, voList, CACHE_TTL, TimeUnit.MINUTES);
                log.debug("缓存人员{}区域关联数据，共{}条记录", personId, voList.size());
            }
        } catch (Exception e) {
            log.error("缓存人员区域关联数据失败，personId: {}", personId, e);
        }
    }

    /**
     * 获取缓存的人员区域关联数据
     *
     * @param personId 人员ID
     * @return 人员区域关联数据
     */
    @SuppressWarnings("unchecked")
    public List<PersonAreaRelationVO> getCachedPersonAreaRelation(Long personId) {
        try {
            String cacheKey = CACHE_PREFIX + personId;
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return JSON.parseObject(JSON.toJSONString(cachedData),
                    List.class, PersonAreaRelationVO.class);
            }
        } catch (Exception e) {
            log.error("获取缓存的人员区域关联数据失败，personId: {}", personId, e);
        }
        return null;
    }

    /**
     * 清除人员区域关联缓存
     *
     * @param personId 人员ID
     */
    public void clearPersonAreaCache(Long personId) {
        try {
            String cacheKey = CACHE_PREFIX + personId;
            redisTemplate.delete(cacheKey);
            log.debug("清除人员{}区域关联缓存", personId);
        } catch (Exception e) {
            log.error("清除人员区域关联缓存失败，personId: {}", personId, e);
        }
    }

    /**
     * 批量清除人员区域关联缓存
     *
     * @param personIds 人员ID列表
     */
    public void batchClearPersonAreaCache(List<Long> personIds) {
        if (CollectionUtils.isEmpty(personIds)) {
            return;
        }

        try {
            List<String> cacheKeys = personIds.stream()
                .map(personId -> CACHE_PREFIX + personId)
                .collect(Collectors.toList());

            redisTemplate.delete(cacheKeys);
            log.debug("批量清除人员区域关联缓存，共{}个人员", personIds.size());
        } catch (Exception e) {
            log.error("批量清除人员区域关联缓存失败", e);
        }
    }

    /**
     * 获取人员区域缓存统计信息
     *
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachePrefix", CACHE_PREFIX);
        stats.put("ttlMinutes", CACHE_TTL);
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    /**
     * 清理指定人员的缓存
     *
     * @param personId 人员ID
     */
    public void evictPersonCache(Long personId) {
        try {
            String cacheKey = CACHE_PREFIX + personId;
            redisTemplate.delete(cacheKey);
            log.debug("清理指定人员{}的区域关联缓存", personId);
        } catch (Exception e) {
            log.error("清理指定人员区域关联缓存失败，personId: {}", personId, e);
        }
    }

    /**
     * 清理指定区域的缓存
     * 由于缓存是以人员ID为键的，这里采用批量清理策略
     *
     * @param areaId 区域ID
     */
    public void evictAreaCache(Long areaId) {
        try {
            // 获取该区域的所有人员关联，然后清理对应的人员缓存
            List<PersonAreaRelationEntity> relations = personAreaRelationDao.selectList(
                new LambdaQueryWrapper<PersonAreaRelationEntity>()
                    .eq(PersonAreaRelationEntity::getAreaId, areaId)
                    .eq(PersonAreaRelationEntity::getDeletedFlag, false)
            );

            if (!CollectionUtils.isEmpty(relations)) {
                List<Long> personIds = relations.stream()
                    .map(PersonAreaRelationEntity::getPersonId)
                    .distinct()
                    .collect(Collectors.toList());

                batchClearPersonAreaCache(personIds);
                log.debug("清理指定区域{}的区域关联缓存，涉及{}个人员", areaId, personIds.size());
            }
        } catch (Exception e) {
            log.error("清理指定区域区域关联缓存失败，areaId: {}", areaId, e);
        }
    }

    /**
     * 获取人员的区域关联列表
     *
     * @param personId 人员ID
     * @return 区域关联列表
     */
    public List<PersonAreaRelationEntity> getPersonAreaRelations(Long personId) {
        try {
            String cacheKey = buildCacheKey(personId);
            Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
            String cachedData = cachedObj != null ? cachedObj.toString() : null;

            if (cachedData != null) {
                List<PersonAreaRelationVO> voList = JSON.parseArray(cachedData, PersonAreaRelationVO.class);
                return voList.stream()
                    .map(vo -> SmartBeanUtil.copy(vo, PersonAreaRelationEntity.class))
                    .collect(Collectors.toList());
            }

            // 缓存未命中，从数据库加载
            List<PersonAreaRelationEntity> relations = personAreaRelationDao.selectList(
                new LambdaQueryWrapper<PersonAreaRelationEntity>()
                    .eq(PersonAreaRelationEntity::getPersonId, personId)
                    .eq(PersonAreaRelationEntity::getDeletedFlag, false)
                    .orderByAsc(PersonAreaRelationEntity::getPriorityLevel)
                    .orderByAsc(PersonAreaRelationEntity::getCreateTime)
            );

            // 缓存查询结果
            if (!CollectionUtils.isEmpty(relations)) {
                List<PersonAreaRelationVO> voList = relations.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                    .collect(Collectors.toList());

                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(voList),
                    CACHE_TTL_HOURS, TimeUnit.HOURS);
            }

            return relations;
        } catch (Exception e) {
            log.error("获取人员区域关联失败，personId: {}", personId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 缓存人员的区域关联列表
     *
     * @param personId 人员ID
     * @param relations 区域关联列表
     */
    public void putPersonAreaRelations(Long personId, List<PersonAreaRelationEntity> relations) {
        try {
            String cacheKey = buildCacheKey(personId);

            if (CollectionUtils.isEmpty(relations)) {
                redisTemplate.delete(cacheKey);
                return;
            }

            List<PersonAreaRelationVO> voList = relations.stream()
                .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                .collect(Collectors.toList());

            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(voList),
                CACHE_TTL_HOURS, TimeUnit.HOURS);

            log.debug("缓存人员{}的区域关联数据，数量: {}", personId, relations.size());
        } catch (Exception e) {
            log.error("缓存人员区域关联失败，personId: {}", personId, e);
        }
    }

    /**
     * 检查人员是否具有区域权限
     *
     * @param personId 人员ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    public boolean hasAreaPermission(Long personId, Long areaId) {
        try {
            List<PersonAreaRelationEntity> relations = getPersonAreaRelations(personId);

            return relations.stream()
                .anyMatch(relation ->
                    areaId.equals(relation.getAreaId()) &&
                    relation.isActive()
                );
        } catch (Exception e) {
            log.error("检查人员区域权限失败，personId: {}, areaId: {}", personId, areaId, e);
            return false;
        }
    }
}