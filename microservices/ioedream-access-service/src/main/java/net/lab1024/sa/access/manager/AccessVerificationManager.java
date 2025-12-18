package net.lab1024.sa.access.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessVerificationProperties;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.dao.InterlockRecordDao;
import net.lab1024.sa.common.organization.dao.MultiPersonRecordDao;
import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.InterlockRecordEntity;
import net.lab1024.sa.common.organization.entity.MultiPersonRecordEntity;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;
import net.lab1024.sa.common.organization.manager.UserAreaPermissionManager;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 门禁验证管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 复杂验证逻辑编排
 * - 反潜/互锁/多人验证
 * - 时间段/黑名单验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AccessVerificationManager {

    private final AntiPassbackRecordDao antiPassbackRecordDao;
    private final UserAreaPermissionDao userAreaPermissionDao;
    private final UserAreaPermissionManager userAreaPermissionManager;
    private final DeviceDao deviceDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AreaAccessExtDao areaAccessExtDao;
    private final InterlockRecordDao interlockRecordDao;
    private final MultiPersonRecordDao multiPersonRecordDao;
    private final AccessVerificationProperties verificationProperties;

    /**
     * 反潜时间窗口（秒）
     * 默认5分钟，在此时间窗口内不允许重复进入
     * 注意：实际值从配置中读取
     */
    private static final int DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS = 300;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_ANTI_PASSBACK_RECORD = "access:anti-passback:record:";
    private static final String CACHE_KEY_AREA_CONFIG = "access:area:config:";
    private static final String CACHE_KEY_BLACKLIST = "access:blacklist:user:";
    private static final String CACHE_KEY_MULTI_PERSON_SESSION = "access:multi-person:session:";
    
    /**
     * 缓存过期时间
     */
    private static final Duration CACHE_EXPIRE_RECORD = Duration.ofMinutes(10); // 反潜记录缓存10分钟
    private static final Duration CACHE_EXPIRE_CONFIG = Duration.ofHours(1); // 配置缓存1小时
    private static final Duration CACHE_EXPIRE_BLACKLIST = Duration.ofHours(1); // 黑名单缓存1小时
    private static final Duration CACHE_EXPIRE_SESSION = Duration.ofMinutes(5); // 多人验证会话缓存5分钟
    
    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 构造函数注入依赖
     *
     * @param antiPassbackRecordDao 反潜记录DAO
     * @param userAreaPermissionDao 用户区域权限DAO
     * @param userAreaPermissionManager 用户区域权限管理器
     * @param deviceDao 设备DAO
     * @param gatewayServiceClient 网关服务客户端
     * @param redisTemplate Redis模板
     * @param objectMapper JSON对象映射器
     * @param areaAccessExtDao 区域门禁扩展DAO
     * @param interlockRecordDao 互锁记录DAO
     * @param multiPersonRecordDao 多人验证记录DAO
     * @param verificationProperties 验证配置属性
     */
    public AccessVerificationManager(
            AntiPassbackRecordDao antiPassbackRecordDao,
            UserAreaPermissionDao userAreaPermissionDao,
            UserAreaPermissionManager userAreaPermissionManager,
            DeviceDao deviceDao,
            GatewayServiceClient gatewayServiceClient,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            AreaAccessExtDao areaAccessExtDao,
            InterlockRecordDao interlockRecordDao,
            MultiPersonRecordDao multiPersonRecordDao,
            AccessVerificationProperties verificationProperties) {
        this.antiPassbackRecordDao = antiPassbackRecordDao;
        this.userAreaPermissionDao = userAreaPermissionDao;
        this.userAreaPermissionManager = userAreaPermissionManager;
        this.deviceDao = deviceDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.areaAccessExtDao = areaAccessExtDao;
        this.interlockRecordDao = interlockRecordDao;
        this.multiPersonRecordDao = multiPersonRecordDao;
        this.verificationProperties = verificationProperties;
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
     * <p>
     * 优化实现：
     * - 从AreaAccessExtEntity.extConfig中读取反潜配置
     * - 使用Redis缓存最近的反潜记录
     * - 支持可配置的时间窗口
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID（Long类型，兼容String转Long）
     * @param inOutStatus 进出状态（1=进, 2=出）
     * @param areaId 区域ID（用于读取反潜配置）
     * @return 是否通过验证
     */
    public boolean verifyAntiPassback(Long userId, Long deviceId, Integer inOutStatus, Long areaId) {
        log.debug("[反潜验证] 开始验证: userId={}, deviceId={}, inOutStatus={}, areaId={}", 
                userId, deviceId, inOutStatus, areaId);

        try {
            // 1. 读取反潜配置（从AreaAccessExtEntity.extConfig中读取）
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
                // 检查时间窗口（使用配置的时间窗口，如果未配置则使用默认值）
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
     * 反潜验证（兼容旧版本，不传areaId时使用默认配置）
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param inOutStatus 进出状态
     * @return 是否通过验证
     */
    public boolean verifyAntiPassback(Long userId, Long deviceId, Integer inOutStatus) {
        return verifyAntiPassback(userId, deviceId, inOutStatus, null);
    }

    /**
     * 反潜配置类
     */
    private static class AntiPassbackConfig {
        private Boolean enabled;
        private Integer timeWindow; // 时间窗口（秒）

        public Boolean isEnabled() {
            return enabled != null && enabled;
        }

        public Integer getTimeWindow() {
            return timeWindow;
        }
    }

    /**
     * 从区域扩展配置中读取反潜配置
     *
     * @param areaId 区域ID
     * @return 反潜配置，如果未配置或解析失败则返回null
     */
    private AntiPassbackConfig getAntiPassbackConfig(Long areaId) {
        if (areaId == null) {
            return null;
        }

        try {
            // 1. 从Redis缓存查询配置
            String cacheKey = CACHE_KEY_AREA_CONFIG + areaId;
            Object cachedConfig = redisTemplate.opsForValue().get(cacheKey);
            if (cachedConfig instanceof AntiPassbackConfig) {
                return (AntiPassbackConfig) cachedConfig;
            }

            // 2. 从数据库查询区域扩展配置
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null || areaExt.getExtConfig() == null || areaExt.getExtConfig().trim().isEmpty()) {
                // 未配置，返回默认配置（启用反潜，使用默认时间窗口）
                AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
                defaultConfig.enabled = true;
                defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                return defaultConfig;
            }

            // 3. 解析extConfig JSON
            try {
                Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), Map.class);
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
                redisTemplate.opsForValue().set(cacheKey, config, CACHE_EXPIRE_CONFIG);
                log.debug("[反潜验证] 反潜配置已缓存: areaId={}, enabled={}, timeWindow={}", 
                        areaId, config.enabled, config.timeWindow);

                return config;

            } catch (Exception e) {
                log.warn("[反潜验证] 解析反潜配置失败: areaId={}, extConfig={}, error={}", 
                        areaId, areaExt.getExtConfig(), e.getMessage());
                // 解析失败，返回默认配置
                AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
                defaultConfig.enabled = true;
                defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
                return defaultConfig;
            }

        } catch (Exception e) {
            log.error("[反潜验证] 获取反潜配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            // 异常时返回默认配置
            AntiPassbackConfig defaultConfig = new AntiPassbackConfig();
            defaultConfig.enabled = true;
            defaultConfig.timeWindow = DEFAULT_ANTI_PASSBACK_WINDOW_SECONDS;
            return defaultConfig;
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
        String cacheKey = CACHE_KEY_ANTI_PASSBACK_RECORD + userId + ":" + deviceId;
        Object cachedRecord = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRecord instanceof AntiPassbackRecordEntity) {
            log.debug("[反潜验证] 从缓存获取记录: userId={}, deviceId={}", userId, deviceId);
            return (AntiPassbackRecordEntity) cachedRecord;
        }

        // 2. 从数据库查询
        List<AntiPassbackRecordEntity> records = antiPassbackRecordDao.selectRecentRecords(userId, deviceId, 1);
        AntiPassbackRecordEntity lastRecord = records != null && !records.isEmpty() ? records.get(0) : null;

        // 3. 写入缓存（如果存在记录）
        if (lastRecord != null) {
            redisTemplate.opsForValue().set(cacheKey, lastRecord, CACHE_EXPIRE_RECORD);
            log.debug("[反潜验证] 记录已缓存: userId={}, deviceId={}", userId, deviceId);
        }

        return lastRecord;
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
            String cacheKey = CACHE_KEY_ANTI_PASSBACK_RECORD + userId + ":" + deviceId;
            redisTemplate.opsForValue().set(cacheKey, record, CACHE_EXPIRE_RECORD);
            log.debug("[反潜验证] 记录已缓存: userId={}, deviceId={}", userId, deviceId);

        } catch (Exception e) {
            log.error("[反潜验证] 记录保存异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 互锁验证
     * <p>
     * 检查互锁门禁是否冲突
     * 互锁规则：A门开时B门不能开
     * </p>
     * <p>
     * 实现逻辑：
     * 1. 根据设备ID查询设备所属区域（如果areaId未提供）
     * 2. 从区域扩展配置中解析互锁组配置
     * 3. 查询互锁组中其他设备的锁定状态
     * 4. 如果其他设备已锁定，则拒绝当前设备开门
     * 5. 如果通过验证，则锁定当前设备，解锁其他设备
     * </p>
     *
     * @param deviceId 设备ID（Long类型，兼容String转Long）
     * @param areaId 区域ID（可选，如果为null则根据deviceId查询，提高性能）
     * @return 是否通过验证
     */
    public boolean verifyInterlock(Long deviceId, Long areaId) {
        log.debug("[互锁验证] 开始验证: deviceId={}, areaId={}", deviceId, areaId);

        if (deviceId == null) {
            log.warn("[互锁验证] 设备ID为空");
            return true; // 设备ID为空时允许通过
        }

        try {
            // 1. 如果areaId未提供，根据设备ID查询设备所属区域
            if (areaId == null) {
                areaId = getAreaIdByDeviceId(String.valueOf(deviceId));
                if (areaId == null) {
                    log.warn("[互锁验证] 无法获取设备区域ID: deviceId={}", deviceId);
                    // 如果无法获取区域ID，允许通过（降级策略）
                    return true;
                }
            }

            // 2. 从区域扩展配置中解析互锁组配置
            InterlockConfig interlockConfig = getInterlockConfig(areaId);
            if (interlockConfig == null || !interlockConfig.isEnabled()) {
                // 未配置互锁或互锁未启用，允许通过
                log.debug("[互锁验证] 互锁未配置或未启用: areaId={}", areaId);
                return true;
            }

            // 3. 查找设备所属的互锁组
            Long interlockGroupId = findInterlockGroupId(deviceId, interlockConfig);
            if (interlockGroupId == null) {
                // 设备不属于任何互锁组，允许通过
                log.debug("[互锁验证] 设备不属于任何互锁组: deviceId={}", deviceId);
                return true;
            }

            // 4. 查询互锁组中其他设备的锁定状态（优先使用Redis分布式锁，性能更好）
            List<Long> interlockDeviceIds = getInterlockDeviceIds(interlockConfig, interlockGroupId);
            if (interlockDeviceIds != null && !interlockDeviceIds.isEmpty()) {
                for (Long otherDeviceId : interlockDeviceIds) {
                    if (otherDeviceId.equals(deviceId)) {
                        continue; // 跳过当前设备
                    }

                    // 从Redis查询锁定状态（使用分布式锁键）
                    String lockKey = "interlock:device:" + otherDeviceId;
                    Boolean isLocked = redisTemplate.hasKey(lockKey);
                    if (Boolean.TRUE.equals(isLocked)) {
                        log.warn("[互锁验证] 互锁设备已锁定: deviceId={}, otherDeviceId={}, interlockGroupId={}",
                                deviceId, otherDeviceId, interlockGroupId);
                        return false; // 互锁冲突
                    }
                }
            }

            // 5. 如果通过验证，则锁定当前设备（使用Redis分布式锁），解锁其他设备
            String currentLockKey = "interlock:device:" + deviceId;
            int timeoutSeconds = getInterlockTimeout(interlockConfig);
            redisTemplate.opsForValue().set(currentLockKey, "locked", Duration.ofSeconds(timeoutSeconds));

            // 解锁其他设备（确保互锁组中只有一个设备锁定）
            if (interlockDeviceIds != null && !interlockDeviceIds.isEmpty()) {
                for (Long otherDeviceId : interlockDeviceIds) {
                    if (otherDeviceId.equals(deviceId)) {
                        continue; // 跳过当前设备
                    }
                    String otherLockKey = "interlock:device:" + otherDeviceId;
                    redisTemplate.delete(otherLockKey);
                }
            }

            // 6. 同步更新数据库记录（用于持久化和审计）
            lockDeviceInDatabase(deviceId, interlockGroupId);
            unlockOtherDevicesInGroup(interlockGroupId, deviceId);

            log.debug("[互锁验证] 验证通过: deviceId={}, interlockGroupId={}", deviceId, interlockGroupId);
            return true;

        } catch (Exception e) {
            log.error("[互锁验证] 验证异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            // 异常时允许通过，避免影响正常通行
            return true;
        }
    }

    /**
     * 从区域扩展配置中读取互锁配置
     *
     * @param areaId 区域ID
     * @return 互锁配置，如果未配置或解析失败则返回null
     */
    private InterlockConfig getInterlockConfig(Long areaId) {
        if (areaId == null) {
            return null;
        }

        try {
            // 从数据库查询区域扩展配置
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null || areaExt.getExtConfig() == null || areaExt.getExtConfig().trim().isEmpty()) {
                // 未配置互锁
                return null;
            }

            // 解析extConfig JSON
            Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), Map.class);
            InterlockConfig config = new InterlockConfig();

            // 读取互锁配置
            if (configMap.containsKey("interlock")) {
                Object interlockObj = configMap.get("interlock");
                if (interlockObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> interlockMap = (Map<String, Object>) interlockObj;
                    config.enabled = (Boolean) interlockMap.getOrDefault("enabled", false);
                    if (interlockMap.containsKey("interlockGroups")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> groups = (List<Map<String, Object>>) interlockMap.get("interlockGroups");
                        config.interlockGroups = groups;
                    }
                    if (interlockMap.containsKey("timeout")) {
                        Object timeoutObj = interlockMap.get("timeout");
                        if (timeoutObj instanceof Number) {
                            config.timeout = ((Number) timeoutObj).intValue();
                        }
                    }
                }
            }

            return config.enabled ? config : null;

        } catch (Exception e) {
            log.error("[互锁验证] 解析互锁配置失败: areaId={}, error={}", areaId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查找设备所属的互锁组ID
     *
     * @param deviceId 设备ID
     * @param interlockConfig 互锁配置
     * @return 互锁组ID，如果设备不属于任何互锁组则返回null
     */
    private Long findInterlockGroupId(Long deviceId, InterlockConfig interlockConfig) {
        if (interlockConfig.getInterlockGroups() == null || interlockConfig.getInterlockGroups().isEmpty()) {
            return null;
        }

        for (Map<String, Object> group : interlockConfig.getInterlockGroups()) {
            Object groupIdObj = group.get("groupId");
            Object deviceIdsObj = group.get("deviceIds");

            if (groupIdObj != null && deviceIdsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> deviceIds = (List<Object>) deviceIdsObj;
                for (Object deviceIdObj : deviceIds) {
                    Long deviceIdInGroup = null;
                    if (deviceIdObj instanceof Number) {
                        deviceIdInGroup = ((Number) deviceIdObj).longValue();
                    } else if (deviceIdObj instanceof String) {
                        try {
                            deviceIdInGroup = Long.parseLong((String) deviceIdObj);
                        } catch (NumberFormatException e) {
                            log.warn("[互锁验证] 设备ID格式错误: deviceId={}", deviceIdObj);
                            continue;
                        }
                    }

                    if (deviceId.equals(deviceIdInGroup)) {
                        // 找到设备所属的互锁组
                        Long groupId = null;
                        if (groupIdObj instanceof Number) {
                            groupId = ((Number) groupIdObj).longValue();
                        } else if (groupIdObj instanceof String) {
                            try {
                                groupId = Long.parseLong((String) groupIdObj);
                            } catch (NumberFormatException e) {
                                log.warn("[互锁验证] 互锁组ID格式错误: groupId={}", groupIdObj);
                                return null;
                            }
                        }
                        return groupId;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 锁定设备
     *
     * @param deviceId 设备ID
     * @param interlockGroupId 互锁组ID
     */
    private void lockDevice(Long deviceId, Long interlockGroupId) {
        try {
            // 查询是否已有锁定记录
            InterlockRecordEntity existingRecord = interlockRecordDao.selectByDeviceId(deviceId);

            if (existingRecord != null) {
                // 更新现有记录
                existingRecord.setLockStatus(InterlockRecordEntity.LockStatus.LOCKED);
                existingRecord.setLockTime(LocalDateTime.now());
                existingRecord.setUnlockTime(null);
                existingRecord.setLockDuration(null);
                interlockRecordDao.updateById(existingRecord);
            } else {
                // 创建新记录
                InterlockRecordEntity record = new InterlockRecordEntity();
                record.setDeviceId(deviceId);
                record.setInterlockGroupId(interlockGroupId);
                record.setLockStatus(InterlockRecordEntity.LockStatus.LOCKED);
                record.setLockTime(LocalDateTime.now());
                interlockRecordDao.insert(record);
            }

            log.debug("[互锁验证] 设备已锁定: deviceId={}, interlockGroupId={}", deviceId, interlockGroupId);

        } catch (Exception e) {
            log.error("[互锁验证] 锁定设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    /**
     * 解锁互锁组中的其他设备
     *
     * @param interlockGroupId 互锁组ID
     * @param excludeDeviceId 排除的设备ID（当前设备，不需要解锁）
     */
    private void unlockOtherDevicesInGroup(Long interlockGroupId, Long excludeDeviceId) {
        try {
            int unlockedCount = interlockRecordDao.unlockDevicesInGroup(interlockGroupId, excludeDeviceId);
            if (unlockedCount > 0) {
                log.debug("[互锁验证] 解锁互锁组中的其他设备: interlockGroupId={}, unlockedCount={}",
                        interlockGroupId, unlockedCount);
            }
        } catch (Exception e) {
            log.error("[互锁验证] 解锁其他设备失败: interlockGroupId={}, error={}",
                    interlockGroupId, e.getMessage(), e);
        }
    }

    /**
     * 清理过期的锁定记录
     *
     * @param lockedDevices 已锁定的设备列表
     */
    private void cleanExpiredLocks(List<InterlockRecordEntity> lockedDevices) {
        if (lockedDevices == null || lockedDevices.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (InterlockRecordEntity record : lockedDevices) {
            if (record.getLockTime() != null) {
                long secondsBetween = ChronoUnit.SECONDS.between(record.getLockTime(), now);
                int timeoutSeconds = 60; // 默认60秒
                if (secondsBetween > timeoutSeconds) {
                    // 锁定时间超过超时时间，自动解锁
                    record.setLockStatus(InterlockRecordEntity.LockStatus.UNLOCKED);
                    record.setUnlockTime(now);
                    record.setLockDuration((int) secondsBetween);
                    interlockRecordDao.updateById(record);
                    log.debug("[互锁验证] 自动解锁过期锁定: deviceId={}, lockDuration={}秒",
                            record.getDeviceId(), secondsBetween);
                }
            }
        }
    }

    /**
     * 互锁配置内部类
     */
    private static class InterlockConfig {
        private Boolean enabled;
        private List<Map<String, Object>> interlockGroups;
        private Integer timeout; // 超时时间（秒）

        public Boolean isEnabled() {
            return enabled != null && enabled;
        }

        public List<Map<String, Object>> getInterlockGroups() {
            return interlockGroups;
        }

        public Integer getTimeout() {
            return timeout;
        }
    }

    /**
     * 获取互锁组中的设备ID列表
     *
     * @param interlockConfig 互锁配置
     * @param interlockGroupId 互锁组ID
     * @return 设备ID列表
     */
    private List<Long> getInterlockDeviceIds(InterlockConfig interlockConfig, Long interlockGroupId) {
        if (interlockConfig.getInterlockGroups() == null || interlockConfig.getInterlockGroups().isEmpty()) {
            return null;
        }

        for (Map<String, Object> group : interlockConfig.getInterlockGroups()) {
            Object groupIdObj = group.get("groupId");
            Object deviceIdsObj = group.get("deviceIds");

            if (groupIdObj != null && deviceIdsObj instanceof List) {
                Long configGroupId = null;
                if (groupIdObj instanceof Number) {
                    configGroupId = ((Number) groupIdObj).longValue();
                } else if (groupIdObj instanceof String) {
                    try {
                        configGroupId = Long.parseLong((String) groupIdObj);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if (interlockGroupId.equals(configGroupId)) {
                    @SuppressWarnings("unchecked")
                    List<Object> deviceIds = (List<Object>) deviceIdsObj;
                    return deviceIds.stream()
                            .filter(id -> id instanceof Number || id instanceof String)
                            .map(id -> {
                                if (id instanceof Number) {
                                    return ((Number) id).longValue();
                                } else {
                                    try {
                                        return Long.parseLong((String) id);
                                    } catch (NumberFormatException e) {
                                        return null;
                                    }
                                }
                            })
                            .filter(id -> id != null)
                            .collect(java.util.stream.Collectors.toList());
                }
            }
        }

        return null;
    }

    /**
     * 获取互锁超时时间（秒）
     *
     * @param interlockConfig 互锁配置
     * @return 超时时间（秒），默认60秒
     */
    private int getInterlockTimeout(InterlockConfig interlockConfig) {
        return interlockConfig.getTimeout() != null ? interlockConfig.getTimeout() : 60;
    }

    /**
     * 锁定设备到数据库（用于持久化和审计）
     *
     * @param deviceId 设备ID
     * @param interlockGroupId 互锁组ID
     */
    private void lockDeviceInDatabase(Long deviceId, Long interlockGroupId) {
        try {
            // 查询是否已有锁定记录
            InterlockRecordEntity existingRecord = interlockRecordDao.selectByDeviceId(deviceId);

            if (existingRecord != null) {
                // 更新现有记录
                existingRecord.setLockStatus(InterlockRecordEntity.LockStatus.LOCKED);
                existingRecord.setLockTime(LocalDateTime.now());
                existingRecord.setUnlockTime(null);
                existingRecord.setLockDuration(null);
                interlockRecordDao.updateById(existingRecord);
            } else {
                // 创建新记录
                InterlockRecordEntity record = new InterlockRecordEntity();
                record.setDeviceId(deviceId);
                record.setInterlockGroupId(interlockGroupId);
                record.setLockStatus(InterlockRecordEntity.LockStatus.LOCKED);
                record.setLockTime(LocalDateTime.now());
                interlockRecordDao.insert(record);
            }

            log.debug("[互锁验证] 设备已锁定（数据库）: deviceId={}, interlockGroupId={}", deviceId, interlockGroupId);

        } catch (Exception e) {
            log.error("[互锁验证] 锁定设备到数据库失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    /**
     * 时间段验证
     * <p>
     * 检查用户是否在有效时间段内
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID（Long类型，兼容String转Long）
     * @param verifyTime 验证时间（如果为null，使用当前时间）
     * @param areaId 区域ID（可选，如果为null则根据deviceId查询，提高性能）
     * @return 是否通过验证
     */
    public boolean verifyTimePeriod(Long userId, Long deviceId, LocalDateTime verifyTime, Long areaId) {
        log.debug("[时间段验证] 开始验证: userId={}, deviceId={}, verifyTime={}, areaId={}", userId, deviceId, verifyTime, areaId);

        if (verifyTime == null) {
            verifyTime = LocalDateTime.now();
        }

        try {
            // 1. 如果areaId未提供，通过设备ID获取区域ID（将Long转换为String）
            if (areaId == null) {
                String deviceIdStr = deviceId != null ? String.valueOf(deviceId) : null;
                areaId = getAreaIdByDeviceId(deviceIdStr);
                if (areaId == null) {
                    log.warn("[时间段验证] 无法获取设备区域ID: deviceId={}", deviceId);
                    // 如果无法获取区域ID，允许通过（降级策略）
                    return true;
                }
            }

            // 2. 使用UserAreaPermissionManager验证权限
            boolean hasPermission = userAreaPermissionDao.hasPermission(userId, areaId);

            if (!hasPermission) {
                log.warn("[时间段验证] 用户无区域权限: userId={}, deviceId={}", userId, deviceId);
                return false;
            }

            // 3. 检查时间段（如果权限配置了时间段）
            UserAreaPermissionEntity permission = userAreaPermissionManager.getValidPermission(userId, areaId);

            if (permission != null && permission.getAccessTimes() != null && !permission.getAccessTimes().trim().isEmpty()) {
                // 解析accessTimes JSON，检查当前时间是否在允许的时间段内
                boolean isInTimePeriod = checkTimePeriod(permission.getAccessTimes(), verifyTime);
                if (!isInTimePeriod) {
                    log.warn("[时间段验证] 不在允许的时间段内: userId={}, verifyTime={}, accessTimes={}",
                            userId, verifyTime, permission.getAccessTimes());
                    return false;
                }
            }

            log.debug("[时间段验证] 验证通过: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("[时间段验证] 验证异常: userId={}, error={}", userId, e.getMessage(), e);
            // 异常时允许通过，避免影响正常通行
            return true;
        }
    }

    /**
     * 根据设备ID获取区域ID
     * <p>
     * 完善实现：通过DeviceDao直接查询设备信息，获取区域ID
     * </p>
     *
     * @param deviceId 设备ID（String类型）
     * @return 区域ID，如果设备不存在或查询失败则返回null
     */
    public Long getAreaIdByDeviceId(String deviceId) {
        log.debug("[设备查询] 根据设备ID获取区域ID: deviceId={}", deviceId);

        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.warn("[设备查询] 设备ID为空");
            return null;
        }

        try {
            // 方案1：优先使用DeviceDao直接查询（性能更好，无需跨服务调用）
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null && device.getAreaId() != null) {
                log.debug("[设备查询] 通过DeviceDao查询成功: deviceId={}, areaId={}", deviceId, device.getAreaId());
                return device.getAreaId();
            }

            // 方案2：如果DeviceDao查询失败，尝试通过GatewayServiceClient查询
            log.debug("[设备查询] DeviceDao查询失败，尝试通过GatewayServiceClient查询: deviceId={}", deviceId);
            ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area-device/device/" + deviceId,
                    HttpMethod.GET,
                    null,
                    DeviceEntity.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                DeviceEntity deviceFromService = response.getData();
                if (deviceFromService.getAreaId() != null) {
                    log.debug("[设备查询] 通过GatewayServiceClient查询成功: deviceId={}, areaId={}",
                            deviceId, deviceFromService.getAreaId());
                    return deviceFromService.getAreaId();
                }
            }

            log.warn("[设备查询] 设备不存在或区域ID为空: deviceId={}", deviceId);
            return null;

        } catch (Exception e) {
            log.error("[设备查询] 查询设备区域ID异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据设备序列号获取设备ID和区域ID
     * <p>
     * 完善实现：通过DeviceDao查询设备信息
     * </p>
     *
     * @param serialNumber 设备序列号
     * @return DeviceEntity对象，包含deviceId和areaId，如果设备不存在则返回null
     */
    public DeviceEntity getDeviceBySerialNumber(String serialNumber) {
        log.debug("[设备查询] 根据序列号查询设备: serialNumber={}", serialNumber);

        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            log.warn("[设备查询] 设备序列号为空");
            return null;
        }

        try {
            // 方案1：优先使用DeviceDao直接查询（性能更好）
            DeviceEntity device = deviceDao.selectBySerialNumber(serialNumber);
            if (device != null) {
                log.debug("[设备查询] 通过DeviceDao查询成功: serialNumber={}, deviceId={}, areaId={}",
                        serialNumber, device.getDeviceId(), device.getAreaId());
                return device;
            }

            // 方案2：如果DeviceDao查询失败，尝试通过设备编码查询（某些设备序列号可能等于设备编码）
            log.debug("[设备查询] 通过序列号查询失败，尝试通过设备编码查询: serialNumber={}", serialNumber);
            device = deviceDao.selectByDeviceCode(serialNumber);
            if (device != null) {
                log.debug("[设备查询] 通过设备编码查询成功: serialNumber={}, deviceId={}, areaId={}",
                        serialNumber, device.getDeviceId(), device.getAreaId());
                return device;
            }

            // 方案3：如果本地查询都失败，尝试通过GatewayServiceClient查询
            log.debug("[设备查询] 本地查询失败，尝试通过GatewayServiceClient查询: serialNumber={}", serialNumber);
            ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area-device/device/code/" + serialNumber,
                    HttpMethod.GET,
                    null,
                    DeviceEntity.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                DeviceEntity deviceFromService = response.getData();
                log.debug("[设备查询] 通过GatewayServiceClient查询成功: serialNumber={}, deviceId={}, areaId={}",
                        serialNumber, deviceFromService.getDeviceId(), deviceFromService.getAreaId());
                return deviceFromService;
            }

            log.warn("[设备查询] 设备不存在: serialNumber={}", serialNumber);
            return null;

        } catch (Exception e) {
            log.error("[设备查询] 查询设备异常: serialNumber={}, error={}", serialNumber, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 黑名单验证
     * <p>
     * 检查用户是否在黑名单中
     * 判断标准：
     * 1. 用户状态为禁用（status != 1）
     * 2. 用户账户被锁定（accountLocked == 1）
     * 3. 用户账户已过期（accountExpireTime < 当前时间）
     * </p>
     *
     * @param userId 用户ID
     * @return 是否在黑名单中（true=在黑名单，false=不在黑名单）
     */
    public boolean isBlacklisted(Long userId) {
        log.debug("[黑名单验证] 检查用户: userId={}", userId);

        if (userId == null) {
            log.warn("[黑名单验证] 用户ID为空");
            return false; // 用户ID为空时，不在黑名单
        }

        try {
            // 1. 从Redis缓存查询黑名单状态
            String cacheKey = CACHE_KEY_BLACKLIST + userId;
            Object cachedStatus = redisTemplate.opsForValue().get(cacheKey);
            if (cachedStatus instanceof Boolean) {
                boolean isBlacklisted = (Boolean) cachedStatus;
                log.debug("[黑名单验证] 从缓存获取黑名单状态: userId={}, isBlacklisted={}", userId, isBlacklisted);
                return isBlacklisted;
            }

            // 2. 通过GatewayServiceClient调用common-service查询用户信息
            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/" + userId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {}
            );

            boolean isBlacklisted = false;
            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> userData = response.getData();
                
                // 检查用户状态
                Object statusObj = userData.get("status");
                if (statusObj != null) {
                    Integer status = statusObj instanceof Number ? ((Number) statusObj).intValue() : null;
                    if (status != null && status != 1) {
                        // 用户状态为禁用
                        log.warn("[黑名单验证] 用户已被禁用: userId={}, status={}", userId, status);
                        isBlacklisted = true;
                    }
                }

                // 检查账户锁定状态
                if (!isBlacklisted) {
                    Object accountLockedObj = userData.get("accountLocked");
                    if (accountLockedObj != null) {
                        Integer accountLocked = accountLockedObj instanceof Number ? ((Number) accountLockedObj).intValue() : null;
                        if (accountLocked != null && accountLocked == 1) {
                            // 用户账户被锁定
                            log.warn("[黑名单验证] 用户账户已被锁定: userId={}", userId);
                            isBlacklisted = true;
                        }
                    }
                }

                // 检查账户过期时间
                if (!isBlacklisted) {
                    Object accountExpireTimeObj = userData.get("accountExpireTime");
                    if (accountExpireTimeObj != null && accountExpireTimeObj instanceof String) {
                        try {
                            LocalDateTime expireTime = LocalDateTime.parse((String) accountExpireTimeObj);
                            if (expireTime.isBefore(LocalDateTime.now())) {
                                // 用户账户已过期
                                log.warn("[黑名单验证] 用户账户已过期: userId={}, expireTime={}", userId, expireTime);
                                isBlacklisted = true;
                            }
                        } catch (Exception e) {
                            log.warn("[黑名单验证] 解析账户过期时间失败: userId={}, expireTime={}", userId, accountExpireTimeObj);
                        }
                    }
                }

                // 3. 写入Redis缓存
                redisTemplate.opsForValue().set(cacheKey, isBlacklisted, CACHE_EXPIRE_BLACKLIST);
                log.debug("[黑名单验证] 黑名单状态已缓存: userId={}, isBlacklisted={}", userId, isBlacklisted);

                return isBlacklisted;
            }

            // 如果查询失败，允许通过（降级策略）
            log.warn("[黑名单验证] 查询用户信息失败: userId={}", userId);
            return false;

        } catch (Exception e) {
            log.error("[黑名单验证] 验证异常: userId={}, error={}", userId, e.getMessage(), e);
            // 异常时返回false（不在黑名单），避免影响正常通行
            return false;
        }
    }

    /**
     * 检查是否需要多人验证
     * <p>
     * 从区域扩展配置中读取多人验证配置
     * </p>
     *
     * @param areaId 区域ID
     * @return 是否需要多人验证
     */
    public boolean isMultiPersonRequired(Long areaId) {
        log.debug("[多人验证] 检查配置: areaId={}", areaId);

        if (areaId == null) {
            return false;
        }

        try {
            // 1. 查询AreaAccessExtEntity
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null || areaExt.getExtConfig() == null || areaExt.getExtConfig().trim().isEmpty()) {
                // 未配置多人验证
                return false;
            }

            // 2. 解析ext_config JSON
            Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), Map.class);

            // 3. 检查是否配置了多人验证（multiPersonRequired字段）
            if (configMap.containsKey("multiPerson")) {
                Object multiPersonObj = configMap.get("multiPerson");
                if (multiPersonObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> multiPersonMap = (Map<String, Object>) multiPersonObj;
                    Boolean required = (Boolean) multiPersonMap.getOrDefault("required", false);
                    return required != null && required;
                } else if (multiPersonObj instanceof Boolean) {
                    return (Boolean) multiPersonObj;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("[多人验证] 检查配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 多人验证
     * <p>
     * 验证多人验证规则
     * 多人验证流程：
     * 1. 创建或获取验证会话
     * 2. 记录当前用户验证
     * 3. 检查是否达到所需人数
     * 4. 如果未达到则返回等待状态
     * 5. 如果达到则返回成功
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    public VerificationResult verifyMultiPerson(AccessVerificationRequest request) {
        log.info("[多人验证] 开始验证: userId={}, areaId={}, deviceId={}",
                request.getUserId(), request.getAreaId(), request.getDeviceId());

        if (request.getAreaId() == null || request.getDeviceId() == null || request.getUserId() == null) {
            log.warn("[多人验证] 参数不完整: areaId={}, deviceId={}, userId={}",
                    request.getAreaId(), request.getDeviceId(), request.getUserId());
            return VerificationResult.failed("MULTI_PERSON_ERROR", "参数不完整");
        }

        try {
            // 1. 获取多人验证配置（所需人数）
            Integer requiredCount = getMultiPersonRequiredCount(request.getAreaId());
            if (requiredCount == null || requiredCount <= 1) {
                // 未配置多人验证或只需要1人，直接返回成功
                return VerificationResult.success("验证通过", null, "backend");
            }

            // 2. 查找或创建验证会话（优先使用Redis缓存）
            MultiPersonRecordEntity session = findOrCreateSessionWithCache(
                    request.getAreaId(), request.getDeviceId(), requiredCount);

            // 3. 检查会话是否已过期
            if (session.getExpireTime() != null && LocalDateTime.now().isAfter(session.getExpireTime())) {
                // 会话已过期，更新状态为超时
                session.setStatus(MultiPersonRecordEntity.Status.TIMEOUT);
                multiPersonRecordDao.updateById(session);
                // 清除Redis缓存
                clearSessionCache(session.getVerificationSessionId());
                log.warn("[多人验证] 会话已过期: sessionId={}", session.getVerificationSessionId());
                return VerificationResult.failed("MULTI_PERSON_TIMEOUT", "验证会话已过期，请重新验证");
            }

            // 4. 检查用户是否已经验证过
            List<Long> userIds = parseUserIds(session.getUserIds());
            if (userIds.contains(request.getUserId())) {
                // 用户已经验证过，返回等待状态
                log.debug("[多人验证] 用户已验证: userId={}, sessionId={}", request.getUserId(), session.getVerificationSessionId());
                return VerificationResult.waiting("MULTI_PERSON_WAITING",
                        String.format("等待其他人员验证，当前已验证人数：%d/%d", session.getCurrentCount(), requiredCount));
            }

            // 5. 添加用户到验证列表
            userIds.add(request.getUserId());
            session.setUserIds(objectMapper.writeValueAsString(userIds));
            session.setCurrentCount(userIds.size());
            
            // 6. 更新数据库和Redis缓存
            multiPersonRecordDao.updateById(session);
            updateSessionCache(session);

            // 7. 检查是否达到所需人数
            if (session.getCurrentCount() >= requiredCount) {
                // 达到所需人数，完成验证
                session.setStatus(MultiPersonRecordEntity.Status.COMPLETED);
                session.setCompleteTime(LocalDateTime.now());
                multiPersonRecordDao.updateById(session);
                // 清除Redis缓存（验证完成）
                clearSessionCache(session.getVerificationSessionId());
                log.info("[多人验证] 验证完成: sessionId={}, requiredCount={}, currentCount={}",
                        session.getVerificationSessionId(), requiredCount, session.getCurrentCount());
                return VerificationResult.success("多人验证通过", null, "backend");
            } else {
                // 未达到所需人数，返回等待状态
                log.info("[多人验证] 等待其他人员: sessionId={}, currentCount={}/{}, userId={}",
                        session.getVerificationSessionId(), session.getCurrentCount(), requiredCount, request.getUserId());
                return VerificationResult.waiting("MULTI_PERSON_WAITING",
                        String.format("等待其他人员验证，当前已验证人数：%d/%d", session.getCurrentCount(), requiredCount));
            }

        } catch (Exception e) {
            log.error("[多人验证] 验证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return VerificationResult.failed("MULTI_PERSON_ERROR", "多人验证异常");
        }
    }

    /**
     * 获取多人验证所需人数
     *
     * @param areaId 区域ID
     * @return 所需人数，如果未配置则返回null
     */
    private Integer getMultiPersonRequiredCount(Long areaId) {
        try {
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null || areaExt.getExtConfig() == null || areaExt.getExtConfig().trim().isEmpty()) {
                return null;
            }

            Map<String, Object> configMap = objectMapper.readValue(areaExt.getExtConfig(), Map.class);
            if (configMap.containsKey("multiPerson")) {
                Object multiPersonObj = configMap.get("multiPerson");
                if (multiPersonObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> multiPersonMap = (Map<String, Object>) multiPersonObj;
                    Object requiredCountObj = multiPersonMap.get("requiredCount");
                    if (requiredCountObj instanceof Number) {
                        return ((Number) requiredCountObj).intValue();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("[多人验证] 获取所需人数失败: areaId={}, error={}", areaId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查找或创建验证会话（带Redis缓存）
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param requiredCount 所需人数
     * @return 验证会话
     */
    private MultiPersonRecordEntity findOrCreateSessionWithCache(Long areaId, Long deviceId, Integer requiredCount) {
        // 1. 从Redis缓存查询活跃会话
        String cacheKey = CACHE_KEY_MULTI_PERSON_SESSION + areaId + ":" + deviceId;
        Object cachedSession = redisTemplate.opsForValue().get(cacheKey);
        if (cachedSession instanceof MultiPersonRecordEntity) {
            MultiPersonRecordEntity session = (MultiPersonRecordEntity) cachedSession;
            // 检查会话是否仍然有效
            if (session.getExpireTime() == null || LocalDateTime.now().isBefore(session.getExpireTime())) {
                log.debug("[多人验证] 从缓存获取会话: sessionId={}", session.getVerificationSessionId());
                return session;
            } else {
                // 缓存中的会话已过期，清除缓存
                redisTemplate.delete(cacheKey);
            }
        }

        // 2. 从数据库查找活跃的验证会话
        List<MultiPersonRecordEntity> activeSessions = multiPersonRecordDao.selectActiveSessions(areaId, deviceId);

        if (activeSessions != null && !activeSessions.isEmpty()) {
            // 使用第一个活跃会话
            MultiPersonRecordEntity session = activeSessions.get(0);
            // 写入Redis缓存
            updateSessionCache(session);
            log.debug("[多人验证] 使用现有会话: sessionId={}", session.getVerificationSessionId());
            return session;
        }

        // 3. 创建新会话
        MultiPersonRecordEntity newSession = new MultiPersonRecordEntity();
        newSession.setVerificationSessionId(UUID.randomUUID().toString());
        newSession.setAreaId(areaId);
        newSession.setDeviceId(deviceId);
        newSession.setRequiredCount(requiredCount);
        newSession.setCurrentCount(0);
        newSession.setUserIds("[]");
        newSession.setStatus(MultiPersonRecordEntity.Status.WAITING);
        newSession.setStartTime(LocalDateTime.now());
        newSession.setExpireTime(LocalDateTime.now().plusSeconds(getMultiPersonTimeout()));

        // 保存到数据库
        multiPersonRecordDao.insert(newSession);
        // 写入Redis缓存
        updateSessionCache(newSession);
        
        log.info("[多人验证] 创建新会话: sessionId={}, areaId={}, deviceId={}, requiredCount={}",
                newSession.getVerificationSessionId(), areaId, deviceId, requiredCount);

        return newSession;
    }

    /**
     * 更新会话缓存
     *
     * @param session 验证会话
     */
    private void updateSessionCache(MultiPersonRecordEntity session) {
        try {
            String cacheKey = CACHE_KEY_MULTI_PERSON_SESSION + session.getAreaId() + ":" + session.getDeviceId();
            redisTemplate.opsForValue().set(cacheKey, session, CACHE_EXPIRE_SESSION);
            log.debug("[多人验证] 会话已缓存: sessionId={}", session.getVerificationSessionId());
        } catch (Exception e) {
            log.warn("[多人验证] 更新会话缓存失败: sessionId={}, error={}", 
                    session.getVerificationSessionId(), e.getMessage());
        }
    }

    /**
     * 清除会话缓存
     *
     * @param sessionId 会话ID
     */
    private void clearSessionCache(String sessionId) {
        try {
            // 注意：这里需要根据sessionId查找对应的areaId和deviceId
            // 为了简化，我们使用通配符删除（实际生产环境建议使用更精确的键）
            // 或者从数据库查询会话信息
            MultiPersonRecordEntity session = multiPersonRecordDao.selectBySessionId(sessionId);
            if (session != null) {
                String cacheKey = CACHE_KEY_MULTI_PERSON_SESSION + session.getAreaId() + ":" + session.getDeviceId();
                redisTemplate.delete(cacheKey);
                log.debug("[多人验证] 会话缓存已清除: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.warn("[多人验证] 清除会话缓存失败: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    /**
     * 解析用户ID列表JSON
     *
     * @param userIdsJson 用户ID列表JSON字符串
     * @return 用户ID列表
     */
    private List<Long> parseUserIds(String userIdsJson) {
        if (userIdsJson == null || userIdsJson.trim().isEmpty() || "[]".equals(userIdsJson.trim())) {
            return new ArrayList<>();
        }

        try {
            List<Object> userIdObjs = objectMapper.readValue(userIdsJson, List.class);
            List<Long> userIds = new ArrayList<>();
            for (Object userIdObj : userIdObjs) {
                if (userIdObj instanceof Number) {
                    userIds.add(((Number) userIdObj).longValue());
                } else if (userIdObj instanceof String) {
                    try {
                        userIds.add(Long.parseLong((String) userIdObj));
                    } catch (NumberFormatException e) {
                        log.warn("[多人验证] 用户ID格式错误: userId={}", userIdObj);
                    }
                }
            }
            return userIds;
        } catch (Exception e) {
            log.error("[多人验证] 解析用户ID列表失败: userIdsJson={}, error={}", userIdsJson, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查时间是否在允许的时间段内
     * <p>
     * 解析accessTimes JSON字段，检查当前时间是否在允许的时间段内
     * JSON格式：[{"startTime": "08:00", "endTime": "18:00", "days": [1,2,3,4,5]}]
     * days数组：1=周一，2=周二，...，7=周日
     * </p>
     *
     * @param accessTimesJson accessTimes JSON字符串
     * @param verifyTime 验证时间（如果为null，使用当前时间）
     * @return 是否在允许的时间段内
     */
    private boolean checkTimePeriod(String accessTimesJson, LocalDateTime verifyTime) {
        if (accessTimesJson == null || accessTimesJson.trim().isEmpty()) {
            // 如果没有配置时间段，允许通过
            return true;
        }

        if (verifyTime == null) {
            verifyTime = LocalDateTime.now();
        }

        try {
            // 解析JSON数组
            List<TimePeriodConfig> timePeriods = objectMapper.readValue(
                    accessTimesJson,
                    new TypeReference<List<TimePeriodConfig>>() {}
            );

            if (timePeriods == null || timePeriods.isEmpty()) {
                // 如果没有配置时间段，允许通过
                return true;
            }

            // 获取当前时间和星期几
            LocalTime currentTime = verifyTime.toLocalTime();
            DayOfWeek currentDayOfWeek = verifyTime.getDayOfWeek();
            int currentDayValue = currentDayOfWeek.getValue(); // 1=周一，7=周日

            // 检查是否在任何一个时间段内
            for (TimePeriodConfig period : timePeriods) {
                // 检查星期几
                if (period.getDays() != null && !period.getDays().isEmpty()) {
                    if (!period.getDays().contains(currentDayValue)) {
                        // 当前星期几不在允许的范围内
                        continue;
                    }
                }

                // 检查时间范围
                LocalTime startTime = LocalTime.parse(period.getStartTime(), TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(period.getEndTime(), TIME_FORMATTER);

                // 处理跨天的情况（如22:00-06:00）
                if (startTime.isBefore(endTime)) {
                    // 正常情况：08:00-18:00
                    if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                        log.debug("[时间段验证] 在允许的时间段内: period={}, currentTime={}", period, currentTime);
                        return true;
                    }
                } else {
                    // 跨天情况：22:00-06:00
                    if (currentTime.isAfter(startTime) || currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
                        log.debug("[时间段验证] 在允许的时间段内（跨天）: period={}, currentTime={}", period, currentTime);
                        return true;
                    }
                }
            }

            // 不在任何时间段内
            log.debug("[时间段验证] 不在允许的时间段内: currentTime={}, currentDay={}, periods={}",
                    currentTime, currentDayValue, timePeriods);
            return false;

        } catch (Exception e) {
            log.error("[时间段验证] 解析时间段JSON失败: accessTimes={}, error={}", accessTimesJson, e.getMessage(), e);
            // 解析失败时允许通过，避免影响正常通行
            return true;
        }
    }

    /**
     * 时间段配置内部类
     * <p>
     * 用于解析accessTimes JSON字段
     * </p>
     */
    private static class TimePeriodConfig {
        /**
         * 开始时间（格式：HH:mm，如08:00）
         */
        private String startTime;

        /**
         * 结束时间（格式：HH:mm，如18:00）
         */
        private String endTime;

        /**
         * 允许的星期几（1=周一，2=周二，...，7=周日）
         * 如果为空，表示每天都允许
         */
        private List<Integer> days;

        // Getters and Setters
        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public List<Integer> getDays() {
            return days;
        }

        public void setDays(List<Integer> days) {
            this.days = days;
        }

        @Override
        public String toString() {
            return String.format("TimePeriodConfig{startTime='%s', endTime='%s', days=%s}",
                    startTime, endTime, days);
        }
    }

    /**
     * 获取多人验证超时时间（秒）
     * <p>
     * 从配置中读取，如果未配置则使用默认值60秒
     * </p>
     *
     * @return 超时时间（秒）
     */
    private int getMultiPersonTimeout() {
        if (verificationProperties != null && verificationProperties.getBackend() != null) {
            Integer timeout = verificationProperties.getBackend().getMultiPersonTimeout();
            if (timeout != null && timeout > 0) {
                return timeout;
            }
        }
        // 默认60秒
        return 60;
    }
}
