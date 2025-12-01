package net.lab1024.sa.admin.module.smart.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.admin.module.smart.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 智能设备服务接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
public interface SmartDeviceService extends IService<SmartDeviceEntity> {

    /**
     * 分页查询设备列表
     *
     * @param pageParam 分页参数
     * @param deviceType 设备类型
     * @param deviceStatus 设备状态
     * @param deviceName 设备名称
     * @return 分页结果
     */
    PageResult<SmartDeviceEntity> queryDevicePage(PageParam pageParam, String deviceType, String deviceStatus, String deviceName);

    /**
     * 注册新设备
     *
     * @param deviceEntity 设备实体
     * @return 操作结果
     */
    ResponseDTO<String> registerDevice(SmartDeviceEntity deviceEntity);

    /**
     * 更新设备信息
     *
     * @param deviceEntity 设备实体
     * @return 操作结果
     */
    ResponseDTO<String> updateDevice(SmartDeviceEntity deviceEntity);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> deleteDevice(Long deviceId);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ResponseDTO<SmartDeviceEntity> getDeviceDetail(Long deviceId);

    /**
     * 启用设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> enableDevice(Long deviceId);

    /**
     * 禁用设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> disableDevice(Long deviceId);

    /**
     * 批量操作设备
     *
     * @param deviceIds 设备ID列表
     * @param operation 操作类型
     * @return 操作结果
     */
    ResponseDTO<String> batchOperateDevices(List<Long> deviceIds, String operation);

    /**
     * 设备心跳检测
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean deviceHeartbeat(Long deviceId);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus);

    /**
     * 获取设备状态统计
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatusStatistics();

    /**
     * 获取设备类型统计
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 远程控制设备
     *
     * @param deviceId 设备ID
     * @param command 控制命令
     * @param params 参数
     * @return 操作结果
     */
    ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params);

    /**
     * 获取设备配置
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId);

    /**
     * 更新设备配置
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config);

    /**
     * 获取在线设备列表
     *
     * @return 在线设备列表
     */
    List<SmartDeviceEntity> getOnlineDevices();

    /**
     * 获取离线设备列表
     *
     * @return 离线设备列表
     */
    List<SmartDeviceEntity> getOfflineDevices();
}