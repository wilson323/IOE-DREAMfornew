package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区域设备管理服务接口
 * <p>
 * 核心职责：管理区域与设备的关联关系，支撑各业务场景
 * </p>
 * <p>
 * 主要功能：
 * - 区域设备关联管理
 * - 设备权限控制
 * - 设备状态监控
 * - 设备业务属性管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
public interface AreaDeviceManager {

    /**
     * 添加设备到区域
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param businessModule 业务模块
     * @return 操作结果
     */
    boolean addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                             String deviceName, Integer deviceType, String businessModule);

    /**
     * 从区域移除设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    boolean removeDeviceFromArea(Long areaId, String deviceId);

    /**
     * 批量添加设备到区域
     *
     * @param areaId 区域ID
     * @param deviceRelations 设备关联列表
     * @return 操作结果
     */
    boolean batchAddDevicesToArea(Long areaId, List<AreaDeviceEntity> deviceRelations);

    /**
     * 获取区域的所有设备
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<AreaDeviceEntity> getAreaDevices(Long areaId);

    /**
     * 获取区域的指定类型设备
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType);

    /**
     * 获取区域的指定业务模块设备
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 设备列表
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
     * 检查设备是否在区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否在区域中
     */
    boolean isDeviceInArea(Long areaId, String deviceId);

    /**
     * 获取设备所属的所有区域
     *
     * @param deviceId 设备ID
     * @return 区域关联列表
     */
    List<AreaDeviceEntity> getDeviceAreas(String deviceId);

    /**
     * 根据用户权限获取可访问的设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块（可选）
     * @return 可访问设备列表
     */
    List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule);

    /**
     * 获取设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID（可选）
     * @return 业务属性Map
     */
    Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId);

    /**
     * 设置设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID（可选）
     * @param attributes 业务属性
     * @return 设置结果
     */
    boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes);

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean updateDeviceRelationStatus(String relationId, Integer status);

    /**
     * 批量更新区域设备状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean batchUpdateDeviceStatusByArea(Long areaId, Integer status);

    /**
     * 设置设备为过期状态
     *
     * @param deviceId 设备ID
     * @return 设置结果
     */
    boolean expireDeviceRelations(String deviceId);

    /**
     * 获取区域设备统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaDeviceStatistics(Long areaId);

    /**
     * 获取业务模块设备分布统计
     *
     * @param businessModule 业务模块
     * @return 分布统计
     */
    List<Map<String, Object>> getModuleDeviceDistribution(String businessModule);

    /**
     * 检查设备是否支持指定业务
     *
     * @param deviceId 设备ID
     * @param businessModule 业务模块
     * @return 是否支持
     */
    boolean isDeviceSupportBusiness(String deviceId, String businessModule);

    /**
     * 获取设备支持的所有业务模块
     *
     * @param deviceId 设备ID
     * @return 支持的业务模块列表
     */
    Set<String> getDeviceSupportedBusinessModules(String deviceId);

    /**
     * 同步设备状态到区域关联
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 同步结果
     */
    boolean syncDeviceStatusToAreas(String deviceId, String deviceStatus);

    /**
     * 根据设备类型获取标准业务属性模板
     *
     * @param deviceType 设备类型
     * @param deviceSubType 设备子类型
     * @return 业务属性模板
     */
    Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType);

    /**
     * 验证设备关联配置
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @param businessModule 业务模块
     * @return 验证结果
     */
    Map<String, String> validateDeviceRelation(Long areaId, String deviceId, String businessModule);

    /**
     * 获取区域的设备部署建议
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 部署建议
     */
    List<String> getAreaDeviceDeploymentSuggestions(Long areaId, String businessModule);

    /**
     * 检查设备关联的完整性
     *
     * @param relationId 关联ID
     * @return 检查结果
     */
    Map<String, Object> checkDeviceRelationIntegrity(String relationId);

    /**
     * 同步区域用户权限到设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     */
    default void syncAreaUserPermissionsToDevice(Long areaId, String deviceId) {
        // 默认空实现，由具体实现类覆盖
    }

    /**
     * 检查用户是否有设备访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否有权限
     */
    default boolean hasDeviceAccessPermission(Long userId, Long areaId, String deviceId) {
        // 默认返回true，由具体实现类覆盖
        return true;
    }
}
