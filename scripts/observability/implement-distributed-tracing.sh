#!/bin/bash
# ============================================================
# IOE-DREAM 分布式追踪体系实施脚本
# 实现基于Micrometer Tracing + Zipkin的分布式追踪
# ============================================================

echo "🔧 开始分布式追踪体系实施..."
echo "实施时间: $(date)"
echo "技术栈: Micrometer Tracing + Zipkin + Brave"
echo "=================================="

# 创建配置目录
mkdir -p microservices/common-config/tracing
mkdir -p deployment/observability/zipkin

# 1. 创建Zipkin配置文件
echo "📝 创建Zipkin配置..."

# Zipkin Docker Compose配置
cat > deployment/observability/docker-compose-zipkin.yml << 'EOF'
# ============================================================
# IOE-DREAM 分布式追踪服务 - Zipkin
# ============================================================
version: '3.8'

services:
  zipkin:
    image: openzipkin/zipkin:2.27
    container_name: ioedream-zipkin
    ports:
      - "9411:9411"        # Zipkin Web UI
      - "9410:9410"        # Admin API
    environment:
      - JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC
      - STORAGE_TYPE=mysql
      - MYSQL_HOST=mysql
      - MYSQL_TCP_PORT=3306
      - MYSQL_USER=zipkin
      - MYSQL_PASS=ENC(AES256:L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A)
      - MYSQL_DB=zipkin
    depends_on:
      - mysql
    networks:
      - ioedream-observability
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:9411/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  mysql:
    image: mysql:8.0
    container_name: ioedream-zipkin-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=ENC(AES256:M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4A5B)
      - MYSQL_DATABASE=zipkin
      - MYSQL_USER=zipkin
      - MYSQL_PASSWORD=ENC(AES256:L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A)
    volumes:
      - zipkin_mysql_data:/var/lib/mysql
      - ./deployment/observability/mysql/init:/docker-entrypoint-initdb.d
    networks:
      - ioedream-observability
    restart: unless-stopped

volumes:
  zipkin_mysql_data:

networks:
  ioedream-observability:
    driver: bridge
EOF

# MySQL初始化脚本
mkdir -p deployment/observability/mysql/init
cat > deployment/observability/mysql/init/01-zipkin-schema.sql << 'EOF'
-- Zipkin数据库初始化脚本
CREATE DATABASE IF NOT EXISTS zipkin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zipkin;

-- 创建zipkin用户并授权
CREATE USER IF NOT EXISTS 'zipkin'@'%' IDENTIFIED BY 'L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A';
GRANT ALL PRIVILEGES ON zipkin.* TO 'zipkin'@'%';
FLUSH PRIVILEGES;
EOF

# 2. 创建分布式追踪配置模板
echo "📝 创建分布式追踪配置模板..."

cat > microservices/common-config/tracing/application-tracing.yml << 'EOF'
# ============================================================
# IOE-DREAM 分布式追踪配置
# Micrometer Tracing + Zipkin + Brave
# ============================================================

# ==================== 分布式追踪配置 ====================
management:
  tracing:
    # 启用分布式追踪
    enabled: true

    # 采样配置
    sampling:
      probability: ${TRACING_SAMPLING_PROBABILITY:0.1}  # 10%采样率（生产环境）

    # 追踪导出配置
    zipkin:
      endpoint: ${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}

  # 指标配置
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
      version: ${app.version:1.0.0}

    # Prometheus导出
    export:
      prometheus:
        enabled: true
        step: 30s

  # 健康检查端点
  health:
    tracing:
      enabled: true

