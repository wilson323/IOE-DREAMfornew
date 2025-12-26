package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.entity.DeviceHealthEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备健康检查服务接口
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface DeviceHealthService {

    /**
     * 执行设备健康检查
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    DeviceHealthEntity performHealthCheck(Long deviceId);

    /**
     * 获取设备最新健康状态
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    DeviceHealthEntity getLatestHealth(Long deviceId);

    /**
     * 获取设备健康历史
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 健康历史记录
     */
    List<DeviceHealthEntity> getHealthHistory(Long deviceId, Integer limit);

    /**
     * 获取健康统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getHealthStatistics();

    /**
     * 获取告警记录
     *
     * @param alarmLevel 告警级别
     * @return 告警记录列表
     */
    List<DeviceHealthEntity> getAlarmRecords(Integer alarmLevel);

    /**
     * 批量执行健康检查
     *
     * @param deviceIds 设备ID列表
     * @return 健康检查结果列表
     */
    List<DeviceHealthEntity> batchPerformHealthCheck(List<Long> deviceIds);

    /**
     * 分页查询健康记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param deviceId 设备ID（可选）
     * @param healthStatus 健康状态（可选）
     * @return 分页结果
     */
    PageResult<DeviceHealthEntity> queryPage(Integer pageNum, Integer pageSize,
                                              Long deviceId, Integer healthStatus);
}
