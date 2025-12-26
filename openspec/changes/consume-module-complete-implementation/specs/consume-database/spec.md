# 消费数据库重构 - 规格说明

**Capability**: consume-database
**Change ID**: consume-module-complete-implementation
**类型**: 数据库架构升级

---

## ADDED Requirements

### Requirement: POSID核心表结构

消费模块MUST使用POSID_*命名规范的9张核心表，完全符合业务文档ER图设计（MUST）

**优先级**: P0

#### Scenario: 创建账户主表POSID_ACCOUNT

**Given** 数据库中存在消费账户表
**When** 执行Flyway迁移脚本V1.0.1__create_consume_tables.sql
**Then** 应成功创建POSID_ACCOUNT表，包含以下字段：
- account_id (BIGINT, PK, AUTO_INCREMENT)
- user_id (BIGINT, NOT NULL)
- account_code (VARCHAR(50), UNIQUE)
- account_name (VARCHAR(100))
- balance (DECIMAL(10,2), DEFAULT 0.00)
- frozen_amount (DECIMAL(10,2), DEFAULT 0.00)
- credit_limit (DECIMAL(10,2), DEFAULT 0.00)
- account_status (TINYINT, DEFAULT 1)
- create_time, update_time, deleted_flag, version (审计字段)

#### Scenario: 创建账户类别表POSID_ACCOUNTKIND含mode_config

**Given** POSID_ACCOUNT表已创建
**When** 执行Flyway迁移脚本
**Then** 应成功创建POSID_ACCOUNTKIND表，包含：
- kind_id (BIGINT, PK)
- kind_code (VARCHAR(50), UNIQUE)
- kind_name (VARCHAR(100))
- mode_config (JSON, COMMENT '各模式参数配置')
- discount_type (INT)
- date_max_money (INT)
- date_max_count (INT)
- 审计字段
**And** mode_config字段可正确存储6种消费模式的JSON配置

#### Scenario: 创建9张核心表

**Given** 执行Flyway迁移脚本
**When** 创建所有表
**Then** 应创建以下9张核心表：
1. POSID_ACCOUNT (账户表)
2. POSID_ACCOUNTKIND (账户类别表，含mode_config)
3. POSID_AREA (区域表，含fixed_value_config和area_config)
4. POSID_MEAL (餐别表)
5. POSID_MEAL_CATEGORY (餐别分类表)
6. POSID_TRANSACTION (交易流水表，按月分区)
7. POSID_SUBSIDY_TYPE (补贴类型表)
8. POSID_SUBSIDY_ACCOUNT (补贴账户表)
9. POSID_SUBSIDY_FLOW (补贴流水表)
10. POSID_RECHARGE_ORDER (充值订单表，按月分区)
11. POSID_CAPITAL_FLOW (资金流水表，按月分区)

---

### Requirement: JSON配置字段支持

数据库MUST支持JSON类型字段，用于存储复杂的业务配置（mode_config, area_config, fixed_value_config）

**优先级**: P0

#### Scenario: mode_config字段存储6种消费模式配置

**Given** POSID_ACCOUNTKIND表包含mode_config字段(JSON类型)
**When** 插入账户类别记录，mode_config包含FIXED_AMOUNT、FREE_AMOUNT、METERED等模式配置
**Then** JSON数据应成功存储
**And** 可通过JSON_EXTRACT函数查询具体配置项

#### Scenario: fixed_value_config字段存储定值配置

**Given** POSID_AREA表包含fixed_value_config字段(JSON类型)
**When** 插入区域记录，fixed_value_config包含各餐别定值金额
**Then** JSON数据应成功存储
**And** 可查询任意餐别定值金额

#### Scenario: area_config字段存储区域权限配置

**Given** POSID_AREA表包含area_config字段(JSON类型)
**When** 插入区域记录，area_config包含区域权限数组
**Then** JSON数组应成功存储
**And** 可查询指定区域的权限列表

---

### Requirement: 表分区策略

交易相关大表MUST按月分区，保证查询性能和数据管理能力

**优先级**: P0

#### Scenario: POSID_TRANSACTION表按月分区

**Given** POSID_TRANSACTION表已创建
**When** 查询information_schema.PARTITIONS
**Then** 应显示分区策略为RANGE
**And** 分区表达式为TO_DAYS(create_time)
**And** 存在当前月及未来5个月的分区
**And** 分区命名规范为p202501, p202502, ...

#### Scenario: 自动创建未来分区

**Given** POSID_TRANSACTION表已分区
**When** 执行定时任务（每月25日执行）
**Then** 应自动创建下个月的分区
**And** 删除12个月前的旧分区

#### Scenario: 分区查询性能验证

**Given** POSID_TRANSACTION表包含1000万条数据
**When** 查询指定月份的交易记录
**Then** 查询时间应<100ms
**And** EXPLAIN显示只扫描对应分区

---

### Requirement: 索引优化

核心查询字段MUST建立合适的索引，保证查询性能

**优先级**: P0

#### Scenario: 账户表索引

**Given** POSID_ACCOUNT表
**When** 查询索引列表
**Then** 应包含以下索引：
- PRIMARY KEY (account_id)
- UNIQUE INDEX uk_account_code (account_code)
- INDEX idx_user_id (user_id)
- INDEX idx_account_status (account_status)

#### Scenario: 交易表索引

**Given** POSID_TRANSACTION表
**When** 查询索引列表
**Then** 应包含：
- PRIMARY KEY (transaction_id)
- UNIQUE INDEX uk_order_no (order_no, deleted_flag)
- INDEX idx_account_id (account_id)
- INDEX idx_consume_time (consume_time)
- INDEX idx_offline_sync (offline_flag, sync_status)

---

### Requirement: 数据迁移完整性

MUST从t_consume_*表迁移数据到POSID_*表，保证零数据丢失

**优先级**: P0

#### Scenario: 账户数据迁移

**Given** t_consume_account表包含10万条记录
**When** 执行迁移脚本migrate_accounts.sql
**Then** POSID_ACCOUNT表应包含10万条记录
**And** 所有字段数据完整映射
**And** 迁移后数据一致性验证通过

#### Scenario: 双写验证数据一致性

**Given** 双写验证期运行中
**When** 每10分钟对比新旧表数据
**Then** 数据一致性应≥99.9%
**And** 差异数据自动告警
**And** 连续24小时无差异后可切换

---

### Requirement: 数据完整性约束

MUST 通过外键、唯一性约束等保证数据完整性

**优先级**: P0

#### Scenario: 外键约束

**Given** POSID_TRANSACTION表
**When** 插入交易记录
**Then** account_idMUST引用POSID_ACCOUNT.account_id
**And** 违反外键约束时应报错

#### Scenario: 唯一性约束

**Given** POSID_ACCOUNT表
**When** 插入重复的account_code
**Then** 应抛出唯一性约束违反异常
**And** account_code全局唯一

---

## 验收标准

### 结构验收
- [ ] 9张POSID_*核心表全部创建成功
- [ ] JSON字段可正确读写
- [ ] 3张大表按月分区
- [ ] 所有索引创建成功

### 数据验收
- [ ] 账户数据迁移成功率100%
- [ ] 交易数据迁移成功率100%
- [ ] 双写验证期数据一致性≥99.9%

---

**版本**: v1.0
**最后更新**: 2025-12-23
