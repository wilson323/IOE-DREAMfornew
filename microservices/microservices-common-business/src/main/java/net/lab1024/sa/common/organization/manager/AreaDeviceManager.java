package net.lab1024.sa.common.organization.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 区域设备关联管理器
 * 负责管理区域与设备之间的双向关联关系
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RequiredArgsConstructor
public class AreaDeviceManager {

    private final AreaDeviceDao areaDeviceDao;
    private final ObjectMapper objectMapper;

    // 关联状态常量
    public static class RelationStatus {
        public static final int NORMAL = 1;      // 正常
        public static final int MAINTENANCE = 2;  // 维护中
        public static final int FAULT = 3;        // 故障
        public static final int OFFLINE = 4;      // 离线
        public static final int DISABLED = 5;      // 停用
    }

    // 设备优先级常量
    public static class DevicePriority {
        public static final int PRIMARY = 1;     // 主设备
        public static final int SECONDARY = 2;   // 辅助设备
        public static final int BACKUP = 3;      // 备用设备
    }

    // 业务模块常量
    public static class BusinessModule {
        public static final String ACCESS = "access";       // 门禁
        public static final String ATTENDANCE = "attendance"; // 考勤
        public static final String CONSUME = "consume";     // 消费
        public static final String VIDEO = "video";         // 视频
        public static final String VISITOR = "visitor";     // 访客
    }

    /**
     * 添加设备到区域
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param deviceSubType 设备子类型
     * @param businessModule 业务模块
     * @param priority 优先级
     * @return 关联ID
     */
    public String addDeviceToArea(Long areaId, String deviceId, String deviceCode, String deviceName,
                                   Integer deviceType, Integer deviceSubType, String businessModule,
                                   Integer priority) {
        // 检查设备是否已经在该区域中
        AreaDeviceEntity existing = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (existing != null) {
            log.warn("设备已存在于区域中, areaId={}, deviceId={}", areaId, deviceId);
            return existing.getRelationId();
        }

        AreaDeviceEntity entity = new AreaDeviceEntity();
        entity.setRelationId(UUID.randomUUID().toString());
        entity.setAreaId(areaId);
        entity.setDeviceId(deviceId);
        entity.setDeviceCode(deviceCode);
        entity.setDeviceName(deviceName);
        entity.setDeviceType(deviceType);
        entity.setDeviceSubType(deviceSubType);
        entity.setBusinessModule(businessModule);
        entity.setPriority(priority != null ? priority : DevicePriority.SECONDARY);
        entity.setRelationStatus(RelationStatus.NORMAL);
        entity.setEnabled(true);
        entity.setEffectiveTime(LocalDateTime.now());

        areaDeviceDao.insert(entity);

        log.info("添加设备到区域成功, relationId={}, areaId={}, deviceId={}",
                entity.getRelationId(), areaId, deviceId);

        return entity.getRelationId();
    }

    /**
     * 从区域移除设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否成功
     */
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        AreaDeviceEntity entity = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (entity == null) {
            log.warn("设备未在区域中找到, areaId={}, deviceId={}", areaId, deviceId);
            return false;
        }

        entity.setExpireTime(LocalDateTime.now());
        entity.setRelationStatus(RelationStatus.DISABLED);
        areaDeviceDao.updateById(entity);

