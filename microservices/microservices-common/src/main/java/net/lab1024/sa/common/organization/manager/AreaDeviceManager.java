package net.lab1024.sa.common.organization.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.device.manager.DeviceStatusManager;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;

import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 区域设备管理器
 * <p>
 * 负责区域与设备的关联管理
 * 实现设备区域权限联动和人员信息下发
 * 支持设备权限继承和业务属性配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class AreaDeviceManager {

    private final AreaDao areaDao;
    private final AreaDeviceDao areaDeviceDao;
    private final AreaUserDao areaUserDao;
    private final DeviceDao deviceDao;
    private final DeviceStatusManager deviceStatusManager;
    private final AreaUnifiedService areaUnifiedService; // 区域统一服务，用于获取子区域
    private final GatewayServiceClient gatewayServiceClient; // 网关服务客户端，用于调用设备通讯服务

    // 设备用户列表缓存（5分钟过期）
    private final Map<String, List<String>> deviceUsersCache = new ConcurrentHashMap<>();
    private final Map<String, Long> deviceUsersCacheTime = new ConcurrentHashMap<>();
    private static final long DEVICE_USERS_CACHE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟

    // 递归深度限制（默认10层）
    private static final int MAX_RECURSION_DEPTH = 10;

    // 构造函数注入依赖
    public AreaDeviceManager(AreaDao areaDao, AreaDeviceDao areaDeviceDao,
                            AreaUserDao areaUserDao, DeviceDao deviceDao,
                            DeviceStatusManager deviceStatusManager,
                            AreaUnifiedService areaUnifiedService,
                            GatewayServiceClient gatewayServiceClient) {
        this.areaDao = areaDao;
        this.areaDeviceDao = areaDeviceDao;
        this.areaUserDao = areaUserDao;
        this.deviceDao = deviceDao;
        this.deviceStatusManager = deviceStatusManager;
        this.areaUnifiedService = areaUnifiedService;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    // ==================== 设备区域关联管理 ====================

    /**
     * 将设备添加到区域
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param priority 优先级
     * @param businessAttributes 业务属性
     * @return 关联关系
     */
    public AreaDeviceEntity addDeviceToArea(Long areaId, String deviceId, String deviceType,
                                           Integer priority, Map<String, Object> businessAttributes) {
        log.debug("[区域设备管理] 添加设备到区域, areaId={}, deviceId={}, deviceType={}",
                 areaId, deviceId, deviceType);

        // 1. 验证区域是否存在
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在: " + areaId);
        }

        // 2. 验证设备是否存在
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }

        // 3. 检查是否已存在关联
        AreaDeviceEntity existingRelation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (existingRelation != null) {
            throw new RuntimeException("设备已关联到该区域: " + deviceId);
        }

        // 4. 创建关联关系
        AreaDeviceEntity relation = new AreaDeviceEntity();
        relation.setAreaId(areaId);
        relation.setDeviceId(deviceId);
        relation.setDeviceCode(device.getDeviceCode());
        relation.setDeviceName(device.getDeviceName());
        try {
            relation.setDeviceType(Integer.parseInt(deviceType));
        } catch (NumberFormatException e) {
            log.error("[区域设备管理] 设备类型转换失败, deviceType={}", deviceType, e);
            relation.setDeviceType(0); // 默认类型
        }
        relation.setBusinessModule(getBusinessModuleByDeviceType(deviceType));
        relation.setRelationStatus(1); // 正常
        relation.setPriority(priority != null ? priority : 2); // 默认辅助设备
        relation.setEffectiveTime(LocalDateTime.now());
        relation.setUserSyncStatus(0); // 未同步

        // 处理businessAttributes转换为JSON
        if (businessAttributes != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String businessAttributesJson = objectMapper.writeValueAsString(businessAttributes);
                relation.setBusinessAttributes(businessAttributesJson);
            } catch (Exception e) {
                log.error("[区域设备管理] 转换业务属性为JSON失败, deviceId={}", deviceId, e);
                relation.setBusinessAttributes("{}");
            }
        } else {
            relation.setBusinessAttributes("{}");
        }

        // 5. 保存关联关系
        areaDeviceDao.insert(relation);

        log.info("[区域设备管理] 设备关联成功, relationId={}, areaId={}, deviceId={}",
                relation.getId(), areaId, deviceId);

        // 6. 触发权限同步
        triggerDevicePermissionSync(relation);

        return relation;
    }

    /**
     * 添加设备到区域（重载方法，适配Service接口）
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param deviceType 设备类型（Integer）
     * @param businessModule 业务模块
     * @return 关联关系
     */
    public AreaDeviceEntity addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                                           String deviceName, Integer deviceType, String businessModule) {
        log.debug("[区域设备管理] 添加设备到区域（重载方法）, areaId={}, deviceId={}, deviceType={}, module={}",
                 areaId, deviceId, deviceType, businessModule);

        // 转换参数并调用主方法
        String deviceTypeStr = deviceType != null ? deviceType.toString() : "0";
        Integer priority = 2; // 默认辅助设备
        Map<String, Object> businessAttributes = new HashMap<>();

        // 如果提供了业务模块，设置到业务属性中
        if (businessModule != null && !businessModule.isEmpty()) {
            businessAttributes.put("businessModule", businessModule);
        }

        // 调用主方法
        AreaDeviceEntity relation = addDeviceToArea(areaId, deviceId, deviceTypeStr, priority, businessAttributes);

        // 更新设备编码和名称（如果提供）
        if (deviceCode != null && !deviceCode.isEmpty()) {
            relation.setDeviceCode(deviceCode);
        }
        if (deviceName != null && !deviceName.isEmpty()) {
            relation.setDeviceName(deviceName);
        }
        if (businessModule != null && !businessModule.isEmpty()) {
            relation.setBusinessModule(businessModule);
        }

        // 更新关联关系
        areaDeviceDao.updateById(relation);

        return relation;
    }

    /**
     * 移除区域设备关联
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否成功移除
     */
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.debug("[区域设备管理] 移除区域设备关联, areaId={}, deviceId={}", areaId, deviceId);

        AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (relation == null) {
            log.warn("[区域设备管理] 设备未关联到该区域, areaId={}, deviceId={}", areaId, deviceId);
            return false;
        }

        try {
            // 撤销设备权限
            revokeDevicePermissions(relation);

            // 删除关联关系
            areaDeviceDao.deleteById(relation.getId());

            log.info("[区域设备管理] 设备关联已移除, areaId={}, deviceId={}", areaId, deviceId);
            return true;
        } catch (Exception e) {
            log.error("[区域设备管理] 移除设备关联失败, areaId={}, deviceId={}", areaId, deviceId, e);
            return false;
        }
    }

    /**
     * 获取区域内的所有设备
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        log.debug("[区域设备管理] 获取区域设备, areaId={}", areaId);

        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);

        // 包含继承权限的子区域设备
        List<AreaDeviceEntity> inheritedDevices = getInheritedDevices(areaId);
        devices.addAll(inheritedDevices);

        // 去重并按优先级排序
        Map<String, AreaDeviceEntity> uniqueDevices = new HashMap<>();
        for (AreaDeviceEntity device : devices) {
            AreaDeviceEntity existing = uniqueDevices.get(device.getDeviceId());
            if (existing == null || device.getPriority() < existing.getPriority()) {
                uniqueDevices.put(device.getDeviceId(), device);
            }
        }

        return uniqueDevices.values().stream()
                .sorted((d1, d2) -> Integer.compare(d1.getPriority(), d2.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * 获取区域中指定业务模块的设备
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 设备列表
     */
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        log.debug("[区域设备管理] 获取区域业务模块设备, areaId={}, module={}", areaId, businessModule);

        List<AreaDeviceEntity> allDevices = getAreaDevices(areaId);

        // 过滤指定业务模块的设备
        return allDevices.stream()
                .filter(device -> businessModule == null || businessModule.isEmpty()
                        || businessModule.equals(device.getBusinessModule()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户可访问的设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块（可选）
     * @return 设备列表
     */
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.debug("[区域设备管理] 获取用户可访问设备, userId={}, module={}", userId, businessModule);

        try {
            // 1. 获取用户有权限的所有区域
            List<AreaUserEntity> userAreas = areaUserDao.selectByUserId(userId);
            if (userAreas == null || userAreas.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 收集所有区域的设备
            Set<String> deviceIdSet = new HashSet<>();
            List<AreaDeviceEntity> accessibleDevices = new ArrayList<>();

            for (AreaUserEntity userArea : userAreas) {
                // 检查用户是否有该区域的访问权限
                if (!userArea.hasAccessPermission()) {
                    continue;
                }

                // 检查时间限制
                if (!userArea.isWithinAllowedTime()) {
                    continue;
                }

                // 获取该区域的设备
                List<AreaDeviceEntity> areaDevices;
                if (businessModule != null && !businessModule.isEmpty()) {
                    areaDevices = getAreaDevicesByModule(userArea.getAreaId(), businessModule);
                } else {
                    areaDevices = getAreaDevices(userArea.getAreaId());
                }

                // 去重并添加到结果列表
                for (AreaDeviceEntity device : areaDevices) {
                    if (!deviceIdSet.contains(device.getDeviceId())) {
                        deviceIdSet.add(device.getDeviceId());
                        accessibleDevices.add(device);
                    }
                }
            }

            log.debug("[区域设备管理] 用户可访问设备数量, userId={}, count={}", userId, accessibleDevices.size());
            return accessibleDevices;

        } catch (Exception e) {
            log.error("[区域设备管理] 获取用户可访问设备异常, userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查设备是否在区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否在区域中
     */
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        log.debug("[区域设备管理] 检查设备是否在区域, areaId={}, deviceId={}", areaId, deviceId);

        try {
            AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
            if (relation != null && relation.getRelationStatus() == 1) {
                return true;
            }

            // 检查子区域
            List<AreaDeviceEntity> inheritedDevices = getInheritedDevices(areaId);
            return inheritedDevices.stream()
                    .anyMatch(device -> deviceId.equals(device.getDeviceId()) && device.getRelationStatus() == 1);

        } catch (Exception e) {
            log.error("[区域设备管理] 检查设备区域关联异常, areaId={}, deviceId={}", areaId, deviceId, e);
            return false;
        }
    }

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param relationStatus 关联状态
     */
    public void updateDeviceRelationStatus(String relationId, Integer relationStatus) {
        log.debug("[区域设备管理] 更新设备关联状态, relationId={}, status={}", relationId, relationStatus);

        try {
            AreaDeviceEntity relation = areaDeviceDao.selectById(relationId);
            if (relation == null) {
                throw new RuntimeException("设备关联关系不存在: " + relationId);
            }

            relation.setRelationStatus(relationStatus);
            areaDeviceDao.updateById(relation);

            log.info("[区域设备管理] 设备关联状态已更新, relationId={}, status={}", relationId, relationStatus);

        } catch (Exception e) {
            log.error("[区域设备管理] 更新设备关联状态失败, relationId={}, status={}", relationId, relationStatus, e);
            throw new RuntimeException("更新设备关联状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取区域设备统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    public Map<String, Object> getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备管理] 获取区域设备统计, areaId={}", areaId);

        try {
            List<AreaDeviceEntity> devices = getAreaDevices(areaId);

            // 统计总数
            int totalCount = devices.size();

            // 统计在线/离线数量
            int onlineCount = 0;
            int offlineCount = 0;
            if (deviceStatusManager != null) {
                for (AreaDeviceEntity device : devices) {
                    boolean isOnline = deviceStatusManager.isDeviceOnline(device.getDeviceId());
                    if (isOnline) {
                        onlineCount++;
                    } else {
                        offlineCount++;
                    }
                }
            } else {
                // 降级处理：根据设备状态字段判断
                for (AreaDeviceEntity device : devices) {
                    if (device.getRelationStatus() == 1) {
                        onlineCount++;
                    } else {
                        offlineCount++;
                    }
                }
            }

            // 计算在线率
            double onlineRate = totalCount > 0 ? (double) onlineCount / totalCount * 100 : 0.0;

            // 按设备类型统计
            Map<String, Integer> deviceTypeCount = new HashMap<>();
            for (AreaDeviceEntity device : devices) {
                String deviceType = getDeviceTypeName(device.getDeviceType());
                deviceTypeCount.put(deviceType, deviceTypeCount.getOrDefault(deviceType, 0) + 1);
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("onlineCount", onlineCount);
            statistics.put("offlineCount", offlineCount);
            statistics.put("onlineRate", onlineRate);
            statistics.put("deviceTypeCount", deviceTypeCount);

            log.debug("[区域设备管理] 区域设备统计完成, areaId={}, totalCount={}, onlineCount={}, onlineRate={}%",
                    areaId, totalCount, onlineCount, onlineRate);

            return statistics;

        } catch (Exception e) {
            log.error("[区域设备管理] 获取区域设备统计异常, areaId={}", areaId, e);
            return Map.of(
                    "totalCount", 0,
                    "onlineCount", 0,
                    "offlineCount", 0,
                    "onlineRate", 0.0,
                    "deviceTypeCount", Map.of()
            );
        }
    }

    /**
     * 获取设备属性模板
     *
     * @param deviceType 设备类型
     * @param deviceSubType 设备子类型
     * @return 属性模板
     */
    public Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType) {
        log.debug("[区域设备管理] 获取设备属性模板, deviceType={}, deviceSubType={}", deviceType, deviceSubType);

        try {
            String deviceTypeStr = deviceType != null ? deviceType.toString() : "0";
            Map<String, Object> template = getDeviceBusinessTemplate(deviceTypeStr);

            // 根据子类型添加特定属性
            if (deviceSubType != null) {
                template.put("deviceSubType", deviceSubType);

                // 根据子类型添加特定配置
                switch (deviceSubType) {
                    case 11: // 门禁控制器
                        template.put("controllerType", "access");
                        template.put("maxUserCount", 10000);
                        break;
                    case 21: // 考勤机
                        template.put("attendanceMode", "normal");
                        template.put("photoRequired", true);
                        break;
                    case 31: // POS机
                        template.put("paymentMethods", List.of("card", "face", "nfc"));
                        template.put("offlineMode", true);
                        break;
                    default:
                        break;
                }
            }

            return template;

        } catch (Exception e) {
            log.error("[区域设备管理] 获取设备属性模板异常, deviceType={}, deviceSubType={}", deviceType, deviceSubType, e);
            return Map.of("enabled", true);
        }
    }

    /**
     * 获取设备类型名称
     */
    private String getDeviceTypeName(Integer deviceType) {
        if (deviceType == null) {
            return "UNKNOWN";
        }
        switch (deviceType) {
            case 1:
                return "ACCESS";
            case 2:
                return "ATTENDANCE";
            case 3:
                return "CONSUME";
            case 4:
                return "VIDEO";
            case 5:
                return "VISITOR";
            default:
                return "OTHER";
        }
    }

    /**
     * 获取设备关联的区域
     *
     * @param deviceId 设备ID
     * @return 区域列表
     */
    public List<AreaDeviceEntity> getDeviceAreas(String deviceId) {
        log.debug("[区域设备管理] 获取设备关联区域, deviceId={}", deviceId);

        return areaDeviceDao.selectByDeviceId(deviceId);
    }

    // ==================== 权限联动管理 ====================

    /**
     * 将区域内的人员权限同步到设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     */
    public void syncAreaUserPermissionsToDevice(Long areaId, String deviceId) {
        log.info("[区域设备管理] 同步区域用户权限到设备, areaId={}, deviceId={}", areaId, deviceId);

        try {
            // 1. 获取区域内所有有效用户
            List<AreaUserEntity> users = areaUserDao.selectByAreaId(areaId).stream()
                    .filter(user -> user.isEffective() && user.hasAccessPermission())
                    .collect(Collectors.toList());

            // 2. 过滤掉已经在设备上的用户（避免重复）
            List<AreaUserEntity> newUsers = filterNewUsersForDevice(deviceId, users);

            // 3. 批量同步用户到设备
            batchSyncUsersToDevice(deviceId, newUsers);

            // 4. 更新设备同步状态
            updateDeviceUserSyncStatus(deviceId, 2); // 同步成功

            log.info("[区域设备管理] 权限同步完成, areaId={}, deviceId={}, userCount={}",
                    areaId, deviceId, newUsers.size());

        } catch (Exception e) {
            log.error("[区域设备管理] 权限同步失败, areaId={}, deviceId={}", areaId, deviceId, e);

            // 更新设备同步状态为失败
            updateDeviceUserSyncStatus(deviceId, 3);
        }
    }

    /**
     * 撤销设备上的所有用户权限
     *
     * @param relation 区域设备关联
     */
    public void revokeDevicePermissions(AreaDeviceEntity relation) {
        log.info("[区域设备管理] 撤销设备权限, deviceId={}, areaId={}",
                relation.getDeviceId(), relation.getAreaId());

        try {
            // 1. 获取设备当前权限
            List<String> currentUsers = getDeviceUsers(relation.getDeviceId());

            // 2. 撤销用户权限
            for (String userId : currentUsers) {
                revokeUserFromDevice(relation.getDeviceId(), userId);
            }

            // 3. 更新同步状态
            updateDeviceUserSyncStatus(relation.getDeviceId(), 4); // 已撤销

            log.info("[区域设备管理] 设备权限撤销完成, deviceId={}", relation.getDeviceId());

        } catch (Exception e) {
            log.error("[区域设备管理] 设备权限撤销失败, deviceId={}", relation.getDeviceId(), e);
        }
    }

    /**
     * 检查用户是否有区域设备访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否有权限
     */
    public boolean hasDeviceAccessPermission(Long userId, Long areaId, String deviceId) {
        log.debug("[区域设备管理] 检查设备访问权限, userId={}, areaId={}, deviceId={}",
                 userId, areaId, deviceId);

        // 1. 检查用户是否有区域权限
        boolean hasAreaPermission = areaUserDao.hasPermission(userId, areaId, 1); // 最低权限级别
        if (!hasAreaPermission) {
            return false;
        }

        // 2. 检查设备是否在区域内
        AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (relation == null || relation.getRelationStatus() != 1) {
            return false;
        }

        // 3. 检查用户权限级别是否满足设备要求
        Integer userPermissionLevel = areaUserDao.selectUserAreaPermissions(userId, Arrays.asList(areaId))
                .stream()
                .filter(user -> user.getAreaId().equals(areaId))
                .mapToInt(AreaUserEntity::getPermissionLevel)
                .max()
                .orElse(0);

        return userPermissionLevel >= 1; // 最低权限要求
    }

    // ==================== 业务属性管理 ====================

    /**
     * 设置设备业务属性（重载方法，适配Service接口）
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param businessAttributes 业务属性
     */
    public void setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> businessAttributes) {
        setDeviceBusinessAttributes(areaId, deviceId, businessAttributes);
    }

    /**
     * 设置设备业务属性
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param businessAttributes 业务属性
     */
    public void setDeviceBusinessAttributes(Long areaId, String deviceId, Map<String, Object> businessAttributes) {
        log.debug("[区域设备管理] 设置设备业务属性, areaId={}, deviceId={}", areaId, deviceId);

        AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (relation == null) {
            throw new RuntimeException("设备未关联到该区域: " + deviceId);
        }

        // 将Map转换为JSON字符串
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String businessAttributesJson = objectMapper.writeValueAsString(businessAttributes);
            relation.setBusinessAttributes(businessAttributesJson);
        } catch (Exception e) {
            log.error("[区域设备管理] 转换业务属性为JSON失败, deviceId={}", deviceId, e);
            relation.setBusinessAttributes("{}"); // 设置空JSON对象
        }
        areaDeviceDao.updateById(relation);

        // 触发属性同步到设备
        syncBusinessAttributesToDevice(relation, businessAttributes);
    }

    /**
     * 获取设备业务属性（重载方法，适配Service接口）
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @return 业务属性
     */
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        return getDeviceBusinessAttributes(areaId, deviceId);
    }

    /**
     * 获取设备业务属性
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 业务属性
     */
    public Map<String, Object> getDeviceBusinessAttributes(Long areaId, String deviceId) {
        AreaDeviceEntity relation = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (relation == null || relation.getBusinessAttributes() == null) {
            return new HashMap<>();
        }

        // 将JSON字符串转换为Map
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(relation.getBusinessAttributes(),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (Exception e) {
            log.error("[区域设备管理] 转换业务属性从JSON失败, deviceId={}", deviceId, e);
            return new HashMap<>();
        }
    }

    /**
     * 应用设备业务属性模板
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    public void applyDeviceBusinessTemplate(Long areaId, String deviceId, String deviceType) {
        log.debug("[区域设备管理] 应用设备业务属性模板, areaId={}, deviceId={}, deviceType={}",
                 areaId, deviceId, deviceType);

        Map<String, Object> template = getDeviceBusinessTemplate(deviceType);
        if (template != null && !template.isEmpty()) {
            setDeviceBusinessAttributes(areaId, deviceId, template);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据设备类型获取业务模块
     */
    private String getBusinessModuleByDeviceType(String deviceType) {
        switch (deviceType.toUpperCase()) {
            case "ACCESS":
            case "BIOMETRIC":
                return "access";
            case "ATTENDANCE":
                return "attendance";
            case "CONSUME":
                return "consume";
            case "VIDEO":
                return "video";
            case "INTERCOM":
                return "visitor";
            default:
                return "common";
        }
    }

    /**
     * 获取继承权限的子区域设备
     * <p>
     * 递归获取所有子区域的设备
     * 支持递归深度限制，避免无限递归
     * </p>
     *
     * @param areaId 区域ID
     * @return 子区域设备列表
     */
    private List<AreaDeviceEntity> getInheritedDevices(Long areaId) {
        return getInheritedDevicesRecursive(areaId, 0);
    }

    /**
     * 递归获取子区域设备（内部方法）
     *
     * @param areaId 区域ID
     * @param depth 当前递归深度
     * @return 子区域设备列表
     */
    private List<AreaDeviceEntity> getInheritedDevicesRecursive(Long areaId, int depth) {
        // 递归深度限制
        if (depth >= MAX_RECURSION_DEPTH) {
            log.warn("[区域设备管理] 递归深度超过限制, areaId={}, depth={}", areaId, depth);
            return Collections.emptyList();
        }

        try {
            // 1. 获取子区域列表
            List<AreaEntity> childAreas;
            if (areaUnifiedService != null) {
                childAreas = areaUnifiedService.getChildAreas(areaId);
            } else {
                // 降级处理：使用AreaDao查询
                childAreas = areaDao.selectByParentId(areaId);
            }

            if (childAreas == null || childAreas.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 收集所有子区域的设备
            List<AreaDeviceEntity> allDevices = new ArrayList<>();
            for (AreaEntity childArea : childAreas) {
                // 获取当前子区域的设备
                List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(childArea.getId());
                if (devices != null) {
                    allDevices.addAll(devices);
                }

                // 递归获取子区域的子区域设备
                List<AreaDeviceEntity> grandChildDevices = getInheritedDevicesRecursive(childArea.getId(), depth + 1);
                allDevices.addAll(grandChildDevices);
            }

            // 3. 去重（按deviceId）
            Map<String, AreaDeviceEntity> uniqueDevices = new HashMap<>();
            for (AreaDeviceEntity device : allDevices) {
                String deviceId = device.getDeviceId();
                if (!uniqueDevices.containsKey(deviceId)) {
                    uniqueDevices.put(deviceId, device);
                }
            }

            return new ArrayList<>(uniqueDevices.values());

        } catch (Exception e) {
            log.error("[区域设备管理] 获取子区域设备异常, areaId={}, depth={}", areaId, depth, e);
            return Collections.emptyList();
        }
    }

    /**
     * 过滤设备新用户
     * <p>
     * 对比需要同步的用户列表和设备上的用户列表，过滤出新用户
     * 使用缓存机制减少设备用户列表查询次数
     * </p>
     *
     * @param deviceId 设备ID
     * @param users 需要同步的用户列表
     * @return 新用户列表（不在设备上的用户）
     */
    private List<AreaUserEntity> filterNewUsersForDevice(String deviceId, List<AreaUserEntity> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 1. 获取设备上的用户列表（带缓存）
            List<String> deviceUsers = getDeviceUsers(deviceId);

            // 2. 过滤出新用户（不在设备上的用户）
            Set<String> deviceUserSet = new HashSet<>(deviceUsers);
            List<AreaUserEntity> newUsers = users.stream()
                    .filter(user -> {
                        String userId = user.getUserId() != null ? user.getUserId().toString() : null;
                        return userId != null && !deviceUserSet.contains(userId);
                    })
                    .collect(Collectors.toList());

            log.debug("[区域设备管理] 过滤设备新用户, deviceId={}, totalUsers={}, deviceUsers={}, newUsers={}",
                    deviceId, users.size(), deviceUsers.size(), newUsers.size());

            return newUsers;

        } catch (Exception e) {
            log.error("[区域设备管理] 过滤设备新用户异常, deviceId={}", deviceId, e);
            // 降级处理：返回全部用户（安全策略：全部同步）
            return users;
        }
    }

    /**
     * 批量同步用户到设备
     */
    private void batchSyncUsersToDevice(String deviceId, List<AreaUserEntity> users) {
        for (AreaUserEntity user : users) {
            try {
                syncUserToDevice(deviceId, user.getUserId());
            } catch (Exception e) {
                log.error("[区域设备管理] 用户同步失败, deviceId={}, userId={}", deviceId, user.getUserId(), e);
            }
        }
    }

    /**
     * 同步单个用户到设备
     * <p>
     * 通过设备通讯服务同步用户信息到设备
     * 支持异步处理和状态回调
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     */
    private void syncUserToDevice(String deviceId, Long userId) {
        log.debug("[区域设备管理] 同步用户到设备, deviceId={}, userId={}", deviceId, userId);

        try {
            if (gatewayServiceClient == null) {
                log.warn("[区域设备管理] GatewayServiceClient未注入，无法同步用户信息");
                return;
            }

            // 1. 构建用户同步请求
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("deviceId", deviceId);
            syncRequest.put("userId", userId);
            syncRequest.put("timestamp", System.currentTimeMillis());

            // 2. 调用设备通讯服务同步用户信息接口
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/user/sync",
                    HttpMethod.POST,
                    syncRequest,
                    Map.class
            );

            // 3. 处理响应
            if (response != null && response.isSuccess()) {
                log.info("[区域设备管理] 用户同步成功, deviceId={}, userId={}", deviceId, userId);
            } else {
                log.warn("[区域设备管理] 用户同步失败, deviceId={}, userId={}, message={}",
                        deviceId, userId, response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("[区域设备管理] 用户同步异常, deviceId={}, userId={}", deviceId, userId, e);
        }
    }

    /**
     * 从设备撤销用户权限
     * <p>
     * 通过设备通讯服务撤销用户在设备上的权限
     * 支持幂等性操作
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     */
    private void revokeUserFromDevice(String deviceId, String userId) {
        log.debug("[区域设备管理] 从设备撤销用户权限, deviceId={}, userId={}", deviceId, userId);

        try {
            if (gatewayServiceClient == null) {
                log.warn("[区域设备管理] GatewayServiceClient未注入，无法撤销用户权限");
                return;
            }

            // 1. 调用设备通讯服务撤销用户权限接口
            // DELETE /api/v1/device/user/{deviceId}/{userId}
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/user/" + deviceId + "/" + userId,
                    HttpMethod.DELETE,
                    null,
                    Map.class
            );

            // 2. 处理响应
            if (response != null && response.isSuccess()) {
                log.info("[区域设备管理] 用户权限撤销成功, deviceId={}, userId={}", deviceId, userId);

                // 清除设备用户列表缓存
                deviceUsersCache.remove(deviceId);
                deviceUsersCacheTime.remove(deviceId);
            } else {
                log.warn("[区域设备管理] 用户权限撤销失败, deviceId={}, userId={}, message={}",
                        deviceId, userId, response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("[区域设备管理] 用户权限撤销异常, deviceId={}, userId={}", deviceId, userId, e);
        }
    }

    /**
     * 获取设备上的用户列表
     * <p>
     * 通过设备通讯服务查询设备上的用户ID列表
     * 使用缓存机制（5分钟）减少查询次数
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备上的用户ID列表
     */
    private List<String> getDeviceUsers(String deviceId) {
        try {
            // 1. 检查缓存
            Long cacheTime = deviceUsersCacheTime.get(deviceId);
            if (cacheTime != null && (System.currentTimeMillis() - cacheTime) < DEVICE_USERS_CACHE_EXPIRE_MS) {
                List<String> cachedUsers = deviceUsersCache.get(deviceId);
                if (cachedUsers != null) {
                    log.debug("[区域设备管理] 从缓存获取设备用户列表, deviceId={}, userCount={}", deviceId, cachedUsers.size());
                    return cachedUsers;
                }
            }

            // 2. 调用设备通讯服务查询接口
            if (gatewayServiceClient == null) {
                log.warn("[区域设备管理] GatewayServiceClient未注入，无法获取设备用户列表");
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            ResponseDTO<List<String>> response = (ResponseDTO<List<String>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/users/" + deviceId,
                    HttpMethod.GET,
                    null,
                    List.class
            );

            // 3. 处理响应
            List<String> deviceUsers = Collections.emptyList();
            if (response != null && response.isSuccess() && response.getData() != null) {
                // 类型转换
                Object data = response.getData();
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> userList = (List<Object>) data;
                    deviceUsers = userList.stream()
                            .map(userId -> userId != null ? userId.toString() : null)
                            .filter(userId -> userId != null)
                            .collect(Collectors.toList());
                }
            }

            // 4. 更新缓存
            deviceUsersCache.put(deviceId, deviceUsers);
            deviceUsersCacheTime.put(deviceId, System.currentTimeMillis());

            log.debug("[区域设备管理] 获取设备用户列表成功, deviceId={}, userCount={}", deviceId, deviceUsers.size());
            return deviceUsers;

        } catch (Exception e) {
            log.error("[区域设备管理] 获取设备用户列表异常, deviceId={}", deviceId, e);
            // 降级处理：返回空列表
            return Collections.emptyList();
        }
    }

    /**
     * 更新设备用户同步状态
     */
    private void updateDeviceUserSyncStatus(String deviceId, Integer syncStatus) {
        List<AreaDeviceEntity> relations = areaDeviceDao.selectByDeviceId(deviceId);
        for (AreaDeviceEntity relation : relations) {
            relation.setUserSyncStatus(syncStatus);
            relation.setLastUserSyncTime(LocalDateTime.now());
            areaDeviceDao.updateById(relation);
        }
    }

    /**
     * 同步业务属性到设备
     * <p>
     * 通过设备通讯服务同步业务属性到设备
     * 支持部分属性更新和版本控制
     * </p>
     *
     * @param relation 区域设备关联
     * @param businessAttributes 业务属性
     */
    private void syncBusinessAttributesToDevice(AreaDeviceEntity relation, Map<String, Object> businessAttributes) {
        log.debug("[区域设备管理] 同步业务属性到设备, deviceId={}, attributes={}",
                 relation.getDeviceId(), businessAttributes);

        try {
            if (gatewayServiceClient == null) {
                log.warn("[区域设备管理] GatewayServiceClient未注入，无法同步业务属性");
                return;
            }

            if (businessAttributes == null || businessAttributes.isEmpty()) {
                log.debug("[区域设备管理] 业务属性为空，跳过同步, deviceId={}", relation.getDeviceId());
                return;
            }

            // 1. 构建业务属性同步请求
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("deviceId", relation.getDeviceId());
            syncRequest.put("attributes", businessAttributes);
            syncRequest.put("timestamp", System.currentTimeMillis());
            syncRequest.put("version", System.currentTimeMillis()); // 版本号（使用时间戳）

            // 2. 调用设备通讯服务同步业务属性接口
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/business-attributes/sync",
                    HttpMethod.POST,
                    syncRequest,
                    Map.class
            );

            // 3. 处理响应
            if (response != null && response.isSuccess()) {
                log.info("[区域设备管理] 业务属性同步成功, deviceId={}, attributeCount={}",
                        relation.getDeviceId(), businessAttributes.size());
            } else {
                log.warn("[区域设备管理] 业务属性同步失败, deviceId={}, message={}",
                        relation.getDeviceId(), response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("[区域设备管理] 业务属性同步异常, deviceId={}", relation.getDeviceId(), e);
        }
    }

    /**
     * 获取设备业务属性模板
     */
    private Map<String, Object> getDeviceBusinessTemplate(String deviceType) {
        Map<String, Object> template = new HashMap<>();

        switch (deviceType.toUpperCase()) {
            case "ACCESS":
                template.put("accessMode", "card");
                template.put("antiPassback", true);
                template.put("openTime", 3000);
                template.put("allowVisitor", false);
                break;

            case "ATTENDANCE":
                template.put("workMode", "normal");
                template.put("locationVerify", true);
                template.put("photoCapture", true);
                template.put("allowOvertime", true);
                break;

            case "CONSUME":
                template.put("paymentMode", "card");
                template.put("allowOffline", true);
                template.put("receiptPrint", false);
                template.put("dailyLimit", 1000.0);
                break;

            case "VIDEO":
                template.put("resolution", "1080p");
                template.put("recordMode", "continuous");
                template.put("aiAnalysis", true);
                template.put("storageType", "cloud");
                break;

            default:
                template.put("enabled", true);
                break;
        }

        return template;
    }

    /**
     * 触发设备权限同步
     */
    private void triggerDevicePermissionSync(AreaDeviceEntity relation) {
        log.info("[区域设备管理] 触发设备权限同步, deviceId={}, areaId={}",
                relation.getDeviceId(), relation.getAreaId());

        // 异步同步权限
        // CompletableFuture.runAsync(() -> syncAreaUserPermissionsToDevice(relation.getAreaId(), relation.getDeviceId()));
    }
}
