# IOE-DREAM 微服务网关路由配置

## 📋 概述

本文档定义了IOE-DREAM微服务架构中统一的网关路由规则，确保所有微服务都通过统一的API网关对外提供服务，实现安全、监控、限流等功能。

## 🚀 核心业务服务路由

### 1. 认证服务 (ioedream-auth-service)
```yaml
- id: auth-service
  uri: lb://ioedream-auth-service
  predicates:
    - Path=/api/auth/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 100
        redis-rate-limiter.burstCapacity: 200
```

**路由路径**: `http://gateway:port/api/auth/**`
**服务端口**: 8081
**健康检查**: `/actuator/health`

### 2. 身份权限服务 (ioedream-identity-service)
```yaml
- id: identity-service
  uri: lb://ioedream-identity-service
  predicates:
    - Path=/api/identity/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 50
        redis-rate-limiter.burstCapacity: 100
```

**路由路径**: `http://gateway:port/api/identity/**`
**服务端口**: 8082

### 3. 设备管理服务 (ioedream-device-service)
```yaml
- id: device-service
  uri: lb://ioedream-device-service
  predicates:
    - Path=/api/device/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 80
        redis-rate-limiter.burstCapacity: 160
```

**路由路径**: `http://gateway:port/api/device/**`
**服务端口**: 8083

### 4. 访客管理服务 (ioedream-visitor-service)
```yaml
- id: visitor-service
  uri: lb://ioedream-visitor-service
  predicates:
    - Path=/api/visitor/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 40
        redis-rate-limiter.burstCapacity: 80
```

**路由路径**: `http://gateway:port/api/visitor/**`
**服务端口**: 8084

### 5. 门禁管理服务 (ioedream-access-service)
```yaml
- id: access-service
  uri: lb://ioedream-access-service
  predicates:
    - Path=/api/access/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 60
        redis-rate-limiter.burstCapacity: 120
```

**路由路径**: `http://gateway:port/api/access/**`
**服务端口**: 8085

### 6. 消费管理服务 (ioedream-consume-service)
```yaml
- id: consume-service
  uri: lb://ioedream-consume-service
  predicates:
    - Path=/api/consume/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 120
        redis-rate-limiter.burstCapacity: 240
```

**路由路径**: `http://gateway:port/api/consume/**`
**服务端口**: 8086

### 7. 考勤管理服务 (ioedream-attendance-service)
```yaml
- id: attendance-service
  uri: lb://ioedream-attendance-service
  predicates:
    - Path=/api/attendance/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 70
        redis-rate-limiter.burstCapacity: 140
```

**路由路径**: `http://gateway:port/api/attendance/**`
**服务端口**: 8087

### 8. 视频监控服务 (ioedream-video-service)
```yaml
- id: video-service
  uri: lb://ioedream-video-service
  predicates:
    - Path=/api/video/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 30
        redis-rate-limiter.burstCapacity: 60
```

**路由路径**: `http://gateway:port/api/video/**`
**服务端口**: 8088

## 🔧 支撑服务路由

### 9. 文件管理服务 (ioedream-file-service)
```yaml
- id: file-service
  uri: lb://ioedream-file-service
  predicates:
    - Path=/api/file/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 100
        redis-rate-limiter.burstCapacity: 200
```

**路由路径**: `http://gateway:port/api/file/**`
**服务端口**: 8089

### 10. 通知服务 (ioedream-notification-service)
```yaml
- id: notification-service
  uri: lb://ioedream-notification-service
  predicates:
    - Path=/api/notification/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 80
        redis-rate-limiter.burstCapacity: 160
```

**路由路径**: `http://gateway:port/api/notification/**`
**服务端口**: 8090

### 11. 报表服务 (ioedream-report-service)
```yaml
- id: report-service
  uri: lb://ioedream-report-service
  predicates:
    - Path=/api/report/**
  filters:
    - StripPrefix=2
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 20
        redis-rate-limiter.burstCapacity: 40
```

**路由路径**: `http://gateway:port/api/report/**`
**服务端口**: 8091

