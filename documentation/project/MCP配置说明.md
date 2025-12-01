# MySQL MCP 服务器配置说明

## 📋 概述

已为项目配置MySQL MCP (Model Context Protocol) 服务器，允许Claude Code直接访问数据库执行查询和操作。

## 📁 配置文件位置

```
.claude/mcp.json
```

## 🔧 配置内容

```json
{
  "mcpServers": {
    "MySQL": {
      "command": "uvx",
      "args": [
        "--from",
        "mysql-mcp-server",
        "mysql_mcp_server"
      ],
      "env": {
        "MYSQL_HOST": "192.168.10.110",
        "MYSQL_PORT": "33060",
        "MYSQL_USER": "ls",
        "MYSQL_PASSWORD": "admin@123",
        "MYSQL_DATABASE": "smart_admin_v3"
      }
    }
  }
}
```

## 📦 前置要求

### 1. 安装 Python (如果还没安装)

MCP服务器需要Python环境：

```bash
# 检查Python版本
python --version
# 或
python3 --version

# 需要 Python 3.8 或更高版本
```

### 2. 安装 uv (Python包管理工具)

```bash
# Windows (使用 PowerShell)
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"

# 或使用 pip 安装
pip install uv
```

### 3. 安装 MySQL MCP Server

MCP服务器会在首次使用时自动通过 `uvx` 安装，无需手动安装。

## 🚀 使用方法

### 重启Claude Code

配置文件创建后，需要重启Claude Code以加载MCP配置：

1. 关闭所有Claude Code窗口
2. 重新打开项目
3. 或者使用命令：Reload Window

### 验证MCP连接

重启后，可以在Claude Code中：

1. **查看MCP状态**：
   - 在对话中询问："MySQL MCP服务器是否已连接？"
   - 或查看状态栏是否显示MCP连接

2. **测试数据库查询**：
   ```
   请使用MCP查询数据库中的所有表
   ```

3. **查询菜单数据**：
   ```
   请使用MCP查询t_sys_menu表中的所有一级菜单
   ```

## 💡 MCP功能说明

配置MySQL MCP服务器后，您可以：

### ✅ 可以做的事情

1. **直接查询数据库**
   - 查看表结构
   - 查询数据
   - 统计分析

2. **执行数据库操作**
   - 插入数据
   - 更新数据
   - 删除数据
   - 创建表/修改表结构

3. **无需切换工具**
   - 不需要打开Navicat
   - 直接在对话中操作数据库
   - 自动获取查询结果

### 🎯 使用示例

```
用户: 请查询系统设置菜单下的所有子菜单
Claude: [使用MCP自动查询数据库并返回结果]

用户: 请统计区域表中的数据总数
Claude: [执行COUNT查询并显示结果]

用户: 请查看t_area表的结构
Claude: [使用DESCRIBE或SHOW COLUMNS查询表结构]
```

## ⚠️ 注意事项

### 1. 安全性

- ✅ 配置文件包含数据库密码，请确保 `.claude/` 目录已添加到 `.gitignore`
- ✅ 不要将包含密码的配置文件提交到版本控制系统
- ✅ 建议使用只读权限的数据库用户（当前用户为 `ls`）

### 2. 性能

- MCP查询会实时连接数据库
- 大量数据查询可能较慢
- 建议使用LIMIT限制返回结果数量

### 3. 权限

当前配置的数据库用户 `ls` 应该具有以下权限：
- SELECT（查询）
- INSERT（插入）
- UPDATE（更新）
- DELETE（删除）
- CREATE/ALTER（如果需要创建/修改表）

## 🔍 故障排查

### 问题1: MCP服务器无法启动

**解决方案**：
```bash
# 检查Python是否安装
python --version

# 检查uv是否安装
uv --version

# 手动安装mysql-mcp-server
pip install mysql-mcp-server
```

### 问题2: 数据库连接失败

**检查项**：
- ✅ 数据库服务器是否运行（192.168.10.110:33060）
- ✅ 用户名密码是否正确（ls/admin@123）
- ✅ 数据库名称是否正确（smart_admin_v3）
- ✅ 网络是否可达

**测试连接**：
```bash
mysql -h 192.168.10.110 -P 33060 -u ls -p"admin@123" smart_admin_v3
```

### 问题3: uvx命令未找到

**解决方案**：
```bash
# 重新安装uv
pip install --upgrade uv

# 或下载独立版本
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"
```

## 📚 相关资源

- [MCP官方文档](https://modelcontextprotocol.io/)
- [mysql-mcp-server GitHub](https://github.com/designcomputer/mysql_mcp_server)
- [uv官方文档](https://docs.astral.sh/uv/)

## 🔄 更新配置

如需修改数据库连接信息，编辑 `.claude/mcp.json` 文件：

```json
{
  "mcpServers": {
    "MySQL": {
      "env": {
        "MYSQL_HOST": "新的主机地址",
        "MYSQL_PORT": "新的端口",
        "MYSQL_USER": "新的用户名",
        "MYSQL_PASSWORD": "新的密码",
        "MYSQL_DATABASE": "新的数据库名"
      }
    }
  }
}
```

修改后需要重启Claude Code。

## ✨ 最佳实践

1. **查询前先了解表结构**
   ```
   请使用MCP查看t_area表的结构
   ```

2. **使用LIMIT限制结果**
   ```
   请查询t_sys_menu表的前10条数据
   ```

3. **备份重要操作**
   ```
   在执行UPDATE/DELETE前，请先执行SELECT确认数据
   ```

4. **利用MCP简化开发**
   - 快速验证数据
   - 调试SQL查询
   - 检查数据完整性

---

**配置完成时间**: 2025-11-11
**数据库**: smart_admin_v3 @ 192.168.10.110:33060
**状态**: ✅ 已配置
