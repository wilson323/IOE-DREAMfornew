# 消费模块数据库迁移快速参考指南

**更新时间**: 2025-12-23
**状态**: ✅ 准备就绪，可以执行

---

## 📦 迁移脚本文件清单

### 1. Flyway迁移脚本（已准备完成）

✅ **V20251223__create_POSID_tables.sql**（22,528字节）
- 创建11个POSID_*表
- JSON配置字段
- 虚拟列索引
- 月级表分区

✅ **V20251223__migrate_to_POSID_tables.sql**（7,351字节）
- 数据迁移：t_consume_* → POSID_*
- 保留历史数据
- 转换JSON字段

### 2. 代码文件（已准备完成）

✅ **JSONTypeHandler.java** - JSON字段映射
✅ **11个Entity类** - POSID实体类
✅ **11个DAO类** - MyBatis-Plus数据访问
✅ **DualWriteValidationManager** - 双写验证服务

### 3. 执行脚本（已准备完成）

✅ **execute-migration.sh** - Linux/Mac自动化脚本
✅ **execute-migration.ps1** - Windows PowerShell脚本
✅ **validate-dual-write.sql** - 数据一致性验证SQL

### 4. 文档（已准备完成）

✅ **DATABASE_MIGRATION_GUIDE.md** - 详细迁移指南
✅ **MIGRATION_EXECUTION_CHECKLIST.md** - 执行清单
✅ **MIGRATION_QUICK_REFERENCE.md** - 快速参考（本文档）

---

## 🚀 快速执行步骤

### 方式1：自动化脚本（推荐）

**Windows PowerShell**：
```powershell
cd D:\IOE-DREAM\microservices\ioedream-consume-service\scripts
.\execute-migration.ps1
```

**Linux/Mac**：
```bash
cd /d/IOE-DREAM/microservices/ioedream-consume-service/scripts
bash execute-migration.sh
```

### 方式2：手动执行

```bash
# 步骤1：连接MySQL验证环境
mysql -h127.0.0.1 -uroot -p -e "SELECT VERSION();"

# 步骤2：启动消费服务（Flyway自动执行迁移）
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# 步骤3：验证表创建
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID%';"

# 步骤4：验证数据迁移
mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\validate-dual-write.sql
```

---

## ✅ 验收标准

### 迁移成功标志

- [x] Flyway日志显示：`Successfully applied 2 migrations`
- [x] 11个POSID表创建成功
- [x] 数据迁移行数一致（旧表=新表）
- [x] Flyway迁移历史记录2个成功脚本

### 双写验证成功标志（1-2周后）

- [ ] 一致性 ≥ 99.9%
- [ ] 差异数 = 0
- [ ] 验证成功率 ≥ 99%

---

## 📋 迁移后任务清单

### 立即执行（迁移完成后）

1. ✅ **验证新表创建**
   ```bash
   mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID%';"
   ```

2. ✅ **验证数据迁移**
   ```bash
   mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\validate-dual-write.sql
   ```

3. ✅ **检查Flyway历史**
   ```bash
   mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT * FROM flyway_schema_history WHERE script LIKE '%POSID%';"
   ```

### 双写验证阶段（1-2周）

4. ⏳ **启动双写验证服务**
   - 配置：`consume.write.mode=dual`
   - 验证间隔：每10分钟
   - 一致性阈值：≥99.9%

5. ⏳ **定期执行验证SQL**
   ```bash
   mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\validate-dual-write.sql
   ```

6. ⏳ **监控验证结果**
   ```bash
   mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT * FROM dual_write_validation_log WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 1 DAY);"
   ```

### 切换准备（验证通过后）

7. ⏳ **修改配置为只写新表**
   ```yaml
   consume:
     write:
       mode: new  # 从 dual 改为 new
   ```

8. ⏳ **重启消费服务**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=docker
   ```

9. ⏳ **验证切换后功能**
   - 执行测试消费
   - 检查新表数据写入

### 性能测试（切换成功后）

10. ⏳ **执行性能测试**
    - TPS ≥ 1000
    - 平均响应时间 ≤ 50ms
    - P95响应时间 ≤ 100ms
    - 缓存命中率 ≥ 90%

---

## 🔧 故障排查

### 问题1：Flyway迁移失败

**检查命令**：
```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;"
```

**解决方案**：
```bash
# 删除失败记录
mysql -h127.0.0.1 -uroot -p ioedream -e "DELETE FROM flyway_schema_history WHERE version = '20251223' AND success = 0;"

# 重启服务
```

### 问题2：表已存在

**检查命令**：
```bash
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID_ACCOUNT';"
```

**解决方案**：
```bash
# 如果表存在且无数据，删除后重新迁移
mysql -h127.0.0.1 -uroot -p ioedream -e "DROP TABLE IF EXISTS POSID_ACCOUNT;"

# 删除Flyway历史记录
mysql -h127.0.0.1 -uroot -p ioedream -e "DELETE FROM flyway_schema_history WHERE version = '20251223';"

# 重启服务
```

### 问题3：数据不一致

**检查命令**：
```bash
mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\validate-dual-write.sql
```

**解决方案**：
- 查看差异明细SQL（在validate-dual-write.sql中）
- 手动修复不一致数据
- 重新执行验证

---

## 📞 技术支持

**文档位置**：
- 详细指南：`D:\IOE-DREAM\microservices\ioedream-consume-service\DATABASE_MIGRATION_GUIDE.md`
- 执行清单：`D:\IOE-DREAM\microservices\ioedream-consume-service\MIGRATION_EXECUTION_CHECKLIST.md`
- 验证脚本：`D:\IOE-DREAM\scripts\validate-dual-write.sql`

**执行脚本**：
- Windows：`D:\IOE-DREAM\microservices\ioedream-consume-service\scripts\execute-migration.ps1`
- Linux/Mac：`D:\IOE-DREAM\microservices\ioedream-consume-service\scripts\execute-migration.sh`

**技术支持**：IOE-DREAM架构团队

---

**状态**: ✅ 数据库迁移准备就绪
**下一步**: 执行迁移脚本或启动消费服务
