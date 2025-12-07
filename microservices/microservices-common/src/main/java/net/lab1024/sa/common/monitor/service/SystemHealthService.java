package net.lab1024.sa.common.monitor.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;

/**
 * 系统健康监控服务接口
 * <p>
 * 负责系统运行状态监控、健康检查、性能指标监控等功能
 * 严格遵循CLAUDE.md规范:
 * - Service层处理核心业务逻辑
 * - 事务管理
 * - 业务规则验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
public interface SystemHealthService {

    /**
     * 获取系统健康概览
     *
     * @return 系统健康概览信息
     */
    SystemHealthVO getSystemHealthOverview();

    /**
     * 获取组件健康状态
     *
     * @return 各组件健康状态列表
     */
    List<Map<String, Object>> getComponentHealthStatus();

    /**
     * 获取系统性能指标
     *
     * @return 系统性能指标
     */
    Map<String, Object> getSystemMetrics();

    /**
     * 获取资源使用情况
     *
     * @return 资源使用情况
     */
    Map<String, Object> getResourceUsage();

    /**
     * 获取微服务状态
     *
     * @return 微服务状态列表
     */
    List<Map<String, Object>> getMicroservicesStatus();

    /**
     * 获取数据库状态
     *
     * @return 数据库状态信息
     */
    Map<String, Object> getDatabaseStatus();

    /**
     * 获取缓存状态
     *
     * @return 缓存状态信息
     */
    Map<String, Object> getCacheStatus();

    /**
     * 执行健康检查
     *
     * @param component 组件名称
     * @return 健康检查结果
     */
    Map<String, Object> performHealthCheck(String component);

    /**
     * 获取活跃告警
     *
     * @return 活跃告警列表
     */
    List<Map<String, Object>> getActiveAlerts();

    /**
     * 获取系统运行时间
     *
     * @return 运行时间统计
     */
    Map<String, Object> getSystemUptime();

    /**
     * 获取健康趋势
     *
     * @param hours 时间范围（小时）
     * @return 健康趋势数据
     */
    List<Map<String, Object>> getHealthTrends(Integer hours);

    /**
     * 解决告警
     *
     * @param alertId    告警ID
     * @param resolution 解决说明
     */
    void resolveAlert(Long alertId, String resolution);
}
