# 消费补贴系统 - 规格说明

**Capability**: consume-subsidy-system
**Change ID**: consume-module-complete-implementation
**类型**: 新业务功能

---

## ADDED Requirements

### Requirement: 多补贴账户体系

用户MUST支持多个独立的补贴账户，每个补贴账户有独立的余额和过期时间

**优先级**: P0

#### Scenario: 创建补贴账户

**Given** 用户已有主账户
**When** 管理员为用户发放补贴
**Then** 应创建独立的补贴账户（POSID_SUBSIDY_ACCOUNT）
**And** 补贴账户包含：subsidy_account_id, user_id, subsidy_type_id, balance, expire_time, account_status
**And** 补贴账户与主账户通过user_id关联

#### Scenario: 查询用户所有补贴账户

**Given** 用户拥有3个补贴账户（餐补、交通补、通讯补）
**When** 调用查询补贴账户接口
**Then** 应返回3个补贴账户的完整信息
**And** 按expire_time升序排序（即将过期优先）

#### Scenario: 补贴账户余额管理

**Given** 用户餐补账户余额500元
**When** 餐补消费20元
**Then** 餐补账户余额应更新为480元
**And** 主账户余额不变
**And** 生成补贴流水记录（POSID_SUBSIDY_FLOW）

---

### Requirement: 补贴扣款优先级

消费扣款MUST遵循补贴优先级规则，优先使用即将过期的补贴

**优先级**: P0

#### Scenario: 优先扣款即将过期的补贴

**Given** 用户拥有：
- 餐补A：余额200元，过期时间2025-01-31
- 餐补B：余额300元，过期时间2025-03-31
- 交通补：余额150元，过期时间2025-02-28
**When** 用户消费100元
**Then** 应优先扣款餐补A（即将过期）
**And** 餐补A余额变为100元

#### Scenario: 同过期日期优先扣款小金额

**Given** 用户拥有：
- 餐补A：余额200元，过期时间2025-01-31
- 餐补B：余额100元，过期时间2025-01-31
**When** 用户消费50元
**Then** 应优先扣款餐补B（金额较小）
**And** 餐补B余额变为50元

#### Scenario: 补贴不足时扣款现金账户

**Given** 用户餐补余额50元
**And** 主账户余额1000元
**When** 用户消费100元
**Then** 应先扣款餐补50元
**And** 再扣款主账户50元
**And** 消费成功

#### Scenario: 补贴和现金都不足

**Given** 用户餐补余额50元
**And** 主账户余额30元
**When** 用户消费100元
**Then** 应先扣款餐补50元
**And** 再扣款主账户30元
**And** 返回"余额不足"错误
**And** 已扣款金额应回滚

---

### Requirement: 补贴类型管理

系统MUST支持多种补贴类型，每种类型有独立的配置

**优先级**: P0

#### Scenario: 定义补贴类型

**Given** 系统初始化
**When** 创建补贴类型（POSID_SUBSIDY_TYPE）
**Then** 应支持以下类型：
- 餐补（MEAL）
- 交通补（TRAFFIC）
- 通讯补（COMMUNICATION）
- 住宿补（ACCOMMODATION）
- 其他补贴（OTHER）

#### Scenario: 补贴类型与消费场景关联

**Given** 餐补类型配置为仅限餐费消费
**And** 交通补类型配置为仅限交通费
**When** 用户在餐厅消费
**Then** 应优先使用餐补
**And** 不使用交通补

---

### Requirement: 补贴发放

系统MUST支持补贴发放功能，管理员可批量发放补贴

**优先级**: P0

#### Scenario: 单个用户补贴发放

**Given** 管理员为用户张三发放餐补500元
**And** 有效期至2025-03-31
**When** 执行发放操作
**Then** 应创建补贴账户
**And** 初始余额500元
**And** 过期时间2025-03-31
**And** 状态为"正常"
**And** 生成补贴流水（发放类型）

#### Scenario: 批量补贴发放

