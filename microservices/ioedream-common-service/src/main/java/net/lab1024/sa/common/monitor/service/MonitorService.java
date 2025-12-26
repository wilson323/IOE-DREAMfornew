package net.lab1024.sa.common.monitor.service;

import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.monitor.domain.vo.ServiceMetricsVO;
import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;

/**
 * 监控服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public interface MonitorService {

    /**
     * 获取系统健康状态
     *
     * @return 系统健康状态VO
     */
    ResponseDTO<SystemHealthVO> getSystemHealth();

    /**
     * 获取服务指标
     *
     * @param serviceName 服务名称
     * @return 服务指标VO
     */
    ResponseDTO<ServiceMetricsVO> getServiceMetrics(String serviceName);

    /**
     * 获取业务指标
     *
     * @param metricName 指标名称
     * @return 业务指标Map
     */
    ResponseDTO<Map<String, Object>> getBusinessMetrics(String metricName);

    /**
     * 创建告警规则
     *
     * @param ruleVO 告警规则VO
     * @return 告警规则ID
     */
    ResponseDTO<Long> createAlertRule(AlertRuleVO ruleVO);
}

