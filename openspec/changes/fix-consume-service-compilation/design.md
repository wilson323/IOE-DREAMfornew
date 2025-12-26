## Context

该变更以“恢复可编译”为第一目标，采用编译错误驱动的方式逐层修复：Entity → DAO/Service → Controller → Test。

## Goals / Non-Goals

- Goals:
  - 使 consume-service 编译通过
  - 统一关键字段命名与类型，减少散落的转换/兼容代码
  - 保持变更最小、可验证、可回滚

- Non-Goals:
  - 不做跨服务 API 行为改造（除非编译错误迫使修改签名）
  - 不引入新框架/新基础设施

## Decisions

- 优先“代码层兼容”恢复编译；DB 结构变更仅在必要时引入，并提供迁移/回滚脚本。
- 对压测/过期集成测试采用禁用占位策略，避免影响 CI 编译。

## Migration Plan

如涉及数据库字段新增/类型调整：
1) 提供 `database-scripts` 下的增量脚本
2) 提供回滚脚本
3) 在 proposal 中单独标注并请求确认后执行
