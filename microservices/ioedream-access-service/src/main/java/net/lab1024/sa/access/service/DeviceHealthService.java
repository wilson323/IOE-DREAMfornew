package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.access.domain.form.DeviceMonitorRequest;
import net.lab1024.sa.access.domain.form.MaintenancePredictRequest;
import net.lab1024.sa.access.domain.vo.DeviceHealthVO;
import net.lab1024.sa.access.domain.vo.DevicePerformanceAnalyticsVO;
import net.lab1024.sa.access.domain.vo.MaintenancePredictionVO;

/**
 * 设备健康监控服务接口
 * <p>
 * 提供设备健康状态监控、性能分析、预测性维护等功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回统一的业务对象
 * - 清晰的方法注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceHealthService {

    /**
     * 监控设备健康状态
     * <p>
     * 实时监控设备的各项健康指标：
     * - 设备在线状态检查
     * - 网络连接质量评估
     * - 响应时间测试
     * - 错误率统计
     * - 资源使用率监控
     * </p>
     *
     * @param request 设备监控请求参数
     * @return 设备健康状态详细信息
     */
    DeviceHealthVO monitorDeviceHealth(DeviceMonitorRequest request);

    /**
     * 获取设备性能分析
     * <p>
     * 基于历史数据分析设备性能趋势：
     * - 响应时间趋势分析
     * - 成功率变化趋势
     * - 负载峰值分析
     * - 性能瓶颈识别
     * - 优化建议生成
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备性能分析结果
     */
    DevicePerformanceAnalyticsVO getDevicePerformanceAnalytics(Long deviceId);

    /**
     * 预测设备维护需求
     * <p>
     * 基于AI算法预测设备的维护需求：
     * - 故障概率预测
     * - 维护时间窗口建议
     * - 维护优先级排序
     * - 维护成本预估
     * - 维护方案推荐
     * </p>
     *
     * @param request 维护预测请求参数
     * @return 维护预测结果列表
     */
    List<MaintenancePredictionVO> predictMaintenanceNeeds(MaintenancePredictRequest request);

    /**
     * 获取设备健康统计信息
     * <p>
     * 统计所有设备的健康状态分布：
     * - 健康设备数量和比例
     * - 亚健康设备数量和比例
     * - 故障设备数量和比例
     * - 离线设备数量和比例
     * - 设备类型健康分布
     * </p>
     *
     * @return 设备健康统计信息
     */
    Object getDeviceHealthStatistics();

    /**
     * 获取设备健康历史数据
     * <p>
     * 获取指定时间范围内的设备健康历史数据：
     * - 健康评分变化趋势
     * - 关键指标历史记录
     * - 异常事件记录
     * - 维护记录关联
     * </p>
     *
     * @param deviceId 设备ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 分页的历史健康数据
     */
    PageResult<Object> getDeviceHealthHistory(Long deviceId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 更新设备健康状态
     * <p>
     * 定时任务调用的健康状态更新：
     * - 批量健康检查
     * - 状态变化通知
     * - 异常告警触发
     * </p>
     *
     * @param deviceIds 设备ID列表，为空时检查所有设备
     */
    void updateDeviceHealthStatus(List<Long> deviceIds);
}