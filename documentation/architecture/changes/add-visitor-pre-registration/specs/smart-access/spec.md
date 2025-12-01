## ADDED Requirements

### Requirement: 访客预约流程
门禁系统 SHALL 支持访客线上预约、审批、撤销全流程，并与人员/区域模型打通。

#### Scenario: 提交访客申请
- **WHEN** 内部员工创建访客预约时
- **THEN** 系统 SHALL 收集访客信息、访问时间、访问区域与陪同人
- **AND** 系统 SHALL 校验时间冲突并生成审批单
- **AND** 系统 SHALL 向审批人发送待办提醒

#### Scenario: 审批与状态同步
- **WHEN** 审批人通过或拒绝访客申请时
- **THEN** 系统 SHALL 更新申请状态并记录操作日志
- **AND** 系统 SHALL 通知申请人结果
- **AND** 系统 SHALL 触发凭证生成或失效处理

### Requirement: 访客二维码凭证核验
系统 SHALL 为已审批访客签发一次性二维码凭证，并在门禁通行时核验凭证有效性。

#### Scenario: 签发访客凭证
- **WHEN** 访客申请被批准时
- **THEN** 系统 SHALL 生成含访客身份、有效期、区域权限的加密二维码
- **AND** 系统 SHALL 将凭证同步至门禁设备白名单
- **AND** 系统 SHALL 向访客发送凭证通知（短信/邮件/微信）

#### Scenario: 核验访客通行
- **WHEN** 访客在闸机扫码通行时
- **THEN** 系统 SHALL 校验二维码签名、有效期、访问区域与审批状态
- **AND** 系统 SHALL 自动写入通行记录并更新凭证使用状态
- **AND** 系统 SHALL 在失败时告警并阻止通行

### Requirement: 访客全程可追溯
系统 SHALL 提供访客预约、审批、通行的端到端审计能力。

#### Scenario: 审计访客轨迹
- **WHEN** 安保人员查询访客轨迹时
- **THEN** 系统 SHALL 展示预约单、审批人、凭证状态与通行记录
- **AND** 系统 SHALL 支持按时间、区域、陪同人等条件过滤
- **AND** 系统 SHALL 支持导出 CSV/PDF 报表
