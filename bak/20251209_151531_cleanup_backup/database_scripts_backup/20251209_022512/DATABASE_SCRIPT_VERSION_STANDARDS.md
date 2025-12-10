# IOE-DREAM 数据库脚本版本体系规范

**版本**: v1.0.0
**生效日期**: 2025-12-08
**适用范围**: IOE-DREAM 数据库初始化脚本 (`scripts/ioedream-db-init/`)
**规范等级**: 强制执行 (P0级)

## 📋 核心设计原则

### 1. 版本生命周期管理

```
dev → test → pre-prod → prod
  ↓      ↓        ↓        ↓
开发版  测试版   预发版   生产版
```

**版本命名规范**:
- `主版本.次版本.修订号-环境`
- 示例: `1.0.0-dev`, `1.0.1-test`, `1.1.0-prod`

### 2. 脚本执行顺序（强制执行）

```
00-环境准备     → 01-创建数据库   → 02-公共表结构  → 03-业务表结构
       ↓              ↓              ↓              ↓
    flyway初始化  → 99-版本管理     → data/基础数据  → rollback/回滚脚本
```

### 3. 文件组织结构

```
scripts/ioedream-db-init/
├── VERSION                      ← 版本管理配置
├── CHANGELOG.md                 ← 版本变更记录
├── init-all.sql                 ← 统一执行入口
├── 00-environment/              ← 环境准备脚本
│   ├── 00-charset.sql
│   └── 00-timezone.sql
├── 01-databases/                ← 数据库创建
│   └── 01-create-databases.sql
├── 02-common-schema/            ← 公共表结构
│   ├── 02-common-tables.sql
│   ├── 02-rbac-tables.sql
│   └── 02-dict-tables.sql
├── 03-business-schema/          ← 业务表结构
│   ├── 03-access-schema.sql
│   ├── 03-attendance-schema.sql
│   ├── 03-consume-schema.sql
│   └── 03-visitor-schema.sql
├── 99-flyway-schema/            ← Flyway版本管理
│   └── 99-flyway-schema.sql
├── data/                        ← 初始化数据
│   ├── common-data.sql
│   └── business-data.sql
└── rollback/                    ← 回滚脚本
    ├── rollback-v1.0.0.sql
    └── rollback-v1.0.1.sql
```

## 🔢 版本执行顺序机制

### 执行顺序矩阵

| 执行步骤 | 目录前缀 | 依赖关系 | 执行方式 | 失败处理 |
|---------|---------|---------|---------|---------|
| 1 | `00-` | 无 | 顺序执行 | 必须成功 |
| 2 | `01-` | 依赖00 | 顺序执行 | 必须成功 |
| 3 | `02-` | 依赖01 | 顺序执行 | 必须成功 |
| 4 | `03-` | 依赖02 | 顺序执行 | 必须成功 |
| 5 | `99-` | 依赖03 | 顺序执行 | 必须成功 |
| 6 | `data/` | 依赖99 | 顺序执行 | 必须成功 |

### 脚本命名规范

```sql
-- ✅ 正确命名
01-create-databases.sql
02-common-tables.sql
03-access-schema.sql
99-flyway-schema.sql

-- ❌ 错误命名
create_database.sql     (缺少前缀)
common_schema_v2.sql    (版本号在文件名中)
access.sql              (太简单)
```

## 📊 版本管理机制

### 1. VERSION文件格式

```ini
[version]
major = 1
minor = 0
patch = 0
environment = prod
build_time = 2025-12-08 10:30:00
git_commit = abc123def456

[scripts]
total_scripts = 15
last_script = 99-flyway-schema.sql
last_update = 2025-12-08 10:30:00

[dependencies]
mysql_version = 8.0+
redis_version = 6.0+
java_version = 17+
```

### 2. CHANGELOG.md格式