**Given** 管理员为部门"技术部"所有员工发放餐补
**And** 每人500元，有效期至2025-03-31
**And** 部门共有100名员工
**When** 执行批量发放
**Then** 应创建100个补贴账户
**And** 每个账户余额500元
**And** 生成100条补贴流水
**And** 批量发放时间≤30秒

#### Scenario: 补贴发放失败回滚

**Given** 管理员批量发放补贴
**When** 发放到第50个用户时系统异常
**Then** 应回滚已发放的前49个账户
**And** 不创建任何补贴账户
**And** 返回"发放失败"错误

---

### Requirement: 补贴清零

系统MUST支持补贴清零功能，过期补贴自动清零

**优先级**: P0

#### Scenario: 补贴到期自动清零

**Given** 当前时间为2025-04-01
**And** 用户餐补A过期时间为2025-03-31
**And** 余额50元
**When** 执行定时任务（每日凌晨1点）
**Then** 应将餐补A余额清零
**And** 状态更新为"已过期"
**And** 生成补贴流水（清零类型）

#### Scenario: 手动补贴清零

**Given** 管理员发现用户张三的餐补发放错误
**And** 余额500元，未过期
**When** 管理员执行手动清零操作
**Then** 应将餐补余额清零
**And** 状态更新为"已清零"
**And** 生成补贴流水（手动清零类型）

#### Scenario: 补贴清零权限控制

**Given** 普通用户登录
**When** 尝试清零自己的补贴
**Then** 应返回"权限不足"错误
**And** 不执行清零操作
**When** 管理员执行清零操作
**Then** 应成功清零
**And** 记录管理员操作日志

---

### Requirement: 补贴流水记录

所有补贴操作MUST记录流水，包括发放、使用、清零

**优先级**: P0

#### Scenario: 补贴发放流水

**Given** 管理员发放补贴500元
**When** 操作完成
**Then** 应生成补贴流水记录
**And** 流水包含：flow_id, subsidy_account_id, flow_type, amount, balance_before, balance_after, operator_id, create_time

#### Scenario: 补贴使用流水

**Given** 用户使用餐补消费20元
**When** 消费完成
**Then** 应生成补贴流水记录
**And** flow_type=USE
**And** amount=-20.00

#### Scenario: 补贴清零流水

**Given** 补贴到期自动清零50元
**When** 清零完成
**Then** 应生成补贴流水记录
**And** flow_type=EXPIRE
**And** amount=-50.00

#### Scenario: 补贴流水查询

**Given** 用户有多条补贴流水
**When** 查询指定时间范围的流水
**Then** 应按时间倒序返回
**And** 包含所有操作类型
**And** 支持按流水类型筛选

---

### Requirement: 补贴账户状态管理

补贴账户MUST支持多种状态管理

**优先级**: P0

#### Scenario: 补贴账户状态枚举

**Given** 补贴账户表创建
**Then** 应支持以下状态：
- ACTIVE (正常)
- FROZEN (冻结)
- EXPIRED (已过期)
- CLEARED (已清零)

#### Scenario: 补贴账户冻结

**Given** 管理员发现用户张三的餐补发放异常
**And** 账户状态为ACTIVE
**When** 管理员执行冻结操作
**Then** 账户状态应更新为FROZEN
**And** 补贴账户余额保留
**And** 用户无法使用该补贴账户

#### Scenario: 补贴账户解冻

**Given** 用户李四的餐补账户被冻结
**And** 账户状态为FROZEN
**When** 管理员执行解冻操作
**Then** 账户状态应更新为ACTIVE
**And** 用户可正常使用补贴账户

---

## 验收标准

### 功能验收
- [ ] 多补贴账户体系完整实现
- [ ] 补贴扣款优先级正确执行
- [ ] 补贴类型管理完整
- [ ] 补贴发放功能完整
- [ ] 补贴清零功能完整
- [ ] 补贴流水记录完整
- [ ] 补贴账户状态管理完整

### 性能验收
- [ ] 补贴扣款响应时间 ≤ 30ms
- [ ] 批量发放100个账户 ≤ 30秒
- [ ] 补贴流水查询响应时间 ≤ 100ms

---

**版本**: v1.0
**最后更新**: 2025-12-23
