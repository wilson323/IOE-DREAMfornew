## ADDED Requirements
### Requirement: 移动端消费接口
系统 SHALL 提供移动端消费接口，路径前缀为 `/api/v1/consume/mobile`，覆盖移动端快速消费、扫码消费、NFC消费、人脸消费等核心流程。

#### Scenario: 快速消费成功
- **WHEN** 客户端提交快速消费请求
- **THEN** 系统 SHALL 返回成功响应并包含标准响应结构

#### Scenario: 离线消费同步
- **WHEN** 客户端提交离线交易同步请求
- **THEN** 系统 SHALL 返回同步处理结果

### Requirement: 移动端消费权限校验
系统 SHALL 提供移动端消费权限校验接口，并返回明确的校验结果。

#### Scenario: 权限校验通过
- **WHEN** 客户端提交权限校验请求
- **THEN** 系统 SHALL 返回校验通过结果