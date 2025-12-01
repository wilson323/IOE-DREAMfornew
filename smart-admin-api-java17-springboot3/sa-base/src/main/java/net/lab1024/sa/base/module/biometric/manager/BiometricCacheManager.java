package net.lab1024.sa.base.module.biometric.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.lab1024.sa.base.common.util.RedisUtil;
import net.lab1024.sa.base.module.biometric.entity.PersonBiometricEntity;
import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;
import net.lab1024.sa.base.module.biometric.entity.DeviceBiometricMappingEntity;
import net.lab1024.sa.base.module.biometric.entity.BiometricRecordEntity;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * 生物特征缓存管理器
 * <p>
 * 多级缓存管理器，提供生物特征数据的高性能访问
 * L1缓存：本地Caffeine缓存（毫秒级访问）
 * L2缓存：Redis分布式缓存（跨节点共享）
 *
 * 缓存策略：
 * - 人员生物特征档案：1小时过期，1000个条目
 * - 生物特征模板：30分钟过期，5000个条目
 * - 设备生物特征映射：2小时过期，2000个条目
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Component
public class BiometricCacheManager {

    // ==================== 缓存键常量 ====================

    private static final String CACHE_PREFIX = "biometric:";
    private static final String PERSON_BIOMETRIC_PREFIX = CACHE_PREFIX + "person:";
    private static final String BIOMETRIC_TEMPLATE_PREFIX = CACHE_PREFIX + "template:";
    private static final String DEVICE_MAPPING_PREFIX = CACHE_PREFIX + "device:";
    private static final String PERSON_TEMPLATES_PREFIX = CACHE_PREFIX + "person_templates:";
    private static final String DEVICE_REQUIREMENTS_PREFIX = CACHE_PREFIX + "device_requirements:";
    private static final String UNIFIED_BIOMETRIC_PREFIX = CACHE_PREFIX + "unified:";
    private static final String REDIS_BIOMETRIC_KEY_PREFIX = "biometric:";

    // ==================== L1本地缓存 ====================

    private Cache<Long, PersonBiometricEntity> personBiometricLocalCache;
    private Cache<Long, BiometricTemplateEntity> biometricTemplateLocalCache;
    private Cache<Long, DeviceBiometricMappingEntity> deviceMappingLocalCache;
    private Cache<String, List<BiometricTemplateEntity>> personTemplatesLocalCache;
    private Cache<String, Map<String, Object>> unifiedBiometricLocalCache;

    // ==================== 缓存配置 ====================

    // 人员生物特征缓存配置
    private static final int PERSON_BIOMETRIC_MAX_SIZE = 1000;
    private static final long PERSON_BIOMETRIC_EXPIRE_MINUTES = 60;

    // 生物特征模板缓存配置
    private static final int BIOMETRIC_TEMPLATE_MAX_SIZE = 5000;
    private static final long BIOMETRIC_TEMPLATE_EXPIRE_MINUTES = 30;

    // 设备生物特征映射缓存配置
    private static final int DEVICE_MAPPING_MAX_SIZE = 2000;
    private static final long DEVICE_MAPPING_EXPIRE_MINUTES = 120;

    // 人员模板列表缓存配置
    private static final int PERSON_TEMPLATES_MAX_SIZE = 500;
    private static final long PERSON_TEMPLATES_EXPIRE_MINUTES = 45;

    // 统一生物特征缓存配置
    private static final int UNIFIED_BIOMETRIC_MAX_SIZE = 800;
    private static final long UNIFIED_BIOMETRIC_EXPIRE_MINUTES = 30;

    @PostConstruct
    public void init() {
        log.info("初始化生物特征缓存管理器...");

        // 初始化L1本地缓存
        personBiometricLocalCache = Caffeine.newBuilder()
                .maximumSize(PERSON_BIOMETRIC_MAX_SIZE)
                .expireAfterWrite(PERSON_BIOMETRIC_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("人员生物特征缓存移除: key={}, cause={}", key, cause);
                })
                .build();

