package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务指标视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class ServiceMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * QPS（每秒请求数）
     */
    private Long qps;

    /**
     * TPS（每秒事务数）
     */
    private Long tps;

    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;

    /**
     * 错误率
     */
    private Double errorRate;

    /**
     * 活跃连接数
     */
    private Integer activeConnections;
}

