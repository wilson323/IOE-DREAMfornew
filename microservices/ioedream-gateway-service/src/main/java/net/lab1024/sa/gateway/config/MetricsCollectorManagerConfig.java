package net.lab1024.sa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.manager.MetricsCollectorManager;

/**
 * MetricsCollectorManager企业级配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册MetricsCollectorManager为Spring Bean
 * - 注入完整的企业级依赖（MeterRegistry、RedisTemplate）
 * - 支持服务性能指标采集、业务指标采集
 * </p>
 * <p>
 * 企业级特性：
 * - 集成Micrometer进行指标采集
 * - 支持Prometheus格式导出
 * - Redis存储指标数据（时序数据）
 * - 实时指标查询和统计
 * - QPS、TPS、响应时间、错误率监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class MetricsCollectorManagerConfig {

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 注册企业级MetricsCollectorManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 使用@Resource注解进行依赖注入（禁止@Autowired）
     * </p>
     * <p>
     * 企业级功能：
     * - 服务性能指标采集（QPS、TPS、响应时间、错误率）
     * - 业务指标采集（自定义业务指标）
     * - 指标数据聚合
     * - 指标数据存储（Redis时序数据）
     * - 活跃连接数监控
     * - Prometheus格式导出
     * </p>
     * <p>
     * 指标类型：
     * - Counter: 计数器（如请求数、错误数）
     * - Gauge: 仪表盘（如活跃连接数、队列大小）
     * - Timer: 计时器（如响应时间、处理时间）
     * </p>
     *
     * @return MetricsCollectorManager实例
     */
    @Bean
    public MetricsCollectorManager metricsCollectorManager() {
        log.info("[MetricsCollectorManager] 初始化企业级指标采集管理器");
        log.info("[MetricsCollectorManager] MeterRegistry: {}", meterRegistry != null ? "已注入" : "未注入");
        log.info("[MetricsCollectorManager] RedisTemplate: {}", redisTemplate != null ? "已注入" : "未注入");

        MetricsCollectorManager metricsCollectorManager = new MetricsCollectorManager(
                meterRegistry,
                redisTemplate
        );

        log.info("[MetricsCollectorManager] 企业级指标采集管理器初始化完成");
        log.info("[MetricsCollectorManager] 采集指标：QPS, TPS, 响应时间, 错误率, 活跃连接");
        log.info("[MetricsCollectorManager] 存储方式：Redis时序数据");
        log.info("[MetricsCollectorManager] 导出格式：Prometheus兼容");
        
        return metricsCollectorManager;
    }
}
