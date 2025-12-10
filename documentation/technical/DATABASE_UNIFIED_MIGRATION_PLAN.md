# IOE-DREAM 数据库统一迁移管理实施计划

> **版本**: v1.0.0  
> **创建日期**: 2025-12-10  
> **执行状态**: 🚀 进行中  
> **优先级**: P0级（架构统一）

---

## 📋 执行概览

### 核心目标

1. ✅ **统一数据库命名**: 所有脚本使用`ioedream`（修复`ioe_dream`不一致）
2. ✅ **整合迁移脚本**: 将Flyway脚本整合到Docker初始化流程
3. ✅ **统一版本管理**: 建立统一的版本号规范和迁移历史
4. ✅ **统一版本迭代**: 支持增量更新和版本回滚

### 执行阶段

| 阶段 | 任务 | 状态 | 优先级 |
|------|------|------|--------|
| **阶段1** | 统一数据库命名 | 🚀 进行中 | P0 |
| **阶段2** | 整合迁移脚本 | ⏳ 待执行 | P0 |
| **阶段3** | 统一版本管理 | ⏳ 待执行 | P1 |
| **阶段4** | 验证所有服务 | ⏳ 待执行 | P1 |

---

## 🔧 阶段1: 统一数据库命名（P0级）

### 任务1.1: 修复Flyway迁移脚本中的数据库名

**需要修复的文件**:
1. `microservices/ioedream-db-init/src/main/resources/db/migration/V1_0_0__INITIAL_SCHEMA.sql`
   - 修复: `CREATE DATABASE IF NOT EXISTS ioe_dream` → `ioedream`
   - 修复: `USE ioe_dream;` → `USE ioedream;`

2. `microservices/ioedream-db-init/src/main/resources/db/migration/V1_1_0__INITIAL_DATA.sql`
   - 修复: `USE ioe_dream;` → `USE ioedream;`

3. `microservices/ioedream-db-init/src/main/resources/db/migration/V2_0_0__ENHANCE_CONSUME_RECORD_TABLE.sql`
   - 添加: `USE ioedream;`（如果缺失）

4. `microservices/ioedream-db-init/src/main/resources/db/migration/V2_0_1__ENHANCE_ACCOUNT_TABLE.sql`
   - 添加: `USE ioedream;`（如果缺失）

5. `microservices/ioedream-db-init/src/main/resources/db/migration/V2_0_2__CREATE_REFUND_TABLE.sql`
   - 添加: `USE ioedream;`（如果缺失）

6. `microservices/ioedream-db-init/src/main/resources/db/migration/V2_1_0__API_COMPATIBILITY_VALIDATION.sql`
   - 添加: `USE ioedream;`（如果缺失）

7. `microservices/ioedream-db-init/src/main/resources/db/ALL_IN_ONE_INIT.sql`
   - 修复: `CREATE DATABASE IF NOT EXISTS ioe_dream` → `ioedream`
   - 修复: `USE ioe_dream;` → `USE ioedream;`

**修复方法**: 使用全局搜索替换 `ioe_dream` → `ioedream`

---

## 🔧 阶段2: 整合迁移脚本（P0级）

### 任务2.1: 将Flyway脚本整合到Docker初始化目录

**整合策略**:
1. 将V2.0.x脚本复制到`deployment/mysql/init/`目录
2. 重命名脚本以符合统一命名规范
3. 更新脚本中的版本号格式
4. 确保脚本中的数据库名统一

**文件映射关系**:

| Flyway脚本 | 整合后文件名 | 版本号 | 执行顺序 |
|-----------|------------|--------|---------|
| `V1_0_0__INITIAL_SCHEMA.sql` | `01-ioedream-schema.sql` | V1.0.0 | ✅ 已存在 |
| `V1_1_0__INITIAL_DATA.sql` | `02-ioedream-data.sql` | V1.1.0 | ✅ 已存在 |
| `V2_0_0__ENHANCE_CONSUME_RECORD_TABLE.sql` | `04-v2.0.0__enhance-consume-record.sql` | V2.0.0 | 04 |
| `V2_0_1__ENHANCE_ACCOUNT_TABLE.sql` | `05-v2.0.1__enhance-account.sql` | V2.0.1 | 05 |
| `V2_0_2__CREATE_REFUND_TABLE.sql` | `06-v2.0.2__create-refund-tables.sql` | V2.0.2 | 06 |
| `V2_1_0__API_COMPATIBILITY_VALIDATION.sql` | `07-v2.1.0__api-compatibility.sql` | V2.1.0 | 07 |

**整合步骤**:
1. 复制V2.0.x脚本到`deployment/mysql/init/`目录
2. 重命名脚本文件
3. 更新脚本头部注释（版本号格式）
4. 确保脚本包含`USE ioedream;`语句
5. 更新Docker Compose执行顺序

### 任务2.2: 更新Docker Compose执行顺序

