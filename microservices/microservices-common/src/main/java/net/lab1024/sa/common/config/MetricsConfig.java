package net.lab1024.sa.common.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 微服务指标配置 - 使用最新版Micrometer API
 * 替代旧的过时API实现
 *
 * @author 老王重写 - 2025年
 */
@Configuration
@Profile("!test")
public class MetricsConfig {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

      /**
     * 自定义MeterRegistry配置 - 企业级标准
     * 提供通用标签和配置
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // 添加通用标签
            registry.config().commonTags(
                "application", "ioedream-microservices",
                "version", "1.0.0",
                "environment", System.getProperty("spring.profiles.active", "default")
            );

            // 配置默认分布统计
            registry.config().meterFilter(MeterFilter.deny(id ->
                id.getName().equals("jvm.gc.pause") &&
                !id.getTag("application").equals("ioedream-microservices")
            ));
        };
    }

    /**
     * JVM内存指标监控
     */
    @Bean
    public MeterBinder jvmMemoryMetrics() {
        return registry -> {
            // 使用简单的方法注册指标
            registry.gauge("jvm.memory.used.heap", memoryBean.getHeapMemoryUsage().getUsed());
            registry.gauge("jvm.memory.max.heap", memoryBean.getHeapMemoryUsage().getMax());
            registry.gauge("jvm.memory.used.nonheap", memoryBean.getNonHeapMemoryUsage().getUsed());
        };
    }

    /**
     * 线程指标监控
     */
    @Bean
    public MeterBinder threadMetrics() {
        return registry -> {
            // 活跃线程数和守护线程数
            registry.gauge("jvm.threads.active", threadBean.getThreadCount());
            registry.gauge("jvm.threads.daemon", threadBean.getDaemonThreadCount());
        };
    }

    /**
     * HTTP请求计时器
     */
    @Bean
    public Timer httpTimer(MeterRegistry registry) {
        return Timer.builder("http.requests")
                .description("HTTP请求处理时间")
                .tags("type", "http")
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    /**
     * 业务事务计时器
     */
    @Bean
    public Timer businessTimer(MeterRegistry registry) {
        return Timer.builder("business.transactions")
                .description("业务事务处理时间")
                .tags("type", "business")
                .minimumExpectedValue(Duration.ofMillis(50))
                .maximumExpectedValue(Duration.ofMinutes(5))
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99)
                .register(registry);
    }

    /**
     * 数据库查询计时器
     */
    @Bean
    public Timer databaseTimer(MeterRegistry registry) {
        return Timer.builder("database.queries")
                .description("数据库查询处理时间")
                .tags("type", "database")
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    /**
     * 缓存操作计时器
     */
    @Bean
    public Timer cacheTimer(MeterRegistry registry) {
        return Timer.builder("cache.operations")
                .description("缓存操作处理时间")
                .tags("type", "cache")
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    /**
     * 企业级自定义MeterFilter配置
     * 过滤和转换监控指标
     */
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id ->
            id.getName().startsWith("jvm.gc.pause") ||
            id.getName().equals("process.files.max") ||
            id.getName().equals("process.start.time")
        );
    }

    /**
     * 数据库连接池监控指标
     */
    @Bean
    public MeterBinder dataSourceMetrics() {
        return registry -> {
            // 这里可以添加数据源相关的监控指标
            // 例如活跃连接数、空闲连接数等
            registry.gauge("datasource.active.connections", 0);
            registry.gauge("datasource.idle.connections", 0);
            registry.gauge("datasource.total.connections", 0);
        };
    }

    /**
     * Redis连接监控指标
     */
    @Bean
    public MeterBinder redisMetrics() {
        return registry -> {
            // Redis相关监控指标
            registry.gauge("redis.active.connections", 0);
            registry.gauge("redis.idle.connections", 0);
            registry.gauge("redis.command.count", 0);
        };
    }

    /**
     * 自定义业务计数器
     */
    @Bean
    public MeterBinder businessCounters(MeterRegistry registry) {
        return binder -> {
            // 用户登录次数
            registry.counter("business.user.login.count", "type", "user");

            // API调用次数
            registry.counter("business.api.call.count", "type", "api");

            // 业务操作次数
            registry.counter("business.operation.count", "type", "operation");
        };
    }

    /**
     * 系统性能指标收集器
     */
    @Bean
    public MeterBinder systemMetrics() {
        return registry -> {
            // CPU使用率（简化版）
            registry.gauge("system.cpu.usage", getSystemCpuUsage());

            // 系统负载
            registry.gauge("system.load.average", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        };
    }

    /**
     * 获取系统CPU使用率的简化实现
     */
    private double getSystemCpuUsage() {
        try {
            // 使用OperatingSystemMXBean获取CPU使用率
            com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad() > 0 ? osBean.getProcessCpuLoad() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}