# ==================== Spring Boot 追踪配置 ====================
spring:
  sleuth:
    # Spring Cloud Sleuth配置（兼容性）
    enabled: true
    zipkin:
      base-url: ${ZIPKIN_ENDPOINT:http://localhost:9411}
      enabled: true

    # 采样配置
    sampler:
      probability: ${TRACING_SAMPLING_PROBABILITY:0.1}

  application:
    name: ${SERVICE_NAME:unknown-service}

# ==================== 日志追踪配置 ====================
logging:
  pattern:
    # 包含追踪ID的日志格式
    level: "[%X{traceId:-},%X{spanId:-}] %5p [%t] %-40.40logger{39} : %m%n"

  # 追踪相关日志级别
  level:
    org.springframework.cloud.sleuth: DEBUG
    io.micrometer.tracing: DEBUG
    zipkin2: DEBUG

# ==================== 自定义追踪配置 ====================
tracing:
  # 自定义标签
  tags:
    service: ${spring.application.name}
    version: ${app.version:1.0.0}
    environment: ${spring.profiles.active}

  # 高优先级追踪的操作
  high-priority-operations:
    - "/api/v1/*/query"
    - "/api/v1/*/create"
    - "/api/v1/*/update"
    - "/api/v1/*/delete"

  # 异步追踪配置
  async:
    enabled: true
    thread-pool-size: 10

# ==================== 性能监控配置 ====================
performance:
  tracing:
    # 慢操作阈值（毫秒）
    slow-operation-threshold: 1000

    # 数据库查询追踪
    database:
      enabled: true
      slow-query-threshold: 500

    # HTTP请求追踪
    http:
      enabled: true
      slow-request-threshold: 1000

    # 缓存操作追踪
    cache:
      enabled: true
      slow-operation-threshold: 100
EOF

# 3. 创建分布式追踪工具类
echo "📝 创建分布式追踪工具类..."

cat > microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/TracingUtils.java << 'EOF'
package net.lab1024.sa.common.tracing;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * 分布式追踪工具类
 * <p>
 * 提供统一的分布式追踪操作工具，简化Span和Trace的使用
 * 支持手动创建Span、添加标签、记录事件等操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class TracingUtils {

    @Resource
    private Tracer tracer;

    @Resource
    private Propagator propagator;

    /**
     * 创建新的Span
     *
     * @param operationName 操作名称
     * @return 创建的Span
     */
    public Span startSpan(String operationName) {
        return tracer.nextSpan().name(operationName).start();
    }

    /**
     * 执行带追踪的操作
     *
     * @param operationName 操作名称
     * @param supplier 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T withTrace(String operationName, Supplier<T> supplier) {
        Span span = startSpan(operationName);
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return supplier.get();
        } finally {
            span.end();
        }
    }

    /**
     * 为Span添加标签
     *
     * @param span Span对象
     * @param key 标签键
     * @param value 标签值
     */
    public void addTag(Span span, String key, String value) {
        if (span != null) {
            span.tag(key, value);
        }
    }

    /**
     * 为当前Span添加标签
     *
     * @param key 标签键
     * @param value 标签值
     */
    public void addTag(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            addTag(currentSpan, key, value);
        }
    }

    /**
     * 为Span添加事件
     *
     * @param span Span对象
     * @param eventName 事件名称
     */
    public void addEvent(Span span, String eventName) {
        if (span != null) {
            span.event(eventName);
        }
    }

    /**
     * 为当前Span添加事件
     *
     * @param eventName 事件名称
     */
    public void addEvent(String eventName) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            addEvent(currentSpan, eventName);
        }
    }

    /**
     * 记录错误信息
     *
     * @param exception 异常对象
     */
    public void recordError(Exception exception) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("error", exception.getMessage());
            currentSpan.tag("error.type", exception.getClass().getSimpleName());
            currentSpan.event("error");
        }
    }

    /**
     * 记录业务指标
     *
     * @param metrics 指标信息
     */
    public void recordMetrics(Map<String, Object> metrics) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            metrics.forEach((key, value) -> {
                addTag(currentSpan, "metric." + key, String.valueOf(value));
            });
        }
    }

    /**
     * 获取当前Trace ID
     *
     * @return Trace ID
     */
    public String getCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().traceId();
        }
        return "unknown";
    }

    /**
     * 获取当前Span ID
     *
     * @return Span ID
     */
    public String getCurrentSpanId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().spanId();
        }
        return "unknown";
    }

    /**
     * 判断是否在追踪上下文中
     *
     * @return 是否在追踪上下文中
     */
    public boolean isTracing() {
        return tracer.currentSpan() != null;
    }

    /**
     * 创建业务操作Span
     *
     * @param moduleName 模块名称
     * @param operation 操作名称
     * @param userId 用户ID
     * @return 创建的Span
     */
    public Span createBusinessSpan(String moduleName, String operation, String userId) {
        Span span = startSpan(String.format("%s.%s", moduleName, operation));
        addTag(span, "module", moduleName);
        addTag(span, "operation", operation);
        addTag(span, "user.id", userId);
        addTag(span, "span.kind", "server");
        return span;
    }
}
EOF

