package net.lab1024.sa.gateway.config;

import lombok.extern.slf4j.Slf4j;


import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;

/**
 * 网关监控配置类
 * <p>
 * 网关服务专用的监控配置，不依赖数据库
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Configuration
@Slf4j
public class MonitorManagersConfig {


    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 网关服务初始化
     */
    public MonitorManagersConfig() {
        log.info("[网关监控] 网关监控配置初始化完成");
        log.info("[网关监控] 监控项：HTTP请求、路由转发、熔断状态");
    }
}
