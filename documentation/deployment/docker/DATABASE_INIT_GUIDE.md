# IOE-DREAM 数据库初始化指南

> **版本**: v1.0.0  
> **更新日期**: 2025-01-31  
> **适用范围**: Docker环境数据库初始化

---

## 📋 概述

IOE-DREAM 使用 MySQL 8.0 作为持久化存储，包含两个核心数据库：

| 数据库 | 用途 | 初始化脚本 |
|--------|------|-----------|
| `ioedream` | 主业务数据库 | `01-ioedream-schema.sql` |
| `nacos` | 服务注册配置中心 | `nacos-schema.sql` |

---

## 🗂️ 文件结构

```
deployment/mysql/init/
├── 01-ioedream-schema.sql    # IOE-DREAM主业务库初始化
└── nacos-schema.sql          # Nacos配置中心库初始化
```

### SQL脚本特性

- ✅ 包含 `CREATE DATABASE IF NOT EXISTS` 语句
- ✅ 包含 `USE database` 切换语句  
- ✅ 使用 `CREATE TABLE IF NOT EXISTS` 确保幂等性
- ✅ 字符集统一使用 `utf8mb4`

---

## ⚙️ 初始化流程

### Docker Compose 自动初始化

`docker-compose-all.yml` 中的 `db-init` 服务会自动执行初始化：

```yaml
db-init:
  image: mysql:8.0
  container_name: ioedream-db-init
  depends_on:
    mysql:
      condition: service_healthy
```

**执行顺序**:
1. 等待 MySQL 健康检查通过
2. 按文件名顺序执行 `deployment/mysql/init/*.sql`
3. 验证数据库和表数量
4. 完成后退出（`restart: "no"`）

### 手动初始化

如果需要手动初始化：

```powershell
# 方式1: 使用Docker Compose
docker-compose -f docker-compose-all.yml up db-init

# 方式2: 直接执行SQL
docker exec -i ioedream-mysql mysql -uroot -proot1234 < deployment/mysql/init/01-ioedream-schema.sql
docker exec -i ioedream-mysql mysql -uroot -proot1234 < deployment/mysql/init/nacos-schema.sql
```

---

## 🔍 验证数据库

### 使用验证脚本

```powershell
# 基本验证
.\scripts\verify-database-init.ps1

# 显示详细信息
.\scripts\verify-database-init.ps1 -ShowDetails

# 重新初始化
.\scripts\verify-database-init.ps1 -Reinitialize
```

### 手动验证

```powershell
# 检查数据库是否存在
docker exec ioedream-mysql mysql -uroot -proot1234 -e "SHOW DATABASES;"

# 检查ioedream表
docker exec ioedream-mysql mysql -uroot -proot1234 -e "SHOW TABLES FROM ioedream;"

# 检查nacos表
docker exec ioedream-mysql mysql -uroot -proot1234 -e "SHOW TABLES FROM nacos;"

# 检查nacos表数量（应≥12）
docker exec ioedream-mysql mysql -uroot -proot1234 -N -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';"
```

---

## 📊 数据库表清单

### ioedream 数据库

| 表名 | 说明 |
|------|------|
| `sys_config` | 系统配置表 |
| `sys_dict_type` | 数据字典类型表 |
| `sys_dict_data` | 数据字典数据表 |

### nacos 数据库

| 表名 | 说明 |
|------|------|
| `config_info` | 配置信息表 |
| `config_info_aggr` | 聚合配置表 |
| `config_info_beta` | Beta配置表 |
| `config_info_tag` | 标签配置表 |
| `config_tags_relation` | 配置标签关系表 |
| `group_capacity` | 分组容量表 |
| `his_config_info` | 配置历史表 |
| `tenant_capacity` | 租户容量表 |
| `tenant_info` | 租户信息表 |
| `users` | 用户表 |
| `roles` | 角色表 |
| `permissions` | 权限表 |

---

## ⚠️ 常见问题

### 问题1: Nacos启动失败 "Unknown database 'nacos'"

**原因**: 数据库未正确初始化

**解决方案**:
```powershell
# 1. 停止所有服务
docker-compose -f docker-compose-all.yml down

# 2. 删除db-init容器
docker rm ioedream-db-init

# 3. 重新启动（会自动初始化）
docker-compose -f docker-compose-all.yml up -d
```

### 问题2: db-init服务失败

**原因**: MySQL连接超时或权限问题

**解决方案**:
```powershell
# 1. 检查MySQL状态
docker logs ioedream-mysql

# 2. 手动测试连接
docker exec ioedream-mysql mysql -uroot -proot1234 -e "SELECT 1;"

# 3. 查看db-init日志
docker logs ioedream-db-init
```

### 问题3: 表已存在导致初始化失败

**说明**: 不会发生，因为所有脚本都使用 `IF NOT EXISTS`

---

## 📝 配置参数

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `root1234` | MySQL root密码 |
| `MYSQL_DATABASE` | `ioedream` | 默认数据库名 |

### 连接信息

| 参数 | 值 |
|------|-----|
| 主机 | `mysql` (Docker内部) / `localhost` (外部) |
| 端口 | `3306` |
| 用户 | `root` |
| 字符集 | `utf8mb4` |

---

## 🔗 相关文档

- [全局配置一致性标准](./GLOBAL_CONFIG_CONSISTENCY.md)
- [Nacos启动修复报告](./NACOS_STARTUP_FIX_REPORT.md)
- [Docker部署指南](../../../CLAUDE.md#docker部署)
