# 数据库迁移执行指南 - SelfServiceRegistrationEntity拆分

**执行日期**: 2025-12-26
**适用模块**: ioedream-visitor-service
**迁移脚本**: `split_self_service_registration.sql`

---

## ⚠️ 执行前检查清单

### 环境准备

- [ ] 数据库连接正常
- [ ] 有数据库的CREATE TABLE权限
- [ ] 有数据库的INSERT权限
- [ ] 确认数据库备份已创建
- [ ] 确认迁移脚本文件存在

### 数据备份（强制执行）

**⚠️ 警告**: 执行迁移前必须备份数据库！

```bash
# Linux/Mac
mysqldump -u root -p ioedream_visitor > backup_visitor_$(date +%Y%m%d_%H%M%S).sql

# Windows
mysqldump -u root -p ioedream_visitor > backup_visitor_%date%.sql

# 验证备份文件
ls -lh backup_visitor_*.sql
```

---

## 📋 迁移执行步骤

### Step 1: 检查迁移脚本

```bash
# 查看迁移脚本
cat microservices/ioedream-visitor-service/src/main/resources/db/migration/split_self_service_registration.sql

# 检查脚本行数
wc -l split_self_service_registration.sql
```

**预期结果**: 脚本应该包含完整的表创建和数据迁移SQL语句

### Step 2: 执行迁移脚本

#### 方式1: 命令行执行（推荐）

```bash
# 连接到MySQL数据库
mysql -u root -p ioedream_visitor

# 执行迁移脚本
source microservices/ioedream-visitor-service/src/main/resources/db/migration/split_self_service_registration.sql

# 或者直接执行
mysql -u root -p ioedream_visitor < split_self_service_registration.sql
```

#### 方式2: 分步执行（推荐用于生产环境）

```sql
-- 2.1 创建新表
SOURCE microservices/ioedream-visitor-service/src/main/resources/db/migration/split_self_service_registration.sql

-- 2.2 验证表创建
SHOW TABLES LIKE 't_visitor_%';

-- 应该看到以下表:
-- t_visitor_self_service_registration (原表)
-- t_visitor_biometric (新建)
-- t_visitor_approval (新建)
-- t_visitor_visit_record (新建)
-- t_visitor_terminal_info (新建)
-- t_visitor_additional_info (新建)
```

### Step 3: 验证数据迁移

```sql
-- 3.1 验证生物识别信息迁移
SELECT
    '生物识别信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_biometric;

-- 3.2 验证审批流程信息迁移
SELECT
    '审批流程信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_approval;

-- 3.3 验证访问记录信息迁移
SELECT
    '访问记录信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_visit_record;

-- 3.4 验证终端信息迁移
SELECT
    '终端信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_terminal_info;

-- 3.5 验证附加信息迁移
SELECT
    '附加信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_additional_info;
```

**预期结果**: 所有新表都应该有数据迁移记录

### Step 4: 验证数据完整性

```sql
-- 4.1 检查外键关联
SELECT
    r.registration_id,
    b.biometric_id IS NOT NULL AS has_biometric,
    a.approval_id IS NOT NULL AS has_approval,
    vr.record_id IS NOT NULL AS has_record,
    t.terminal_info_id IS NOT NULL AS has_terminal,
    ad.additional_info_id IS NOT NULL AS has_additional
FROM t_visitor_self_service_registration r
LEFT JOIN t_visitor_biometric b ON r.registration_id = b.registration_id
LEFT JOIN t_visitor_approval a ON r.registration_id = a.registration_id
LEFT JOIN t_visitor_visit_record vr ON r.registration_id = vr.registration_id
LEFT JOIN t_visitor_terminal_info t ON r.registration_id = t.registration_id
LEFT JOIN t_visitor_additional_info ad ON r.registration_id = ad.registration_id
WHERE r.deleted_flag = 0
LIMIT 10;

-- 4.2 检查数据一致性
SELECT
    COUNT(*) AS total_registrations,
    COUNT(b.biometric_id) AS has_biometric_count,
    COUNT(a.approval_id) AS has_approval_count,
    COUNT(vr.record_id) AS has_record_count,
    COUNT(t.terminal_info_id) AS has_terminal_count,
    COUNT(ad.additional_info_id) AS has_additional_count
FROM t_visitor_self_service_registration r
LEFT JOIN t_visitor_biometric b ON r.registration_id = b.registration_id AND b.deleted_flag = 0
LEFT JOIN t_visitor_approval a ON r.registration_id = a.registration_id AND a.deleted_flag = 0
LEFT JOIN t_visitor_visit_record vr ON r.registration_id = vr.registration_id AND vr.deleted_flag = 0
LEFT JOIN t_visitor_terminal_info t ON r.registration_id = t.registration_id AND t.deleted_flag = 0
LEFT JOIN t_visitor_additional_info ad ON r.registration_id = ad.registration_id AND ad.deleted_flag = 0
WHERE r.deleted_flag = 0;
```

