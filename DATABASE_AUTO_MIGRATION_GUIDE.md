# IOE-DREAM 数据库自动迁移配置指南

**重要声明**: ⚠️ **MyBatis-Plus不支持自动建表！**

---

## 🎯 核心事实

### MyBatis-Plus vs JPA

```
┌─────────────────┬──────────────┬──────────────────┐
│     特性        │     JPA      │  MyBatis-Plus    │
├─────────────────┼──────────────┼──────────────────┤
│ 自动建表         │  ✅ 支持     │  ❌ 不支持        │
│ ddl-auto配置    │  ✅ 支持     │  ❌ 不存在        │
│ 表结构自动更新   │  ✅ 支持     │  ❌ 不支持        │
│ SQL性能控制     │  ⚠️ 较差     │  ✅ 优秀          │
│ 适用场景        │  快速开发     │  生产环境         │
└─────────────────┴──────────────┴──────────────────┘
```

**结论**: IOE-DREAM选择MyBatis-Plus是正确的架构决策，符合企业级生产环境要求。

---

## ✅ 正确的数据库管理方案

### 方案1: Docker自动初始化（已配置）

**优点**:
- ✅ 简单直接，立即可用
- ✅ 适合开发环境快速启动
- ✅ 无需额外依赖

**使用方法**:

```bash
# 使用快速启动脚本
quick-start.bat

# 或手动执行
docker-compose -f docker-compose-all.yml up -d
```

**工作原理**:
1. MySQL容器启动时，自动执行 `/docker-entrypoint-initdb.d/` 目录下的SQL脚本
2. `db-init` 服务确保Nacos数据库正确初始化
3. 所有业务表在首次启动时自动创建

**当前配置**:
- ✅ Nacos表：`deployment/mysql/init/nacos-schema.sql`
- ✅ 业务表：`database-scripts/common-service/*.sql`

---

### 方案2: Flyway数据库迁移（企业级推荐）

**优点**:
- ✅ 版本化管理，可追溯
- ✅ 支持回滚
- ✅ 自动执行
- ✅ 多环境支持
- ✅ 符合企业级规范

#### 步骤1: 添加Flyway依赖

```xml
<!-- microservices-common/pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

✅ **已完成** - 依赖已添加到 `microservices-common/pom.xml`

#### 步骤2: 配置Flyway

```yaml
# application.yml
spring:
  flyway:
    enabled: true                    # 启用Flyway
    baseline-on-migrate: true        # 首次迁移时建立基线
    baseline-version: 0              # 基线版本号
    locations: classpath:db/migration # SQL脚本位置
    table: flyway_schema_history     # 迁移历史表名
    validate-on-migrate: true        # 验证已执行的迁移
    clean-disabled: true             # 禁用clean命令（生产环境安全）
```

✅ **已完成** - 配置已添加到 `ioedream-common-service/application.yml`

#### 步骤3: 组织迁移脚本

**目录结构**:
```
microservices/ioedream-common-service/src/main/resources/db/migration/
├── V1.0.0__init_user_tables.sql          # 用户表
├── V1.0.1__init_role_permission.sql      # 角色权限表
├── V1.0.2__init_notification.sql         # 通知表
├── V1.0.3__init_audit.sql                # 审计表
├── V1.0.4__init_schedule.sql             # 调度表
└── V1.0.5__init_system_config.sql        # 系统配置表
```

**命名规范**:
- `V{版本号}__{描述}.sql` - 版本迁移（执行一次）
- `R__{描述}.sql` - 可重复执行脚本

**示例**:
```sql
-- V1.0.0__init_user_tables.sql
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 步骤4: 创建迁移脚本

**PowerShell脚本**:
```powershell
# 创建目录
New-Item -Path "microservices/ioedream-common-service/src/main/resources/db/migration" -ItemType Directory -Force

# 复制SQL脚本并重命名
$scripts = @(
    @{Source="database-scripts/common-service/02-t_user.sql"; Version="V1.0.0__init_user_tables.sql"},
    @{Source="database-scripts/common-service/03-t_role.sql"; Version="V1.0.1__init_role_tables.sql"},
    @{Source="database-scripts/common-service/04-t_permission.sql"; Version="V1.0.2__init_permission_tables.sql"}
)

foreach ($script in $scripts) {
    Copy-Item -Path $script.Source -Destination "microservices/ioedream-common-service/src/main/resources/db/migration/$($script.Version)"
}
```

#### 步骤5: 验证Flyway配置

**启动服务后检查**:
```sql
-- 查看迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 输出示例
+----------------+-------------+-----------------------+----------+---------+
| installed_rank | version     | description           | success  | ...     |
+----------------+-------------+-----------------------+----------+---------+
| 1              | 1.0.0       | init user tables      | 1        | ...     |
| 2              | 1.0.1       | init role tables      | 1        | ...     |
| 3              | 1.0.2       | init permission tables| 1        | ...     |
+----------------+-------------+-----------------------+----------+---------+
```