        biometricTemplateLocalCache = Caffeine.newBuilder()
                .maximumSize(BIOMETRIC_TEMPLATE_MAX_SIZE)
                .expireAfterWrite(BIOMETRIC_TEMPLATE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("生物特征模板缓存移除: key={}, cause={}", key, cause);
                })
                .build();

        deviceMappingLocalCache = Caffeine.newBuilder()
                .maximumSize(DEVICE_MAPPING_MAX_SIZE)
                .expireAfterWrite(DEVICE_MAPPING_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("设备生物特征映射缓存移除: key={}, cause={}", key, cause);
                })
                .build();

        personTemplatesLocalCache = Caffeine.newBuilder()
                .maximumSize(PERSON_TEMPLATES_MAX_SIZE)
                .expireAfterWrite(PERSON_TEMPLATES_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("人员模板列表缓存移除: key={}, cause={}", key, cause);
                })
                .build();

        unifiedBiometricLocalCache = Caffeine.newBuilder()
                .maximumSize(UNIFIED_BIOMETRIC_MAX_SIZE)
                .expireAfterWrite(UNIFIED_BIOMETRIC_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("统一生物特征缓存移除: key={}, cause={}", key, cause);
                })
                .build();

        log.info("生物特征缓存管理器初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁生物特征缓存管理器...");

        if (personBiometricLocalCache != null) {
            personBiometricLocalCache.invalidateAll();
        }
        if (biometricTemplateLocalCache != null) {
            biometricTemplateLocalCache.invalidateAll();
        }
        if (deviceMappingLocalCache != null) {
            deviceMappingLocalCache.invalidateAll();
        }
        if (personTemplatesLocalCache != null) {
            personTemplatesLocalCache.invalidateAll();
        }
        if (unifiedBiometricLocalCache != null) {
            unifiedBiometricLocalCache.invalidateAll();
        }

        log.info("生物特征缓存管理器销毁完成");
    }

    // ==================== 人员生物特征缓存 ====================

    /**
     * 获取人员生物特征档案
     *
     * @param personId 人员ID
     * @return 人员生物特征档案
     */
    public PersonBiometricEntity getPersonBiometric(Long personId) {
        if (personId == null) {
            return null;
        }

        // 先从L1缓存获取
        PersonBiometricEntity entity = personBiometricLocalCache.getIfPresent(personId);
        if (entity != null) {
            return entity;
        }

        // 从L2缓存获取
        String redisKey = PERSON_BIOMETRIC_PREFIX + personId;
        try {
            entity = RedisUtil.getBean(redisKey, PersonBiometricEntity.class);
            if (entity != null) {
                // 回填L1缓存
                personBiometricLocalCache.put(personId, entity);
                return entity;
            }
        } catch (Exception e) {
            log.warn("从Redis获取人员生物特征缓存失败: personId={}", personId, e);
        }

        return null;
    }

    /**
     * 缓存人员生物特征档案
     *
     * @param entity 人员生物特征档案
     */
    public void cachePersonBiometric(PersonBiometricEntity entity) {
        if (entity == null || entity.getPersonId() == null) {
            return;
        }

        Long personId = entity.getPersonId();

        // 更新L1缓存
        personBiometricLocalCache.put(personId, entity);

        // 更新L2缓存
        String redisKey = PERSON_BIOMETRIC_PREFIX + personId;
        try {
            RedisUtil.setBean(redisKey, entity, PERSON_BIOMETRIC_EXPIRE_MINUTES * 60);
        } catch (Exception e) {
            log.warn("缓存人员生物特征到Redis失败: personId={}", personId, e);
        }

        // 清除相关的模板列表缓存
        removePersonTemplates(personId);
    }

    /**
     * 移除人员生物特征缓存
     *
     * @param personId 人员ID
     */
    public void removePersonBiometric(Long personId) {
        if (personId == null) {
            return;
        }

        // 移除L1缓存
        personBiometricLocalCache.invalidate(personId);

        // 移除L2缓存
        String redisKey = PERSON_BIOMETRIC_PREFIX + personId;
        try {
            RedisUtil.delete(redisKey);
        } catch (Exception e) {
            log.warn("删除Redis人员生物特征缓存失败: personId={}", personId, e);
        }

        // 清除相关的模板列表缓存
        removePersonTemplates(personId);
    }

    // ==================== 生物特征模板缓存 ====================

    /**
     * 获取生物特征模板
     *
     * @param templateId 模板ID
     * @return 生物特征模板
     */
    public BiometricTemplateEntity getBiometricTemplate(Long templateId) {
        if (templateId == null) {
            return null;
        }

        // 先从L1缓存获取
        BiometricTemplateEntity template = biometricTemplateLocalCache.getIfPresent(templateId);
        if (template != null) {
            return template;
        }

        // 从L2缓存获取
        String redisKey = BIOMETRIC_TEMPLATE_PREFIX + templateId;
        try {
            template = RedisUtil.getBean(redisKey, BiometricTemplateEntity.class);
            if (template != null) {
                // 回填L1缓存
                biometricTemplateLocalCache.put(templateId, template);
                return template;
            }
        } catch (Exception e) {
            log.warn("从Redis获取生物特征模板缓存失败: templateId={}", templateId, e);
        }

        return null;
    }

    /**
     * 缓存生物特征模板
     *
     * @param template 生物特征模板
     */
    public void cacheBiometricTemplate(BiometricTemplateEntity template) {
        if (template == null || template.getId() == null) {
            return;
        }

        Long templateId = template.getId();
        Long personId = template.getPersonId();

        // 更新L1缓存
        biometricTemplateLocalCache.put(templateId, template);

        // 更新L2缓存
        String redisKey = BIOMETRIC_TEMPLATE_PREFIX + templateId;
        try {
            RedisUtil.setBean(redisKey, template, BIOMETRIC_TEMPLATE_EXPIRE_MINUTES * 60);
        } catch (Exception e) {
            log.warn("缓存生物特征模板到Redis失败: templateId={}", templateId, e);
        }

        // 清除相关的模板列表缓存
        if (personId != null) {
            removePersonTemplates(personId);
        }
    }

    /**
     * 移除生物特征模板缓存
     *
     * @param templateId 模板ID
     */
    public void removeBiometricTemplate(Long templateId) {
        if (templateId == null) {
            return;
        }

        // 获取模板信息（需要清除相关缓存）
        BiometricTemplateEntity template = getBiometricTemplate(templateId);

        // 移除L1缓存
        biometricTemplateLocalCache.invalidate(templateId);

        // 移除L2缓存
        String redisKey = BIOMETRIC_TEMPLATE_PREFIX + templateId;
        try {
            RedisUtil.delete(redisKey);
        } catch (Exception e) {
            log.warn("删除Redis生物特征模板缓存失败: templateId={}", templateId, e);
        }

        // 清除相关的模板列表缓存
        if (template != null && template.getPersonId() != null) {
            removePersonTemplates(template.getPersonId());
        }
    }

    // ==================== 人员模板列表缓存 ====================

    /**
     * 获取人员模板列表
     *
     * @param personId 人员ID
     * @return 模板列表
     */
    @SuppressWarnings("unchecked")
    public List<BiometricTemplateEntity> getPersonTemplates(Long personId) {
        if (personId == null) {
            return null;
        }

        String cacheKey = PERSON_TEMPLATES_PREFIX + personId;

        // 先从L1缓存获取
        List<BiometricTemplateEntity> templates = personTemplatesLocalCache.getIfPresent(cacheKey);
        if (templates != null) {
            return templates;
        }

        // 从L2缓存获取
        try {
            Object cached = RedisUtil.get(cacheKey);
            if (cached instanceof List) {
                templates = (List<BiometricTemplateEntity>) cached;
                // 回填L1缓存
                personTemplatesLocalCache.put(cacheKey, templates);
                return templates;
            }
        } catch (Exception e) {
            log.warn("从Redis获取人员模板列表缓存失败: personId={}", personId, e);
        }

        return null;
    }

    /**
     * 缓存人员模板列表
     *
     * @param personId 人员ID
     * @param templates 模板列表
     */
    public void cachePersonTemplates(Long personId, List<BiometricTemplateEntity> templates) {
        if (personId == null || templates == null) {
            return;
        }

        String cacheKey = PERSON_TEMPLATES_PREFIX + personId;

        // 更新L1缓存
        personTemplatesLocalCache.put(cacheKey, templates);

        // 更新L2缓存
        try {
            RedisUtil.set(cacheKey, templates, PERSON_TEMPLATES_EXPIRE_MINUTES * 60);
        } catch (Exception e) {
            log.warn("缓存人员模板列表到Redis失败: personId={}", personId, e);
        }
    }

    /**
     * 移除人员模板列表缓存
     *
     * @param personId 人员ID
     */
    public void removePersonTemplates(Long personId) {
        if (personId == null) {
            return;
        }

        String cacheKey = PERSON_TEMPLATES_PREFIX + personId;

        // 移除L1缓存
        personTemplatesLocalCache.invalidate(cacheKey);

        // 移除L2缓存
        try {
            RedisUtil.delete(cacheKey);
        } catch (Exception e) {
            log.warn("删除Redis人员模板列表缓存失败: personId={}", personId, e);
        }
    }

    // ==================== 设备生物特征映射缓存 ====================

    /**
     * 获取设备生物特征映射
     *
     * @param deviceId 设备ID
     * @return 设备生物特征映射
     */
    public DeviceBiometricMappingEntity getDeviceBiometricMapping(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        // 先从L1缓存获取
        DeviceBiometricMappingEntity mapping = deviceMappingLocalCache.getIfPresent(deviceId);
        if (mapping != null) {
            return mapping;
        }

        // 从L2缓存获取
        String redisKey = DEVICE_MAPPING_PREFIX + deviceId;
        try {
            mapping = RedisUtil.getBean(redisKey, DeviceBiometricMappingEntity.class);
            if (mapping != null) {
                // 回填L1缓存
                deviceMappingLocalCache.put(deviceId, mapping);
                return mapping;
            }
        } catch (Exception e) {
            log.warn("从Redis获取设备生物特征映射缓存失败: deviceId={}", deviceId, e);
        }

        return null;
    }

    /**
     * 缓存设备生物特征映射
     *
     * @param mapping 设备生物特征映射
     */
    public void cacheDeviceBiometricMapping(DeviceBiometricMappingEntity mapping) {
        if (mapping == null || mapping.getDeviceId() == null) {
            return;
        }

        Long deviceId = mapping.getDeviceId();

        // 更新L1缓存
        deviceMappingLocalCache.put(deviceId, mapping);

        // 更新L2缓存
        String redisKey = DEVICE_MAPPING_PREFIX + deviceId;
        try {
            RedisUtil.setBean(redisKey, mapping, DEVICE_MAPPING_EXPIRE_MINUTES * 60);
        } catch (Exception e) {
            log.warn("缓存设备生物特征映射到Redis失败: deviceId={}", deviceId, e);
        }

        // 清除相关的设备需求缓存
        removeDeviceRequirements(deviceId);
    }

    /**
     * 移除设备生物特征映射缓存
     *
     * @param deviceId 设备ID
     */
    public void removeDeviceBiometricMapping(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 移除L1缓存
        deviceMappingLocalCache.invalidate(deviceId);

        // 移除L2缓存
        String redisKey = DEVICE_MAPPING_PREFIX + deviceId;
        try {
            RedisUtil.delete(redisKey);
        } catch (Exception e) {
            log.warn("删除Redis设备生物特征映射缓存失败: deviceId={}", deviceId, e);
        }

        // 清除相关的设备需求缓存
        removeDeviceRequirements(deviceId);
    }

    // ==================== 设备需求缓存 ====================

    /**
     * 获取设备生物特征需求
     *
     * @param deviceId 设备ID
     * @return 生物特征需求列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getDeviceRequirements(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String cacheKey = DEVICE_REQUIREMENTS_PREFIX + deviceId;

        // 从L2缓存获取（此类型数据变化不频繁，主要使用L2缓存）
        try {
            Object cached = RedisUtil.get(cacheKey);
            if (cached instanceof List) {
                return (List<String>) cached;
            }
        } catch (Exception e) {
            log.warn("从Redis获取设备生物特征需求缓存失败: deviceId={}", deviceId, e);
        }

        return null;
    }

    /**
     * 缓存设备生物特征需求
     *
     * @param deviceId 设备ID
     * @param requirements 生物特征需求列表
     */
    public void cacheDeviceRequirements(Long deviceId, List<String> requirements) {
        if (deviceId == null || requirements == null) {
            return;
        }

        String cacheKey = DEVICE_REQUIREMENTS_PREFIX + deviceId;

        try {
            RedisUtil.set(cacheKey, requirements, DEVICE_MAPPING_EXPIRE_MINUTES * 60);
        } catch (Exception e) {
            log.warn("缓存设备生物特征需求到Redis失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 移除设备生物特征需求缓存
     *
     * @param deviceId 设备ID
     */
    public void removeDeviceRequirements(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        String cacheKey = DEVICE_REQUIREMENTS_PREFIX + deviceId;

        try {
            RedisUtil.delete(cacheKey);
        } catch (Exception e) {
            log.warn("删除Redis设备生物特征需求缓存失败: deviceId={}", deviceId, e);
        }
    }

    // ==================== 缓存管理 ====================

    /**
     * 清除所有生物特征缓存
     */
    public void clearAllCache() {
        log.info("清除所有生物特征缓存...");

        // 清除L1缓存
        personBiometricLocalCache.invalidateAll();
        biometricTemplateLocalCache.invalidateAll();
        deviceMappingLocalCache.invalidateAll();
        personTemplatesLocalCache.invalidateAll();
        unifiedBiometricLocalCache.invalidateAll();

        // 清除L2缓存
        try {
            RedisUtil.deleteByPattern(CACHE_PREFIX + "*");
        } catch (Exception e) {
            log.warn("清除Redis生物特征缓存失败", e);
        }

        log.info("所有生物特征缓存清除完成");
    }

    /**
     * 清除指定人员的所有缓存
     *
     * @param personId 人员ID
     */
    public void clearPersonCache(Long personId) {
        if (personId == null) {
            return;
        }

        log.info("清除人员缓存: personId={}", personId);

        removePersonBiometric(personId);
        removePersonTemplates(personId);

        // 清除该人员的所有模板缓存
        List<BiometricTemplateEntity> templates = getPersonTemplates(personId);
        if (templates != null) {
            for (BiometricTemplateEntity template : templates) {
                if (template.getId() != null) {
                    biometricTemplateLocalCache.invalidate(template.getId());
                    String redisKey = BIOMETRIC_TEMPLATE_PREFIX + template.getId();
                    try {
                        RedisUtil.delete(redisKey);
                    } catch (Exception e) {
                        log.warn("删除Redis模板缓存失败: templateId={}", template.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * 清除指定设备的所有缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        log.info("清除设备缓存: deviceId={}", deviceId);

        removeDeviceBiometricMapping(deviceId);
        removeDeviceRequirements(deviceId);
    }

    // ==================== 缓存统计 ====================

    /**
     * 获取L1缓存统计信息
     *
     * @return 缓存统计信息
     */
    public Map<String, Object> getL1CacheStats() {
        return Map.of(
            "personBiometric", Map.of(
                "size", personBiometricLocalCache.estimatedSize(),
                "hitCount", personBiometricLocalCache.stats().hitCount(),
                "missCount", personBiometricLocalCache.stats().missCount(),
                "hitRate", personBiometricLocalCache.stats().hitRate()
            ),
            "biometricTemplate", Map.of(
                "size", biometricTemplateLocalCache.estimatedSize(),
                "hitCount", biometricTemplateLocalCache.stats().hitCount(),
                "missCount", biometricTemplateLocalCache.stats().missCount(),
                "hitRate", biometricTemplateLocalCache.stats().hitRate()
            ),
            "deviceMapping", Map.of(
                "size", deviceMappingLocalCache.estimatedSize(),
                "hitCount", deviceMappingLocalCache.stats().hitCount(),
                "missCount", deviceMappingLocalCache.stats().missCount(),
                "hitRate", deviceMappingLocalCache.stats().hitRate()
            ),
            "personTemplates", Map.of(
                "size", personTemplatesLocalCache.estimatedSize(),
                "hitCount", personTemplatesLocalCache.stats().hitCount(),
                "missCount", personTemplatesLocalCache.stats().missCount(),
                "hitRate", personTemplatesLocalCache.stats().hitRate()
            )
        );
    }

    /**
     * 预热缓存
     *
     * @param personIds 人员ID列表
     * @param deviceIds 设备ID列表
     */
    public void warmupCache(List<Long> personIds, List<Long> deviceIds) {
        log.info("开始预热生物特征缓存: persons={}, devices={}",
                personIds != null ? personIds.size() : 0,
                deviceIds != null ? deviceIds.size() : 0);

        // TODO: 实际项目中这里应该从数据库批量加载并缓存
        // 这里只是示例，实际实现需要依赖Service层

        log.info("生物特征缓存预热完成");
    }

    // ==================== 统一生物特征缓存 ====================

    /**
     * 获取统一生物特征数据
     *
     * @param personId 人员ID
     * @return 统一生物特征数据
     */
    public Map<String, Object> getUnifiedBiometricData(Long personId) {
        if (personId == null) {
            return null;
        }

        try {
            // 先从本地缓存获取
            String cacheKey = UNIFIED_BIOMETRIC_PREFIX + personId;
            Map<String, Object> data = unifiedBiometricLocalCache.getIfPresent(cacheKey);

            if (data == null) {
                // 从Redis获取
                String redisKey = REDIS_BIOMETRIC_KEY_PREFIX + "unified:" + personId;
                try {
                    data = RedisUtil.getBean(redisKey, Map.class);
                    if (data != null) {
                        // 更新本地缓存
                        unifiedBiometricLocalCache.put(cacheKey, data);
                    }
                } catch (Exception e) {
                    log.warn("从Redis获取统一生物特征数据异常: personId={}", personId, e);
                }
            }

            return data;
        } catch (Exception e) {
            log.error("获取统一生物特征数据异常: personId={}", personId, e);
            return null;
        }
    }

    /**
     * 缓存统一生物特征数据
     *
     * @param personId 人员ID
     * @param data 统一生物特征数据
     */
    public void cacheUnifiedBiometricData(Long personId, Map<String, Object> data) {
        if (personId == null || data == null) {
            return;
        }

        try {
            String cacheKey = UNIFIED_BIOMETRIC_PREFIX + personId;

            // 更新本地缓存
            unifiedBiometricLocalCache.put(cacheKey, data);

            // 更新Redis缓存
            String redisKey = REDIS_BIOMETRIC_KEY_PREFIX + "unified:" + personId;
            try {
                RedisUtil.setBean(redisKey, data, UNIFIED_BIOMETRIC_EXPIRE_MINUTES * 60);
            } catch (Exception e) {
                log.warn("缓存统一生物特征数据到Redis异常: personId={}", personId, e);
            }

            log.debug("缓存统一生物特征数据: personId={}", personId);
        } catch (Exception e) {
            log.error("缓存统一生物特征数据异常: personId={}", personId, e);
        }
    }

    /**
     * 移除统一生物特征数据缓存
     *
     * @param personId 人员ID
     */
    public void removeUnifiedBiometricData(Long personId) {
        if (personId == null) {
            return;
        }

        try {
            String cacheKey = UNIFIED_BIOMETRIC_PREFIX + personId;

            // 移除本地缓存
            unifiedBiometricLocalCache.invalidate(cacheKey);

            // 移除Redis缓存
            String redisKey = REDIS_BIOMETRIC_KEY_PREFIX + "unified:" + personId;
            try {
                RedisUtil.delete(redisKey);
            } catch (Exception e) {
                log.warn("删除Redis统一生物特征数据缓存失败: personId={}", personId, e);
            }

            log.debug("移除统一生物特征数据缓存: personId={}", personId);
        } catch (Exception e) {
            log.error("移除统一生物特征数据缓存异常: personId={}", personId, e);
        }
    }

    /**
     * 从数据库直接获取人员生物特征
     *
     * @param personId 人员ID
     * @return 生物特征记录列表
     */
    public List<BiometricRecordEntity> getPersonBiometricsFromDB(Long personId) {
        // TODO: 实现从数据库查询逻辑
        // 这里应该调用DAO或Service来获取数据
        log.debug("从数据库获取人员生物特征: personId={}", personId);
        return new ArrayList<>();
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        try {
            personBiometricLocalCache.invalidateAll();
            biometricTemplateLocalCache.invalidateAll();
            deviceMappingLocalCache.invalidateAll();
            personTemplatesLocalCache.invalidateAll();
            unifiedBiometricLocalCache.invalidateAll();

            // 清理Redis缓存
            try {
                RedisUtil.deleteByPattern(REDIS_BIOMETRIC_KEY_PREFIX + "*");
            } catch (Exception redisEx) {
                log.warn("清理Redis生物特征缓存失败", redisEx);
            }

            log.info("生物特征缓存清理完成");
        } catch (Exception e) {
            log.error("清理生物特征缓存异常", e);
        }
    }
}