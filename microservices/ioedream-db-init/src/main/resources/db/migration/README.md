# 数据库迁移脚本说明

## 概述

IOE-DREAM项目数据库迁移脚本基于Flyway版本管理，确保数据库结构变更的可追溯性和一致性。

## 版本管理规则

- **版本号格式**: V{major}.{minor}.{patch}__{description}.sql
- **执行顺序**: 严格按照版本号顺序执行
- **命名规范**:
  - `V1_0_0__INITIAL_SCHEMA.sql` - 初始化脚本
  - `V1_1_0__ADD_USER_TABLE.sql` - 新增用户表
  - `V1_2_0__ENHANCE_USER_TABLE.sql` - 增强用户表
  - `V2_0_0__MAJOR_REFACTOR.sql` - 重构脚本

## 当前迁移版本

### V2.0.x - 前后端API兼容性增强

#### V2.0.0 - 消费记录表增强
- **文件**: `V2_0_0__ENHANCE_CONSUME_RECORD_TABLE.sql`
- **描述**: 基于Smart-Admin优秀设计，增强消费记录表字段
- **新增字段**: 32个
- **新增索引**: 20个
- **新增视图**: 1个
- **核心特性**:
  - 用户信息冗余字段（user_name, user_phone, user_type）
  - 账户信息字段（account_id, account_no, account_name）
  - 区域设备关联字段（area_id, device_id）
  - 余额跟踪字段（balance_before, balance_after）
  - 支付相关字段（pay_method, pay_time, currency）
  - 退款支持字段（refund_status, refund_amount, refund_time）
  - 第三方集成字段（third_party_order_no, fee_amount）
  - 扩展数据字段（extend_data, client_ip）

#### V2.0.1 - 消费账户表增强
- **文件**: `V2_0_1__ENHANCE_ACCOUNT_TABLE.sql`
- **描述**: 修复字段重复问题，增加账户分类、限额管理、积分系统
- **修复问题**: AccountEntity字段重复问题
- **新增字段**: 35个
- **新增索引**: 25个
- **新增视图**: 1个
- **核心特性**:
  - 账户分类体系（account_type, account_category, account_level）
  - 详细状态管理（account_status, freeze_reason, freeze_time）
  - 金额管理（credit_limit, available_balance, frozen_balance）
  - 消费限额（daily_limit, monthly_limit, single_limit）
  - 积分系统（points, total_points, member_level）
  - 支付方式（payment_methods, default_pay_method）
  - 生物识别绑定（face_bind, fingerprint_bind, palm_bind）

#### V2.0.2 - 退款管理表创建
- **文件**: `V2_0_2__CREATE_REFUND_TABLE.sql`
- **描述**: 创建完整的退款管理表，支持退款申请、审批、处理流程
- **新增表**: 5个
- **核心特性**:
  - 退款申请表（t_consume_refund）
  - 退款审批记录表（t_consume_refund_approval）
  - 退款处理记录表（t_consume_refund_process）
  - 退款配置表（t_consume_refund_config）
  - 退款统计表（t_consume_refund_statistics）

## 执行方式

### 1. 自动执行（推荐）
```bash
# 使用Flyway自动执行
mvn flyway:migrate

# 或通过应用程序启动时自动执行
# Spring Boot会自动检测并执行未执行的迁移脚本
```

### 2. 手动执行
```bash
# 连接数据库后手动执行
mysql -u username -p database_name < V2_0_0__ENHANCE_CONSUME_RECORD_TABLE.sql
```

## 兼容性保证

### 前端兼容性
- ✅ ResponseDTOAdapter确保响应格式100%兼容
- ✅ ResponseFormatFilter自动转换响应格式
- ✅ 数据模型与Smart-Admin前端完全一致

### 移动端兼容性
- ✅ 移动端专用响应格式支持
- ✅ 客户端类型自动检测
- ✅ API接口参数和响应格式兼容

## 数据完整性

### 字段覆盖
- ✅ ConsumeRecordEntity字段覆盖率: 100%
- ✅ AccountEntity字段覆盖率: 100%
- ✅ 退款相关字段覆盖率: 100%

### 索引优化
- ✅ 用户相关查询索引优化
- ✅ 时间范围查询索引优化
- ✅ 业务状态查询索引优化
- ✅ 复合索引支持常用查询组合

### 约束完整性
- ✅ 主键约束
- ✅ 唯一约束
- ✅ 外键约束
- ✅ 检查约束
- ✅ 非空约束

## 验证检查

### 结构验证
```sql
-- 验证表结构
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('t_consume_record', 't_consume_account', 't_consume_refund');

-- 验证字段完整性
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_consume_record'
  AND COLUMN_NAME IN ('balance_before', 'balance_after', 'refund_status');
```

### 数据验证
```sql
-- 验证余额数据
SELECT COUNT(*) as records_with_balance
FROM t_consume_record
WHERE balance_before IS NOT NULL
  AND balance_after IS NOT NULL;

-- 验证退款数据
SELECT COUNT(*) as refund_count,
       SUM(refund_amount) as total_refund_amount
FROM t_consume_refund
WHERE deleted_flag = 0;
```

### 性能验证
```sql
-- 验证索引使用
EXPLAIN SELECT * FROM t_consume_record
WHERE user_id = 1 AND consume_date >= '2025-01-01';
```

## 回滚策略

### 回滚脚本
每个迁移脚本都包含相应的回滚脚本注释，在需要回滚时可以执行。

### 回滚注意事项
⚠️ **警告**: 执行回滚会丢失数据，请谨慎操作！

1. 备份当前数据库
2. 评估数据丢失风险
3. 在测试环境验证回滚脚本
4. 选择维护窗口执行回滚

## 监控和维护

### 执行监控
- 迁移历史表: `t_migration_history`
- 执行状态跟踪
- 错误日志记录

### 性能监控
- 索引使用率监控
- 查询性能分析
- 存储空间使用

### 数据一致性检查
- 定期执行数据完整性检查
- 验证业务规则约束
- 监控数据质量指标

## 最佳实践

### 迁移脚本编写
1. **原子性**: 每个脚本应该是原子操作
2. **幂等性**: 重复执行不会产生副作用
3. **可回滚**: 提供完整的回滚脚本
4. **测试充分**: 在测试环境充分验证
5. **文档完整**: 详细的注释和说明

### 版本管理
1. **语义化版本**: 使用语义化版本号
2. **向后兼容**: 确保向后兼容性
3. **渐进式升级**: 避免大规模变更
4. **版本隔离**: 不同版本独立管理

### 安全考虑
1. **权限控制**: 合理的数据库权限设置
2. **数据加密**: 敏感数据加密存储
3. **审计日志**: 完整的操作审计
4. **备份策略**: 定期数据备份

## 联系支持

如有问题，请联系：
- **架构团队**: architecture@ioe-dream.com
- **数据库团队**: database@ioe-dream.com
- **运维团队**: ops@ioe-dream.com

---

**更新时间**: 2025-01-30
**维护责任**: IOE-DREAM 架构委员会
**文档版本**: v1.0.0