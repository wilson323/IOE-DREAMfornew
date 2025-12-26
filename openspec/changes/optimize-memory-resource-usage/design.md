# Design: Memory and Resource Optimization

## 概述

本文档详细说明内存和资源优化的技术设计，目标是将开发环境内存占用从15-20GB降低至8-12GB（优化40-50%）。

## 当前状态分析

### 内存占用分布

```
当前内存占用估算 (15-20GB)
├── 微服务JVM堆内存: ~12GB (60%)
│   ├── gateway-service: 1GB
│   ├── common-service: 1GB
│   ├── device-comm-service: 2GB
│   ├── access-service: 2GB
│   ├── attendance-service: 2GB
│   ├── consume-service: 2GB
│   ├── visitor-service: 2GB
│   ├── video-service: 4GB
│   └── oa-service: 2GB
├── 非堆内存 (Metaspace/DirectMemory): ~3GB (15%)
├── 基础设施 (MySQL/Redis/Nacos): ~4GB (20%)
└── Docker容器开销: ~1GB (5%)
```

### 问题根因

1. **JVM堆内存过大**: 开发环境使用生产环境配置
2. **类加载冗余**: 所有服务扫描整个common包
3. **Metaspace占用**: 重复加载相同类定义
4. **依赖传递**: 不必要的库依赖

---

## 优化策略

### 策略1: JVM内存配置优化 (P0)

#### 1.1 Profile分离配置

**设计原则**:
- 开发环境使用较小内存配置
- 生产环境保持现有配置
- 通过Spring Profile隔离

**配置结构**:
```yaml
# bootstrap.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# 开发环境 - 小内存配置
spring:
  config:
    activate:
      on-profile: dev

java:
  opts: >-
    -Xms256m
    -Xmx512m
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
    -XX:+UseStringDeduplication
    -XX:MaxMetaspaceSize=128m
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=/tmp/heapdump.hprof

---
# 生产环境 - 保持现有配置
spring:
  config:
    activate:
      on-profile: prod

java:
  opts: >-
    -Xms1g
    -Xmx2g
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
```

#### 1.2 服务分级内存策略

| 服务类型 | 开发配置 | 生产配置 | 说明 |
|---------|---------|---------|------|
| 轻量级(gateway/common) | 256m-512m | 512m-1g | 请求转发/基础服务 |
| 标准业务(access/attendance等) | 512m-1g | 1g-2g | 标准CRUD业务 |
| 重量级(video) | 1g-2g | 2g-4g | 视频处理 |

#### 1.3 JVM参数优化

```
# 内存优化参数
-XX:+UseG1GC                    # 使用G1垃圾回收器
-XX:MaxGCPauseMillis=200        # 最大GC暂停时间
-XX:+UseStringDeduplication     # 字符串去重，节省15-20%字符串内存
-XX:MaxMetaspaceSize=128m       # 限制Metaspace大小
-XX:+UseCompressedOops          # 压缩指针，32位引用节省内存
-XX:+UseCompressedClassPointers # 压缩类指针

# 内存回收参数
-XX:InitiatingHeapOccupancyPercent=45  # 提前触发GC
-XX:G1HeapRegionSize=4m               # 适合小堆的Region大小

# 诊断参数
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/
```

---

### 策略2: 类加载优化 (P1)

#### 2.1 scanBasePackages精确配置

**当前问题**:
```java
// 当前配置 - 扫描所有包
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa"})
```

**优化后**:
```java
// 优化配置 - 只扫描需要的包
@SpringBootApplication(
    scanBasePackages = {
        "net.lab1024.sa.access",           // 服务自身
        "net.lab1024.sa.common.core",      // 核心工具
        "net.lab1024.sa.common.config",    // 通用配置
        "net.lab1024.sa.common.security",  // 安全模块
        "net.lab1024.sa.common.module.systemconfig"  // 系统配置
    }
)
```

#### 2.2 服务包扫描矩阵

