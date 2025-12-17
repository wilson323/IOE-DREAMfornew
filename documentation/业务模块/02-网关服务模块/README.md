# IOE-DREAM 网关服务模块

> **版本**: v1.0.0  
> **微服务**: ioedream-gateway-service (8080)  
> **创建日期**: 2025-12-17

---

## 📋 模块概述

网关服务是IOE-DREAM平台的统一入口，负责请求路由、负载均衡、认证鉴权、限流熔断等功能。

---

## 🏗️ 核心功能

| 功能 | 说明 |
|------|------|
| 请求路由 | 根据路径转发到对应微服务 |
| 负载均衡 | 多实例负载分发 |
| 认证鉴权 | Sa-Token统一认证 |
| 限流熔断 | Resilience4j限流保护 |
| 跨域处理 | CORS统一配置 |
| 日志记录 | 请求/响应日志 |

---

## 📁 代码结构

```
ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/
├── GatewayApplication.java          # 启动类
├── config/
│   ├── CorsConfiguration.java       # 跨域配置
│   ├── RouteConfiguration.java      # 路由配置
│   └── SecurityConfiguration.java   # 安全配置
├── filter/
│   ├── AuthGlobalFilter.java        # 认证过滤器
│   ├── LoggingFilter.java           # 日志过滤器
│   └── RateLimitFilter.java         # 限流过滤器
└── handler/
    └── GlobalExceptionHandler.java  # 异常处理
```

---

## 🔧 路由配置

| 路径前缀 | 目标服务 | 端口 |
|----------|----------|------|
| /api/common/** | ioedream-common-service | 8088 |
| /api/access/** | ioedream-access-service | 8090 |
| /api/attendance/** | ioedream-attendance-service | 8091 |
| /api/video/** | ioedream-video-service | 8092 |
| /api/consume/** | ioedream-consume-service | 8094 |
| /api/visitor/** | ioedream-visitor-service | 8095 |
| /api/oa/** | ioedream-oa-service | 8089 |
| /api/device/** | ioedream-device-comm-service | 8087 |

---

## 📊 性能指标

| 指标 | 目标值 |
|------|--------|
| 请求转发延迟 | < 50ms |
| 并发连接数 | ≥ 10000 |
| 可用性 | ≥ 99.99% |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
