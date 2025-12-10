package net.lab1024.sa.common.monitor.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.monitor.domain.vo.ServiceMetricsVO;
import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;

/**
 * 监控服务接口
 * 整合自ioedream-monitor-service
 *
 * 功能职责：
 * - 系统健康监控
 * - 服务性能监控
 * - 业务指标监控
 * - 告警规则管理
 * - 告警通知发送
 *
 * 企业级特性：
 * - 实时监控指标采集
 * - 多维度性能分析
 * - 智能告警规则
 * - 告警聚合和降噪
 * - 监控大盘展示
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
public interface MonitorService {

    /**
     * 获取系统健康状态
     *
     * 企业级特性：
     * - CPU、内存、磁盘监控
     * - JVM堆内存监控
     * - 线程池监控
     * - 数据库连接池监控
     */
    ResponseDTO<SystemHealthVO> getSystemHealth();

    /**
     * 获取服务性能指标
     *
     * 企业级特性：
     * - QPS/TPS统计
     * - 响应时间分布
     * - 错误率统计
     * - 慢查询统计
     */
    ResponseDTO<ServiceMetricsVO> getServiceMetrics(String serviceName);

    /**
     * 获取业务指标
     *
     * 企业级特性：
     * - 自定义业务指标
     * - 实时数据采集
     * - 趋势分析
     */
    ResponseDTO<Map<String, Object>> getBusinessMetrics(String metricName);

    /**
     * 创建告警规则
     */
    ResponseDTO<Long> createAlertRule(AlertRuleVO ruleVO);

    /**
     * 触发告警
     *
     * 企业级特性：
     * - 告警聚合
     * - 告警降噪
     * - 多渠道通知
     * - 告警升级
     */
    void triggerAlert(String alertType, String alertMessage, Integer severity);

    /**
     * 获取告警历史
     */
    ResponseDTO<List<Map<String, Object>>> getAlertHistory(Integer limit);

    /**
     * 记录性能指标
     *
     * 企业级特性：
     * - 异步记录
     * - 批量写入
     * - 数据聚合
     */
    void recordMetric(String metricName, Double value, Map<String, String> tags);
}