**更新`docker-compose-all.yml`中的脚本执行顺序**:
```yaml
# 执行顺序
1. 00-version-check.sql
2. 01-ioedream-schema.sql (V1.0.0)
3. 02-ioedream-data.sql (V1.1.0)
4. 02-ioedream-data-{env}.sql (环境特定数据)
5. 03-optimize-indexes.sql (V1.0.1)
6. 04-v2.0.0__enhance-consume-record.sql (V2.0.0)  # 新增
7. 05-v2.0.1__enhance-account.sql (V2.0.1)         # 新增
8. 06-v2.0.2__create-refund-tables.sql (V2.0.2)    # 新增
9. 07-v2.1.0__api-compatibility.sql (V2.1.0)      # 新增
10. nacos-schema.sql
```

---

## 🔧 阶段3: 统一版本管理（P1级）

### 任务3.1: 统一版本号格式

**统一格式**: `V{major}.{minor}.{patch}`

**版本号映射**:
- `V1_0_0` → `V1.0.0`
- `V1_1_0` → `V1.1.0`
- `V2_0_0` → `V2.0.0`
- `V2_0_1` → `V2.0.1`
- `V2_0_2` → `V2.0.2`
- `V2_1_0` → `V2.1.0`

### 任务3.2: 统一迁移历史记录

**确保所有脚本都记录到`t_migration_history`表**:
```sql
INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V2.0.0',
    '消费记录表增强 - 基于Smart-Admin优秀设计',
    '04-v2.0.0__enhance-consume-record.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name),
    status = 'SUCCESS',
    end_time = NOW();
```

---

## 🔧 阶段4: 验证所有服务（P1级）

### 任务4.1: 验证数据库配置

**验证所有微服务的数据库配置**:
- ✅ `ioedream-common-service`: `ioedream`
- ✅ `ioedream-access-service`: `ioedream`
- ✅ `ioedream-attendance-service`: `ioedream`
- ✅ `ioedream-consume-service`: `ioedream`
- ✅ `ioedream-visitor-service`: `ioedream`
- ✅ `ioedream-video-service`: `ioedream`
- ✅ `ioedream-oa-service`: `ioedream`
- ✅ `ioedream-device-comm-service`: `ioedream`

### 任务4.2: 验证初始化流程

**验证步骤**:
1. 清理现有数据库
2. 执行Docker初始化
3. 验证所有表创建成功
4. 验证版本历史记录
5. 验证数据完整性

---

## 📊 执行进度跟踪

### 阶段1: 统一数据库命名

- [x] 分析现状
- [ ] 修复V1_0_0__INITIAL_SCHEMA.sql
- [ ] 修复V1_1_0__INITIAL_DATA.sql
- [ ] 修复V2_0_0__ENHANCE_CONSUME_RECORD_TABLE.sql
- [ ] 修复V2_0_1__ENHANCE_ACCOUNT_TABLE.sql
- [ ] 修复V2_0_2__CREATE_REFUND_TABLE.sql
- [ ] 修复V2_1_0__API_COMPATIBILITY_VALIDATION.sql
- [ ] 修复ALL_IN_ONE_INIT.sql

### 阶段2: 整合迁移脚本

- [ ] 复制V2.0.x脚本到deployment/mysql/init/
- [ ] 重命名脚本文件
- [ ] 更新脚本头部注释
- [ ] 更新Docker Compose执行顺序
- [ ] 验证脚本执行顺序

### 阶段3: 统一版本管理

- [ ] 统一版本号格式
- [ ] 统一迁移历史记录
- [ ] 创建版本管理工具
- [ ] 验证版本管理机制

### 阶段4: 验证所有服务

- [ ] 验证数据库配置
- [ ] 验证初始化流程
- [ ] 验证版本历史
- [ ] 验证数据完整性

---

## 🎯 成功标准

### 功能标准

- ✅ 所有SQL脚本使用统一的`ioedream`数据库名
- ✅ 所有迁移脚本按版本顺序正确执行
- ✅ 版本管理机制正确记录执行历史
- ✅ 环境隔离功能正常工作
- ✅ 版本回滚功能可用

### 兼容性标准

- ✅ 现有数据不受影响
- ✅ 所有微服务正常连接数据库
- ✅ Docker初始化流程正常
- ✅ 版本检查机制正确

### 性能标准

- ✅ 初始化时间在可接受范围内
- ✅ 索引创建性能优化生效
- ✅ 批量插入性能优化生效

---

## 📚 相关文档

- [数据库统一迁移分析报告](./DATABASE_UNIFIED_MIGRATION_ANALYSIS.md)
- [数据库初始化指南](../deployment/docker/DATABASE_INIT_GUIDE.md)
- [P2级优化指南](./DATABASE_P2_OPTIMIZATION_GUIDE.md)

---

**👥 执行团队**: IOE-DREAM 架构委员会  
**📅 创建日期**: 2025-12-10  
**🔧 计划版本**: v1.0.0  
**✅ 执行状态**: 🚀 进行中