## 🛡️ 安全配置

### JWT验证配置
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - TokenRelay=
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
            methods: GET,POST
      routes:
        # 公开路由（不需要认证）
        - id: auth-public
          uri: lb://ioedream-auth-service
          predicates:
            - Path=/api/auth/login,/api/auth/register,/api/auth/refresh
          filters:
            - StripPrefix=2
```

### CORS配置
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

## 📊 监控配置

### 健康检查路由
```yaml
- id: health-check
  uri: no://op
  predicates:
    - Path=/actuator/health,/health
  filters:
    - SetStatus=200
    - SetResponseHeader=Content-Type,text/plain
    - ModifyResponseBody=
      '{"status":"UP","timestamp":"' + '#{T(java.time.Instant).now()}' + '"}'
```

### 指标收集
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## ⚡ 限流策略

### 全局限流配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: global-rate-limit
          uri: no://op
          predicates:
            - Path=/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 1000
                redis-rate-limiter.burstCapacity: 2000
                key-resolver: "#{@ipKeyResolver}"
```

### IP限流解析器
```java
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> exchange.getRequest()
        .getRemoteAddress()
        .getAddress()
        .getHostAddress()
        .defaultIfEmpty("unknown");
}
```

## 🔍 服务发现配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: GATEWAY_GROUP
        weight: 1
        register-enabled: true
        enabled: true
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: GATEWAY_GROUP
        file-extension: yml
        shared-configs:
          - data-id: gateway-routes.yml
            group: ROUTE_GROUP
            refresh: true
          - data-id: gateway-security.yml
            group: SECURITY_GROUP
            refresh: true
```

## 🚨 熔断降级配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: circuit-breaker-example
          uri: lb://ioedream-consume-service
          predicates:
            - Path=/api/consume/**
          filters:
            - name: CircuitBreaker
              args:
                name: consume-service-cb
                fallbackUri: forward:/fallback/consume
                fallbackHeaders:
                  CircuitBreaker-Exception: true
```

### 降级处理
```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/consume")
    public ResponseDTO<String> consumeFallback() {
        return ResponseDTO.error("消费服务暂时不可用，请稍后重试");
    }
}
```

## 📝 API版本管理

### 版本路由配置
```yaml
- id: api-version-v1
  uri: lb://ioedream-consume-service
  predicates:
    - Path=/api/v1/consume/**
  filters:
    - StripPrefix=3

- id: api-version-v2
  uri: lb://ioedream-consume-service-v2
  predicates:
    - Path=/api/v2/consume/**
  filters:
    - StripPrefix=3
```

## 🔄 负载均衡策略

```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        default:
          zone: primary
      cache:
        enabled: true
        ttl: 60s
        capacity: 256
      ribbon:
        enabled: false  # 使用Spring Cloud LoadBalancer替代Ribbon
```

## 📈 性能优化配置

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 60s
        pool:
          type: elastic
          max-connections: 1000
          max-idle-time: 60s
          acquire-timeout: 30000
      routes:
        - id: compression
          uri: lb://ioedream-report-service
          predicates:
            - Path=/api/report/**
          filters:
            - name: Compression
              args:
                response: true
                request: false
```

## 🔧 部署配置

### Docker Compose示例
```yaml
version: '3.8'
services:
  gateway:
    image: ioedream/gateway:latest
    ports:
      - "8080:8080"
    environment:
      - NACOS_SERVER_ADDR=nacos:8848
      - REDIS_HOST=redis
    depends_on:
      - nacos
      - redis
    networks:
      - ioedream-network
```

## 📋 测试验证

### 健康检查测试
```bash
# 基础健康检查
curl http://localhost:8080/actuator/health

# 服务状态检查
curl http://localhost:8080/api/auth/health

# 路由测试
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/consume/account/list
```

### 性能测试
```bash
# 使用JMeter进行压力测试
# 目标: 每秒1000个请求
# 持续时间: 10分钟
# 并发用户: 100
```

---

**文档维护**: IOE-DREAM架构团队
**最后更新**: 2025-01-27
**版本**: v1.0.0