```markdown
# 版本变更记录

## [1.0.1-prod] - 2025-12-08

### 修复 (Fixed)
- 修复 AccountEntity 字段重复问题
- 修复 AttendanceShiftEntity 表名不匹配问题
- 统一金额字段类型为 DECIMAL(12,2)

### 新增 (Added)
- 新增 t_consume_record 表
- 新增 t_visitor_record 表
- 新增完整的审计字段

### 变更 (Changed)
- 数据库表结构优化
- 索引性能优化
- 初始化数据完善

## [1.0.0-prod] - 2025-12-07

### 新增 (Added)
- 初始版本创建
- 基础表结构定义
- 初始化数据填充
```

### 3. 版本升级流程

```
1. 开发阶段 (dev)
   ├── 修改脚本文件
   ├── 测试脚本执行
   └── 更新 CHANGELOG.md

2. 测试验证 (test)
   ├── 环境测试
   ├── 功能验证
   └── 性能测试

3. 预发验证 (pre-prod)
   ├── 生产环境验证
   ├── 数据迁移测试
   └── 回滚测试

4. 生产发布 (prod)
   ├── 备份当前数据库
   ├── 执行新版本脚本
   └── 验证功能正常
```

## 🔧 自动化执行机制

### 1. 执行配置文件

```yaml
# execution-config.yml
version: 1.0.0
environment: prod
execution_mode: sequential

execution_steps:
  - name: "环境准备"
    directory: "00-environment"
    required: true
    timeout: 300

  - name: "数据库创建"
    directory: "01-databases"
    required: true
    timeout: 600
    depends_on: ["环境准备"]

  - name: "公共表结构"
    directory: "02-common-schema"
    required: true
    timeout: 1200
    depends_on: ["数据库创建"]

  - name: "业务表结构"
    directory: "03-business-schema"
    required: true
    timeout: 1800
    depends_on: ["公共表结构"]

  - name: "版本管理"
    directory: "99-flyway-schema"
    required: true
    timeout: 300
    depends_on: ["业务表结构"]

  - name: "初始化数据"
    directory: "data"
    required: true
    timeout: 600
    depends_on: ["版本管理"]

error_handling:
  max_retries: 3
  retry_delay: 10
  rollback_on_failure: true
  notify_admins: true

validation:
  check_table_count: true
  check_data_integrity: true
  check_foreign_keys: true
```

### 2. 执行状态跟踪

```sql
-- 执行状态记录表
CREATE TABLE script_execution_log (
    execution_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_name VARCHAR(200) NOT NULL,
    script_version VARCHAR(50) NOT NULL,
    execution_order INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    status ENUM('RUNNING', 'SUCCESS', 'FAILED', 'ROLLED_BACK') NOT NULL,
    error_message TEXT,
    affected_tables INT DEFAULT 0,
    checksum VARCHAR(64),
    execution_user VARCHAR(100),
    INDEX idx_script_order (script_name, execution_order),
    INDEX idx_execution_time (start_time),
    INDEX idx_status (status)
);
```

## 🔍 质量检查机制

### 1. 脚本质量检查清单

```yaml
# quality-check.yml
syntax_check:
  - check_sql_syntax: true
  - check_table_naming: true
  - check_column_naming: true
  - check_index_usage: true

consistency_check:
  - check_entity_mapping: true
  - check_data_types: true
  - check_foreign_keys: true
  - check_audit_fields: true

performance_check:
  - check_query_performance: true
  - check_index_effectiveness: true
  - check_table_size: true

security_check:
  - check_sql_injection: true
  - check_privilege_escalation: true
  - check_sensitive_data: true
```

### 2. 自动化测试

```sql
-- 自动化测试用例
DELIMITER $$
CREATE PROCEDURE test_database_integrity()
BEGIN
    DECLARE test_count INT DEFAULT 0;

    -- 测试表数量
    SELECT COUNT(*) INTO test_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'ioedream_common_db';

    IF test_count < 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '公共表数量不足';
    END IF;

    -- 测试外键约束
    SELECT COUNT(*) INTO test_count
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'ioedream_common_db'
      AND REFERENCED_TABLE_NAME IS NOT NULL;

    IF test_count < 5 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '外键约束不足';
    END IF;
END$$
DELIMITER ;
```

