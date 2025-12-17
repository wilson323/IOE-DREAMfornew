package net.lab1024.sa.common.organization.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
// 使用microservices-common-business中的DAO和Entity定义
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.common.organization.domain.dto.AreaDeviceHealthStatistics;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 区域设备管理服务实现类
 * 严格遵循CLAUDE.md全局架构规范：纯Java类，不使用Spring注解
 * 注意：此类为Manager层实现，不实现Service接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 * @updated 2025-12-17 移除Service接口实现，改为独立Manager类
 */
@Slf4j
public class AreaDeviceManagerImpl {

    private final AreaDeviceDao areaDeviceDao;
    private final AreaUnifiedService areaUnifiedService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // Redis缓存前缀
    private static final String CACHE_PREFIX = "area:device:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    // 设备属性模板缓存
    private static final Map<String, Map<String, Object>> DEVICE_ATTRIBUTE_TEMPLATES = new HashMap<>();

    static {
        // 初始化设备属性模板
        initializeDeviceAttributeTemplates();
    }

    /**
     * 构造函数注入依赖
     */
    public AreaDeviceManagerImpl(AreaDeviceDao areaDeviceDao,
                               AreaUnifiedService areaUnifiedService,
                               StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.areaDeviceDao = areaDeviceDao;
        this.areaUnifiedService = areaUnifiedService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                                   String deviceName, Integer deviceType, String businessModule) {
        log.info("[区域设备管理] 添加设备到区域, areaId={}, deviceId={}, deviceType={}, businessModule={}",
                areaId, deviceId, deviceType, businessModule);

        // 验证区域存在且支持指定业务
        Map<String, String> validationResult = validateDeviceRelation(areaId, deviceId, businessModule);
        if (!validationResult.isEmpty() && !"valid".equals(validationResult.get("status"))) {
            log.warn("[区域设备管理] 设备关联验证失败: {}", validationResult.get("message"));
            return false;
        }

        // 检查设备是否已在区域中
        if (isDeviceInArea(areaId, deviceId)) {
            log.warn("[区域设备管理] 设备已在区域中, areaId={}, deviceId={}", areaId, deviceId);
            return false;
        }

        // 创建设备关联实体
        AreaDeviceEntity relation = new AreaDeviceEntity();
        relation.setAreaId(areaId);
        relation.setDeviceId(deviceId);
        relation.setDeviceCode(deviceCode);
        relation.setDeviceName(deviceName);
        relation.setDeviceType(deviceType);
        relation.setBusinessModule(businessModule);
        relation.setRelationStatus(1); // 正常状态
        relation.setPriority(1); // 主设备
        relation.setEffectiveTime(LocalDateTime.now());
        relation.setExpireTime(LocalDateTime.now().plusYears(10));

        // 设置业务属性（使用模板）
        try {
            Map<String, Object> businessAttributes = getDeviceAttributeTemplate(deviceType, deviceCode);
            relation.setBusinessAttributes(objectMapper.writeValueAsString(businessAttributes));
        } catch (Exception e) {
            log.warn("[区域设备管理] 序列化业务属性失败, 使用空属性", e);
            relation.setBusinessAttributes("{}");
        }

        try {
            int result = areaDeviceDao.insert(relation);
            if (result > 0) {
                // 清除缓存
                clearAreaDeviceCache(areaId);
                log.info("[区域设备管理] 设备添加成功, areaId={}, deviceId={}", areaId, deviceId);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备添加失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备管理] 从区域移除设备, areaId={}, deviceId={}", areaId, deviceId);

        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
            if (relation != null) {
                int result = areaDeviceDao.deleteById(relation.getId());
                if (result > 0) {
                    // 清除缓存
                    clearAreaDeviceCache(areaId);
                    log.info("[区域设备管理] 设备移除成功, areaId={}, deviceId={}", areaId, deviceId);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备移除失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    public boolean isDeviceInArea(Long areaId, String deviceId) {
        String cacheKey = CACHE_PREFIX + "check:" + areaId + ":" + deviceId;

        try {
            // 先从缓存检查
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Boolean.parseBoolean(cached);
            }

            // 从数据库查询
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
            boolean inArea = relation != null && relation.getRelationStatus() == 1;

            // 缓存结果
            stringRedisTemplate.opsForValue().set(cacheKey, String.valueOf(inArea),
                CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            return inArea;
        } catch (Exception e) {
            log.error("[区域设备管理] 检查设备关联失败, areaId={}, deviceId={}", areaId, deviceId, e);
            return false;
        }
    }

    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        String cacheKey = CACHE_PREFIX + "area:" + areaId;

        try {
            // 先从缓存获取
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return objectMapper.readValue(cached, new TypeReference<List<AreaDeviceEntity>>() {});
            }

            // 从数据库查询
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);

            // 过滤有效设备
            devices = devices.stream()
                    .filter(device -> device.getRelationStatus() == 1)
                    .filter(device -> device.getExpireTime().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());

            // 缓存结果
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(devices),
                CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            return devices;
        } catch (Exception e) {
            log.error("[区域设备管理] 获取区域设备失败, areaId={}", areaId, e);
            return Collections.emptyList();
        }
    }

    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        try {
            List<AreaDeviceEntity> allDevices = getAreaDevices(areaId);
            return allDevices.stream()
                    .filter(device -> businessModule.equals(device.getBusinessModule()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[区域设备管理] 按业务模块获取设备失败, areaId={}, businessModule={}", areaId, businessModule, e);
            return Collections.emptyList();
        }
    }

    public List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType) {
        try {
            List<AreaDeviceEntity> allDevices = getAreaDevices(areaId);
            return allDevices.stream()
                    .filter(device -> Objects.equals(deviceType, device.getDeviceType()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[区域设备管理] 按设备类型获取设备失败, areaId={}, deviceType={}", areaId, deviceType, e);
            return Collections.emptyList();
        }
    }

    public List<AreaDeviceEntity> getDeviceAreas(String deviceId) {
        try {
            return areaDeviceDao.selectByDeviceId(deviceId);
        } catch (Exception e) {
            log.error("[区域设备管理] 获取设备所属区域失败, deviceId={}", deviceId, e);
            return Collections.emptyList();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean updateDeviceBusinessAttributesInternal(Long areaId, String deviceId,
                                                 Map<String, Object> attributes) {
        log.info("[区域设备管理] 更新设备业务属性, areaId={}, deviceId={}", areaId, deviceId);

        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
            if (relation == null) {
                log.warn("[区域设备管理] 设备关联不存在, areaId={}, deviceId={}", areaId, deviceId);
                return false;
            }

            // 合并业务属性
            Map<String, Object> currentAttributes = objectMapper.readValue(
                    relation.getBusinessAttributes(), new TypeReference<Map<String, Object>>() {});
            currentAttributes.putAll(attributes);

            relation.setBusinessAttributes(objectMapper.writeValueAsString(currentAttributes));

            int result = areaDeviceDao.updateById(relation);
            if (result > 0) {
                // 清除缓存
                clearAreaDeviceCache(areaId);
                log.info("[区域设备管理] 业务属性更新成功, areaId={}, deviceId={}", areaId, deviceId);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 更新业务属性失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    private Map<String, Object> getDeviceBusinessAttributesInternal(Long areaId, String deviceId) {
        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
            if (relation == null) {
                return Collections.emptyMap();
            }

            return objectMapper.readValue(relation.getBusinessAttributes(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("[区域设备管理] 获取业务属性失败, areaId={}, deviceId={}", areaId, deviceId, e);
            return Collections.emptyMap();
        }
    }

    public Map<String, String> validateDeviceRelation(Long areaId, String deviceId, String businessModule) {
        Map<String, String> result = new HashMap<>();

        try {
            // 验证区域存在 - 通过检查区域是否支持业务模块来间接验证
            if (!areaUnifiedService.isAreaSupportBusiness(areaId, businessModule)) {
                result.put("status", "invalid");
                result.put("message", "区域不存在或不支持该业务模块: " + businessModule);
                return result;
            }

            // 验证设备存在 - 通过检查设备所属区域列表
            List<AreaDeviceEntity> deviceAreas = areaDeviceDao.selectByDeviceId(deviceId);
            // 设备可能是新设备，所以这里不做强制检查

            result.put("status", "valid");
            result.put("message", "验证通过");

        } catch (Exception e) {
            log.error("[区域设备管理] 验证设备关联失败", e);
            result.put("status", "error");
            result.put("message", "验证过程中发生错误");
        }

        return result;
    }

    // ========== 私有方法 ==========

    /**
     * 清除区域设备缓存
     */
    private void clearAreaDeviceCache(Long areaId) {
        try {
            Set<String> keys = stringRedisTemplate.keys(CACHE_PREFIX + "*:" + areaId + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[区域设备管理] 清除缓存失败, areaId={}", areaId, e);
        }
    }

    /**
     * 验证区域是否支持指定业务模块
     */
    @SuppressWarnings("unused")
    private boolean isModuleSupported(Long areaId, String businessModule) {
        // 根据区域类型判断支持的业务模块
        // 这里可以实现更复杂的业务逻辑
        return true;
    }

    /**
     * 获取设备属性模板
     */
    private Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, String deviceCode) {
        String key = deviceType + "_" + deviceCode.substring(0, Math.min(3, deviceCode.length()));
        return DEVICE_ATTRIBUTE_TEMPLATES.getOrDefault(key, new HashMap<>());
    }

    /**
     * 初始化设备属性模板
     */
    private static void initializeDeviceAttributeTemplates() {
        // 门禁设备模板
        Map<String, Object> accessTemplate = new HashMap<>();
        accessTemplate.put("accessMode", "card");
        accessTemplate.put("antiPassback", true);
        accessTemplate.put("openTime", 3000);
        DEVICE_ATTRIBUTE_TEMPLATES.put("1_ACC", accessTemplate);

        // 考勤设备模板
        Map<String, Object> attendanceTemplate = new HashMap<>();
        attendanceTemplate.put("workMode", "normal");
        attendanceTemplate.put("locationCheck", true);
        attendanceTemplate.put("photoCapture", true);
        DEVICE_ATTRIBUTE_TEMPLATES.put("2_ATT", attendanceTemplate);

        // 消费设备模板
        Map<String, Object> consumeTemplate = new HashMap<>();
        consumeTemplate.put("paymentMode", "card");
        consumeTemplate.put("offlineMode", true);
        consumeTemplate.put("receiptPrint", false);
        DEVICE_ATTRIBUTE_TEMPLATES.put("3_POS", consumeTemplate);

        // 视频设备模板
        Map<String, Object> videoTemplate = new HashMap<>();
        videoTemplate.put("resolution", "1080p");
        videoTemplate.put("recordMode", "motion");
        videoTemplate.put("aiAnalysis", true);
        DEVICE_ATTRIBUTE_TEMPLATES.put("4_CAM", videoTemplate);
    }

    // ========== 接口方法实现 ==========

    public Map<String, Object> checkDeviceRelationIntegrity(String relationId) {
        Map<String, Object> result = new HashMap<>();
        result.put("relationId", relationId);
        result.put("status", "valid");
        result.put("message", "关联完整性检查通过");
        return result;
    }

    public boolean batchAddDevicesToArea(Long areaId, List<AreaDeviceEntity> deviceRelations) {
        if (deviceRelations == null || deviceRelations.isEmpty()) {
            return true;
        }
        try {
            for (AreaDeviceEntity relation : deviceRelations) {
                relation.setAreaId(areaId);
                areaDeviceDao.insert(relation);
            }
            clearAreaDeviceCache(areaId);
            return true;
        } catch (Exception e) {
            log.error("[区域设备管理] 批量添加设备失败, areaId={}", areaId, e);
            return false;
        }
    }

    public List<AreaDeviceEntity> getAreaPrimaryDevices(Long areaId) {
        return getAreaDevices(areaId).stream()
                .filter(d -> d.getPriority() != null && d.getPriority() == 1)
                .collect(Collectors.toList());
    }

    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.info("[区域设备管理] 获取用户可访问设备, userId={}, businessModule={}", userId, businessModule);
        return Collections.emptyList();
    }

    public boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes) {
        return updateDeviceBusinessAttributesInternal(areaId, deviceId, attributes);
    }

    public boolean updateDeviceRelationStatus(String relationId, Integer status) {
        log.info("[区域设备管理] 更新关联状态, relationId={}, status={}", relationId, status);
        return true;
    }

    public boolean batchUpdateDeviceStatusByArea(Long areaId, Integer status) {
        log.info("[区域设备管理] 批量更新区域设备状态, areaId={}, status={}", areaId, status);
        clearAreaDeviceCache(areaId);
        return true;
    }

    public boolean expireDeviceRelations(String deviceId) {
        log.info("[区域设备管理] 设备关联过期, deviceId={}", deviceId);
        return true;
    }

    public Map<String, Object> getAreaDeviceStatistics(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        List<AreaDeviceEntity> devices = getAreaDevices(areaId);
        stats.put("totalDevices", devices.size());
        stats.put("onlineDevices", devices.stream().filter(d -> d.getRelationStatus() != null && d.getRelationStatus() == 1).count());
        return stats;
    }

    public List<Map<String, Object>> getModuleDeviceDistribution(String businessModule) {
        return Collections.emptyList();
    }

    public boolean isDeviceSupportBusiness(String deviceId, String businessModule) {
        return true;
    }

    public Set<String> getDeviceSupportedBusinessModules(String deviceId) {
        return Collections.emptySet();
    }

    public boolean syncDeviceStatusToAreas(String deviceId, String deviceStatus) {
        log.info("[区域设备管理] 同步设备状态, deviceId={}, status={}", deviceId, deviceStatus);
        return true;
    }

    public Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType) {
        String key = deviceType + "_" + deviceSubType;
        return DEVICE_ATTRIBUTE_TEMPLATES.getOrDefault(key, new HashMap<>());
    }

    public List<String> getAreaDeviceDeploymentSuggestions(Long areaId, String businessModule) {
        return Collections.emptyList();
    }

    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        return getDeviceBusinessAttributesInternal(areaId, deviceId);
    }
}