# 4. 创建分布式追踪配置类
cat > microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/config/TracingConfiguration.java << 'EOF'
package net.lab1024.sa.common.tracing.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.tracing.TracingUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式追踪配置类
 * <p>
 * 配置Micrometer Tracing、Zipkin集成和自定义追踪组件
 * 提供统一的分布式追踪基础设施
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ObservationProperties.class, MetricsProperties.class})
@ConditionalOnEnabledTracing
public class TracingConfiguration {

    /**
     * 配置追踪工具类
     *
     * @param tracer Tracer实例
     * @param propagator 传播器
     * @return TracingUtils实例
     */
    @Bean
    @ConditionalOnMissingBean
    public TracingUtils tracingUtils(Tracer tracer, Propagator propagator) {
        log.info("[分布式追踪] 初始化TracingUtils");
        return new TracingUtils();
    }

    /**
     * 配置自定义Span过滤器
     *
     * @return SpanCustomizer实例
     */
    @Bean
    public SpanCustomizer spanCustomizer() {
        return new SpanCustomizer();
    }
}

/**
 * Span自定义器
 * <p>
 * 为所有Span添加标准标签和事件
 * </p>
 */
class SpanCustomizer {

    /**
     * 自定义Span
     *
     * @param span Span对象
     * @param operationName 操作名称
     */
    public void customize(io.micrometer.tracing.Span span, String operationName) {
        // 添加标准标签
        span.tag("application.name", getApplicationName());
        span.tag("operation.name", operationName);
        span.tag("span.type", "custom");

        // 添加开始事件
        span.event("span.started");
    }

    private String getApplicationName() {
        // 从系统属性或环境变量获取应用名称
        return System.getProperty("spring.application.name", "unknown-application");
    }
}
EOF

echo "=================================="
echo "✅ 分布式追踪体系配置创建完成！"
echo "=================================="

echo "📊 配置文件创建总结："
echo "✅ Zipkin Docker配置: deployment/observability/docker-compose-zipkin.yml"
echo "✅ 追踪配置模板: microservices/common-config/tracing/application-tracing.yml"
echo "✅ 追踪工具类: microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/TracingUtils.java"
echo "✅ 追踪配置类: microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/config/TracingConfiguration.java"

echo "=================================="
echo "🎯 后续实施步骤："
echo "1. 启动Zipkin服务: docker-compose -f deployment/observability/docker-compose-zipkin.yml up -d"
echo "2. 在各微服务中引入追踪依赖"
echo "3. 配置应用引入追踪配置文件"
echo "4. 在关键业务代码中使用TracingUtils"
echo "5. 验证追踪数据: http://localhost:9411"
echo "=================================="

echo "🚨 重要提醒："
echo "⚠️ 请将明文密码替换为生产环境加密密码"
echo "⚠️ 生产环境建议调整采样率为0.01-0.05"
echo "⚠️ 建议配置Zipkin数据持久化"
echo "=================================="