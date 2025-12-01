package net.lab1024.sa.device.service;

import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备服务接口 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的门禁设备管理方法
 * - 支持设备控制和状态监控
 * - 统一的业务接口设计
 * - 完整的CRUD操作支持
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
public interface AccessDeviceService {

    /**
     * 分页查询门禁设备列表
     */
    PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam, Long deviceId, Long areaId,
                                                  String deviceName, Integer accessDeviceType,
                                                  Integer onlineStatus, Integer enabled);

    /**
     * 根据ID获取门禁设备详情
     */
    AccessDeviceEntity getDeviceById(Long accessDeviceId);

    /**
     * 获取指定区域下的设备列表
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
     * 添加门禁设备
     */
    void addDevice(AccessDeviceEntity device);

    /**
     * 更新门禁设备
     */
    void updateDevice(AccessDeviceEntity device);

    /**
     * 删除门禁设备
     */
    void deleteDevice(Long accessDeviceId);

    /**
     * 批量删除门禁设备
     */
    void batchDeleteDevices(List<Long> accessDeviceIds);

    /**
     * 更新设备在线状态
     */
    void updateDeviceOnlineStatus(Long accessDeviceId, Integer onlineStatus);

    /**
     * 批量更新设备在线状态
     */
    void batchUpdateDeviceOnlineStatus(List<Long> accessDeviceIds, Integer onlineStatus);

    /**
     * 远程开门
     */
    boolean remoteOpenDoor(Long accessDeviceId);

    /**
     * 重启设备
     */
    boolean restartDevice(Long accessDeviceId);

    /**
     * 获取设备统计信息
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取设备健康状态
     */
    Map<String, Object> getDeviceHealthStatus(Long accessDeviceId);

    /**
     * 设备心跳上报
     */
    void deviceHeartbeat(Long accessDeviceId, Map<String, Object> heartbeatData);

    /**
     * 同步设备时间
     */
    boolean syncDeviceTime(Long accessDeviceId);

    /**
     * 获取需要维护的设备
     */
    List<AccessDeviceEntity> getDevicesNeedingMaintenance();

    /**
     * 获取心跳超时设备
     */
    List<AccessDeviceEntity> getHeartbeatTimeoutDevices();

    /**
     * 更新设备工作模式
     */
    void updateDeviceWorkMode(Long accessDeviceId, Integer workMode);
}