## 🚨 错误处理机制

### 1. 错误分类和处理

| 错误级别 | 错误类型 | 处理方式 | 是否中断 |
|---------|---------|---------|---------|
| P0 | 语法错误 | 立即停止 | 是 |
| P1 | 权限错误 | 立即停止 | 是 |
| P2 | 数据错误 | 回滚脚本 | 是 |
| P3 | 性能问题 | 记录警告 | 否 |
| P4 | 命名问题 | 记录警告 | 否 |

### 2. 回滚机制

```sql
-- 回滚脚本示例
-- rollback-v1.0.1.sql

SET FOREIGN_KEY_CHECKS = 0;

-- 记录回滚开始
INSERT INTO script_execution_log (
    script_name, script_version, execution_order,
    start_time, status, execution_user
) VALUES (
    'rollback-v1.0.1.sql', '1.0.1', 999,
    NOW(), 'RUNNING', 'system'
);

-- 回滚表结构变更
DROP TABLE IF EXISTS t_consume_record_new;
ALTER TABLE t_consume_record RENAME TO t_consume_record_backup;

-- 回滚数据变更
DELETE FROM t_consume_account WHERE account_id > 1000;

-- 更新回滚状态
UPDATE script_execution_log
SET end_time = NOW(), status = 'SUCCESS'
WHERE script_name = 'rollback-v1.0.1.sql';

SET FOREIGN_KEY_CHECKS = 1;
```

## 📈 监控和报告

### 1. 执行报告生成

```sql
-- 生成执行报告
CREATE PROCEDURE generate_execution_report(IN execution_id BIGINT)
BEGIN
    SELECT
        script_name,
        script_version,
        execution_order,
        TIMESTAMPDIFF(SECOND, start_time, end_time) as duration_seconds,
        status,
        error_message,
        affected_tables
    FROM script_execution_log
    WHERE execution_id = execution_id
    ORDER BY execution_order;
END;
```

### 2. 性能监控

```sql
-- 性能监控视图
CREATE VIEW v_script_performance AS
SELECT
    script_name,
    AVG(TIMESTAMPDIFF(SECOND, start_time, end_time)) as avg_duration,
    MAX(TIMESTAMPDIFF(SECOND, start_time, end_time)) as max_duration,
    MIN(TIMESTAMPDIFF(SECOND, start_time, end_time)) as min_duration,
    COUNT(*) as execution_count,
    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count
FROM script_execution_log
GROUP BY script_name;
```

## 🔄 持续改进机制

### 1. 定期审查

- **每周审查**: 脚本质量和性能
- **每月审查**: 版本管理规范执行情况
- **每季度审查**: 整体架构和设计优化

### 2. 规范更新

- 规范版本更新需要架构委员会审批
- 更新后需要通知所有开发团队
- 提供培训和迁移指南

### 3. 最佳实践收集

- 收集优秀实践案例
- 建立知识库
- 定期分享经验

## 📞 支持和联系

### 规范维护团队

- **架构师**: 老王 (架构委员会主席)
- **DBA团队**: 数据库管理团队
- **DevOps团队**: 运维自动化团队

### 问题反馈渠道

- **GitHub Issues**: 项目Issue
- **企业微信**: 数据库架构群
- **邮件**: dbarchitects@ioe-dream.com

---

**⚠️ 重要提醒**:
1. 本规范为强制执行规范，所有数据库脚本必须严格遵循
2. 任何违反本规范的脚本将被拒绝执行
3. 规范更新需要架构委员会审批
4. 持续违反规范的团队将影响绩效考核

**让我们一起构建高质量、高可靠的数据库管理体系！** 🚀