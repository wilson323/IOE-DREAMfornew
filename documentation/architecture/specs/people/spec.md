# people Specification

## Purpose
People capability manages unified personnel profiles, credentials, and区域授权，用于支撑门禁、考勤、消费等业务的身份可信与数据域控制。
## Requirements
### Requirement: 人员扩展模型与多凭证
系统 SHALL 支持人员档案扩展、证件脱敏、状态机字段以及多类凭证（CARD/BIOMETRIC/PASSWORD）的统一建模与唯一性约束。

#### Scenario: 人员多凭证唯一约束
- WHEN 为同一人员新增相同类型且相同值的凭证
- THEN 系统 SHALL 按唯一索引拒绝并返回明确错误码

#### Scenario: 人员档案状态管理
- WHEN 管理员需要更新人员状态（在职/停用/离职）
- THEN 系统 SHALL 支持状态机字段更新并记录变更历史

#### Scenario: 证件号脱敏存储
- WHEN 系统存储人员证件信息
- THEN 敏感证件号 SHALL 自动脱敏存储

### Requirement: 人员与区域授权
人员与区域的授权 SHALL 引入数据域维度并具备有效期控制，授权关系可审计。

#### Scenario: 人员跨区域查询受限
- WHEN 用户数据域为 AREA 且请求跨区域数据
- THEN 鉴权中间层应拒绝访问并记录审计日志
#### Scenario: 区域授权有效期控制
- WHEN 人员的区域授权过期
- THEN 系统应自动撤销相关权限
#### Scenario: 数据域权限继承
- WHEN 用户具有高级别数据域权限（如AREA）
- THEN 用户应自动获得低级别数据域权限（如AREA下的部门）
