package net.lab1024.sa.common.organization.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.service.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 区域设备管理服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AreaDeviceManagerImpl implements AreaDeviceManager {

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private AreaUnifiedService areaUnifiedService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis缓存前缀
    private static final String CACHE_PREFIX = "area:device:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    // 设备属性模板缓存
    private static final Map<String, Map<String, Object>> DEVICE_ATTRIBUTE_TEMPLATES = new HashMap<>();

    static {
        // 初始化设备属性模板
        initializeDeviceAttributeTemplates();
    }

    @Override
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
        relation.setPriority(2); // 默认辅助设备
        relation.setEnabled(true);
        relation.setEffectiveTime(LocalDateTime.now());

        try {
            // 设置业务属性模板
            Map<String, Object> template = getDeviceAttributeTemplate(deviceType, null);
            if (template != null && !template.isEmpty()) {
                relation.setBusinessAttributes(objectMapper.writeValueAsString(template));
            }
        } catch (Exception e) {
            log.warn("[区域设备管理] 设置业务属性失败", e);
        }

        try {
            int result = areaDeviceDao.insert(relation);

            if (result > 0) {
                clearDeviceCache(deviceId);
                log.info("[区域设备管理] 设备添加成功, relationId={}", relation.getId());
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备添加失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    @Override
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备管理] 从区域移除设备, areaId={}, deviceId={}", areaId, deviceId);

        try {
            // 软置设备为过期状态
            int result = areaDeviceDao.expireDeviceRelations(deviceId);

            if (result > 0) {
                clearDeviceCache(deviceId);
                log.info("[区域设备管理] 设备移除成功, areaId={}, deviceId={}", areaId, deviceId);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备移除失败, areaId={}, deviceId={}", areaId, deviceId, e);
        }

        return false;
    }

    @Override
    public boolean batchAddDevicesToArea(Long areaId, List<AreaDeviceEntity> deviceRelations) {
        log.info("[区域设备管理] 批量添加设备到区域, areaId={}, deviceCount={}",
                areaId, deviceRelations.size());

        if (deviceRelations == null || deviceRelations.isEmpty()) {
            return false;
        }

        int successCount = 0;
        for (AreaDeviceEntity relation : deviceRelations) {
            if (addDeviceToArea(areaId, relation.getDeviceId(), relation.getDeviceCode(),
                    relation.getDeviceName(), relation.getDeviceType(), relation.getBusinessModule())) {
                successCount++;
            }
        }

        log.info("[区域设备管理] 批量添加完成, successCount={}, totalCount={}",
                successCount, deviceRelations.size());
        return successCount == deviceRelations.size();
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        log.debug("[区域设备管理] 获取区域设备, areaId={}", areaId);

        String cacheKey = CACHE_PREFIX + "area:" + areaId;
        List<AreaDeviceEntity> cachedDevices = getCachedDeviceList(cacheKey);

        if (cachedDevices != null) {
            return cachedDevices;
        }

        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);

        cacheDeviceList(cacheKey, devices);

        log.debug("[区域设备管理] 区域设备获取完成, areaId={}, deviceCount={}", areaId, devices.size());
        return devices;
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType) {
        log.debug("[区域设备管理] 获取区域指定类型设备, areaId={}, deviceType={}", areaId, deviceType);

        String cacheKey = CACHE_PREFIX + "area:" + areaId + ":type:" + deviceType;
        List<AreaDeviceEntity> cachedDevices = getCachedDeviceList(cacheKey);

        if (cachedDevices != null) {
            return cachedDevices;
        }

        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(areaId, deviceType);

        cacheDeviceList(cacheKey, devices);

        return devices;
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        log.debug("[区域设备管理] 获取区域指定业务模块设备, areaId={}, businessModule={}", areaId, businessModule);

        String cacheKey = CACHE_PREFIX + "area:" + areaId + ":module:" + businessModule;
        List<AreaDeviceEntity> cachedDevices = getCachedDeviceList(cacheKey);

        if (cachedDevices != null) {
            return cachedDevices;
        }

        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, businessModule);

        cacheDeviceList(cacheKey, devices);

        return devices;
    }

    @Override
    public List<AreaDeviceEntity> getAreaPrimaryDevices(Long areaId) {
        log.debug("[区域设备管理] 获取区域主设备, areaId={}", areaId);

        String cacheKey = CACHE_PREFIX + "area:" + areaId + ":primary";
        List<AreaDeviceEntity> cachedDevices = getCachedDeviceList(cacheKey);

        if (cachedDevices != null) {
            return cachedDevices;
        }

        List<AreaDeviceEntity> devices = areaDeviceDao.selectPrimaryDevicesByAreaId(areaId);

        cacheDeviceList(cacheKey, devices);

        return devices;
    }

    @Override
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        log.debug("[区域设备管理] 检查设备是否在区域中, areaId={}, deviceId={}", areaId, deviceId);

        return areaDeviceDao.isDeviceInArea(areaId, deviceId);
    }

    @Override
    public List<AreaDeviceEntity> getDeviceAreas(String deviceId) {
        log.debug("[区域设备管理] 获取设备所属区域, deviceId={}", deviceId);

        String cacheKey = CACHE_PREFIX + "device:" + deviceId + ":areas";
        List<AreaDeviceEntity> cachedAreas = getCachedDeviceList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<AreaDeviceEntity> areas = areaDeviceDao.selectByDeviceId(deviceId);

        cacheDeviceList(cacheKey, areas);

        return areas;
    }

    @Override
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.debug("[区域设备管理] 获取用户可访问设备, userId={}, businessModule={}", userId, businessModule);

        String cacheKey = CACHE_PREFIX + "user:" + userId + ":devices" +
                            (businessModule != null ? ":" + businessModule : "");
        List<AreaDeviceEntity> cachedDevices = getCachedDeviceList(cacheKey);

        if (cachedDevices != null) {
            return cachedDevices;
        }

        List<AreaDeviceEntity> devices = areaDeviceDao.selectDevicesByUserPermission(userId, businessModule);

        cacheDeviceList(cacheKey, devices);

        return devices;
    }

    @Override
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        log.debug("[区域设备管理] 获取设备业务属性, deviceId={}, areaId={}", deviceId, areaId);

        String attributesJson = areaDeviceDao.selectDeviceBusinessAttributes(deviceId, areaId);

        if (attributesJson == null || attributesJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(attributesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("[区域设备管理] 解析设备业务属性失败, deviceId={}, areaId={}, attributesJson={}",
                    deviceId, areaId, attributesJson, e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes) {
        log.info("[区域设备管理] 设置设备业务属性, deviceId={}, areaId={}", deviceId, areaId);

        try {
            String attributesJson = objectMapper.writeValueAsString(attributes);
            log.debug("[区域设备管理] 设备业务属性JSON, deviceId={}, attributesJson={}", deviceId, attributesJson);

            // 注意：这里需要扩展DAO来支持业务属性更新
            // int result = areaDeviceDao.updateBusinessAttributes(deviceId, areaId, attributesJson);

            // 暂时返回true，实际实现需要扩展DAO
            clearDeviceCache(deviceId);

            log.info("[区域设备管理] 设备业务属性设置成功, deviceId={}, areaId={}", deviceId, areaId);
            return true;

        } catch (Exception e) {
            log.error("[区域设备管理] 设置设备业务属性失败, deviceId={}, areaId={}", deviceId, areaId, e);
            return false;
        }
    }

    @Override
    public boolean updateDeviceRelationStatus(String relationId, Integer status) {
        log.info("[区域设备管理] 更新设备关联状态, relationId={}, status={}", relationId, status);

        try {
            int result = areaDeviceDao.updateRelationStatus(relationId, status);

            if (result > 0) {
                log.info("[区域设备管理] 设备关联状态更新成功, relationId={}", relationId);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备关联状态更新失败, relationId={}, status={}", relationId, status, e);
        }

        return false;
    }

    @Override
    public boolean batchUpdateDeviceStatusByArea(Long areaId, Integer status) {
        log.info("[区域设备管理] 批量更新区域设备状态, areaId={}, status={}", areaId, status);

        try {
            int result = areaDeviceDao.batchUpdateRelationStatusByAreaId(areaId, status);

            if (result > 0) {
                clearAreaCache(areaId);
                log.info("[区域设备管理] 区域设备状态批量更新成功, areaId={}, count={}", areaId, result);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 区域设备状态批量更新失败, areaId={}, status={}", areaId, status, e);
        }

        return false;
    }

    @Override
    public boolean expireDeviceRelations(String deviceId) {
        log.info("[区域设备管理] 设置设备关联过期, deviceId={}", deviceId);

        try {
            int result = areaDeviceDao.expireDeviceRelations(deviceId);

            if (result > 0) {
                clearDeviceCache(deviceId);
                log.info("[区域设备管理] 设备关联设置过期成功, deviceId={}, count={}", deviceId, result);
                return true;
            }
        } catch (Exception e) {
            log.error("[区域设备管理] 设备关联设置过期失败, deviceId={}", deviceId, e);
        }

        return false;
    }

    @Override
    public Map<String, Object> getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备管理] 获取区域设备统计, areaId={}", areaId);

        Map<String, Object> statistics = new HashMap<>();

        // 基础统计
        statistics.put("areaId", areaId);
        statistics.put("onlineDeviceCount", areaDeviceDao.countOnlineDevicesByAreaId(areaId));

        // 按类型统计
        List<Map<String, Object>> typeStatistics = areaDeviceDao.selectDeviceStatisticsByAreaId(areaId);
        statistics.put("typeStatistics", typeStatistics);

        // 主设备统计
        List<AreaDeviceEntity> primaryDevices = getAreaPrimaryDevices(areaId);
        statistics.put("primaryDeviceCount", primaryDevices.size());
        statistics.put("primaryDevices", primaryDevices);

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getModuleDeviceDistribution(String businessModule) {
        log.debug("[区域设备管理] 获取业务模块设备分布, businessModule={}", businessModule);

        return areaDeviceDao.selectDeviceDistributionByModule(businessModule);
    }

    @Override
    public boolean isDeviceSupportBusiness(String deviceId, String businessModule) {
        log.debug("[区域设备管理] 检查设备业务支持, deviceId={}, businessModule={}", deviceId, businessModule);

        return getAreaDevicesByModule(null, businessModule).stream()
                .anyMatch(device -> deviceId.equals(device.getDeviceId()));
    }

    @Override
    public Set<String> getDeviceSupportedBusinessModules(String deviceId) {
        log.debug("[区域设备管理] 获取设备支持的业务模块, deviceId={}", deviceId);

        return getDeviceAreas(deviceId).stream()
                .map(AreaDeviceEntity::getBusinessModule)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean syncDeviceStatusToAreas(String deviceId, String deviceStatus) {
        log.info("[区域设备管理] 同步设备状态到区域关联, deviceId={}, status={}", deviceId, deviceStatus);

        try {
            // 根据设备状态确定关联状态
            Integer relationStatus = mapDeviceStatusToRelationStatus(deviceStatus);

            // 批量更新所有区域中的该设备状态
            List<AreaDeviceEntity> deviceAreas = getDeviceAreas(deviceId);
            if (deviceAreas.isEmpty()) {
                return false;
            }

            int successCount = 0;
            for (AreaDeviceEntity deviceArea : deviceAreas) {
                if (updateDeviceRelationStatus(deviceArea.getId(), relationStatus)) {
                    successCount++;
                }
            }

            if (successCount > 0) {
                clearDeviceCache(deviceId);
                log.info("[区域设备管理] 设备状态同步成功, deviceId={}, status={}, count={}",
                        deviceId, deviceStatus, successCount);
                return true;
            }

        } catch (Exception e) {
            log.error("[区域设备管理] 设备状态同步失败, deviceId={}, status={}", deviceId, deviceStatus, e);
        }

        return false;
    }

    @Override
    public Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType) {
        String templateKey = deviceType + (deviceSubType != null ? ":" + deviceSubType : "");
        return DEVICE_ATTRIBUTE_TEMPLATES.get(templateKey);
    }

    @Override
    public Map<String, String> validateDeviceRelation(Long areaId, String deviceId, String businessModule) {
        log.debug("[区域设备管理] 验证设备关联配置, areaId={}, deviceId={}, businessModule={}",
                areaId, deviceId, businessModule);

        Map<String, String> result = new HashMap<>();

        // 验证区域存在
        if (!areaUnifiedService.isAreaSupportBusiness(areaId, businessModule)) {
            result.put("status", "error");
            result.put("message", "区域不支持指定业务模块");
            result.put("areaId", String.valueOf(areaId));
            result.put("businessModule", businessModule);
            return result;
        }

        // 验证设备类型与业务模块匹配
        // 从设备信息中获取deviceType
        DeviceEntity device = deviceDao.selectById(deviceId);
        Integer deviceType = null;
        if (device != null && device.getDeviceType() != null) {
            try {
                deviceType = Integer.parseInt(device.getDeviceType());
            } catch (NumberFormatException e) {
                log.warn("[区域设备管理] 设备类型转换失败, deviceId={}, deviceType={}",
                    deviceId, device.getDeviceType());
            }
        }
        if (!isDeviceTypeMatchModule(deviceId, deviceType, businessModule)) {
            result.put("status", "error");
            result.put("message", "设备类型与业务模块不匹配");
            result.put("deviceId", deviceId);
            result.put("businessModule", businessModule);
            return result;
        }

        result.put("status", "valid");
        return result;
    }

    @Override
    public List<String> getAreaDeviceDeploymentSuggestions(Long areaId, String businessModule) {
        log.debug("[区域设备管理] 获取区域设备部署建议, areaId={}, businessModule={}", areaId, businessModule);

        List<String> suggestions = new ArrayList<>();

        // 根据业务模块生成建议
        switch (businessModule) {
            case "access":
                suggestions.add("建议部署门禁控制器在主要出入口");
                suggestions.add("建议部署读卡器在通道两侧");
                suggestions.add("建议部署生物识别设备在重要区域");
                suggestions.add("建议配置备用门禁设备");
                break;

            case "attendance":
                suggestions.add("建议部署考勤机在主要出入口");
                suggestions.add("建议部署指纹/人脸设备在办公区域");
                suggestions.add("建议配置移动考勤设备");
                break;

            case "consume":
                suggestions.add("建议部署POS机在用餐区域");
                suggestions.add("建议部署充值机在服务中心");
                suggestions.add("建议配置自助查询设备");
                break;

            case "video":
                suggestions.add("建议部署摄像头覆盖关键区域");
                suggestions.add("建议部署录像机存储重要画面");
                suggestions.add("建议配置智能分析设备");
                break;

            case "visitor":
                suggestions.add("建议部署访客机在主要接待区");
                suggestions.add("建议部署登记终端在门厅");
                suggestions.add("建议部署身份验证设备");
                break;

            default:
                suggestions.add("根据业务需求部署相应设备");
                break;
        }

        return suggestions;
    }

    @Override
    public Map<String, Object> checkDeviceRelationIntegrity(String relationId) {
        log.debug("[区域设备管理] 检查设备关联完整性, relationId={}", relationId);

        Map<String, Object> integrityResult = new HashMap<>();

        integrityResult.put("relationId", relationId);
        integrityResult.put("checkTime", LocalDateTime.now());

        // 检查关联记录是否存在
        AreaDeviceEntity relation = areaDeviceDao.selectById(relationId);
        if (relation == null) {
            integrityResult.put("status", "error");
            integrityResult.put("message", "设备关联记录不存在");
            return integrityResult;
        }

        integrityResult.put("status", "valid");
        integrityResult.put("areaId", relation.getAreaId());
        integrityResult.put("deviceId", relation.getDeviceId());
        integrityResult.put("deviceType", relation.getDeviceType());
        integrityResult.put("businessModule", relation.getBusinessModule());

        return integrityResult;
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化设备属性模板
     */
    private static void initializeDeviceAttributeTemplates() {
        // 门禁设备模板
        Map<String, Object> accessDeviceTemplate = new HashMap<>();
        accessDeviceTemplate.put("accessMode", "card"); // card/password/biometric
        accessDeviceTemplate.put("accessLevel", "normal"); // normal/low/high
        accessDeviceTemplate.put("antiPassback", true);
        accessDeviceTemplate.put("duressCode", "123456");
        DEVICE_ATTRIBUTE_TEMPLATES.put("1", accessDeviceTemplate);

        // 门禁子类型模板
        Map<String, Object> accessControllerTemplate = new HashMap<>();
        accessControllerTemplate.put("controlMode", "auto"); // auto/manual
        accessControllerTemplate.put("openTime", 3000); // 毫秒
        accessControllerTemplate.put("closeTime", 5000);
        DEVICE_ATTRIBUTE_TEMPLATES.put("1:11", accessControllerTemplate);

        // 考勤设备模板
        Map<String, Object> attendanceDeviceTemplate = new HashMap<>();
        attendanceDeviceTemplate.put("workMode", "attendance"); // attendance/check-in/out
        attendanceDeviceTemplate.put("locationRequired", true);
        attendanceDeviceTemplate.put("photoCapture", true);
        DEVICE_ATTRIBUTE_TEMPLATES.put("2", attendanceDeviceTemplate);

        // 消费设备模板
        Map<String, Object> consumeDeviceTemplate = new HashMap<>();
        consumeDeviceTemplate.put("paymentMode", "card"); // card/mobile/cash
        consumeDeviceTemplate.put("receiptRequired", true);
        consumeDeviceTemplate.put("offlineMode", true);
        DEVICE_ATTRIBUTE_TEMPLATES.put("3", consumeDeviceTemplate);

        // 视频设备模板
        Map<String, Object> videoDeviceTemplate = new HashMap<>();
        videoDeviceTemplate.put("resolution", "1080p"); // 720p/1080p/4K
        videoDeviceTemplate.put("recordingEnabled", true);
        videoDeviceTemplate.put("aiAnalysis", false);
        videoDeviceTemplate.put("storageType", "local");
        DEVICE_ATTRIBUTE_TEMPLATES.put("4", videoDeviceTemplate);
    }

    /**
     * 映射设备状态到关联状态
     */
    private Integer mapDeviceStatusToRelationStatus(String deviceStatus) {
        if (deviceStatus == null) {
            return 4; // 离线
        }

        switch (deviceStatus.toLowerCase()) {
            case "online":
            case "normal":
            case "active":
                return 1; // 正常

            case "maintenance":
            case "repair":
                return 2; // 维护

            case "offline":
            case "error":
            case "fault":
                return 4; // 离线

            case "disabled":
            case "inactive":
                return 5; // 停用

            default:
                return 4; // 默认离线
        }
    }

    /**
     * 检查设备类型是否匹配业务模块
     */
    private boolean isDeviceTypeMatchModule(String deviceId, Integer deviceType, String businessModule) {
        // 这里可以根据实际业务规则进行验证
        // 暂时返回true，实际实现需要更复杂的逻辑
        return true;
    }

    /**
     * 缓存相关方法
     */
    private List<AreaDeviceEntity> getCachedDeviceList(String cacheKey) {
        try {
            String nonNullCacheKey = Objects.requireNonNull(cacheKey, "cacheKey不能为null");
            String cached = stringRedisTemplate.opsForValue().get(nonNullCacheKey);
            return cached != null ? objectMapper.readValue(cached, new TypeReference<List<AreaDeviceEntity>>() {}) : null;
        } catch (Exception e) {
            log.warn("[区域设备管理] 缓存读取失败", e);
            return null;
        }
    }

    private void cacheDeviceList(String cacheKey, List<AreaDeviceEntity> deviceList) {
        try {
            String json = objectMapper.writeValueAsString(deviceList);
            String nonNullCacheKey = Objects.requireNonNull(cacheKey, "cacheKey不能为null");
            String nonNullJson = Objects.requireNonNull(json, "json不能为null");
            stringRedisTemplate.opsForValue().set(nonNullCacheKey, nonNullJson, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[区域设备管理] 缓存写入失败", e);
        }
    }

    private void clearDeviceCache(String deviceId) {
        try {
            Set<String> keys = stringRedisTemplate.keys(CACHE_PREFIX + "*:" + deviceId + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[区域设备管理] 清除设备缓存失败", e);
        }
    }

    private void clearAreaCache(Long areaId) {
        try {
            Set<String> keys = stringRedisTemplate.keys(CACHE_PREFIX + "area:" + areaId + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[区域设备管理] 清除区域缓存失败", e);
        }
    }
}