| 服务 | core | config | security | module | device | 业务模块 |
|-----|------|--------|----------|--------|--------|---------|
| gateway | ✓ | ✓ | ✓ | - | - | - |
| common | ✓ | ✓ | ✓ | ✓ | - | - |
| device-comm | ✓ | ✓ | ✓ | - | ✓ | - |
| access | ✓ | ✓ | ✓ | ✓ | ✓ | access |
| attendance | ✓ | ✓ | ✓ | ✓ | ✓ | attendance |
| consume | ✓ | ✓ | ✓ | ✓ | - | consume |
| visitor | ✓ | ✓ | ✓ | ✓ | - | visitor |
| video | ✓ | ✓ | ✓ | - | ✓ | video |
| oa | ✓ | ✓ | ✓ | ✓ | - | oa |

#### 2.3 自动配置排除

```java
@SpringBootApplication(
    exclude = {
        // 数据库相关（如果服务不需要）
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        
        // 缓存相关（如果服务不需要）
        RedisAutoConfiguration.class,
        CacheAutoConfiguration.class,
        
        // 消息队列（如果服务不需要）
        RabbitAutoConfiguration.class,
        
        // Web相关（对于非Web服务）
        WebMvcAutoConfiguration.class
    }
)
```

---

### 策略3: 公共库依赖优化 (P2)

#### 3.1 Maven依赖优化

**当前结构**:
```xml
<!-- 服务POM - 引入全部common -->
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common</artifactId>
</dependency>
```

**优化后**:
```xml
<!-- 服务POM - 按需引入 -->
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common-core</artifactId>
</dependency>
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common-security</artifactId>
</dependency>
<!-- 可选依赖按需添加 -->
```

#### 3.2 条件加载配置

```java
// 使用@ConditionalOnProperty控制组件加载
@Component
@ConditionalOnProperty(
    name = "app.feature.workflow.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class WorkflowManager {
    // 只在需要时加载
}
```

---

## Docker配置优化

### docker-compose-dev.yml

```yaml
version: '3.8'

services:
  gateway-service:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
    deploy:
      resources:
        limits:
          memory: 768M
        reservations:
          memory: 256M

  common-service:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
    deploy:
      resources:
        limits:
          memory: 768M
        reservations:
          memory: 256M

  access-service:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC
    deploy:
      resources:
        limits:
          memory: 1.5G
        reservations:
          memory: 512M

  # 其他服务类似配置...

  video-service:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
    deploy:
      resources:
        limits:
          memory: 3G
        reservations:
          memory: 1G
```

---

## 验证方案

### 内存监控脚本

```powershell
# memory-monitor.ps1
# 监控所有Java进程内存使用

function Get-JavaProcessMemory {
    $processes = Get-Process -Name java -ErrorAction SilentlyContinue
    $totalMemory = 0
    
    foreach ($proc in $processes) {
        $memoryMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
        $totalMemory += $memoryMB
        Write-Host "$($proc.Id): $memoryMB MB"
    }
    
    Write-Host "Total: $totalMemory MB"
    return $totalMemory
}

Get-JavaProcessMemory
```

### 测试用例

1. **启动测试**: 验证所有服务正常启动
2. **API测试**: 验证核心API响应正常
3. **功能测试**: 验证业务功能完整
4. **压力测试**: 验证负载下稳定性

---

## 回滚方案

如果优化导致问题，可通过以下方式回滚：

1. **Profile切换**: `SPRING_PROFILES_ACTIVE=prod`
2. **Docker配置**: 使用docker-compose-all.yml
3. **JVM参数**: 恢复原有Xms/Xmx配置

---

## 预期成果

| 指标 | 优化前 | 优化后 | 改进 |
|-----|-------|-------|-----|
| 总内存占用 | 15-20GB | 8-12GB | -40~50% |
| 单服务平均内存 | 1.5-2GB | 0.8-1GB | -50% |
| 启动时间 | 正常 | +10%以内 | 可接受 |
| 运行性能 | 正常 | 保持 | 无降级 |