        log.info("从区域移除设备成功, areaId={}, deviceId={}", areaId, deviceId);
        return true;
    }

    /**
     * 移动设备到新区域
     *
     * @param deviceId 设备ID
     * @param oldAreaId 原区域ID
     * @param newAreaId 新区域ID
     * @return 是否成功
     */
    public boolean moveDeviceToArea(String deviceId, Long oldAreaId, Long newAreaId) {
        // 检查新区域是否已有该设备
        AreaDeviceEntity existing = areaDeviceDao.selectByAreaIdAndDeviceId(newAreaId, deviceId);
        if (existing != null) {
            log.warn("设备已存在于新区域中, deviceId={}, newAreaId={}", deviceId, newAreaId);
            return false;
        }

        // 获取原区域关联信息
        AreaDeviceEntity oldRelation = areaDeviceDao.selectByAreaIdAndDeviceId(oldAreaId, deviceId);
        if (oldRelation == null) {
            log.warn("设备未在原区域中找到, deviceId={}, oldAreaId={}", deviceId, oldAreaId);
            return false;
        }

        // 在原区域中过期旧关联
        oldRelation.setExpireTime(LocalDateTime.now());
        oldRelation.setRelationStatus(RelationStatus.DISABLED);
        areaDeviceDao.updateById(oldRelation);

        // 在新区域中创建新关联
        AreaDeviceEntity newRelation = new AreaDeviceEntity();
        newRelation.setRelationId(UUID.randomUUID().toString());
        newRelation.setAreaId(newAreaId);
        newRelation.setDeviceId(oldRelation.getDeviceId());
        newRelation.setDeviceCode(oldRelation.getDeviceCode());
        newRelation.setDeviceName(oldRelation.getDeviceName());
        newRelation.setDeviceType(oldRelation.getDeviceType());
        newRelation.setDeviceSubType(oldRelation.getDeviceSubType());
        newRelation.setBusinessModule(oldRelation.getBusinessModule());
        newRelation.setPriority(oldRelation.getPriority());
        newRelation.setRelationStatus(RelationStatus.NORMAL);
        newRelation.setEnabled(true);
        newRelation.setEffectiveTime(LocalDateTime.now());
        newRelation.setBusinessAttributes(oldRelation.getBusinessAttributes());

        areaDeviceDao.insert(newRelation);

        log.info("移动设备到新区域成功, deviceId={}, oldAreaId={}, newAreaId={}",
                deviceId, oldAreaId, newAreaId);

        return true;
    }

    /**
     * 批量添加设备到区域
     *
     * @param areaId 区域ID
     * @param deviceRequests 设备请求列表
     * @return 成功添加的数量
     */
    public int batchAddDevicesToArea(Long areaId, List<DeviceRequest> deviceRequests) {
        int successCount = 0;

        for (DeviceRequest request : deviceRequests) {
            try {
                addDeviceToArea(areaId, request.getDeviceId(), request.getDeviceCode(),
                        request.getDeviceName(), request.getDeviceType(), request.getDeviceSubType(),
                        request.getBusinessModule(), request.getPriority());
                successCount++;
            } catch (Exception e) {
                log.error("批量添加设备失败, deviceId={}", request.getDeviceId(), e);
            }
        }

        log.info("批量添加设备完成, areaId={}, 成功数量={}", areaId, successCount);
        return successCount;
    }

    /**
     * 设置设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param attributes 业务属性
     * @return 是否成功
     */
    public boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes) {
        AreaDeviceEntity entity = areaDeviceDao.selectByAreaIdAndDeviceId(areaId, deviceId);
        if (entity == null) {
            log.warn("设备关联不存在, deviceId={}, areaId={}", deviceId, areaId);
            return false;
        }

        try {
            String attributesJson = objectMapper.writeValueAsString(attributes);
            entity.setBusinessAttributes(attributesJson);
            areaDeviceDao.updateById(entity);

            log.info("设置设备业务属性成功, deviceId={}, areaId={}", deviceId, areaId);
            return true;
        } catch (JsonProcessingException e) {
            log.error("序列化业务属性失败", e);
            return false;
        }
    }

    /**
     * 获取设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @return 业务属性
     */
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        String attributesJson = areaDeviceDao.selectDeviceBusinessAttributes(deviceId, areaId);
        if (attributesJson == null) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(attributesJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("反序列化业务属性失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param status 新状态
     * @return 是否成功
     */
    public boolean updateDeviceRelationStatus(String relationId, Integer status) {
        int result = areaDeviceDao.updateRelationStatus(relationId, status);
        if (result > 0) {
            log.info("更新设备关联状态成功, relationId={}, status={}", relationId, status);
            return true;
        }
        return false;
    }

    /**
     * 批量更新区域设备状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @return 更新的设备数量
     */
    public int batchUpdateAreaDeviceStatus(Long areaId, Integer status) {
        int result = areaDeviceDao.batchUpdateRelationStatusByAreaId(areaId, status);
        log.info("批量更新区域设备状态完成, areaId={}, status={}, 更新数量={}", areaId, status, result);
        return result;
    }

    /**
     * 检查设备是否在区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否在区域中
     */
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        return areaDeviceDao.isDeviceInArea(areaId, deviceId);
    }

    /**
     * 获取区域的所有设备
     *
     * @param areaId 区域ID
     * @return 设备关联列表
     */
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        return areaDeviceDao.selectByAreaId(areaId);
    }

    /**
     * 获取区域指定类型的设备
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备关联列表
     */
    public List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType) {
        return areaDeviceDao.selectByAreaIdAndDeviceType(areaId, deviceType);
    }

    /**
     * 获取区域指定业务模块的设备
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 设备关联列表
     */
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        return areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, businessModule);
    }

    /**
     * 获取区域的主设备
     *
     * @param areaId 区域ID
     * @return 主设备列表
     */
    public List<AreaDeviceEntity> getAreaPrimaryDevices(Long areaId) {
        return areaDeviceDao.selectPrimaryDevicesByAreaId(areaId);
    }

    /**
     * 获取设备所属的所有区域
     *
     * @param deviceId 设备ID
     * @return 区域关联列表
     */
    public List<AreaDeviceEntity> getDeviceAreas(String deviceId) {
        return areaDeviceDao.selectByDeviceId(deviceId);
    }

    /**
     * 获取设备统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    public AreaDeviceStatistics getAreaDeviceStatistics(Long areaId) {
        AreaDeviceStatistics statistics = new AreaDeviceStatistics();

        // 获取基础统计
        List<Map<String, Object>> statsList = areaDeviceDao.selectDeviceStatisticsByAreaId(areaId);
        int totalCount = 0;
        int onlineCount = 0;
        int primaryCount = 0;

        for (Map<String, Object> stats : statsList) {
            totalCount += ((Number) stats.get("device_count")).intValue();
            onlineCount += ((Number) stats.get("online_count")).intValue();
            primaryCount += ((Number) stats.get("primary_count")).intValue();
        }

        statistics.setTotalCount(totalCount);
        statistics.setOnlineCount(onlineCount);
        statistics.setOfflineCount(totalCount - onlineCount);
        statistics.setPrimaryCount(primaryCount);
        statistics.setSecondaryCount(totalCount - primaryCount);

        // 按类型统计
        Map<Integer, Integer> typeCountMap = new HashMap<>();
        Map<Integer, Integer> subtypeCountMap = new HashMap<>();

        for (Map<String, Object> stats : statsList) {
            Integer deviceType = (Integer) stats.get("device_type");
            Integer deviceSubType = (Integer) stats.get("device_sub_type");
            Integer deviceCount = ((Number) stats.get("device_count")).intValue();

            typeCountMap.merge(deviceType, deviceCount, Integer::sum);
            subtypeCountMap.merge(deviceSubType, deviceCount, Integer::sum);
        }

        statistics.setTypeStatistics(typeCountMap);
        statistics.setSubtypeStatistics(subtypeCountMap);

        return statistics;
    }

    /**
     * 获取业务模块设备分布
     *
     * @param businessModule 业务模块
     * @return 分布统计
     */
    public List<ModuleDeviceDistribution> getModuleDeviceDistribution(String businessModule) {
        List<Map<String, Object>> distributionList = areaDeviceDao.selectDeviceDistributionByModule(businessModule);
        List<ModuleDeviceDistribution> result = new ArrayList<>();

        for (Map<String, Object> distribution : distributionList) {
            ModuleDeviceDistribution item = new ModuleDeviceDistribution();
            item.setDeviceType((Integer) distribution.get("device_type"));
            item.setDeviceCount(((Number) distribution.get("device_count")).intValue());
            item.setAreaCount(((Number) distribution.get("area_count")).intValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户有权限访问的设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块
     * @return 设备列表
     */
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        return areaDeviceDao.selectDevicesByUserPermission(userId, businessModule);
    }

    /**
     * 设置设备为过期状态
     *
     * @param deviceId 设备ID
     * @return 过期的关联数量
     */
    public int expireDeviceRelations(String deviceId) {
        int result = areaDeviceDao.expireDeviceRelations(deviceId);
        log.info("设备关联已过期, deviceId={}, 过期数量={}", deviceId, result);
        return result;
    }

    /**
     * 获取区域的在线设备数量
     *
     * @param areaId 区域ID
     * @return 在线设备数量
     */
    public int getAreaOnlineDeviceCount(Long areaId) {
        return areaDeviceDao.countOnlineDevicesByAreaId(areaId);
    }

    /**
     * 设备请求
     */
    public static class DeviceRequest {
        private String deviceId;
        private String deviceCode;
        private String deviceName;
        private Integer deviceType;
        private Integer deviceSubType;
        private String businessModule;
        private Integer priority;

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceCode() { return deviceCode; }
        public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public Integer getDeviceType() { return deviceType; }
        public void setDeviceType(Integer deviceType) { this.deviceType = deviceType; }
        public Integer getDeviceSubType() { return deviceSubType; }
        public void setDeviceSubType(Integer deviceSubType) { this.deviceSubType = deviceSubType; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    /**
     * 区域设备统计信息
     */
    public static class AreaDeviceStatistics {
        private Integer totalCount;
        private Integer onlineCount;
        private Integer offlineCount;
        private Integer primaryCount;
        private Integer secondaryCount;
        private Map<Integer, Integer> typeStatistics;
        private Map<Integer, Integer> subtypeStatistics;

        // getters and setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        public Integer getOnlineCount() { return onlineCount; }
        public void setOnlineCount(Integer onlineCount) { this.onlineCount = onlineCount; }
        public Integer getOfflineCount() { return offlineCount; }
        public void setOfflineCount(Integer offlineCount) { this.offlineCount = offlineCount; }
        public Integer getPrimaryCount() { return primaryCount; }
        public void setPrimaryCount(Integer primaryCount) { this.primaryCount = primaryCount; }
        public Integer getSecondaryCount() { return secondaryCount; }
        public void setSecondaryCount(Integer secondaryCount) { this.secondaryCount = secondaryCount; }
        public Map<Integer, Integer> getTypeStatistics() { return typeStatistics; }
        public void setTypeStatistics(Map<Integer, Integer> typeStatistics) { this.typeStatistics = typeStatistics; }
        public Map<Integer, Integer> getSubtypeStatistics() { return subtypeStatistics; }
        public void setSubtypeStatistics(Map<Integer, Integer> subtypeStatistics) { this.subtypeStatistics = subtypeStatistics; }
    }

    /**
     * 模块设备分布
     */
    public static class ModuleDeviceDistribution {
        private String businessModule;
        private String moduleName;
        private Long areaId;
        private String areaName;
        private Integer deviceType;
        private Integer deviceCount;
        private Integer areaCount;

        // getters and setters
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Integer getDeviceType() { return deviceType; }
        public void setDeviceType(Integer deviceType) { this.deviceType = deviceType; }
        public Integer getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
        public Integer getAreaCount() { return areaCount; }
        public void setAreaCount(Integer areaCount) { this.areaCount = areaCount; }
    }
}
