# IOE-DREAM 分布式追踪实现文档

> **实施阶段**: 阶段0 - 任务0.2  
> **优先级**: P0 - 最高优先级  
> **实施时间**: 2025-01-30  
> **状态**: 进行中

---

## 执行摘要

### 问题描述

项目完全缺少分布式追踪实现，无法有效监控服务调用链，影响：
- 无法定位性能瓶颈
- 无法追踪故障链路
- 无法分析服务依赖关系

### 解决方案

基于Spring Boot 3.x，使用Micrometer Tracing + Brave + Zipkin实现分布式追踪。

---

## 技术选型

### 为什么选择Micrometer Tracing？

Spring Boot 3.x中，Spring Cloud Sleuth已被废弃，推荐使用Micrometer Tracing：
- ✅ 原生支持Spring Boot 3.x
- ✅ 更好的性能和低延迟
- ✅ 统一的监控指标和追踪
- ✅ 支持多种追踪后端（Zipkin、Jaeger等）

### 技术栈

- **Micrometer Tracing**: 追踪抽象层
- **Brave**: 追踪实现（Zipkin兼容）
- **Zipkin**: 追踪数据存储和可视化

---

## 实施步骤

### 步骤1: 添加依赖（已完成）

已在`microservices-common/pom.xml`中添加：
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

所有服务通过依赖`microservices-common`自动获得追踪支持。

### 步骤2: 配置追踪（已完成）

在`common-monitoring.yaml`中配置了Micrometer Tracing设置：
- 采样率配置（开发100%，生产1%）
- Zipkin端点配置
- 日志集成（Trace ID自动添加到日志）

### 步骤3: 更新TracingConfiguration（已完成）

更新了`TracingConfiguration.java`，使用Micrometer Tracing API。

### 步骤4: 验证追踪配置

需要验证：
- [ ] 所有服务正常启动
- [ ] Trace ID出现在日志中
- [ ] Zipkin服务器可接收追踪数据
- [ ] 服务间调用链路可追踪

---

## 配置说明

### 环境变量配置

在`.env`文件中添加：
```bash
# Zipkin服务器地址
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# 追踪采样率（0.0-1.0）
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=1.0  # 开发环境100%
# MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.1   # 测试环境10%
# MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.01  # 生产环境1%

# 是否启用追踪
MANAGEMENT_TRACING_ENABLED=true
```

### 日志配置

Trace ID会自动添加到日志中，格式：
```
[2025-01-30 10:00:00.000] [abc123def456,span001] [main] INFO  ... - ...
```

其中`abc123def456`是Trace ID，`span001`是Span ID。

---

## Zipkin服务器部署

### 使用Docker部署

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

访问：http://localhost:9411

### 使用docker-compose

添加到`docker-compose-all.yml`：
```yaml
services:
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: ioedream-zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=mem
    networks:
      - ioedream-network
```

---

## 验证清单

- [ ] 所有9个微服务都包含追踪依赖
- [ ] 所有服务配置文件引用了common-monitoring.yaml
- [ ] TracingConfiguration正确配置
- [ ] 日志格式包含Trace ID
- [ ] Zipkin服务器可访问
- [ ] 测试请求能在Zipkin中查看完整调用链

---

## 下一步工作

1. 部署Zipkin服务器
2. 测试服务间调用追踪
3. 验证日志中的Trace ID
4. 优化采样率配置（开发/测试/生产环境不同）

---

## 相关文档

- [Micrometer Tracing文档](https://micrometer.io/docs/tracing)
- [Zipkin文档](https://zipkin.io/)
- [Spring Boot 3.x追踪指南](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.micrometer-tracing)

