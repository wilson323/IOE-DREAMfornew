# 提案：优化考勤规则执行上下文

## 变更ID
`refine-attendance-rule-execution-context`

## 概述

优化考勤服务规则执行上下文（`RuleExecutionContext`）的设计和实现，提升代码质量、可维护性和测试覆盖率。

**背景**：
- 在修复 `ioedream-attendance-service` 编译错误过程中，发现 `RuleExecutionContext` 使用手动编写的内部Builder类，而不是Lombok的 `@Builder` 注解
- 测试服务（`RuleTestServiceImpl`）需要将大量测试专用字段（如 `punchTime`、`deviceId`等）放入 `customVariables` Map中，缺乏类型安全
- 缺少单元测试覆盖修复的关键方法

## 目标

1. **使用Lombok @Builder注解**：替换手动Builder，减少样板代码，提升可维护性
2. **引入RuleTestContext专用类**：将测试专用字段从 `customVariables` Map中提取到强类型类中
3. **添加单元测试**：为修复的方法和新增的类添加完整的单元测试覆盖

## 范围

### 包含
- `RuleExecutionContext` 类的重构（添加Lombok注解）
- 新增 `RuleTestContext` 类（继承或组合 `RuleExecutionContext`）
- `RuleTestServiceImpl.buildExecutionContext()` 方法的重构
- 单元测试覆盖（≥80%覆盖率）

### 不包含
- 规则引擎核心逻辑的修改
- 其他服务的类似重构（仅限考勤服务）
- 性能优化（除非当前实现存在明显性能问题）

## 相关变更

- **依赖**：无（独立优化）
- **后续影响**：无（向后兼容）
- **相关规范**：
  - `api-contract` (可能影响规则测试API)
  - `common-boundaries` (engine层与service层边界)

## 验收标准

1. ✅ `RuleExecutionContext` 使用 `@Builder` 注解，编译通过
2. ✅ `RuleTestContext` 类提供类型安全的测试字段访问
3. ✅ 所有新增和修改的方法单元测试覆盖率 ≥80%
4. ✅ 现有功能100%向后兼容，无破坏性变更
5. ✅ 代码符合CLAUDE.md规范（Jakarta EE、@Resource、四层架构）

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| Lombok @Builder生成的方法签名与手动Builder不一致 | 中 | 先生成新Builder，保留旧方法作为@Deprecated，逐步迁移 |
| RuleTestContext引入导致API复杂化 | 低 | 保持内部使用类，不对外暴露 |
| 单元测试编写工作量较大 | 低 | 优先覆盖核心方法，测试辅助方法可适当降低覆盖率 |

## 时间估算

- 设计评审：0.5小时
- 实现（包括测试）：2-3小时
- 代码审查和验证：0.5小时
- **总计**：3-4小时

## 提案状态

**状态**：待批准

**下一步**：等待架构委员会审查和批准
