# 消费模块数据库迁移执行指南

**项目**: IOE-DREAM消费模块
**执行时间**: 2025-12-23
**数据库**: MySQL 8.0+

---

## 📋 迁移步骤

### 步骤1：数据库环境检查

**1.1 检查MySQL版本**

```bash
mysql --version
# 期望输出: mysql Ver 8.0.x
```

**1.2 检查数据库连接**

```bash
# 使用Docker环境
docker exec -it ieo-dream-mysql mysql -uroot -p

# 或直接连接
mysql -h localhost -uroot -p
```

**1.3 检查现有数据库**

```sql
SHOW DATABASES;
USE ioedream;
SHOW TABLES LIKE 't_consume%';
SHOW TABLES LIKE 'POSID%';
```

---

### 步骤2：备份现有数据

**⚠️ 重要：在生产环境执行前必须备份！**

**2.1 备份消费相关表**

```sql
-- 备份账户表
mysqldump -uroot -p ioedream t_consume_account > backup_consume_account_$(date +%Y%m%d).sql

-- 备份账户交易表
mysqldump -uroot -p ioedream t_consume_account_transaction > backup_consume_transaction_$(date +%Y%m%d).sql

-- 备份消费记录表
mysqldump -uroot -p ioedream t_consume_record > backup_consume_record_$(date +%Y%m%d).sql

-- 备份所有表
mysqldump -uroot -p ioedream > ioedream_full_backup_$(date +%Y%m%d).sql
```

---

### 步骤3：执行Flyway迁移脚本

**3.1 启动消费服务**

Flyway会在服务启动时自动执行迁移脚本。

```bash
# 方式1: 使用Maven启动（自动执行迁移）
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# 方式2: 使用已编译的JAR包启动
java -jar ioedream-consume-service/target/ioedream-consume-service-1.0.0.jar --spring.profiles.active=docker
```

**3.2 查看Flyway迁移历史**

```sql
-- 进入MySQL
USE ioedream;

-- 查看Flyway迁移历史表
SELECT * FROM flyway_schema_history
WHERE script LIKE '%POSID%'
ORDER BY installed_rank DESC;
```

期望输出：
```
+------------------+---------------+------------------+----------------+
| installed_rank   | version       | script           | success        |
+------------------+---------------+------------------+----------------+
| ...              | ...           | ...              | ...            |
| 8                | 20251223      | V20251223__create_POSID_tables.sql | true     |
| 9                | 20251223      | V20251223__migrate_to_POSID_tables.sql | true  |
+------------------+---------------+------------------+----------------+
```

---

### 步骤4：验证表结构

**4.1 检查POSID表是否创建成功**

```sql
USE ioedream;

-- 检查所有POSID表
SHOW TABLES LIKE 'POSID%';
```

期望输出（11个表）：
```
POSID_ACCOUNT
POSID_ACCOUNTKIND
POSID_AREA
POSID_SUBSIDY_TYPE
POSID_SUBSIDY_ACCOUNT
POSID_TRANSACTION
POSID_CAPITAL_FLOW
POSID_CONSUME_RECORD
POSID_REFUND_RECORD
POSID_DEVICE_CONFIG
POSID_AREA_DEVICE
```

**4.2 验证表结构**

```sql
-- 验证主账户表
DESC POSID_ACCOUNT;

-- 验证区域表（包含JSON字段）
DESC POSID_AREA;

-- 验证交易表（包含分区）
SHOW CREATE TABLE POSID_TRANSACTION;
```

---

### 步骤5：验证数据迁移

**5.1 检查数据行数**

```sql
-- 旧表数据量
SELECT 't_consume_account' AS table_name, COUNT(*) AS row_count FROM t_consume_account
UNION ALL
SELECT 't_consume_record' AS table_name, COUNT(*) AS row_count FROM t_consume_record
UNION ALL
SELECT 't_consume_account_transaction' AS table_name, COUNT(*) AS row_count FROM t_consume_account_transaction;

-- 新表数据量
SELECT 'POSID_ACCOUNT' AS table_name, COUNT(*) AS row_count FROM POSID_ACCOUNT
UNION ALL
SELECT 'POSID_CONSUME_RECORD' AS table_name, COUNT(*) AS row_count FROM POSID_CONSUME_RECORD
UNION ALL
SELECT 'POSID_TRANSACTION' AS table_name, COUNT(*) AS row_count FROM POSID_TRANSACTION;
```

