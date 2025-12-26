# 代码迁移计划

## 迁移原则

1. **按能力归属迁移**：将当前散落在 `microservices-common` 或错误位置的共享实现，按"能力归属"迁移到对应模块
2. **禁止领域实现回流**：领域实现（如 `video`、`visitor`）不得以 `net.lab1024.sa.common.<domain>` 形式长期滞留在公共库
3. **保持向后兼容**：迁移过程中保持包名不变或提供适配器，避免大规模修改引用方

## 迁移清单

### 1. 安全相关代码 → `microservices-common-security`

**源位置**：

- `microservices/microservices-common/src/main/java/net/lab1024/sa/common/auth/util/JwtTokenUtil.java`

**目标位置**：

- `microservices/microservices-common-security/src/main/java/net/lab1024/sa/common/auth/util/JwtTokenUtil.java`

**影响范围**：

- 所有使用 `JwtTokenUtil` 的服务需要更新依赖（添加 `microservices-common-security`）

**迁移步骤**：

1. 复制文件到目标位置
2. 更新所有引用该类的服务的 `pom.xml`，添加 `microservices-common-security` 依赖
3. 验证编译通过后，删除源文件

### 2. 监控相关代码 → `microservices-common-monitor`

**源位置**：

- `microservices/microservices-common/src/main/java/net/lab1024/sa/common/monitoring/ExceptionMetricsCollector.java`
- `microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/TracingUtils.java`

**目标位置**：

- `microservices/microservices-common-monitor/src/main/java/net/lab1024/sa/common/monitoring/ExceptionMetricsCollector.java`
- `microservices/microservices-common-monitor/src/main/java/net/lab1024/sa/common/tracing/TracingUtils.java`

**影响范围**：

- 所有使用监控/追踪功能的服务需要更新依赖（添加 `microservices-common-monitor`）

**迁移步骤**：

1. 复制文件到目标位置
2. 更新所有引用该类的服务的 `pom.xml`，添加 `microservices-common-monitor` 依赖
3. 验证编译通过后，删除源文件

### 3. 缓存相关代码 → `microservices-common-cache`

**源位置**：

- `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/cache/CacheServiceImpl.java`

**目标位置**：

- `microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/CacheServiceImpl.java`

**注意**：此代码位于 `ioedream-common-service`，需要判断是否为真正的共享实现，还是应该保留在 `common-service` 中。

### 4. 导出相关代码 → `microservices-common-export`

**待查找**：需要搜索项目中所有与 Excel、PDF、二维码导出相关的代码。

### 5. 工作流相关代码 → `microservices-common-workflow`

**待查找**：需要搜索项目中所有与 Aviator 表达式引擎、Quartz 任务调度相关的代码。

### 6. 数据访问相关代码 → `microservices-common-data`

**待查找**：需要搜索项目中所有与 MyBatis-Plus、Druid、Flyway 相关的横切配置代码。

## 领域实现泄漏修复

### 问题：跨域包名泄漏

**发现的问题**：

- `ioedream-common-service` 中存在 `net.lab1024.sa.common.video.*`、`net.lab1024.sa.common.visitor.*` 等跨域包名

**修复方案**：

1. 将 `net.lab1024.sa.common.video.*` 迁移到 `ioedream-video-service`
2. 将 `net.lab1024.sa.common.visitor.*` 迁移到 `ioedream-visitor-service`
3. 如果这些代码确实是跨服务共享的，应改为通过 RPC/事件通信，而非共享实现

## 迁移顺序

1. **第一阶段**：迁移安全相关代码（JWT工具类）
2. **第二阶段**：迁移监控相关代码（指标收集、追踪工具）
3. **第三阶段**：迁移缓存相关代码（如果确认为共享实现）
4. **第四阶段**：迁移导出相关代码
5. **第五阶段**：迁移工作流相关代码
6. **第六阶段**：修复领域实现泄漏

## 验收标准

- [ ] 所有迁移的代码在新模块中编译通过
- [ ] 所有引用方更新依赖后编译通过
- [ ] 运行单元测试确保功能正常
- [ ] 运行集成测试确保服务间调用正常
- [ ] 代码扫描无重复类/包冲突
