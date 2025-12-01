## MODIFIED Requirements

### Requirement: 访客权限集成
The access control system MUST integrate with the visitor microservice to implement unified visitor permission management and real-time verification.

#### Scenario: 访客权限验证
- **WHEN** 访客在门禁设备进行身份验证
- **THEN** 门禁系统调用访客微服务验证权限
- **AND** 根据返回结果决定是否允许通行

#### Scenario: 权限状态同步
- **WHEN** 访客权限状态发生变更
- **THEN** 访客微服务推送状态变更事件
- **AND** 门禁系统实时更新权限缓存

#### Scenario: 访客通行记录
- **WHEN** 访客通过门禁设备
- **THEN** 门禁系统记录通行信息
- **AND** 异步同步到访客微服务

### Requirement: 设备通信协议
Access control devices MUST support remote visitor permission verification and management, including permission distribution, status reporting, and emergency revocation capabilities.

#### Scenario: 远程权限下发
- **WHEN** 访客预约获得批准
- **THEN** 系统向相关门禁设备下发访客权限
- **AND** 设备确认权限接收成功

#### Scenario: 设备状态上报
- **WHEN** 门禁设备检测到访客通行
- **THEN** 设备上报通行结果和相关信息
- **AND** 访客微服务记录并处理通行事件

#### Scenario: 紧急权限撤销
- **WHEN** 需要紧急撤销访客权限
- **THEN** 系统向所有相关设备发送撤销指令
- **AND** 设备立即停止该访客的通行权限

### Requirement: 实时事件处理
The access control system MUST implement real-time event communication with the visitor microservice, supporting visitor arrival notifications, anomaly alerts, and departure confirmations.

#### Scenario: 访客到达通知
- **WHEN** 访客到达指定区域
- **THEN** 门禁系统触发到达事件
- **AND** 通知接待人和相关管理系统

#### Scenario: 异常行为告警
- **WHEN** 检测到访客异常行为
- **THEN** 系统生成安全告警
- **AND** 通知安保人员和管理员

#### Scenario: 访客离开确认
- **WHEN** 访客离开访问区域
- **THEN** 系统记录离开时间和地点
- **AND** 更新访客访问状态为已完成