package net.lab1024.sa.system.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.system.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.system.domain.form.UnifiedDeviceAddForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceQueryForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceUpdateForm;

/**
 * 统一设备管理服务接口
 *
 * @author IOE-DREAM Team
 */
public interface UnifiedDeviceService {

    /**
     * 分页查询设备
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<UnifiedDeviceEntity> queryDevicePage(UnifiedDeviceQueryForm queryForm);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    UnifiedDeviceEntity getDeviceDetail(Long deviceId);

    /**
     * 新增设备
     *
     * @param addForm 设备信息
     * @return 设备ID
     */
    Long addDevice(UnifiedDeviceAddForm addForm);

    /**
     * 更新设备
     *
     * @param updateForm 更新信息
     */
    void updateDevice(UnifiedDeviceUpdateForm updateForm);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     */
    void deleteDevice(Long deviceId);

    /**
     * 启用设备
     *
     * @param deviceId 设备ID
     */
    void enableDevice(Long deviceId);

    /**
     * 禁用设备
     *
     * @param deviceId 设备ID
     */
    void disableDevice(Long deviceId);

    /**
     * 更新设备状态
     *
     * @param deviceId     设备ID
     * @param deviceStatus 设备状态
     */
    void updateDeviceStatus(Long deviceId, String deviceStatus);

    /**
     * 设备心跳上报
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean reportDeviceHeartbeat(Long deviceId);

    /**
     * 远程控制设备
     *
     * @param deviceId 设备ID
     * @param command  控制命令
     * @param params   控制参数
     * @return 控制结果
     */
    String remoteControlDevice(Long deviceId, String command, Map<String, Object> params);

    /**
     * 远程开门
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    String remoteOpenDoor(Long deviceId);

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command  云台命令
     * @param speed    控制速度
     * @return 控制结果
     */
    String controlPtzDevice(Long deviceId, String command, Integer speed);

    /**
     * 获取实时视频流
     *
     * @param deviceId 设备ID
     * @return 视频流URL
     */
    String getLiveStream(Long deviceId);

    /**
     * 获取设备状态统计
     *
     * @param deviceType 设备类型
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatusStatistics(String deviceType);

    /**
     * 获取设备类型统计
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 获取在线设备列表
     *
     * @param deviceType 设备类型
     * @return 在线设备列表
     */
    List<UnifiedDeviceEntity> getOnlineDevices(String deviceType);

    /**
     * 获取离线设备列表
     *
     * @param deviceType 设备类型
     * @return 离线设备列表
     */
    List<UnifiedDeviceEntity> getOfflineDevices(String deviceType);

    /**
     * 批量操作设备
     *
     * @param deviceIds 设备ID列表
     * @param operation 操作类型
     * @return 操作结果
     */
    String batchOperateDevices(List<Long> deviceIds, String operation);
}