**期望结果**：旧表和新表的行数应该相等。

**5.2 抽样验证数据一致性**

```sql
-- 随机抽取10条账户数据进行对比
SELECT
    old.account_id,
    old.user_id,
    old.balance,
    new.account_id,
    new.user_id,
    new.balance,
    CASE
        WHEN old.balance = new.balance THEN '一致'
        ELSE '不一致'
    END AS status
FROM t_consume_account old
INNER JOIN POSID_ACCOUNT new ON old.account_id = new.account_id
LIMIT 10;
```

---

### 步骤6：双写验证（1-2周）

**6.1 启动双写验证服务**

双写验证服务会在`DualWriteValidationManager`中自动运行。

**6.2 查看验证报告**

```sql
-- 查询验证结果
SELECT * FROM dual_write_validation_log
WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY validate_time DESC
LIMIT 100;
```

**6.3 监控一致性指标**

期望指标：
- ✅ 一致性 ≥ 99.9%
- ✅ 差异数 = 0
- ✅ 验证成功率 ≥ 99%

---

### 步骤7：切换到新表

**⚠️ 重要：只有在双写验证通过后才能执行此步骤！**

**7.1 停止双写，只写新表**

修改配置：
```yaml
# application.yml
consume:
  write:
    mode: new  # 从 dual 改为 new
```

**7.2 重启消费服务**

```bash
# 停止服务
# 启动服务（只写新表）
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

**7.3 验证切换后功能**

```sql
-- 执行测试消费
-- 检查新表数据是否正确
SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE create_time >= NOW() - INTERVAL 1 HOUR;
```

---

### 步骤8：归档旧表

**⚠️ 重要：只有在切换成功并稳定运行1周后才能执行！**

**8.1 重命名旧表**

```sql
-- 归档旧表（添加_后缀）
RENAME TABLE t_consume_account TO t_consume_account_backup_20251223;
RENAME TABLE t_consume_record TO t_consume_record_backup_20251223;
RENAME TABLE t_consume_account_transaction TO t_consume_account_transaction_backup_20251223;
```

**8.2 清理Flyway历史（可选）**

```sql
-- 删除旧版本的迁移记录
DELETE FROM flyway_schema_history
WHERE script LIKE '%V20251219%';
```

---

## 🔍 故障排查

### 问题1：Flyway迁移失败

**现象**：服务启动时报错，迁移脚本未执行

**解决方案**：
```sql
-- 检查Flyway历史表
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- 如果有失败的记录，删除该记录
DELETE FROM flyway_schema_history
WHERE version = '20251223' AND success = 0;

-- 重启服务
```

### 问题2：表已存在错误

**现象**：报错`Table 'POSID_ACCOUNT' already exists`

**解决方案**：
```sql
-- 检查表是否存在
SHOW TABLES LIKE 'POSID_ACCOUNT';

-- 如果存在且无数据，删除后重新迁移
DROP TABLE IF EXISTS POSID_ACCOUNT;

-- 重启服务重新执行迁移
```

### 问题3：字符集错误

**现象**：插入中文数据时报错

**解决方案**：
```sql
-- 修改表字符集
ALTER TABLE POSID_ACCOUNT CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改数据库字符集
ALTER DATABASE ioedream CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```

---

## ✅ 验收清单

迁移完成后，请验证以下项目：

- [ ] Flyway迁移脚本全部执行成功
- [ ] 11个POSID表创建成功
- [ ] 数据迁移行数一致（旧表=新表）
- [ ] 抽样验证数据一致性≥99.9%
- [ ] 双写验证运行1-2周无异常
- [ ] 切换到新表后功能正常
- [ ] 性能测试TPS≥1000
- [ ] 性能测试响应时间≤50ms
- [ ] 监控告警正常工作
- [ ] 旧表已归档（保留30天）

---

## 📞 联系方式

**技术支持**: IOE-DREAM架构团队
**文档位置**: `D:\IOE-DREAM\openspec\changes\consume-module-complete-implementation\`

---

**执行时间**: $(date)
**执行人**: IOE-DREAM架构团队
**状态**: ✅ 准备执行
