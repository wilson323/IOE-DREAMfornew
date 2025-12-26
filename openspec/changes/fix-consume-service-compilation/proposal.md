# Proposal: 修复 Consume Service 编译错误并对齐领域模型

## 变更ID

`fix-consume-service-compilation`

## 优先级

**P0** - 阻断编译/CI

## Why

`ioedream-consume-service` 当前存在大量编译错误与模型/类型不一致问题（见 `D:/IOE-DREAM/chonggou.txt`），会阻断 CI 与交付。

## What Changes

- 补齐/对齐 consume 领域核心实体字段与类型（含必要 getter/兼容方法）
- 修复 DAO/Service/Controller 层因类型不一致导致的编译错误
- 补齐消费流程中的关键 TODO（设备校验、持久化、通知/消息），仅实现当前业务必需逻辑
- 修复/禁用无法在单测阶段执行且导致编译失败的测试类（仅限压测/过期集成测试）

## 目标（验收标准）

- [ ] `ioedream-consume-service` 在 `mvn -f microservices/pom.xml -pl ioedream-consume-service -am test` 下编译通过
- [ ] 关键实体字段与代码引用一致（避免反射/拼字段名式访问）
- [ ] Service/Controller 方法签名与 DTO/Entity 类型一致，消除显式类型转换散落
- [ ] 保持变更最小化（KISS/DRY/YAGNI），不引入不必要的新框架

## 风险与边界

- 数据库结构变更（新增列/改类型）属于高风险操作：本变更默认优先采用“代码兼容修复”以恢复编译；若必须改表结构，将单独列出迁移脚本与回滚方案并请求明确确认。

## Impact

- Affected specs: consume-service
- Affected code: `microservices/ioedream-consume-service`（Entity/DAO/Service/Controller/Test/Flow）

## 验证方式

- `mvn -f microservices/pom.xml -pl ioedream-consume-service -am test`
- 如需全量：`mvn -f microservices/pom.xml test`（非本变更默认验收范围）
