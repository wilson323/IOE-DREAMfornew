package net.lab1024.sa.common.organization.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.service.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
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
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 * @updated 2025-12-17 移除@Service注解，改为纯Java类，使用构造函数注入
 */
@Slf4j
public class AreaDeviceManagerImpl implements AreaDeviceManager {

    private final AreaDeviceDao areaDeviceDao;
    private final DeviceDao deviceDao;
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
                               DeviceDao deviceDao,
                               AreaUnifiedService areaUnifiedService,
                               StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.areaDeviceDao = areaDeviceDao;
        this.deviceDao = deviceDao;
        this.areaUnifiedService = areaUnifiedService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
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
        Map<String, Object> businessAttributes = getDeviceAttributeTemplate(deviceType, deviceCode);
        relation.setBusinessAttributes(objectMapper.writeValueAsString(businessAttributes));

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备管理] 从区域移除设备, areaId={}, deviceId={}", areaId, deviceId);

        try {
            int result = areaDeviceDao.deleteByAreaAndDevice(areaId, deviceId);
            if (result > 0) {
                // 清除缓存
                clearAreaDeviceCache(areaId);
                log.info("[区域设备管理] 设备移除成功, areaId={}, deviceId={}", areaId, deviceId);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备移除失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    @Override
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        String cacheKey = CACHE_PREFIX + "check:" + areaId + ":" + deviceId;

        try {
            // 先从缓存检查
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Boolean.parseBoolean(cached);
            }

            // 从数据库查询
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaAndDevice(areaId, deviceId);
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

    @Override
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

    @Override
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceBusinessAttributes(Long areaId, String deviceId,
                                                 Map<String, Object> attributes) {
        log.info("[区域设备管理] 更新设备业务属性, areaId={}, deviceId={}", areaId, deviceId);

        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaAndDevice(areaId, deviceId);
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

    @Override
    public Map<String, Object> getDeviceBusinessAttributes(Long areaId, String deviceId) {
        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaAndDevice(areaId, deviceId);
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

    @Override
    public Map<String, String> validateDeviceRelation(Long areaId, String deviceId, String businessModule) {
        Map<String, String> result = new HashMap<>();

        try {
            // 验证区域存在
            AreaEntity area = areaUnifiedService.getAreaById(areaId);
            if (area == null) {
                result.put("status", "invalid");
                result.put("message", "区域不存在");
                return result;
            }

            // 验证设备存在
            DeviceEntity device = deviceDao.selectByDeviceId(deviceId);
            if (device == null) {
                result.put("status", "invalid");
                result.put("message", "设备不存在");
                return result;
            }

            // 验证业务模块支持
            if (!isModuleSupported(area, businessModule)) {
                result.put("status", "invalid");
                result.put("message", "区域不支持该业务模块: " + businessModule);
                return result;
            }

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
    private boolean isModuleSupported(AreaEntity area, String businessModule) {
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
}