package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.manager.AreaDeviceManager;

import java.util.List;
import java.util.Map;

/**
 * 区域设备关联管理服务接口
 * 提供区域与设备的双向关联管理功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface AreaDeviceService {

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
    String addDeviceToArea(Long areaId, String deviceId, String deviceCode, String deviceName,
                           Integer deviceType, Integer deviceSubType, String businessModule, Integer priority);

    /**
     * 从区域移除设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean removeDeviceFromArea(Long areaId, String deviceId);

    /**
     * 移动设备到新区域
     *
     * @param deviceId 设备ID
     * @param oldAreaId 原区域ID
     * @param newAreaId 新区域ID
     * @return 是否成功
     */
    boolean moveDeviceToArea(String deviceId, Long oldAreaId, Long newAreaId);

    /**
     * 批量添加设备到区域
     *
     * @param areaId 区域ID
     * @param deviceRequests 设备请求列表
     * @return 成功添加的数量
     */
    int batchAddDevicesToArea(Long areaId, List<AreaDeviceManager.DeviceRequest> deviceRequests);

    /**
     * 设置设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param attributes 业务属性
     * @return 是否成功
     */
    boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes);

    /**
     * 获取设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @return 业务属性
     */
    Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId);

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateDeviceRelationStatus(String relationId, Integer status);

    /**
     * 批量更新区域设备状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @return 更新的设备数量
     */
    int batchUpdateAreaDeviceStatus(Long areaId, Integer status);

    /**
     * 检查设备是否在区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否在区域中
     */
    boolean isDeviceInArea(Long areaId, String deviceId);

    /**
     * 获取区域的所有设备
     *
     * @param areaId 区域ID
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> getAreaDevices(Long areaId);

    /**
     * 获取区域指定类型的设备
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType);

    /**
     * 获取区域指定业务模块的设备
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule);

    /**
     * 获取区域的主设备
     *
     * @param areaId 区域ID
     * @return 主设备列表
     */
    List<AreaDeviceEntity> getAreaPrimaryDevices(Long areaId);

    /**
     * 获取设备所属的所有区域
     *
     * @param deviceId 设备ID
     * @return 区域关联列表
     */
    List<AreaDeviceEntity> getDeviceAreas(String deviceId);

    /**
     * 获取设备统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    AreaDeviceManager.AreaDeviceStatistics getAreaDeviceStatistics(Long areaId);

    /**
     * 获取业务模块设备分布
     *
     * @param businessModule 业务模块
     * @return 分布统计
     */
    List<AreaDeviceManager.ModuleDeviceDistribution> getModuleDeviceDistribution(String businessModule);

    /**
     * 获取用户有权限访问的设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块
     * @return 设备列表
     */
    List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule);

    /**
     * 设置设备为过期状态
     *
     * @param deviceId 设备ID
     * @return 过期的关联数量
     */
    int expireDeviceRelations(String deviceId);

    /**
     * 获取区域的在线设备数量
     *
     * @param areaId 区域ID
     * @return 在线设备数量
     */
    int getAreaOnlineDeviceCount(Long areaId);

    /**
     * 获取所有模块的设备分布统计
     *
     * @return 各模块设备分布
     */
    Map<String, List<AreaDeviceManager.ModuleDeviceDistribution>> getAllModuleDeviceDistribution();

    /**
     * 批量移动设备到新区域
     *
     * @param moveRequests 移动请求列表
     * @return 成功移动的数量
     */
    int batchMoveDevices(List<DeviceMoveRequest> moveRequests);

    /**
     * 根据设备编码获取所属区域
     *
     * @param deviceCode 设备编码
     * @return 区域关联列表
     */
    List<AreaDeviceEntity> getDeviceAreasByCode(String deviceCode);

    /**
     * 获取区域设备健康状态统计
     *
     * @param areaId 区域ID
     * @return 健康状态统计
     */
    AreaDeviceHealthStatistics getAreaDeviceHealthStatistics(Long areaId);

    /**
     * 设备移动请求
     */
    class DeviceMoveRequest {
        private String deviceId;
        private Long oldAreaId;
        private Long newAreaId;

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public Long getOldAreaId() { return oldAreaId; }
        public void setOldAreaId(Long oldAreaId) { this.oldAreaId = oldAreaId; }
        public Long getNewAreaId() { return newAreaId; }
        public void setNewAreaId(Long newAreaId) { this.newAreaId = newAreaId; }
    }

    /**
     * 区域设备健康状态统计
     */
    class AreaDeviceHealthStatistics {
        private int totalDevices;
        private int normalDevices;
        private int maintenanceDevices;
        private int faultDevices;
        private int offlineDevices;
        private int disabledDevices;
        private double healthRate;

        // getters and setters
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public int getNormalDevices() { return normalDevices; }
        public void setNormalDevices(int normalDevices) { this.normalDevices = normalDevices; }
        public int getMaintenanceDevices() { return maintenanceDevices; }
        public void setMaintenanceDevices(int maintenanceDevices) { this.maintenanceDevices = maintenanceDevices; }
        public int getFaultDevices() { return faultDevices; }
        public void setFaultDevices(int faultDevices) { this.faultDevices = faultDevices; }
        public int getOfflineDevices() { return offlineDevices; }
        public void setOfflineDevices(int offlineDevices) { this.offlineDevices = offlineDevices; }
        public int getDisabledDevices() { return disabledDevices; }
        public void setDisabledDevices(int disabledDevices) { this.disabledDevices = disabledDevices; }
        public double getHealthRate() { return healthRate; }
        public void setHealthRate(double healthRate) { this.healthRate = healthRate; }
    }
}