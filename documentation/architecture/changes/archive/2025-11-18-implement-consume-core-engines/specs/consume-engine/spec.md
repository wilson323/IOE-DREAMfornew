## ADDED Requirements

### Requirement: 原子性消费处理引擎
ConsumeEngineService **MUST** provide atomic consumption processing capability, ensuring balance deduction and transaction record creation are completed within the same transaction, guaranteeing zero risk for fund operations.

#### Scenario: 正常消费流程处理
- WHEN 用户发起包含账户ID、消费金额和订单号的消费请求
- THEN 系统在单一事务中完成账户余额扣减和交易记录创建
- AND 返回成功结果包含交易ID和余额信息
- AND 确保数据库状态一致性，无中间状态数据

#### Scenario: 并发消费保护
- WHEN 同一账户有多个并发消费请求
- THEN 系统使用分布式锁按顺序处理消费请求
- AND 防止重复扣费或余额错误
- AND 确保最终余额计算准确

### Requirement: 幂等性保护机制
System **MUST** implement order number-based idempotency protection, ensuring duplicate requests with the same order number will not result in duplicate deductions.

#### Scenario: 重复订单请求处理
- WHEN 用户使用相同订单号发起重复消费请求
- THEN 系统检测到订单号已存在，返回原始交易结果
- AND 不执行任何余额扣减操作
- AND 记录重复请求日志用于监控

### Requirement: 账户验证和权限检查
Before processing consumption, system **MUST** perform complete account validation, permission checking, and limit control, ensuring only compliant consumption requests can be executed.

#### Scenario: 账户状态验证
- WHEN 请求消费的账户处于非正常状态（冻结、注销等）
- THEN 系统拒绝消费请求并返回明确错误信息
- AND 记录异常操作日志

#### Scenario: 消费限额检查
- WHEN 用户的消费金额或频率超出预设限额
- THEN 系统拒绝消费请求并提示限额信息
- AND 支持多级限额（单次、日度、月度）检查

### Requirement: 消费模式引擎架构
System **MUST** implement a pluggable consumption mode engine, supporting dynamic management and execution of 6 consumption modes: fixed amount, free amount, metering, product scanning, ordering, and smart mode.

#### Scenario: 消费模式动态注册
- WHEN 消费模式引擎初始化
- THEN 系统自动注册所有可用的消费模式
- AND 支持模式的动态启用/禁用
- AND 提供模式健康检查功能

#### Scenario: 消费模式执行
- WHEN 用户指定特定的消费模式发起消费
- THEN 系统执行该模式特有的验证和计算逻辑
- AND 返回模式特定的处理结果
- AND 记录模式执行日志用于性能分析

### Requirement: 固定金额档位支持
Fixed amount mode **MUST** support predefined amount tiers (5, 8, 10, 12, 15, 18, 20, 25 yuan) and custom amounts, ensuring accurate amount calculation.

#### Scenario: 预定义档位消费
- WHEN 用户选择预定义的金额档位进行消费
- THEN 系统使用预定义档位金额作为基础金额
- AND 应用相应的折扣或补贴政策
- AND 返回最终的消费金额

#### Scenario: 自定义金额验证
- WHEN 用户输入自定义金额进行消费
- THEN 系统验证金额是否为有效的固定金额档位
- AND 拒绝非标准金额的消费请求
- AND 提供金额档位建议

### Requirement: 餐别时段控制
Fixed amount mode **MUST** support meal period control, applying different amounts and subsidy policies based on different meal types (breakfast, lunch, dinner).

#### Scenario: 餐别时间验证
- WHEN 用户在非允许的餐别时间段发起消费
- THEN 系统拒绝消费请求并提示餐别时间限制
- AND 支持节假日和特殊日期的时间调整

### Requirement: 原子性余额扣减
AccountService **MUST** provide atomic balance deduction functionality, using optimistic locking and version control to ensure data consistency in concurrent environments.

#### Scenario: 乐观锁余额扣减
- WHEN 存在并发余额扣减请求
- THEN 系统使用版本号控制并发更新
- AND 版本冲突时抛出异常，要求重试
- AND 确保最终余额计算准确

#### Scenario: 余额不足检查
- WHEN 用户账户余额不足当前消费金额
- THEN 系统拒绝消费请求并返回余额不足提示
- AND 显示当前余额和消费金额差值

### Requirement: 余额变动历史记录
System **MUST** record all balance change history, supporting detailed change reasons, time, amount and other information for reconciliation and auditing.

#### Scenario: 余额变动记录
- WHEN 发生余额扣减操作
- THEN 系统在余额变动历史表中记录详细信息
- AND 包含变动前余额、变动金额、变动后余额
- AND 记录操作时间、操作原因、关联订单号等

### Requirement: 分布式锁保护
System **MUST** use distributed locks to protect critical fund operations, preventing data races and inconsistencies in concurrent environments.

#### Scenario: 账户级别锁定
- WHEN 多个请求同时操作同一账户
- THEN 系统获取分布式锁，按顺序处理请求
- AND 锁超时后自动释放，防止死锁
- AND 记录锁竞争情况用于性能监控

### Requirement: 操作审计日志
System **MUST** record complete operation audit logs, including user identity, operation time, operation content, results, etc., to meet security compliance requirements.

#### Scenario: 消费操作审计
- WHEN 用户执行消费操作
- THEN 系统记录详细的审计日志
- AND 包含用户ID、账户ID、消费金额、设备信息
- AND 支持日志查询和分析功能

## MODIFIED Requirements

### 无修改需求

## REMOVED Requirements

### 无移除需求