# Proposal: 修复 Micrometer 指标爆炸问题

## 变更ID

`fix-micrometer-metrics-explosion`

## 优先级

**P1** - 影响监控系统性能和稳定性

## Why

工作流模块中多个组件在方法内部重复注册 Micrometer Meter，并使用高基数标签（如 `timestamp`、动态 `process_key`），导致：

1. **指标爆炸**: 每次调用都创建新的 Meter 实例，Prometheus 指标数量无限增长
2. **内存泄漏**: Meter 对象不断累积，JVM 内存持续增长
3. **监控系统过载**: Prometheus/Grafana 查询性能下降，甚至系统崩溃
4. **违反最佳实践**: Micrometer 规范要求 Meter 应在初始化时一次性注册，而非每次调用时注册

## What Changes

### 修复的组件

1. **WorkflowEngineMetricsCollector**: 移除 `recordXXX` 方法中重复注册的带标签 Counter
2. **FlowableExceptionHandler**: 移除 `timestamp` 高基数标签，改为固定标签集合
3. **WorkflowCacheManager**: 移除方法内部重复注册的带标签 Counter

### 修复原则

- ✅ Meter 在 `@PostConstruct` 中一次性注册
- ✅ 避免高基数标签（`timestamp`、动态 `process_key`、`business_key` 等）
- ✅ 使用固定标签集合，避免动态标签值
- ✅ 如需按维度统计，使用日志记录替代高基数指标

## 目标（验收标准）

- [ ] 所有 Meter 在初始化时一次性注册，无方法内部重复注册
- [ ] 无高基数标签（`timestamp`、动态业务键等）
- [ ] Prometheus 指标数量稳定，无持续增长
- [ ] JVM 内存使用正常，无内存泄漏
- [ ] 监控系统查询性能正常

## 风险与边界

- **低风险**: 仅修改指标收集逻辑，不影响业务功能
- **监控兼容性**: 指标名称可能变更，需同步更新 Grafana Dashboard（如有）
- **向后兼容**: 移除的标签不影响已有监控告警规则（因为它们本身就是错误的）

## Impact

- **Affected specs**: 无（本次为修复性变更，不涉及规范变更）
- **Affected code**:
  - `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/metrics/WorkflowEngineMetricsCollector.java`
  - `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/config/FlowableExceptionHandler.java`
  - `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/performance/WorkflowCacheManager.java`

## 验证方式

1. **编译验证**: `mvn -f microservices/pom.xml -pl ioedream-oa-service -am compile`
2. **监控验证**: 部署到测试环境，观察 Prometheus 指标数量是否稳定
3. **内存验证**: 使用 JVM 监控工具（如 JProfiler）观察 Meter 对象数量
4. **性能验证**: 执行高并发审批流程，验证监控系统性能
