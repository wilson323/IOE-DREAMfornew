# MCP 服务器配置说明

## 📋 概述

已为项目配置多个 MCP (Model Context Protocol) 服务器，扩展 Claude Code 的功能：
- **Maven Tools MCP**: 提供 Maven Central 依赖智能分析
- **MySQL MCP**: 允许直接访问数据库执行查询和操作（可选）

## 📁 配置文件位置

```
.claude/mcp.json
```

## 🔧 已配置的 MCP 服务器

### 1. Maven Tools MCP

**功能**: 提供 Maven Central 依赖智能分析，支持所有 JVM 构建工具（Maven、Gradle、SBT、Mill）

**主要功能**:
- ✅ 获取最新版本或稳定版本
- ✅ 批量检查多个依赖
- ✅ 版本比较和升级建议
- ✅ 依赖年龄分析
- ✅ 发布模式分析
- ✅ 项目健康评分
- ✅ Context7 文档集成（默认启用）

**配置方式**: 使用 Docker 容器运行

**使用示例**:
- "检查项目中所有依赖的最新版本"
- "Spring Boot 的最新稳定版本是什么？"
- "哪些依赖需要更新？"
- "分析我的依赖健康状况"

**GitHub**: https://github.com/PhilippMT/maven-tools-mcp

### 2. MySQL MCP（可选）

如需配置 MySQL MCP，可参考 `.claude/mcp.json.example` 文件中的示例配置。

## 🚀 快速开始

### 前置要求

1. **Docker**: 确保已安装并运行 Docker Desktop
   ```powershell
   docker --version
   docker ps
   ```

2. **配置文件**: 配置文件已创建在 `.claude/mcp.json`

### 验证配置

1. 重启 Cursor IDE
2. 在 Cursor 中，MCP 服务器会自动连接
3. 可以在对话中直接使用 Maven Tools 功能

### 使用 Maven Tools MCP

**示例问题**:
- "检查 microservices-common 模块的 pom.xml 中所有依赖的最新版本"
- "Spring Boot 3.5.8 是最新版本吗？"
- "分析项目中所有依赖的升级建议"
- "哪些依赖已经过时需要更新？"

**支持的工具**:
- `get_latest_version` - 获取最新版本
- `check_version_exists` - 检查版本是否存在
- `check_multiple_dependencies` - 批量检查依赖
- `compare_dependency_versions` - 版本比较
- `analyze_dependency_age` - 依赖年龄分析
- `analyze_release_patterns` - 发布模式分析
- `analyze_project_health` - 项目健康分析

## 📝 配置详情

### Maven Tools MCP 配置

```json
{
  "mcpServers": {
    "maven-tools": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SPRING_PROFILES_ACTIVE=docker",
        "arvindand/maven-tools-mcp:latest"
      ]
    }
  }
}
```

### 禁用 Context7 集成（可选）

如果不需要 Context7 文档功能，可以添加环境变量：

```json
{
  "mcpServers": {
    "maven-tools": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SPRING_PROFILES_ACTIVE=docker",
        "-e",
        "CONTEXT7_ENABLED=false",
        "arvindand/maven-tools-mcp:latest"
      ]
    }
  }
}
```

## 🔍 故障排查

### Docker 相关问题

1. **Docker 未运行**
   ```powershell
   # 检查 Docker 状态
   docker ps
   
   # 如果失败，启动 Docker Desktop
   ```

2. **镜像拉取失败**
   ```powershell
   # 手动拉取镜像
   docker pull arvindand/maven-tools-mcp:latest
   ```

3. **权限问题**
   - 确保 Docker Desktop 正在运行
   - 检查 Docker 服务状态

### MCP 连接问题

1. **重启 Cursor**: 配置更改后需要重启 Cursor IDE
2. **检查日志**: 查看 Cursor 的 MCP 连接日志
3. **验证配置**: 确保 JSON 格式正确

## 📚 相关资源

- **Maven Tools MCP GitHub**: https://github.com/PhilippMT/maven-tools-mcp
- **MCP 协议文档**: https://modelcontextprotocol.io
- **Maven Central**: https://repo1.maven.org/maven2

## 📞 技术支持

遇到问题请参考相关文档或联系技术团队。

---

**最后更新**: 2025-01-30
**版本**: v2.0.0
