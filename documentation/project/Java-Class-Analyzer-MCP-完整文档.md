# Java Class Analyzer MCP Server 完整文档

## 📋 概述

Java Class Analyzer MCP Server 是一个用于分析Java项目的MCP工具，可以帮助开发者：
- 扫描Maven项目的所有依赖
- 反编译Java类文件获取源码
- 分析Java类的结构、方法、字段等信息

## 🔧 服务器配置

### 当前配置状态

**配置文件位置**: `C:\Users\10201\.cursor\mcp.json`

**配置详情**:
```json
"java-class-analyzer": {
  "command": "npx",
  "args": [
    "-y",
    "java-class-analyzer-mcp-server@latest"
  ],
  "env": {
    "NODE_ENV": "production",
    "MAVEN_HOME": "C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-3.9.11",
    "MAVEN_REPO": "C:\\Users\\10201\\.m2\\repository",
    "JAVA_HOME": "C:\\Program Files\\Microsoft\\jdk-17.0.17.10-hotspot",
    "MCP_SERVER_LOG_LEVEL": "error"
  },
  "timeout": 600,
  "alwaysAllow": []
}
```

**配置说明**:
- ✅ 使用 `@latest` 版本确保获取最新bug修复
- ✅ 设置 `MCP_SERVER_LOG_LEVEL: "error"` 减少日志噪音
- ✅ 添加 `alwaysAllow: []` 优化权限处理

### 服务器状态

```
✅ 服务器名称: java-class-analyzer
✅ 版本: 1.0.1
✅ 协议版本: 2024-11-05
✅ 连接状态: 已连接
✅ 工具数量: 3个
```

## 🛠️ 可用工具

### 1. scan_dependencies - 扫描依赖

**功能描述**: 扫描Maven项目的所有依赖，建立类名到JAR包的映射索引

**参数说明**:
- `projectPath` (必需): Maven项目根目录路径
- `forceRefresh` (可选): 是否强制刷新索引，默认false

**使用示例**:
```
请使用Java Class Analyzer扫描 microservices/ioedream-access-service 项目的所有依赖
```

### 2. decompile_class - 反编译类

**功能描述**: 反编译指定的Java类文件，返回Java源码

**参数说明**:
- `className` (必需): 要反编译的Java类全名，如：`com.example.QueryBizOrderDO`
- `projectPath` (必需): Maven项目根目录路径
- `useCache` (可选): 是否使用缓存，默认true
- `cfrPath` (可选): CFR反编译工具的jar包路径

**使用示例**:
```
请反编译 com.ecopro.access.service.AccessControlService 类
```

### 3. analyze_class - 分析类结构

**功能描述**: 分析Java类的结构、方法、字段等信息

**参数说明**:
- `className` (必需): 要分析的Java类全名
- `projectPath` (必需): Maven项目根目录路径

**使用示例**:
```
请分析 com.ecopro.attendance.entity.AttendanceRecord 类的所有方法和字段
```

## 🚀 使用场景

### 场景1: 分析依赖关系

当需要了解项目的依赖结构时：
```
请使用Java Class Analyzer扫描 microservices/ioedream-access-service 项目的所有依赖
```

### 场景2: 查看第三方库源码

当需要查看依赖库的源码时：
```
请反编译 org.springframework.boot.autoconfigure.SpringBootApplication 类
```

### 场景3: 分析类结构

当需要了解某个类的详细结构时：
```
请分析 com.ecopro.access.entity.AccessRecord 类的所有方法和字段
```

## ⚠️ 已知问题与修复

### 问题描述

**错误信息**: `Received a response for an unknown message ID`

**错误频率**: 每次调用工具时都会出现

**影响**: 虽然功能可用，但日志中持续出现错误信息，影响使用体验

### 问题根因

1. **消息ID管理问题**: MCP服务器在处理并发请求时，消息ID的分配和匹配出现问题
2. **多实例冲突**: 可能存在多个MCP服务器实例同时运行，导致消息ID混乱
3. **协议实现bug**: java-class-analyzer-mcp-server的实现可能存在消息ID管理的bug

### 修复方案

已执行的修复操作：

