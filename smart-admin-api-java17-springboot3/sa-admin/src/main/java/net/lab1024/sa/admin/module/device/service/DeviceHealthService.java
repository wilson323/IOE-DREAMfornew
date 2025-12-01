package net.lab1024.sa.admin.module.device.service;

import net.lab1024.sa.admin.module.device.domain.vo.DeviceHealthOverviewVO;
import net.lab1024.sa.admin.module.device.domain.vo.DeviceHealthTrendVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备健康诊断服务接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
public interface DeviceHealthService {

    /**
     * 获取设备健康总览
     */
    ResponseDTO<DeviceHealthOverviewVO> getHealthOverview(Long deviceId);

    /**
     * 获取所有设备健康总览列表
     */
    ResponseDTO<PageResult<DeviceHealthOverviewVO>> getHealthOverviewList(Integer pageNum, Integer pageSize, Long areaId, String healthLevel);

    /**
     * 获取设备健康趋势
     */
    ResponseDTO<DeviceHealthTrendVO> getHealthTrend(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 计算设备健康评分
     */
    ResponseDTO<BigDecimal> calculateHealthScore(Long deviceId);

    /**
     * 创建设备健康快照
     */
    ResponseDTO<Void> createHealthSnapshot(Long deviceId);

    /**
     * 批量创建设备健康快照
     */
    ResponseDTO<Integer> batchCreateHealthSnapshots(List<Long> deviceIds);

    /**
     * 获取健康评分统计
     */
    ResponseDTO<Map<String, Object>> getHealthScoreStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取健康等级分布
     */
    ResponseDTO<Map<String, Object>> getHealthLevelDistribution();

    /**
     * 获取需要关注的设备列表
     */
    ResponseDTO<List<DeviceHealthOverviewVO>> getAttentionDevices(BigDecimal scoreThreshold);

    /**
     * 预测设备维护时间
     */
    ResponseDTO<LocalDateTime> predictMaintenanceTime(Long deviceId);

    /**
     * 获取设备健康配置
     */
    ResponseDTO<Map<String, Object>> getHealthConfig(Long deviceId);

    /**
     * 更新设备健康配置
     */
    ResponseDTO<Void> updateHealthConfig(Long deviceId, Map<String, Object> config);

    /**
     * 导出健康报告
     */
    ResponseDTO<String> exportHealthReport(List<Long> deviceIds, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 触发健康检查任务
     */
    ResponseDTO<Void> triggerHealthCheck();

    /**
     * 获取健康数据采集状态
     */
    ResponseDTO<Map<String, Object>> getHealthCollectionStatus();

    /**
     * 手动采集设备指标数据
     */
    ResponseDTO<Void> collectDeviceMetrics(Long deviceId);

    /**
     * 获取设备健康诊断建议
     */
    ResponseDTO<List<String>> getHealthDiagnosticSuggestions(Long deviceId);

    /**
     * 清理历史健康数据
     */
    ResponseDTO<Integer> cleanHealthHistoryData(Integer retentionDays);
}