---

## 📊 两种方案对比

| 特性 | Docker初始化 | Flyway迁移 |
|------|-------------|-----------|
| **复杂度** | ⭐ 简单 | ⭐⭐⭐ 中等 |
| **版本管理** | ❌ 无 | ✅ 完整 |
| **可追溯性** | ❌ 无 | ✅ 完整历史 |
| **回滚支持** | ❌ 无 | ✅ 支持 |
| **团队协作** | ⚠️ 可能冲突 | ✅ 版本化 |
| **生产环境** | ⚠️ 不推荐 | ✅ 推荐 |
| **开发环境** | ✅ 快速 | ✅ 自动化 |

**建议**:
- **开发环境**: 使用Docker初始化（快速启动）
- **生产环境**: 使用Flyway迁移（规范管理）

---

## 🚀 快速开始

### 当前环境立即可用

```bash
# 1. 运行快速启动脚本
quick-start.bat

# 2. 等待服务启动（约2分钟）

# 3. 访问Nacos控制台
http://localhost:8848/nacos
用户名: nacos
密码: nacos

# 4. 验证数据库
docker exec -it ioedream-mysql mysql -uroot -proot -e "USE ioedream; SHOW TABLES;"
```

### 未来集成Flyway

```bash
# 1. 创建迁移脚本
# 按照上述步骤3组织SQL脚本

# 2. 编译项目
mvn clean install

# 3. 启动服务
# Flyway会自动执行迁移脚本

# 4. 验证迁移历史
docker exec -it ioedream-mysql mysql -uroot -proot -e "USE ioedream; SELECT * FROM flyway_schema_history;"
```

---

## ❓ 常见问题

### Q1: 为什么MyBatis-Plus不支持自动建表？

**A**: MyBatis-Plus定位是**SQL映射框架**，不是**ORM框架**。它的设计哲学是：
- ✅ 开发者完全控制SQL
- ✅ 性能优化优先
- ✅ 适合复杂业务场景

自动建表功能会：
- ❌ 限制SQL灵活性
- ❌ 影响性能
- ❌ 不适合生产环境

### Q2: 那JPA为什么支持自动建表？

**A**: JPA是**ORM框架**，设计目标不同：
- ✅ 快速开发
- ✅ 对象关系映射
- ⚠️ 但牺牲了SQL控制能力
- ⚠️ 生产环境不推荐使用ddl-auto

### Q3: 如何在开发时快速添加新表？

**方法1: Docker初始化（开发环境）**
```bash
# 1. 在 database-scripts/common-service/ 添加新SQL
# 2. 删除MySQL数据卷
docker volume rm ioedream_mysql_data
# 3. 重新启动
docker-compose -f docker-compose-all.yml up -d
```

**方法2: Flyway迁移（推荐）**
```bash
# 1. 创建新迁移脚本
# db/migration/V1.0.6__add_new_table.sql
CREATE TABLE t_new_table (...);

# 2. 重启服务，Flyway自动执行
```

### Q4: 生产环境如何管理数据库变更？

**企业级最佳实践**:
```
开发环境 → 测试环境 → 预发布环境 → 生产环境
   ↓          ↓            ↓             ↓
 Flyway     Flyway       Flyway        Flyway
 自动执行    自动执行      审批后执行     审批后执行
```

**审批流程**:
1. 开发人员创建迁移脚本
2. DBA审核SQL安全性
3. 测试环境验证
4. 生产环境执行
5. 监控执行结果

---

## 📚 延伸阅读

- [MyBatis-Plus官方文档](https://baomidou.com/)
- [Flyway官方文档](https://flywaydb.org/)
- [数据库迁移最佳实践](https://flywaydb.org/documentation/concepts/migrations)
- [CLAUDE.md - 项目架构规范](./CLAUDE.md)

---

## ✨ 总结

### 核心要点

1. ❌ **MyBatis-Plus不支持自动建表** - 这是设计特性，不是缺陷
2. ✅ **Docker初始化已可用** - 开发环境立即可用
3. ✅ **Flyway已集成** - 企业级数据库版本管理
4. ✅ **符合架构规范** - 遵循CLAUDE.md企业级标准

### 下一步

- [ ] 运行 `quick-start.bat` 验证数据库自动初始化
- [ ] 创建Flyway迁移脚本目录
- [ ] 组织现有SQL脚本到Flyway
- [ ] 测试Flyway自动迁移功能

---

**🎯 记住**: 不要尝试让MyBatis-Plus自动建表，那是JPA的事情！使用Flyway才是企业级的正确姿势！
