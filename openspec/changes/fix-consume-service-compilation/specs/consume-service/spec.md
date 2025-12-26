# Consume Service Specification Delta

## ADDED Requirements

### Requirement: Consume Service 编译与模型一致性

系统 SHALL 保证 `ioedream-consume-service` 的核心领域模型（Entity/DTO）与业务代码引用保持一致，且模块在 CI 编译阶段无错误。

#### Scenario: Compile consume-service successfully
- **WHEN** 执行 `mvn -f microservices/pom.xml -pl ioedream-consume-service -am test`
- **THEN** 构建 SHALL 成功完成且无编译错误

#### Scenario: Entity fields match service/controller usage
- **GIVEN** Service/Controller 引用某实体字段
- **WHEN** 代码编译与静态检查执行
- **THEN** 实体 SHALL 定义对应字段或提供明确的兼容访问器，且类型一致

### Requirement: Consume 流程设备校验

系统 SHALL 在消费流程执行前完成设备合法性校验（状态、归属、可用性），失败时返回明确错误。

#### Scenario: Reject invalid device
- **GIVEN** 设备状态不可用或不存在
- **WHEN** 发起消费流程
- **THEN** 系统 SHALL 拒绝流程并返回明确错误码

### Requirement: Consume 流程持久化

系统 SHALL 在消费流程完成时持久化消费记录与支付记录，字段完整且类型一致。

#### Scenario: Persist consume record
- **WHEN** 消费流程成功完成
- **THEN** 系统 SHALL 写入消费记录与支付记录并保持字段一致性

### Requirement: 消费成功通知与消息

系统 SHALL 在消费成功后按配置触发通知/消息（WebSocket/RabbitMQ），失败不影响主流程但记录错误。

#### Scenario: Notify after success
- **GIVEN** 通知功能已启用
- **WHEN** 消费成功完成
- **THEN** 系统 SHALL 触发通知/消息并记录异常（如失败）

## MODIFIED Requirements

### Requirement: Common Module Boundary

系统 SHALL 遵循公共模块边界：消费域实体/DAO/业务逻辑不得回流到 `microservices-common`。

#### Scenario: Prevent consume-domain leakage
- **GIVEN** 开发者新增消费域 Entity/DAO/Manager
- **WHEN** 代码试图提交到 `microservices-common`
- **THEN** CI/静态检查 SHALL 阻止合入并提示迁移到 `ioedream-consume-service`
