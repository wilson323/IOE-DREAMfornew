package net.lab1024.sa.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;

/**
 * HealthCheckManager企业级配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册HealthCheckManager为Spring Bean
 * - 注入完整的企业级依赖（DiscoveryClient、RestTemplate、ObjectMapper）
 * - 支持微服务健康检查、系统资源监控
 * </p>
 * <p>
 * 企业级特性：
 * - 多微服务并发健康检查
 * - 系统资源监控（CPU、内存、线程）
 * - 服务发现集成（Nacos DiscoveryClient）
 * - 健康检查超时控制
 * - 异步并发检查（线程池）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class HealthCheckManagerConfig {

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 配置RestTemplate用于微服务健康检查调用
     * <p>
     * 企业级配置：
     * - 支持HTTP健康检查端点调用
     * - 用于调用各微服务的/actuator/health端点
     * </p>
     *
     * @return RestTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        log.info("[RestTemplate] 初始化健康检查HTTP客户端");
        return new RestTemplate();
    }

    /**
     * 注册企业级HealthCheckManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 使用@Resource注解进行依赖注入（禁止@Autowired）
     * </p>
     * <p>
     * 企业级功能：
     * - 系统健康状态检查（CPU、内存、线程）
     * - 微服务健康状态监控（通过Nacos DiscoveryClient）
     * - 并发健康检查（10线程线程池）
     * - 健康检查超时控制（5秒超时）
     * - 数据库健康检查
     * - 缓存健康检查
     * </p>
     * <p>
     * 监控的微服务：
     * - ioedream-gateway-service (8080)
     * - ioedream-common-service (8088)
     * - ioedream-device-comm-service (8087)
     * - ioedream-oa-service (8089)
     * - ioedream-access-service (8090)
     * - ioedream-attendance-service (8091)
     * - ioedream-video-service (8092)
     * - ioedream-consume-service (8094)
     * - ioedream-visitor-service (8095)
     * </p>
     *
     * @param restTemplate HTTP客户端
     * @return HealthCheckManager实例
     */
    @Bean
    public HealthCheckManager healthCheckManager(RestTemplate restTemplate) {
        log.info("[HealthCheckManager] 初始化企业级健康检查管理器");
        log.info("[HealthCheckManager] DiscoveryClient: {}", discoveryClient != null ? "已注入" : "未注入");
        log.info("[HealthCheckManager] RestTemplate: {}", restTemplate != null ? "已注入" : "未注入");
        log.info("[HealthCheckManager] ObjectMapper: {}", objectMapper != null ? "已注入" : "未注入");

        HealthCheckManager healthCheckManager = new HealthCheckManager(
                discoveryClient,
                restTemplate,
                objectMapper
        );

        log.info("[HealthCheckManager] 企业级健康检查管理器初始化完成");
        log.info("[HealthCheckManager] 并发检查线程池：10线程");
        log.info("[HealthCheckManager] 健康检查超时：5秒");
        log.info("[HealthCheckManager] 监控微服务：9个核心服务");
        
        return healthCheckManager;
    }
}
