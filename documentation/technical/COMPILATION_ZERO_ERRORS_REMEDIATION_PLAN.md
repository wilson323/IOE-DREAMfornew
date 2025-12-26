# 编译 0 异常整改清单与分阶段执行计划

## 目标
消除当前工程在本地与 CI 环境中的编译异常，确保**全量模块编译通过（0 异常）**。

## 现状与关键阻断
基于 `erro.txt` 与代码扫描，当前编译异常主要集中在以下三类：
1) **公共依赖缺失**：`ResponseDTO`/`BusinessException` 等公共类型无法解析。
2) **配置项弃用/未知**：Spring Boot 配置键升级未迁移。
3) **测试/类型不匹配**：测试类与主代码签名不同步。

---

## 一、整改总览（按优先级）

### P0（阻断级）：公共依赖链修复
**目标**：保证所有业务模块能解析 `microservices-common-*` 公共类型。

**动作清单**
- 统一父 POM 与依赖版本（确保公共模块先构建）
- 将公共模块纳入聚合构建顺序（`microservices/pom.xml`）
- 校验所有业务模块是否显式依赖必要公共模块

**验证方式**
- `mvn -pl microservices/microservices-common -am clean install -DskipTests`
- 任意业务模块 `mvn -pl microservices/ioedream-xxx-service -am clean compile`

**风险与注意**
- 如使用私服，需确认公共模块版本已发布并可被拉取。

---

### P1（阻断级）：Spring Boot 配置迁移
**目标**：消除配置键弃用/未知导致的 IDE/构建异常。

**动作清单（核心替换）**
- `spring.redis.*` → `spring.data.redis.*`
- `spring.redis.lettuce.pool.*` → `spring.data.redis.lettuce.pool.*`
- `spring.redis.lettuce.cluster.refresh.*` → `spring.data.redis.lettuce.cluster.refresh.*`
- `server.tomcat.max-threads` → `server.tomcat.threads.max`
- `server.tomcat.min-spare-threads` → `server.tomcat.threads.min-spare`
- `management.metrics.export.prometheus.*` → `management.prometheus.metrics.export.*`
- `management.endpoint.metrics.enabled` → `management.endpoint.metrics.access`
- `management.endpoint.prometheus.enabled` → `management.endpoint.prometheus.access`
- `spring.sleuth`/`management.sleuth` → `management.tracing.*`（配套 micrometer-tracing）

**验证方式**
- 运行配置校验脚本：`scripts/check-spring-config-keys.ps1 -SkipBuild`

---

### P2（阻断级）：测试与类型修复
**目标**：修复测试编译错误与类型不匹配。

**动作清单**
- 修复 `DeviceEntity` setter 类型不匹配（`Long`/`Integer`）
- 修复缺失 DAO/DTO 的测试依赖
- 对已废弃测试按需移除或 `@Disabled`

**验证方式**
- `mvn -pl microservices/ioedream-video-service -DskipTests=false test`

---

### P3（质量级）：TODO 占位与缺口修复
**目标**：消除关键业务链路 TODO，避免编译通过但运行失败。

**高优先级 TODO**
- 门禁报警/统计（`AccessMonitorServiceImpl`）
- 权限同步（`PermissionDataServiceImpl`）
- 设备通讯适配核心流程（多厂商 Adapter）

---

## 二、分阶段执行计划

### 阶段 1：构建链路恢复（P0）
- 修复依赖树与公共模块构建顺序
- 验证 `ResponseDTO`/`BusinessException` 可解析

**输出**
- 公共模块构建日志
- 业务模块编译通过证明

### 阶段 2：配置迁移（P1）
- 批量迁移配置键
- 更新统一模板（`microservices/config-templates/`）

**输出**
- 配置校验通过报告

### 阶段 3：测试修复（P2）
- 修正类型与依赖
- 跑通关键模块单元测试

**输出**
- 测试通过报告

### 阶段 4：关键 TODO 清理（P3）
- 按业务优先级逐条消除占位
- 保证运行链路可用

**输出**
- TODO 清零报告

---

## 三、验收标准

- `mvn -pl microservices/* -am clean compile` 无报错
- `scripts/check-spring-config-keys.ps1 -SkipBuild` 通过
- 主要服务测试通过或已明确标记跳过（含理由）

---

## 四、需要确认的范围

请确认是否按以下顺序执行：
1) 依赖链修复 → 2) 配置迁移 → 3) 测试修复 → 4) TODO 清理

如果同意，请指定优先修复的服务范围（例如先从 `ioedream-common-service` 和 `ioedream-access-service` 开始）。
