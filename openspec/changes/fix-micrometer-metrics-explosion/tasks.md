# Tasks: 修复 Micrometer 指标爆炸问题

## 1. WorkflowEngineMetricsCollector 修复

- [ ] 1.1 移除 `recordProcessDeployment` 方法中重复注册的 `workflow.process.deployment.count.tagged` Counter（带 `process_key` 标签）
- [ ] 1.2 移除 `recordProcessStart` 方法中重复注册的 `workflow.process.start.count.tagged` Counter（带 `process_key`、`business_key` 标签）
- [ ] 1.3 移除 `recordProcessComplete` 方法中重复注册的 `workflow.process.complete.count.tagged` Counter（带 `process_key`、`outcome` 标签）
- [ ] 1.4 移除 `recordTaskCreate` 方法中重复注册的 `workflow.task.create.count.tagged` Counter（带 `task_name`、`process_key` 标签）
- [ ] 1.5 移除 `recordTaskComplete` 方法中重复注册的 `workflow.task.complete.count.tagged` Counter（带 `task_name`、`outcome` 标签）
- [ ] 1.6 移除 `recordTaskAssign` 方法中重复注册的 `workflow.task.assign.count.tagged` Counter（带 `task_name`、`assignee_id` 标签）
- [ ] 1.7 移除 `recordError` 方法中重复注册的 `workflow.error.count.tagged` Counter（带 `operation`、`error_type` 标签）
- [ ] 1.8 保留基础 Counter（在 `@PostConstruct` 中已注册的），仅调用 `increment()` 方法

## 2. FlowableExceptionHandler 修复

- [ ] 2.1 移除 `recordExceptionDetailMetrics` 方法中的 `timestamp` 标签（高基数标签）
- [ ] 2.2 改为使用固定标签集合：`error_code`、`category`（移除 `timestamp`）
- [ ] 2.3 确保 Counter 在 `@PostConstruct` 中一次性注册，而非每次调用时注册

## 3. WorkflowCacheManager 修复

- [ ] 3.1 检查 `get` 方法中是否存在重复注册的带标签 Counter
- [ ] 3.2 如存在，移除方法内部的 `Counter.builder(...).tag(...).register(meterRegistry).increment()` 调用
- [ ] 3.3 保留在构造函数中已注册的基础 Counter，仅调用 `increment()` 方法

## 4. 代码审查与验证

- [ ] 4.1 全量搜索 `Counter.builder(...).register(meterRegistry)` 模式，确保无遗漏
- [ ] 4.2 全量搜索 `tag("timestamp"` 模式，确保无高基数时间戳标签
- [ ] 4.3 验证所有 Meter 注册均在 `@PostConstruct` 或构造函数中完成

## 5. 测试验证

- [ ] 5.1 编译验证：`mvn -f microservices/pom.xml -pl ioedream-oa-service -am compile`
- [ ] 5.2 单元测试：执行相关组件的单元测试，确保指标收集功能正常
- [ ] 5.3 集成测试：部署到测试环境，执行审批流程，验证指标收集
- [ ] 5.4 监控验证：观察 Prometheus 指标数量是否稳定（无持续增长）

## 6. 文档更新（如需要）

- [ ] 6.1 如有 Grafana Dashboard，更新指标名称（如需要）
- [ ] 6.2 更新监控文档，说明指标标签变更
