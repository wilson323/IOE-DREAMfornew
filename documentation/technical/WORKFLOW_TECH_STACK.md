# IOE-DREAM 工作流技术栈文档

> **版本**: v2.0.0
> **更新日期**: 2025-12-18
> **适用范围**: ioedream-oa-service 工作流模块

## 1. 核心技术栈

### 1.1 工作流引擎

| 组件 | 版本 | 说明 |
|------|------|------|
| **Flowable** | **7.2.0** | 核心工作流引擎（已从6.8.1升级） |
| Flowable BPMN | 7.2.0 | 业务流程建模与执行 |
| Flowable CMMN | 7.2.0 | 案例管理模型 |
| Flowable DMN | 7.2.0 | 决策表引擎 |
| Flowable Form | 7.2.0 | 表单引擎 |

### 1.2 关键依赖

```xml
<!-- Flowable Spring Boot Starter -->
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-process</artifactId>
    <version>7.2.0</version>
</dependency>

<!-- Flowable CMMN -->
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-cmmn</artifactId>
    <version>7.2.0</version>
</dependency>

<!-- Flowable DMN -->
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-dmn</artifactId>
    <version>7.2.0</version>
</dependency>

<!-- Flowable DMN API (显式引入) -->
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-dmn-api</artifactId>
    <version>7.2.0</version>
</dependency>
```

## 2. Flowable 7.x 迁移要点

### 2.1 API变更清单

| 旧API (6.x) | 新API (7.x) | 说明 |
|-------------|-------------|------|
| `DmnRuleService` | `DmnDecisionService` | DMN服务重命名 |
| `RuntimeService.setAuthenticatedUserId()` | `Authentication.setAuthenticatedUserId()` | 认证API迁移到common包 |
| `AppRepositoryService` | 已移除 | App模块已移除 |
| `AppRuntimeService` | 已移除 | App模块已移除 |
| `PROCESS_SUSPENDED` | 使用状态查询替代 | 事件类型变更 |
| `PROCESS_RESUMED` | 使用状态查询替代 | 事件类型变更 |

### 2.2 包路径变更

```java
// DMN决策服务
// 旧: org.flowable.dmn.api.DmnRuleService
// 新: org.flowable.dmn.api.DmnDecisionService

// 认证工具
// 旧: org.flowable.engine.RuntimeService.setAuthenticatedUserId()
// 新: org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId()
```

## 3. 服务包装器架构

项目采用包装器模式封装Flowable原生服务，提供统一的错误处理和日志记录：

```
net.lab1024.sa.oa.workflow.config.wrapper/
├── FlowableRepositoryService.java      # 流程定义服务
├── FlowableRuntimeService.java         # 流程运行时服务
├── FlowableTaskService.java            # 任务服务
├── FlowableHistoryService.java         # 历史服务
├── FlowableManagementService.java      # 管理服务
├── FlowableCmmnRepositoryService.java  # CMMN定义服务
├── FlowableCmmnRuntimeService.java     # CMMN运行时服务
├── FlowableDmnRepositoryService.java   # DMN定义服务
└── FlowableDmnRuleService.java         # DMN决策服务（使用DmnDecisionService）
```

## 4. 配置说明

### 4.1 Flowable配置

```yaml
flowable:
  # 数据库配置
  database-schema-update: true
  
  # 异步执行器
  async-executor-activate: true
  
  # 历史级别
  history-level: audit
  
  # CMMN配置
  cmmn:
    enabled: true
    
  # DMN配置
  dmn:
    enabled: true
    strict-mode: false
```

### 4.2 Spring Boot集成

- **Spring Boot版本**: 3.5.8
- **Java版本**: 17
- **Jakarta EE**: 使用jakarta命名空间

## 5. 功能模块

### 5.1 核心功能

- **流程设计器**: 可视化BPMN流程设计
- **表单设计器**: 动态表单配置
- **审批流程**: 会签/或签、撤回、催办、抄送
- **决策引擎**: DMN规则表执行

### 5.2 企业级特性

- **批量操作**: 批量审批、批量启动等
- **流程监控**: 性能指标收集
- **缓存管理**: 多级缓存策略
- **通知服务**: 实时消息推送

## 6. 注意事项

### 6.1 Lombok兼容性

✅ **已修复**: 所有类统一使用`@Slf4j`注解，移除了重复的手动Logger声明，`Field 'log' already exists`警告已消除。

### 6.2 待办事项

- [ ] 完善WorkflowBatchOperationServiceImpl的企业级实现
- [ ] 实现WorkflowPerformanceOptimizer优化器
- [ ] 启用WorkflowEngineHealthIndicator健康检查
- [ ] 完善Listener类的事件处理逻辑

## 7. 参考文档

- [Flowable 7.x 官方文档](https://www.flowable.com/open-source/docs/)
- [Flowable GitHub](https://github.com/flowable/flowable-engine)
- [IOE-DREAM 架构规范](../../CLAUDE.md)
