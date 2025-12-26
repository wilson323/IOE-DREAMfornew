package net.lab1024.sa.access.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessCacheConstants;
import net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 反潜回管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 反潜回验证逻辑
 * - 反潜回记录管理
 * - 反潜回配置管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AntiPassbackManager {

    // 显式添加logger声明以确保编译通过

    private final AntiPassbackRecordDao antiPassbackRecordDao;
    private final AreaAccessExtDao areaAccessExtDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 默认反潜时间窗口（秒）
     */
    private static final int DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS = 300;

    /**
     * 构造函数注入依赖
     *
     * @param antiPassbackRecordDao 反潜记录DAO
     * @param areaAccessExtDao 区域门禁扩展DAO
     * @param redisTemplate Redis模板
     * @param objectMapper JSON对象映射器
     */
    public AntiPassbackManager(
            AntiPassbackRecordDao antiPassbackRecordDao,
            AreaAccessExtDao areaAccessExtDao,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.antiPassbackRecordDao = antiPassbackRecordDao;
        this.areaAccessExtDao = areaAccessExtDao;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 反潜验证
     * <p>
     * 检查同一用户是否从正确的门进出
     * 反潜规则：
     * - 如果上次是进入，当前不能是进入（必须先离开）
     * - 如果上次是离开，当前可以是进入或离开
     * - 时间窗口内不允许重复进入
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param inOutStatus 进出状态（1=进, 2=出）
     * @param areaId 区域ID（用于读取反潜配置）
     * @return 是否通过验证
     */
    public boolean verifyAntiPassback(Long userId, Long deviceId, Integer inOutStatus, Long areaId) {
        log.debug("[反潜验证] 开始验证: userId={}, deviceId={}, inOutStatus={}, areaId={}",
                userId, deviceId, inOutStatus, areaId);

        try {
            // 1. 读取反潜配置
            AntiPassbackConfig config = getAntiPassbackConfig(areaId);
            if (config == null || !config.isEnabled()) {
                log.debug("[反潜验证] 反潜功能未启用，允许通过: areaId={}", areaId);
                return true; // 未启用反潜，允许通过
            }

            // 2. 查询用户最近的进出记录（优先从Redis缓存查询）
            AntiPassbackRecordEntity lastRecord = getLastRecordWithCache(userId, deviceId);

            // 3. 如果没有历史记录，允许通过（首次进入）
            if (lastRecord == null) {
                log.debug("[反潜验证] 无历史记录，允许通过: userId={}", userId);
                return true;
            }

            // 4. 检查反潜规则
            Integer lastInOutStatus = lastRecord.getInOutStatus();
            LocalDateTime lastRecordTime = lastRecord.getRecordTime();

            // 4.1 如果上次是进入，当前不能是进入
            if (AntiPassbackRecordEntity.InOutStatus.IN == lastInOutStatus
                    && AntiPassbackRecordEntity.InOutStatus.IN == inOutStatus) {
                // 检查时间窗口
                int timeWindow = config.getTimeWindow() != null ? config.getTimeWindow() : DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                long secondsBetween = ChronoUnit.SECONDS.between(lastRecordTime, LocalDateTime.now());
                if (secondsBetween < timeWindow) {
                    log.warn("[反潜验证] 反潜违规: userId={}, 上次进入时间={}, 间隔={}秒, 时间窗口={}秒",
                            userId, lastRecordTime, secondsBetween, timeWindow);
                    return false;
                }
            }

            // 4.2 如果上次是离开，当前可以是进入或离开（允许）
            log.debug("[反潜验证] 验证通过: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("[反潜验证] 验证异常: userId={}, error={}", userId, e.getMessage(), e);
            // 异常时允许通过，避免影响正常通行
            return true;
        }
    }

    /**
     * 记录反潜验证结果
     * <p>
     * 验证通过后记录本次进出
     * 同时更新Redis缓存
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param inOutStatus 进出状态
     * @param verifyType 验证方式
     */
    public void recordAntiPassback(Long userId, Long deviceId, Long areaId,
                                   Integer inOutStatus, Integer verifyType) {
        try {
            AntiPassbackRecordEntity record = new AntiPassbackRecordEntity();
            record.setUserId(userId);
            record.setDeviceId(deviceId);
            record.setAreaId(areaId);
            record.setInOutStatus(inOutStatus);
            record.setRecordTime(LocalDateTime.now());
            record.setAccessType(inOutStatus == 1 ? "IN" : "OUT");
            record.setVerifyType(verifyType);

            // 1. 保存到数据库
            antiPassbackRecordDao.insert(record);
            log.debug("[反潜验证] 记录已保存: userId={}, deviceId={}, inOutStatus={}", userId, deviceId, inOutStatus);

            // 2. 更新Redis缓存
            String cacheKey = AccessCacheConstants.buildAntiPassbackRecordKey(userId, deviceId);
            redisTemplate.opsForValue().set(cacheKey, record, AccessCacheConstants.CACHE_EXPIRE_ANTI_PASSBACK_RECORD);
            log.debug("[反潜验证] 记录已缓存: userId={}, deviceId={}", userId, deviceId);

        } catch (Exception e) {
            log.error("[反潜验证] 记录保存异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 分页查询反潜回记录
     *
     * @param userId 用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param areaId 区域ID（可选）
     * @param inOutStatus 进出状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<AntiPassbackRecordEntity> queryRecords(Long userId, Long deviceId, Long areaId,
                                                        Integer inOutStatus, LocalDateTime startTime, LocalDateTime endTime,
                                                        Integer pageNum, Integer pageSize) {
        log.debug("[反潜记录] 分页查询: userId={}, deviceId={}, areaId={}, inOutStatus={}, startTime={}, endTime={}, pageNum={}, pageSize={}",
                userId, deviceId, areaId, inOutStatus, startTime, endTime, pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<AntiPassbackRecordEntity> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(AntiPassbackRecordEntity::getUserId, userId);
        }
        if (deviceId != null) {
            wrapper.eq(AntiPassbackRecordEntity::getDeviceId, deviceId);
        }
        if (areaId != null) {
            wrapper.eq(AntiPassbackRecordEntity::getAreaId, areaId);
        }
        if (inOutStatus != null) {
            wrapper.eq(AntiPassbackRecordEntity::getInOutStatus, inOutStatus);
        }
        if (startTime != null) {
            wrapper.ge(AntiPassbackRecordEntity::getRecordTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AntiPassbackRecordEntity::getRecordTime, endTime);
        }

        // 按记录时间倒序
        wrapper.orderByDesc(AntiPassbackRecordEntity::getRecordTime);

        // 分页查询
        Page<AntiPassbackRecordEntity> page = new Page<>(pageNum, pageSize);
        return antiPassbackRecordDao.selectPage(page, wrapper);
    }

    /**
     * 获取反潜回配置
     *
     * @param areaId 区域ID
     * @return 反潜回配置
     */
    public AntiPassbackManager.AntiPassbackConfig getAntiPassbackConfig(Long areaId) {
        if (areaId == null) {
            return null;
        }

        try {
            // 1. 从Redis缓存查询配置
            String cacheKey = AccessCacheConstants.buildAreaConfigKey(areaId);
            Object cachedConfig = redisTemplate.opsForValue().get(cacheKey);
            if (cachedConfig instanceof AntiPassbackConfig) {
                return (AntiPassbackConfig) cachedConfig;
            }

            // 2. 从数据库查询区域扩展配置
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null || areaExt.getExtConfig() == null || areaExt.getExtConfig().trim().isEmpty()) {
                // 未配置，返回默认配置
                AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
                defaultConfig.enabled = true;
                defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                return defaultConfig;
            }

            // 3. 解析extConfig JSON
            try {
                Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), new TypeReference<Map<String, Object>>() {});
                AntiPassbackConfig config = new AntiPassbackConfig();

                // 读取反潜配置
                if (configMap.containsKey("antiPassback")) {
                    Object antiPassbackObj = configMap.get("antiPassback");
                    if (antiPassbackObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> antiPassbackMap = (Map<String, Object>) antiPassbackObj;
                        config.enabled = (Boolean) antiPassbackMap.getOrDefault("enabled", true);
                        if (antiPassbackMap.containsKey("timeWindow")) {
                            Object timeWindowObj = antiPassbackMap.get("timeWindow");
                            if (timeWindowObj instanceof Number) {
                                config.timeWindow = ((Number) timeWindowObj).intValue();
                            }
                        }
                    } else if (antiPassbackObj instanceof Boolean) {
                        config.enabled = (Boolean) antiPassbackObj;
                    }
                } else {
                    // 未配置反潜，使用默认值
                    config.enabled = true;
                    config.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                }

                // 4. 写入缓存
                redisTemplate.opsForValue().set(cacheKey, config, AccessCacheConstants.CACHE_EXPIRE_AREA_CONFIG);
                log.debug("[反潜配置] 反潜配置已缓存: areaId={}, enabled={}, timeWindow={}",
                        areaId, config.enabled, config.timeWindow);

                return config;

            } catch (Exception e) {
                log.warn("[反潜配置] 解析反潜配置失败: areaId={}, extConfig={}, error={}",
                        areaId, areaExt.getExtConfig(), e.getMessage());
                // 解析失败，返回默认配置
                AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
                defaultConfig.enabled = true;
                defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                return defaultConfig;
            }

        } catch (Exception e) {
            log.error("[反潜配置] 获取反潜配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            // 异常时返回默认配置
            AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
            defaultConfig.enabled = true;
            defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
            return defaultConfig;
        }
    }

    /**
     * 更新反潜回配置
     *
     * @param areaId 区域ID
     * @param enabled 是否启用
     * @param timeWindow 时间窗口（秒）
     */
    public void updateAntiPassbackConfig(Long areaId, Boolean enabled, Integer timeWindow) {
        log.info("[反潜配置] 更新配置: areaId={}, enabled={}, timeWindow={}", areaId, enabled, timeWindow);

        try {
            // 1. 查询或创建区域扩展配置
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null) {
                areaExt = new AreaAccessExtEntity();
                areaExt.setAreaId(areaId);
                areaExt.setExtConfig("{}");
                areaAccessExtDao.insert(areaExt);
            }

            // 2. 解析现有配置
            Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), new TypeReference<Map<String, Object>>() {});
            if (areaExt.getExtConfig() != null && !areaExt.getExtConfig().trim().isEmpty()) {
                try {
                    configMap = objectMapper.readValue(areaExt.getExtConfig(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    log.warn("[反潜配置] 解析现有配置失败，使用空配置: areaId={}, error={}", areaId, e.getMessage());
                    configMap = new HashMap<>();
                }
            } else {
                configMap = new HashMap<>();
            }

            // 3. 更新反潜配置
            Map<String, Object> antiPassbackMap = new HashMap<>();
            antiPassbackMap.put("enabled", enabled);
            antiPassbackMap.put("timeWindow", timeWindow);
            configMap.put("antiPassback", antiPassbackMap);

            // 4. 保存配置
            areaExt.setExtConfig(objectMapper.writeValueAsString(configMap));
            areaAccessExtDao.updateById(areaExt);

            // 5. 清除缓存
            String cacheKey = AccessCacheConstants.buildAreaConfigKey(areaId);
            redisTemplate.delete(cacheKey);

            log.info("[反潜配置] 配置更新成功: areaId={}", areaId);

        } catch (Exception e) {
            log.error("[反潜配置] 更新配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            throw new RuntimeException("更新反潜配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户最近的进出记录（带缓存）
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 最近的记录，不存在返回null
     */
    private AntiPassbackRecordEntity getLastRecordWithCache(Long userId, Long deviceId) {
        // 1. 从Redis缓存查询
        String cacheKey = AccessCacheConstants.buildAntiPassbackRecordKey(userId, deviceId);
        Object cachedRecord = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRecord instanceof AntiPassbackRecordEntity) {
            log.debug("[反潜验证] 从缓存获取记录: userId={}, deviceId={}", userId, deviceId);
            return (AntiPassbackRecordEntity) cachedRecord;
        }

        // 2. 从数据库查询
        java.util.List<AntiPassbackRecordEntity> records = antiPassbackRecordDao.selectRecentRecords(userId, deviceId, 1);
        AntiPassbackRecordEntity lastRecord = records != null && !records.isEmpty() ? records.get(0) : null;

        // 3. 写入缓存（如果存在记录）
        if (lastRecord != null) {
            redisTemplate.opsForValue().set(cacheKey, lastRecord, AccessCacheConstants.CACHE_EXPIRE_ANTI_PASSBACK_RECORD);
            log.debug("[反潜验证] 记录已缓存: userId={}, deviceId={}", userId, deviceId);
        }

        return lastRecord;
    }

    /**
     * 反潜配置类
     */
    public static class AntiPassbackConfig {
        private Boolean enabled;
        private Integer timeWindow; // 时间窗口（秒）

        public Boolean isEnabled() {
            return enabled != null && enabled;
        }

        public Integer getTimeWindow() {
            return timeWindow;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public void setTimeWindow(Integer timeWindow) {
            this.timeWindow = timeWindow;
        }
    }
}
