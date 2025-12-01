package net.lab1024.sa.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 智能设备服务接口 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承IService使用MyBatis-Plus
 * - 命名规范：{Module}Service
 * - 完整的CRUD操作和业务方法
 * - 支持设备管理和状态监控
 * - 统一的返回类型ResponseDTO
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
public interface SmartDeviceService extends IService<SmartDeviceEntity> {

    /**
     * 分页查询设备列表
     *
     * @param pageParam    分页参数
     * @param deviceType   设备类型
     * @param deviceStatus 设备状态
     * @param deviceName   设备名称
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
     * @param operation 操作类型 (enable/disable/delete)
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
     * @param deviceId     设备ID
     * @param deviceStatus 设备状态
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus);

    /**
     * 远程控制设备
     *
     * @param deviceId 设备ID
     * @param command  控制命令
     * @param params   命令参数
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
     * @param config   配置信息
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

    /**
     * 获取设备状态统计
     *
     * @return 状态统计信息
     */
    Map<String, Object> getDeviceStatusStatistics();

    /**
     * 获取设备类型统计
     *
     * @return 类型统计信息
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 根据设备编码获取设备信息
     *
     * @param deviceCode 设备编码
     * @return 设备信息
     */
    SmartDeviceEntity getDeviceByCode(String deviceCode);

    /**
     * 根据设备类型获取设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<SmartDeviceEntity> getDevicesByType(String deviceType);

    /**
     * 根据分组ID获取设备列表
     *
     * @param groupId 分组ID
     * @return 设备列表
     */
    List<SmartDeviceEntity> getDevicesByGroup(Long groupId);

    /**
     * 检查设备编码是否已存在
     *
     * @param deviceCode 设备编码
     * @return 是否存在
     */
    boolean checkDeviceCodeExists(String deviceCode);

    /**
     * 批量同步设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 同步结果
     */
    ResponseDTO<String> batchSyncDeviceStatus(List<Long> deviceIds);

    /**
     * 获取设备完整统计信息
     *
     * @return 完整统计信息
     */
    Map<String, Object> getFullDeviceStatistics();

    /**
     * 更新设备最后在线时间
     *
     * @param deviceId        设备ID
     * @param lastOnlineTime 最后在线时间
     * @return 是否成功
     */
    boolean updateLastOnlineTime(Long deviceId, java.time.LocalDateTime lastOnlineTime);

    /**
     * 获取需要维护的设备列表
     *
     * @return 需要维护的设备列表
     */
    List<SmartDeviceEntity> getMaintenanceDevices();

    /**
     * 获取故障设备列表
     *
     * @return 故障设备列表
     */
    List<SmartDeviceEntity> getFaultDevices();
}