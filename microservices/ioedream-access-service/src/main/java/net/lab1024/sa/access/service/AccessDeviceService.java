package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.access.service.SmartDeviceService;
import net.lab1024.sa.access.domain.entity.SmartAccessAlertEntity;
import net.lab1024.sa.access.domain.entity.SmartDeviceAccessExtensionEntity;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.access.domain.vo.AccessDeviceDetailVO;
import net.lab1024.sa.access.domain.vo.AccessDeviceStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessDeviceStatusVO;
import net.lab1024.sa.access.domain.vo.AccessControlConfigVO;

/**
 * 门禁设备管理服务接口
 * <p>
 * 严格遵循repowiki规范：
 * - 继承现有SmartDeviceService，扩展门禁特有功能
 * - 遵循四层架构规范：Controller→Service→Manager→DAO
 * - 职责单一：专门处理门禁设备业务逻辑
 * - 支持设备控制、状态同步、配置管理等核心功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface AccessDeviceService extends SmartDeviceService {

    // ==================== 设备扩展配置管理 ====================

    /**
     * 获取门禁设备扩展配置
     *
     * @param deviceId 设备ID
     * @return 门禁设备扩展配置
     */
    SmartDeviceAccessExtensionEntity getAccessExtension(Long deviceId);

    /**
     * 更新门禁设备扩展配置
     *
     * @param deviceId 设备ID
     * @param extension 扩展配置信息
     * @return 操作结果
     */
    ResponseDTO<String> updateAccessExtension(Long deviceId, SmartDeviceAccessExtensionEntity extension);

    /**
     * 获取门禁控制配置
     *
     * @param deviceId 设备ID
     * @return 门禁控制配置
     */
    AccessControlConfigVO getAccessControlConfig(Long deviceId);

    /**
     * 更新门禁控制配置
     *
     * @param deviceId 设备ID
     * @param config 门禁控制配置
     * @return 操作结果
     */
    ResponseDTO<String> updateAccessControlConfig(Long deviceId, AccessControlConfigVO config);

    // ==================== 设备控制操作 ====================

    /**
     * 远程开门
     * 扩展基础设备管理，增加门禁特有控制功能
     *
     * @param deviceId 设备ID
     * @param doorId 门ID（可选，设备单门时可省略）
     * @return 操作结果
     */
    ResponseDTO<String> remoteOpenDoor(Long deviceId, Long doorId);

    /**
     * 远程锁门
     *
     * @param deviceId 设备ID
     * @param doorId 门ID（可选，设备单门时可省略）
     * @return 操作结果
     */
    ResponseDTO<String> remoteLockDoor(Long deviceId, Long doorId);

    /**
     * 批量远程开门
     *
     * @param deviceIds 设备ID列表
     * @return 批量操作结果
     */
    ResponseDTO<Map<Long, String>> batchRemoteOpenDoor(List<Long> deviceIds);

    // ==================== 设备状态同步 ====================

    /**
     * 同步设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    ResponseDTO<AccessDeviceStatusVO> syncDeviceStatus(Long deviceId);

    /**
     * 批量同步设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 批量同步结果
     */
    ResponseDTO<Map<Long, AccessDeviceStatusVO>> batchSyncDeviceStatus(List<Long> deviceIds);

    /**
     * 获取设备最后心跳时间
     *
     * @param deviceId 设备ID
     * @return 最后心跳时间
     */
    LocalDateTime getLastHeartbeatTime(Long deviceId);

    // ==================== 设备验证功能 ====================

    /**
     * 验证设备是否支持门禁功能
     *
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateAccessDevice(Long deviceId);

    /**
     * 验证设备配置完整性
     *
     * @param deviceId 设备ID
     * @return 验证结果和缺失配置项
     */
    ResponseDTO<List<String>> validateDeviceConfig(Long deviceId);

    /**
     * 检查设备安全状态
     *
     * @param deviceId 设备ID
     * @return 安全检查结果
     */
    ResponseDTO<Map<String, Object>> checkDeviceSecurity(Long deviceId);

    // ==================== 设备详情查询 ====================

    /**
     * 获取门禁设备详细信息
     * 包含基础设备信息和门禁扩展信息
     *
     * @param deviceId 设备ID
     * @return 设备详细信息
     */
    ResponseDTO<AccessDeviceDetailVO> getAccessDeviceDetail(Long deviceId);

    // ==================== 设备统计功能 ====================

    /**
     * 获取门禁设备统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<AccessDeviceStatisticsVO> getAccessDeviceStatistics();

    /**
     * 按区域统计设备数量
     *
     * @param areaIds 区域ID列表（可选）
     * @return 区域设备统计
     */
    ResponseDTO<Map<Long, Integer>> countDevicesByArea(List<Long> areaIds);

    // ==================== 设备告警管理 ====================

    /**
     * 获取设备最新告警
     *
     * @param deviceId 设备ID
     * @param limit 告警数量限制
     * @return 告警列表
     */
    ResponseDTO<List<SmartAccessAlertEntity>> getDeviceLatestAlerts(Long deviceId, Integer limit);

    /**
     * 清除设备告警
     *
     * @param alertIds 告警ID列表
     * @return 操作结果
     */
    ResponseDTO<String> clearDeviceAlerts(List<Long> alertIds);

    /**
     * 获取设备告警统计
     *
     * @param deviceId 设备ID
     * @param days 统计天数
     * @return 告警统计
     */
    ResponseDTO<Map<String, Integer>> getDeviceAlertStatistics(Long deviceId, Integer days);

    // ==================== 与现有设备管理集成 ====================

    /**
     * 批量设置门禁设备
     * 继承现有设备管理功能，同时设置门禁扩展信息
     *
     * @param deviceIds 设备ID列表
     * @param accessType 门禁类型
     * @return 操作结果
     */
    ResponseDTO<String> batchSetAccessDevices(List<Long> deviceIds, String accessType);

    /**
     * 移除设备的门禁功能
     * 保留基础设备信息，只删除门禁扩展配置
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> removeAccessFunction(Long deviceId);

    /**
     * 检查设备门禁功能冲突
     * 确保与现有设备管理无冲突
     *
     * @param deviceId 设备ID
     * @return 冲突检查结果
     */
    ResponseDTO<List<String>> checkAccessFunctionConflict(Long deviceId);

    // ==================== 额外缺失的方法 ====================

    /**
     * 设备心跳
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     * @return 操作结果
     */
    Boolean deviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData);

    /**
     * 同步设备时间
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    Boolean syncDeviceTime(Long deviceId);

    /**
     * 获取需要维护的设备列表
     *
     * @return 设备列表
     */
    List<AccessDeviceEntity> getDevicesNeedingMaintenance();

    /**
     * 获取心跳超时的设备列表
     *
     * @return 设备列表
     */
    List<AccessDeviceEntity> getHeartbeatTimeoutDevices();

    /**
     * 更新设备工作模式
     *
     * @param deviceId 设备ID
     * @param workMode 工作模式
     * @return 操作结果
     */
    Boolean updateDeviceWorkMode(Long deviceId, Integer workMode);

    // ==================== 控制器要求的方法 ====================

    /**
     * 分页查询设备列表
     */
    PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam, Long deviceId, Long areaId, String deviceName, Integer accessDeviceType, Integer onlineStatus, Integer enabled);

    /**
     * 根据区域ID获取设备列表
     */
    List<AccessDeviceEntity> getDevicesByAreaId(Long areaId);

    /**
     * 获取在线设备列表
     */
    List<AccessDeviceEntity> getOnlineDevices();

    /**
     * 获取离线设备列表
     */
    List<AccessDeviceEntity> getOfflineDevices();

    /**
     * 更新设备在线状态
     */
    void updateDeviceOnlineStatus(Long deviceId, Integer onlineStatus);

    /**
     * 批量更新设备在线状态
     */
    void batchUpdateDeviceOnlineStatus(List<Long> deviceIds, Integer onlineStatus);

    /**
     * 远程开门（单参数版本）
     */
    Boolean remoteOpenDoor(Long deviceId);

    /**
     * 重启设备
     */
    Boolean restartDevice(Long deviceId);

    /**
     * 获取设备统计信息
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取设备健康状态
     */
    Map<String, Object> getDeviceHealthStatus(Long deviceId);

    /**
     * 根据ID获取设备
     */
    AccessDeviceEntity getDeviceById(Long deviceId);

    /**
     * 添加设备
     */
    ResponseDTO<String> addDevice(AccessDeviceEntity device);

    /**
     * 更新设备
     */
    ResponseDTO<String> updateDevice(AccessDeviceEntity device);

    /**
     * 删除设备
     */
    ResponseDTO<String> deleteDevice(Long deviceId);

    /**
     * 批量删除设备
     */
    ResponseDTO<String> batchDeleteDevices(List<Long> deviceIds);
}