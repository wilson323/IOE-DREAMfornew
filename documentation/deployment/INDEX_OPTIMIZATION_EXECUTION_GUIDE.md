# 索引优化SQL执行指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待执行

---

## 📋 执行前准备

### 1. 环境检查
- [ ] MySQL数据库已启动
- [ ] 数据库连接信息已确认
- [ ] 有执行SQL的权限
- [ ] 数据库已备份（重要！）

### 2. 执行时间
- **建议时间**: 非高峰期（如凌晨2-4点）
- **预计耗时**: 5-10分钟（取决于数据量）

---

## 🔧 执行方式

### 方式1：使用Shell脚本（推荐）

```bash
# 设置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=your_password
export DB_NAME=ioedream

# 执行脚本
cd scripts/database
chmod +x execute_index_optimization.sh
./execute_index_optimization.sh
```

### 方式2：手动执行SQL文件

```bash
# 连接到MySQL
mysql -h localhost -P 3306 -u root -p ioedream

# 执行各模块的SQL文件
source microservices/ioedream-access-service/src/main/resources/sql/access_index_optimization.sql;
source microservices/ioedream-attendance-service/src/main/resources/sql/attendance_index_optimization.sql;
source microservices/ioedream-visitor-service/src/main/resources/sql/visitor_index_optimization.sql;
source microservices/ioedream-video-service/src/main/resources/sql/video_index_optimization.sql;
source microservices/ioedream-consume-service/src/main/resources/sql/consume_index_optimization.sql;
```

### 方式3：使用MySQL客户端工具

1. 使用Navicat、DBeaver等工具
2. 依次打开并执行各模块的SQL文件
3. 确认执行成功

---

## 📊 执行后验证

### 1. 检查索引是否创建成功

```sql
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'ioedream'
    AND INDEX_NAME LIKE 'idx_%'
GROUP BY 
    TABLE_NAME, INDEX_NAME
ORDER BY 
    TABLE_NAME, INDEX_NAME;
```

### 2. 验证索引使用情况

```sql
-- 使用EXPLAIN分析查询计划
EXPLAIN SELECT * FROM t_access_record 
WHERE user_id = 1001 AND access_time > '2025-01-01';
```

**预期结果**: `key` 列应显示使用的索引名称

---

## ⚠️ 注意事项

1. **备份数据库**: 执行前必须备份数据库
2. **非高峰期执行**: 索引创建会锁表，建议在非高峰期执行
3. **监控执行时间**: 如果执行时间过长，考虑分批执行
4. **检查磁盘空间**: 索引会占用额外磁盘空间

---

## 🔍 故障排查

### 问题1: 索引已存在
**错误**: `Duplicate key name 'idx_xxx'`

**解决**: 
```sql
-- 删除已存在的索引
DROP INDEX idx_xxx ON table_name;
-- 重新执行SQL
```

### 问题2: 表不存在
**错误**: `Table 'xxx' doesn't exist`

**解决**: 确认表名是否正确，或先创建表结构

### 问题3: 权限不足
**错误**: `Access denied`

**解决**: 使用有CREATE INDEX权限的用户执行

---

## 📈 性能提升验证

执行索引优化后，可以通过以下方式验证性能提升：

1. **慢查询日志**: 检查慢查询是否减少
2. **查询响应时间**: 对比优化前后的响应时间
3. **数据库监控**: 查看CPU、IO使用率是否降低

---

**执行完成后，请更新执行状态**

