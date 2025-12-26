# 消费模块数据库迁移执行清单

**执行时间**: 2025-12-23
**数据库**: MySQL 8.0+
**迁移类型**: 结构变更 + 数据迁移

---

## ✅ 执行前检查清单

### 1. 环境检查

- [ ] MySQL版本 ≥ 8.0
- [ ] 数据库 `ioedream` 已创建
- [ ] 数据库用户具有CREATE TABLE、ALTER TABLE、INSERT权限
- [ ] Redis服务正常运行
- [ ] 消费服务已编译（`mvn clean install`）

### 2. 数据备份

⚠️ **生产环境必须执行完整备份！**

```bash
# 备份整个数据库
mysqldump -uroot -p ioedream > ioedream_backup_$(date +%Y%m%d_%H%M%S).sql

# 或备份消费相关表
mysqldump -uroot -p ioedream t_consume_account > backup_account_$(date +%Y%m%d).sql
mysqldump -uroot -p ioedream t_consume_record > backup_record_$(date +%Y%m%d).sql
mysqldump -uroot -p ioedream t_consume_account_transaction > backup_transaction_$(date +%Y%m%d).sql
```

### 3. 迁移脚本验证

- [x] V20251223__create_POSID_tables.sql（11个表，JSON字段，分区）
- [x] V20251223__migrate_to_POSID_tables.sql（数据迁移）
- [x] JSONTypeHandler.java（JSON映射）
- [x] Entity类（11个Posid*Entity）
- [x] DAO类（11个Posid*Dao）

---

## 📋 执行步骤

### 步骤1：验证数据库连接

```bash
# Windows PowerShell
mysql -h127.0.0.1 -uroot -p -e "SELECT VERSION();"

# Linux/Mac
mysql -h127.0.0.1 -uroot -p -e "SELECT VERSION();"
```

期望输出：
```
+-----------+
| VERSION() |
+-----------+
| 8.0.x     |
+-----------+
```

### 步骤2：检查现有表结构

```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 't_consume%';"
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID%';"
```

期望输出（步骤2前）：
```
+---------------------------+
| Tables_in_ioedream        |
+---------------------------+
| t_consume_account         |  ← 旧表
| t_consume_record          |  ← 旧表
| t_consume_account_transaction | ← 旧表
+---------------------------+
```

### 步骤3：启动消费服务（自动执行Flyway迁移）

**方式1：Maven启动（推荐）**

```bash
cd D:\IOE-DREAM\microservices\ioedream-consume-service

# Windows PowerShell
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Linux/Mac
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

**方式2：JAR包启动**

```bash
# 先构建JAR包
mvn clean package -DskipTests

# 启动服务
java -jar target/ioedream-consume-service-1.0.0.jar --spring.profiles.active=docker
```

**关键日志**：
```
✅ 成功标志：
[Flyway] Successfully applied 2 migrations to schema `ioedream`
[Flyway] Schema `ioedream` is up to date

❌ 失败标志：
[Flyway] Migration of schema `ioedream` to version "20251223" failed!
```

### 步骤4：验证表结构创建

```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID%';"
```

期望输出（步骤3后）：
```
+---------------------------+
| Tables_in_ioedream        |
+---------------------------+
| POSID_ACCOUNT             |
| POSID_ACCOUNTKIND         |
| POSID_AREA                |
| POSID_SUBSIDY_TYPE        |
| POSID_SUBSIDY_ACCOUNT     |
| POSID_TRANSACTION         |
| POSID_CAPITAL_FLOW        |
| POSID_CONSUME_RECORD      |
| POSID_REFUND_RECORD       |
| POSID_DEVICE_CONFIG       |
| POSID_AREA_DEVICE         |
+---------------------------+
```

### 步骤5：验证数据迁移

```bash
mysql -h127.0.0.1 -uroot -p ioedream <<EOF
USE ioedream;

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
EOF
```

**验证标准**：旧表和新表的行数应该相等。

### 步骤6：Flyway迁移历史验证

```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT installed_rank, version, script, success FROM flyway_schema_history WHERE script LIKE '%POSID%' ORDER BY installed_rank DESC;"
```

期望输出：
```
+----------------+----------+--------------------------------------------+---------+
| installed_rank | version  | script                                     | success |
+----------------+----------+--------------------------------------------+---------+
| 8              | 20251223 | V20251223__create_POSID_tables.sql         | true    |
| 9              | 20251223 | V20251223__migrate_to_POSID_tables.sql     | true    |
+----------------+----------+--------------------------------------------+---------+
```

### 步骤7：数据抽样验证

```bash
mysql -h127.0.0.1 -uroot -p ioedream <<EOF
USE ioedream;

