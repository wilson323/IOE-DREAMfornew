package net.lab1024.sa.admin.module.smart.access.service;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备管理服务接口
 * <p>
 * 严格遵循repowiki规范：
 * - 接口定义清晰，职责单一
 * - 方法命名遵循RESTful规范
 * - 完整的业务逻辑封装
 * - 设备状态管理和缓存支持
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface AccessDeviceService {

    /**
     * 分页查询设备列表
     *
     * @param pageParam 分页参数
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param deviceName 设备名称
     * @param accessDeviceType 设备类型
     * @param onlineStatus 在线状态
     * @param enabled 启用状态
     * @return 分页结果
     */
    PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam, Long deviceId, Long areaId, 
                                                  String deviceName, Integer accessDeviceType, 
                                                  Integer onlineStatus, Integer enabled);

    /**
     * 根据ID获取设备详情
     *
     * @param accessDeviceId 设备ID
     * @return 设备详情
     */
    AccessDeviceEntity getDeviceById(Long accessDeviceId);

    /**
     * 获取区域下的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<AccessDeviceEntity> getDevicesByAreaId(Long areaId);

    /**
     * 获取在线设备列表
     *
     * @return 在线设备列表
     */
    List<AccessDeviceEntity> getOnlineDevices();

    /**
     * 获取离线设备列表
     *
     * @return 离线设备列表
     */
    List<AccessDeviceEntity> getOfflineDevices();

    /**
     * 创建设备
     *
     * @param device 设备信息
     */
    void addDevice(AccessDeviceEntity device);

    /**
     * 更新设备信息
     *
     * @param device 设备信息
     */
    void updateDevice(AccessDeviceEntity device);

    /**
     * 删除设备
     *
     * @param accessDeviceId 设备ID
     */
    void deleteDevice(Long accessDeviceId);

    /**
     * 批量删除设备
     *
     * @param accessDeviceIds 设备ID列表
     */
    void batchDeleteDevices(List<Long> accessDeviceIds);

    /**
     * 更新设备在线状态
     *
     * @param accessDeviceId 设备ID
     * @param onlineStatus 在线状态
     */
    void updateDeviceOnlineStatus(Long accessDeviceId, Integer onlineStatus);

    /**
     * 批量更新设备在线状态
     *
     * @param accessDeviceIds 设备ID列表
     * @param onlineStatus 在线状态
     */
    void batchUpdateDeviceOnlineStatus(List<Long> accessDeviceIds, Integer onlineStatus);

    /**
     * 远程开门
     *
     * @param accessDeviceId 设备ID
     * @return 操作结果
     */
    boolean remoteOpenDoor(Long accessDeviceId);

    /**
     * 重启设备
     *
     * @param accessDeviceId 设备ID
     * @return 操作结果
     */
    boolean restartDevice(Long accessDeviceId);

    /**
     * 获取设备统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取设备健康状态
     *
     * @param accessDeviceId 设备ID
     * @return 健康状态
     */
    Map<String, Object> getDeviceHealthStatus(Long accessDeviceId);

    /**
     * 设备心跳上报
     *
     * @param accessDeviceId 设备ID
     * @param heartbeatData 心跳数据
     */
    void deviceHeartbeat(Long accessDeviceId, Map<String, Object> heartbeatData);

    /**
     * 同步设备时间
     *
     * @param accessDeviceId 设备ID
     * @return 操作结果
     */
    boolean syncDeviceTime(Long accessDeviceId);

    /**
     * 获取需要维护的设备列表
     *
     * @return 需要维护的设备列表
     */
    List<AccessDeviceEntity> getDevicesNeedingMaintenance();

    /**
     * 获取心跳超时的设备列表
     *
     * @return 心跳超时设备列表
     */
    List<AccessDeviceEntity> getHeartbeatTimeoutDevices();

    /**
     * 更新设备工作模式
     *
     * @param accessDeviceId 设备ID
     * @param workMode 工作模式
     */
    void updateDeviceWorkMode(Long accessDeviceId, Integer workMode);
}
