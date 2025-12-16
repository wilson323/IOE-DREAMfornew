## 阶段2增强：全局架构梳理（2025-12-14）

- **伦理防火墙模块已存在**：`microservices-common-core/src/main/java/net/lab1024/sa/common/ai/ethics/` 下已实现 `EthicsFirewall`、`EthicsRequest`、`EthicsDecisionType`，为纯Java类、策略链、Fail-Closed、审计日志；包含 TRACE 注释。
- **Feign 使用风险点**：发现 `ioedream-database-service` 启用了 `@EnableFeignClients`（架构基线偏离风险）。
- **Tracing/metrics 配置线索较多**：存在 tracing/monitoring 相关配置与类（需进一步梳理是否全链路闭环）。
- **配置中疑似明文密码/ENC 标记**：在 microservices 配置文件中存在 `password:` 等匹配命中（需进一步逐文件确认是否明文或已加密/占位）。
- **PowerShell 启动链路乱码风险**：start.ps1 与项目脚本编码规范存在潜在冲突（需统一）。