### Step 5: 性能验证（可选）

```sql
-- 检查索引是否正确创建
SHOW INDEX FROM t_visitor_biometric;
SHOW INDEX FROM t_visitor_approval;
SHOW INDEX FROM t_visitor_visit_record;
SHOW INDEX FROM t_visitor_terminal_info;
SHOW INDEX FROM t_visitor_additional_info;

-- 预期应该看到registration_id的索引
```

---

## ⏭️ 可选: 清理原表字段

### ⚠️ 警告

**执行以下步骤将永久删除原表字段，建议先保留作为备份！**

### 清理前再次确认

- [ ] 新表数据验证通过
- [ ] 外键关联正确
- [ ] Service层更新完成
- [ ] 单元测试通过
- [ ] 集成测试通过

### 清理脚本

```sql
-- 备份原表（建议）
CREATE TABLE t_visitor_self_service_registration_backup AS SELECT * FROM t_visitor_self_service_registration;

-- 删除生物识别相关字段
ALTER TABLE t_visitor_self_service_registration DROP COLUMN face_photo_url;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN face_feature;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN id_card_photo_url;

-- 删除审批流程相关字段
ALTER TABLE t_visitor_self_service_registration DROP COLUMN approver_id;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN approver_name;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN approval_time;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN approval_comment;

-- 删除访问记录相关字段
ALTER TABLE t_visitor_self_service_registration DROP COLUMN check_in_time;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN check_out_time;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN escort_required;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN escort_user;

-- 删除终端信息相关字段
ALTER TABLE t_visitor_self_service_registration DROP COLUMN terminal_id;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN terminal_location;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN visitor_card;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN card_print_status;

-- 删除附加信息相关字段
ALTER TABLE t_visitor_self_service_registration DROP COLUMN belongings;
ALTER TABLE t_visitor_self_service_registration DROP COLUMN license_plate;
```

---

## 🔄 回滚方案

### 如果迁移失败，立即执行回滚

```sql
-- 回滚步骤1: 删除新创建的表
DROP TABLE IF EXISTS t_visitor_additional_info;
DROP TABLE IF EXISTS t_visitor_terminal_info;
DROP TABLE IF EXISTS t_visitor_visit_record;
DROP TABLE IF EXISTS t_visitor_approval;
DROP TABLE IF EXISTS t_visitor_biometric;

-- 回滚步骤2: 恢复备份（如果已清理原表字段）
-- source backup_visitor_YYYYMMDD_HHMMSS.sql;

-- 回滚步骤3: 验证回滚
SELECT COUNT(*) FROM t_visitor_self_service_registration;
SHOW TABLES LIKE 't_visitor_%';
```

---

## ✅ 迁移完成检查清单

### 数据库层面

- [ ] 5个新表创建成功
- [ ] 数据迁移完成（验证查询通过）
- [ ] 外键索引创建成功
- [ ] 数据完整性验证通过

### 应用层面

- [ ] Entity类已创建（6个）
- [ ] DAO接口已创建（5个）
- [ ] Manager层已更新（支持JOIN查询）
- [ ] Service层已更新（向后兼容）
- [ ] 单元测试已更新

### 测试验证

- [ ] 编译通过
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 功能验证通过（创建、查询、更新）

---

## 📞 支持信息

**数据库管理员**: 负责迁移脚本执行和验证
**架构团队**: 负责迁移方案评审和争议处理
**DevOps团队**: 负责备份恢复和环境准备

**迁移问题反馈**: 提交GitHub Issue或联系架构团队

---

**文档版本**: v1.0.0
**生成时间**: 2025-12-26
**维护人**: IOE-DREAM架构团队
**状态**: ✅ 迁移脚本已就绪，待执行
