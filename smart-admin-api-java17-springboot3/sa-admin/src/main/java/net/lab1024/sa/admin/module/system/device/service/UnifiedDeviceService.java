package net.lab1024.sa.admin.module.system.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 统一设备管理服务接口
 * <p>
 * 严格遵循repowiki业务架构规范：
 * - 统一设备管理，消除SmartDeviceService、VideoDeviceService、AccessDeviceService重复逻辑
 * - 支持多种设备类型：门禁设备、视频设备、消费设备、考勤设备等
 * - 四层架构：Controller → Service → Manager → DAO
 * - 事件驱动架构支持
 * - 领域驱动设计模式
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface UnifiedDeviceService extends IService<UnifiedDeviceEntity> {

    /**
     * 统一设备类型枚举
     */
    enum DeviceType {
        ACCESS("ACCESS", "门禁设备"),
        VIDEO("VIDEO", "视频设备"),
        CONSUME("CONSUME", "消费设备"),
        ATTENDANCE("ATTENDANCE", "考勤设备"),
        SMART("SMART", "智能设备");

        private final String code;
        private final String description;

        DeviceType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 分页查询设备列表（统一接口，支持所有设备类型）
     *
     * @param pageParam 分页参数
     * @param deviceType 设备类型（可选，用于过滤）
     * @param deviceStatus 设备状态（可选）
     * @param deviceName 设备名称（可选）
     * @param areaId 区域ID（可选，用于门禁设备）
     * @return 分页结果
     */
    PageResult<UnifiedDeviceEntity> queryDevicePage(PageParam pageParam, String deviceType,
                                                   String deviceStatus, String deviceName, Long areaId);

    /**
     * 注册新设备（统一接口，支持所有设备类型）
     *
     * @param deviceEntity 设备实体
     * @return 操作结果
     */
    ResponseDTO<String> registerDevice(UnifiedDeviceEntity deviceEntity);

    /**
     * 更新设备信息（统一接口）
     *
     * @param deviceEntity 设备实体
     * @return 操作结果
     */
    ResponseDTO<String> updateDevice(UnifiedDeviceEntity deviceEntity);

    /**
     * 删除设备（统一接口，软删除）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> deleteDevice(Long deviceId);

    /**
     * 获取设备详情（统一接口，根据设备类型返回详细信息）
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ResponseDTO<UnifiedDeviceEntity> getDeviceDetail(Long deviceId);

    /**
     * 启用设备（统一接口）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> enableDevice(Long deviceId);

    /**
     * 禁用设备（统一接口）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> disableDevice(Long deviceId);

    /**
     * 批量操作设备（统一接口）
     *
     * @param deviceIds 设备ID列表
     * @param operation 操作类型（ENABLE、DISABLE、DELETE等）
     * @return 操作结果
     */
    ResponseDTO<String> batchOperateDevices(List<Long> deviceIds, String operation);

    /**
     * 设备心跳检测（统一接口）
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean deviceHeartbeat(Long deviceId);

    /**
     * 更新设备状态（统一接口）
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus);

    /**
     * 获取设备状态统计（统一接口，支持按设备类型分组）
     *
     * @param deviceType 设备类型（可选）
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatusStatistics(String deviceType);

    /**
     * 获取设备类型统计（统一接口）
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 远程控制设备（统一接口，根据设备类型调用不同的控制逻辑）
     *
     * @param deviceId 设备ID
     * @param command 控制命令
     * @param params 参数
     * @return 操作结果
     */
    ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params);

    /**
     * 获取设备配置（统一接口，根据设备类型返回不同的配置信息）
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId);

    /**
     * 更新设备配置（统一接口）
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config);

    /**
     * 获取在线设备列表（统一接口，支持设备类型过滤）
     *
     * @param deviceType 设备类型（可选）
     * @return 在线设备列表
     */
    List<UnifiedDeviceEntity> getOnlineDevices(String deviceType);

    /**
     * 获取离线设备列表（统一接口，支持设备类型过滤）
     *
     * @param deviceType 设备类型（可选）
     * @return 离线设备列表
     */
    List<UnifiedDeviceEntity> getOfflineDevices(String deviceType);

    // ========== 门禁设备专用方法 ==========

    /**
     * 远程开门（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> remoteOpenDoor(Long deviceId);

    /**
     * 重启设备（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> restartDevice(Long deviceId);

    /**
     * 同步设备时间（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> syncDeviceTime(Long deviceId);

    // ========== 视频设备专用方法 ==========

    /**
     * 云台控制（视频设备专用）
     *
     * @param deviceId 设备ID
     * @param command 控制命令（UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT）
     * @param speed 控制速度
     * @return 控制结果
     */
    ResponseDTO<String> ptzControl(Long deviceId, String command, Integer speed);

    /**
     * 启动设备录像（视频设备专用）
     *
     * @param deviceId 设备ID
     * @return 录像ID
     */
    ResponseDTO<Long> startRecording(Long deviceId);

    /**
     * 停止设备录像（视频设备专用）
     *
     * @param recordId 录像ID
     * @return 操作结果
     */
    ResponseDTO<String> stopRecording(Long recordId);

    /**
     * 获取实时视频流（视频设备专用）
     *
     * @param deviceId 设备ID
     * @return 视频流地址
     */
    ResponseDTO<String> getLiveStream(Long deviceId);

    // ========== 高级功能方法 ==========

    /**
     * 获取设备健康状态（统一接口）
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    ResponseDTO<Map<String, Object>> getDeviceHealthStatus(Long deviceId);

    /**
     * 获取需要维护的设备列表（统一接口）
     *
     * @param deviceType 设备类型（可选）
     * @return 需要维护的设备列表
     */
    List<UnifiedDeviceEntity> getDevicesNeedingMaintenance(String deviceType);

    /**
     * 获取心跳超时的设备列表（统一接口）
     *
     * @param deviceType 设备类型（可选）
     * @return 心跳超时设备列表
     */
    List<UnifiedDeviceEntity> getHeartbeatTimeoutDevices(String deviceType);

    /**
     * 更新设备工作模式（统一接口，主要用于门禁设备）
     *
     * @param deviceId 设备ID
     * @param workMode 工作模式
     * @return 操作结果
     */
    ResponseDTO<String> updateDeviceWorkMode(Long deviceId, Integer workMode);

    /**
     * 根据区域获取设备列表（统一接口）
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型（可选）
     * @return 设备列表
     */
    List<UnifiedDeviceEntity> getDevicesByAreaId(Long areaId, String deviceType);

    /**
     * 设备状态变更事件发布（事件驱动架构支持）
     *
     * @param deviceId 设备ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    void publishDeviceStatusChangeEvent(Long deviceId, String oldStatus, String newStatus);

    /**
     * 设备配置变更事件发布（事件驱动架构支持）
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     */
    void publishDeviceConfigChangeEvent(Long deviceId, Map<String, Object> config);
}