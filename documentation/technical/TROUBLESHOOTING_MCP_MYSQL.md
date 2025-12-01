# MCP 和 MySQL 连接问题诊断报告

## 📋 问题概述

**报告时间**: 2025-01-XX  
**问题类型**: 
1. MCP MySQL 连接失败
2. Docker MySQL 连接配置不匹配

---

## 🔍 问题分析

### 1. MCP MySQL 配置问题

#### 当前配置 (`c:\Users\10201\.cursor\mcp.json`)
```json
"MySQL": {
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-mysql"],
  "env": {
    "MYSQL_HOST": "localhost",
    "MYSQL_PORT": "3306",
    "MYSQL_USER": "ecopro",
    "MYSQL_PASSWORD": "EcoProApp#2025!",
    "MYSQL_DATABASE": "ecopro"
  },
  "timeout": 600
}
```

#### Docker MySQL 实际配置 (`docker-compose.yml`)
```yaml
mysql:
  container_name: smart-admin-mysql
  environment:
    MYSQL_ROOT_PASSWORD: root1234
    MYSQL_DATABASE: smart_admin_v3
    MYSQL_USER: smartadmin
    MYSQL_PASSWORD: smartadmin123
  ports:
    - "3306:3306"
```

#### ❌ 发现的问题

| 配置项 | MCP 配置 | Docker 实际配置 | 状态 |
|--------|---------|----------------|------|
| **数据库名** | `ecopro` | `smart_admin_v3` | ❌ **不匹配** |
| **用户名** | `ecopro` | `root` / `smartadmin` | ❌ **不存在** |
| **密码** | `EcoProApp#2025!` | `root1234` / `smartadmin123` | ❌ **不匹配** |
| **主机** | `localhost` | ✅ `localhost:3306` | ✅ **正确** |
| **端口** | `3306` | ✅ `3306` | ✅ **正确** |

---

## 🔧 解决方案

### 方案 1: 修改 MCP 配置匹配 Docker MySQL（推荐）

**适用场景**: 使用项目 Docker 环境中的 MySQL

#### 步骤 1: 更新 MCP 配置

修改 `c:\Users\10201\.cursor\mcp.json` 中的 MySQL 配置：

```json
"MySQL": {
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-mysql"],
  "env": {
    "MYSQL_HOST": "localhost",
    "MYSQL_PORT": "3306",
    "MYSQL_USER": "root",
    "MYSQL_PASSWORD": "root1234",
    "MYSQL_DATABASE": "smart_admin_v3"
  },
  "timeout": 600
}
```

#### 步骤 2: 验证连接

```powershell
# 测试 MySQL 连接
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SELECT 'Connection OK' AS Status;"

# 验证数据库是否存在
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SHOW DATABASES LIKE 'smart_admin_v3';"
```

#### 步骤 3: 重启 Cursor

1. 完全退出 Cursor（任务管理器确认进程结束）
2. 重新启动 Cursor
3. MCP 将使用新配置重新连接

---

### 方案 2: 在 Docker MySQL 中创建 ecopro 用户和数据库

**适用场景**: 需要保留 MCP 的 ecopro 配置

#### 步骤 1: 创建 ecopro 用户和数据库

```powershell
# 进入 MySQL 容器
docker exec -it smart-admin-mysql mysql -uroot -proot1234

# 执行以下 SQL
CREATE DATABASE IF NOT EXISTS ecopro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ecopro'@'%' IDENTIFIED BY 'EcoProApp#2025!';
GRANT ALL PRIVILEGES ON ecopro.* TO 'ecopro'@'%';
FLUSH PRIVILEGES;
```

#### 步骤 2: 验证创建

```powershell
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SELECT User, Host FROM mysql.user WHERE User='ecopro';"
docker exec smart-admin-mysql mysql -uecopro -p'EcoProApp#2025!' -e "SHOW DATABASES;"
```

---

### 方案 3: 使用多个 MCP MySQL 配置（高级）

**适用场景**: 需要同时连接多个 MySQL 实例

在 `mcp.json` 中配置多个 MySQL 服务：

```json
{
  "mcpServers": {
    "MySQL-SmartAdmin": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "root1234",
        "MYSQL_DATABASE": "smart_admin_v3"
      },
      "timeout": 600
    },
    "MySQL-EcoPro": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3307",  // 如果 ecopro 在其他端口
        "MYSQL_USER": "ecopro",
        "MYSQL_PASSWORD": "EcoProApp#2025!",
        "MYSQL_DATABASE": "ecopro"
      },
      "timeout": 600
    }
  }
}
```

---

## 🐛 常见问题排查

### 问题 1: MCP 仍然连接失败

**可能原因**:
1. Cursor 未完全重启
2. 端口被其他程序占用
3. MySQL 容器未正常运行

**排查步骤**:

```powershell
# 1. 检查 MySQL 容器状态
docker ps | findstr mysql

# 2. 检查端口占用
netstat -ano | findstr ":3306"

# 3. 测试 MySQL 连接
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SELECT VERSION();"

# 4. 查看 MySQL 日志
docker logs smart-admin-mysql --tail 50
```

### 问题 2: 密码包含特殊字符导致连接失败

**解决方案**: 使用 URL 编码或转义特殊字符

```json
// 原密码: EcoProApp#2025!
// URL 编码后: EcoProApp%232025%21
"MYSQL_PASSWORD": "EcoProApp%232025%21"
```

### 问题 3: 连接超时

**解决方案**: 增加超时时间并检查网络

```json
{
  "timeout": 1200,  // 增加到 20 分钟
  "env": {
    "MYSQL_CONNECT_TIMEOUT": "60"
  }
}
```

---

## 📊 Docker MySQL 连接配置验证

### 当前 Docker MySQL 状态

✅ **容器状态**: `Up 15 hours (healthy)`  
✅ **端口映射**: `0.0.0.0:3306->3306/tcp`  
✅ **健康检查**: 通过

### 连接测试命令

```powershell
# 测试 root 用户连接
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SELECT 'Connection OK' AS Status, DATABASE() AS CurrentDB;"

# 测试 smartadmin 用户连接
docker exec smart-admin-mysql mysql -usmartadmin -psmartadmin123 -e "SELECT 'Connection OK' AS Status;"

# 列出所有数据库
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SHOW DATABASES;"

# 列出所有用户
docker exec smart-admin-mysql mysql -uroot -proot1234 -e "SELECT User, Host FROM mysql.user;"
```

---

## 🔐 安全建议

1. **密码管理**: 不要在配置文件中硬编码密码，使用环境变量
2. **用户权限**: 为 MCP 创建专用数据库用户，只授予必要权限
3. **网络隔离**: 如果可能，使用 Docker 网络而非端口映射
4. **日志审计**: 启用 MySQL 慢查询日志和连接日志

---

## 📝 修复清单

- [ ] 选择修复方案（推荐方案 1）
- [ ] 备份当前 `mcp.json` 配置
- [ ] 更新 MCP MySQL 配置
- [ ] 验证 Docker MySQL 连接
- [ ] 重启 Cursor IDE
- [ ] 测试 MCP MySQL 功能
- [ ] 记录修复结果

---

## 📞 技术支持

如果问题仍然存在，请提供以下信息：

1. Cursor 日志: `%APPDATA%\Cursor\logs\main.log`
2. MCP 配置: `c:\Users\10201\.cursor\mcp.json`
3. Docker MySQL 日志: `docker logs smart-admin-mysql`
4. 连接测试结果: 执行上述验证命令的输出

---

**最后更新**: 2025-01-XX  
**维护者**: AI Assistant

