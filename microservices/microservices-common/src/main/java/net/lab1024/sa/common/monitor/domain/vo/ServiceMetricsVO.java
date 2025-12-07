package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

/**
 * 服务指标VO
 * 整合自ioedream-monitor-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Data
public class ServiceMetricsVO {

    private String serviceName;
    private Long qps;
    private Long tps;
    private Double avgResponseTime;
    private Double errorRate;
    private Integer activeConnections;
}