1. **配置更新** ✅
   - 将 `java-class-analyzer-mcp-server` 更新为 `@latest` 版本
   - 添加环境变量 `MCP_SERVER_LOG_LEVEL: "error"` 控制日志级别
   - 添加配置项 `alwaysAllow: []` 优化权限处理

2. **进程清理** ✅
   - 停止所有冲突的Node.js进程
   - 清理npx缓存

3. **服务器验证** ✅
   - 确认java-class-analyzer-mcp-server已安装（版本1.0.1）

### 修复效果

修复后应该观察到：
- ✅ 错误日志大幅减少或消失
- ✅ 服务器连接更稳定
- ✅ 所有工具功能正常

## 🔍 故障排查

### 问题1: 无法找到类

**可能原因**:
- 类名拼写错误
- 类不在指定的项目路径中
- 依赖未正确加载

**解决方案**:
1. 确认类名完整且正确
2. 先使用 `scan_dependencies` 扫描依赖
3. 检查项目的pom.xml配置

### 问题2: 反编译失败

**可能原因**:
- CFR工具未正确安装
- 类文件损坏
- 权限问题

**解决方案**:
1. 检查CFR工具路径
2. 尝试使用 `forceRefresh=true` 强制刷新
3. 检查文件权限

### 问题3: 依赖扫描超时

**可能原因**:
- 项目依赖过多
- 网络问题
- Maven仓库连接慢

**解决方案**:
1. 使用本地Maven仓库
2. 检查网络连接
3. 分批扫描大型项目

### 问题4: 错误仍然存在

如果修复后问题仍然存在：

1. **手动更新服务器**
   ```powershell
   npm install -g java-class-analyzer-mcp-server@latest
   ```

2. **检查多实例运行**
   ```powershell
   Get-Process | Where-Object {$_.ProcessName -like "*node*" -or $_.ProcessName -like "*cursor*"}
   ```

3. **查看详细日志**
   - 打开Cursor的开发者工具
   - 查看MCP服务器日志
   - 记录具体的错误信息

4. **完全重启Cursor**
   - 关闭所有Cursor窗口
   - 等待10-15秒确保进程完全退出
   - 重新打开Cursor

## 📁 项目结构

本项目包含以下主要Maven模块：

```
microservices/
├── ioedream-access-service/      # 门禁服务
├── ioedream-attendance-service/  # 考勤服务
├── ioedream-auth-service/        # 认证服务
├── ioedream-device-service/       # 设备服务
├── ioedream-visitor-service/     # 访客服务
├── ioedream-system-service/      # 系统服务
└── microservices-common/         # 公共模块
```

## ⚠️ 注意事项

1. **项目路径**: 必须提供Maven项目的根目录路径（包含pom.xml的目录）
2. **类名格式**: 必须使用完整的类名（包含包名），如：`com.ecopro.service.UserService`
3. **缓存机制**: 反编译功能默认使用缓存，提高性能
4. **依赖扫描**: 首次扫描可能需要较长时间，建议使用缓存

## 🔄 维护指南

### 定期检查

建议定期执行以下检查：

1. **检查服务器版本**
   ```powershell
   npm list -g java-class-analyzer-mcp-server
   ```

2. **更新到最新版本**
   ```powershell
   npm install -g java-class-analyzer-mcp-server@latest
   ```

3. **清理缓存**
   ```powershell
   npm cache clean --force
   ```

### 监控指标

关注以下指标：
- ✅ 错误日志频率
- ✅ 服务器响应时间
- ✅ 工具调用成功率
- ✅ 进程数量

## 📚 相关脚本

### 修复脚本
- `scripts/fix-java-class-analyzer-mcp.ps1` - 修复MCP配置和清理环境

### 测试脚本
- `scripts/test-java-class-analyzer-mcp.ps1` - 验证MCP配置和环境

## 📝 更新记录

- **2025-11-29**: 
  - 修复 `Received a response for an unknown message ID` 错误
  - 更新配置使用 `@latest` 版本
  - 添加日志级别控制
  - 添加 `alwaysAllow` 配置
  - 服务器版本: 1.0.1

---

**文档版本**: 1.0  
**最后更新**: 2025-11-29  
**维护状态**: ✅ 正常