-- 随机抽取10条账户数据进行对比
SELECT
    old.account_id,
    old.user_id,
    old.balance AS old_balance,
    new.balance AS new_balance,
    CASE
        WHEN old.balance = new.balance THEN '一致'
        ELSE '不一致'
    END AS status
FROM t_consume_account old
INNER JOIN POSID_ACCOUNT new ON old.account_id = new.account_id
LIMIT 10;
EOF
```

---

## 🔄 双写验证阶段（1-2周）

### 步骤8：启动双写验证服务

双写验证服务会在消费服务启动时自动运行。

**验证配置**（application.yml）：
```yaml
consume:
  write:
    mode: dual  # 双写模式（同时写旧表和新表）
  validation:
    enabled: true
    interval: 10  # 每10分钟验证一次
    threshold: 99.9  # 一致性阈值≥99.9%
```

### 步骤9：监控验证结果

```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT * FROM dual_write_validation_log WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) ORDER BY validate_time DESC LIMIT 100;"
```

**成功标准**：
- ✅ 一致性 ≥ 99.9%
- ✅ 差异数 = 0
- ✅ 验证成功率 ≥ 99%

### 步骤10：定期执行验证SQL

```bash
mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\validate-dual-write.sql
```

---

## 🚀 切换到新表

**⚠️ 只有在双写验证通过后才能执行！**

### 步骤11：修改配置为只写新表

```yaml
# application.yml
consume:
  write:
    mode: new  # 从 dual 改为 new
```

### 步骤12：重启消费服务

```bash
# 停止服务（Ctrl+C）
# 重新启动
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### 步骤13：验证切换后功能

```bash
# 执行测试消费
# 检查新表数据是否正确
mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE create_time >= NOW() - INTERVAL 1 HOUR;"
```

---

## 📦 归档旧表

**⚠️ 只有在切换成功并稳定运行1周后才能执行！**

### 步骤14：重命名旧表

```bash
mysql -h127.0.0.1 -uroot -p ioedream <<EOF
USE ioedream;

-- 归档旧表（添加_后缀）
RENAME TABLE t_consume_account TO t_consume_account_backup_20251223;
RENAME TABLE t_consume_record TO t_consume_record_backup_20251223;
RENAME TABLE t_consume_account_transaction TO t_consume_account_transaction_backup_20251223;
EOF
```

### 步骤15：清理Flyway历史（可选）

```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "DELETE FROM flyway_schema_history WHERE script LIKE '%V20251219%';"
```

---

## ❌ 故障排查

### 问题1：Flyway迁移失败

**现象**：服务启动时报错，迁移脚本未执行

**解决方案**：
```bash
# 检查Flyway历史表
mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;"

# 如果有失败的记录，删除该记录
mysql -h127.0.0.1 -uroot -p ioedream -e "DELETE FROM flyway_schema_history WHERE version = '20251223' AND success = 0;"

# 重启服务
```

### 问题2：表已存在错误

**现象**：报错`Table 'POSID_ACCOUNT' already exists`

**解决方案**：
```bash
# 检查表是否存在
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID_ACCOUNT';"

# 如果存在且无数据，删除后重新迁移
mysql -h127.0.0.1 -uroot -p ioedream -e "DROP TABLE IF EXISTS POSID_ACCOUNT;"

# 重启服务重新执行迁移
```

### 问题3：字符集错误

**现象**：插入中文数据时报错

**解决方案**：
```bash
# 修改表字符集
mysql -h127.0.0.1 -uroot -p ioedream -e "ALTER TABLE POSID_ACCOUNT CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 修改数据库字符集
mysql -h127.0.0.1 -uroot -p ioedream -e "ALTER DATABASE ioedream CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;"
```

---

## ✅ 最终验收清单

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

## 📞 技术支持

**技术支持**: IOE-DREAM架构团队
**文档位置**: `D:\IOE-DREAM\microservices\ioedream-consume-service\DATABASE_MIGRATION_GUIDE.md`
**验证脚本**: `D:\IOE-DREAM\scripts\validate-dual-write.sql`

---

**执行时间**: $(date)
**执行人**: IOE-DREAM架构团队
**状态**: ✅ 准备执行
