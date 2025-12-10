package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;

import java.util.List;
import java.util.Map;

/**
 * 区域设备服务接口
 * <p>
 * 企业级区域设备管理服务，支持设备关联、状态监控、权限管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
public interface AreaDeviceService {

    /**
     * 添加设备到区域
     *
     * @param areaId         区域ID
     * @param deviceId       设备ID
     * @param deviceCode     设备编码
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param businessModule 业务模块
     * @return 关联结果
     */
    AreaDeviceEntity addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                                   String deviceName, Integer deviceType, String businessModule);

    /**
     * 移除区域中的设备
     *
     * @param areaId   区域ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    boolean removeDeviceFromArea(Long areaId, String deviceId);

    /**
     * 获取区域中的所有设备
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<AreaDeviceEntity> getAreaDevices(Long areaId);

    /**
     * 获取区域中指定业务模块的设备
     *
     * @param areaId         区域ID
     * @param businessModule 业务模块
     * @return 设备列表
     */
    List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule);

    /**
     * 获取用户可访问的设备
     *
     * @param userId        用户ID
     * @param businessModule 业务模块（可选）
     * @return 设备列表
     */
    List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule);

    /**
     * 检查设备是否在区域中
     *
     * @param areaId   区域ID
     * @param deviceId 设备ID
     * @return 是否在区域中
     */
    boolean isDeviceInArea(Long areaId, String deviceId);

    /**
     * 设置设备业务属性
     *
     * @param deviceId         设备ID
     * @param areaId           区域ID
     * @param businessAttributes 业务属性
     */
    void setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> businessAttributes);

    /**
     * 获取设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId   区域ID（可选）
     * @return 业务属性
     */
    Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId);

    /**
     * 更新设备状态
     *
     * @param relationId     关联ID
     * @param relationStatus 关联状态
     */
    void updateDeviceRelationStatus(String relationId, Integer relationStatus);

    /**
     * 获取区域设备统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaDeviceStatistics(Long areaId);

    /**
     * 获取设备属性模板
     *
     * @param deviceType     设备类型
     * @param deviceSubType 设备子类型
     * @return 属性模板
     */
    Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType);

    /**
     * 同步区域用户权限到设备
     *
     * @param areaId   区域ID
     * @param deviceId 设备ID
     */
    void syncAreaUserPermissionsToDevice(Long areaId, String deviceId);

    /**
     * 检查用户是否有设备访问权限
     *
     * @param userId   用户ID
     * @param areaId   区域ID
     * @param deviceId 设备ID
     * @return 是否有权限
     */
    boolean hasDeviceAccessPermission(Long userId, Long areaId, String deviceId);
}