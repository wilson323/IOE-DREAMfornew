# IntelliJ IDEA 编译服务器连接超时问题修复指南

## 📋 问题描述

**错误信息**:
```
Error connecting to 127.0.0.1:56742; reason: Connection timed out: getsockopt
io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection timed out
```

**问题原因**:
- IntelliJ IDEA编译服务器进程未正常启动
- 编译服务器端口被占用或防火墙阻止
- 编译服务器缓存损坏
- Java模块访问权限问题

---

## 🔍 问题诊断

### 快速诊断脚本

运行以下脚本自动诊断问题：

```powershell
.\scripts\fix-intellij-compile-server.ps1
```

### 手动诊断步骤

#### 1. 检查编译服务器进程

```powershell
# 查找编译服务器进程
Get-Process -Name "java" | Where-Object {
    $_.CommandLine -like "*jps-launcher*" -or 
    $_.CommandLine -like "*compile-server*"
}
```

#### 2. 检查端口占用

```powershell
# 检查常见编译服务器端口
Get-NetTCPConnection -LocalPort 56742 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 56743 -ErrorAction SilentlyContinue
```

#### 3. 检查编译服务器日志

```
日志位置: %LOCALAPPDATA%\JetBrains\IntelliJIdea2025.2\log\build-log
```

---

## 🛠️ 解决方案

### 方案1: 使用修复脚本（推荐）

```powershell
# 运行自动修复脚本
.\scripts\fix-intellij-compile-server.ps1
```

脚本会自动：
- ✅ 终止异常的编译服务器进程
- ✅ 清理编译服务器缓存
- ✅ 清理项目编译缓存
- ✅ 检查端口占用情况

### 方案2: 手动修复步骤

#### 步骤1: 终止编译服务器进程

1. **在IntelliJ IDEA中**:
   - `File → Invalidate Caches / Restart...`
   - 选择 `Invalidate and Restart`
   - 等待IDE重启

2. **在PowerShell中**:
   ```powershell
   # 查找并终止编译服务器进程
   Get-Process -Name "java" | Where-Object {
       $_.Path -like "*IntelliJ*"
   } | Stop-Process -Force
   ```

#### 步骤2: 清理编译服务器缓存

```powershell
# 清理编译服务器目录
$compileServerDir = "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\compile-server"
if (Test-Path $compileServerDir) {
    Remove-Item -Path $compileServerDir -Recurse -Force
}

# 清理构建日志
$buildLogDir = "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\log\build-log"
if (Test-Path $buildLogDir) {
    Remove-Item -Path $buildLogDir -Recurse -Force
}
```

#### 步骤3: 清理项目缓存

```powershell
# 清理项目编译输出
Remove-Item -Path ".\out" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".\target" -Recurse -Force -ErrorAction SilentlyContinue

# 清理.idea缓存（可选，需要重新导入项目）
# Remove-Item -Path ".\.idea\workspace.xml" -Force -ErrorAction SilentlyContinue
```

#### 步骤4: 重新导入Maven项目

1. 打开 **Maven工具窗口** (`View → Tool Windows → Maven`)
2. 点击 **Reload All Maven Projects** 按钮
3. 等待Maven项目重新导入完成

#### 步骤5: 配置Maven Runner

1. `File → Settings → Build, Execution, Deployment → Build Tools → Maven → Runner`
2. 选择以下选项之一：
   - ✅ **使用IDE构建**: 取消勾选 `Delegate IDE build/run actions to Maven`
   - ✅ **使用Maven构建**: 勾选 `Delegate IDE build/run actions to Maven`

---

## 🔧 高级修复方案

### 方案A: 重置IDE配置（彻底解决）

**警告**: 此操作会删除IDE配置，需要重新配置项目。

```powershell
# 1. 关闭IntelliJ IDEA

# 2. 备份并删除IDE配置目录
$ideaConfigDir = "$env:APPDATA\JetBrains\IntelliJIdea2025.2"
$ideaLocalDir = "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2"

# 备份（可选）
Copy-Item -Path $ideaConfigDir -Destination "$ideaConfigDir.backup" -Recurse -ErrorAction SilentlyContinue
Copy-Item -Path $ideaLocalDir -Destination "$ideaLocalDir.backup" -Recurse -ErrorAction SilentlyContinue

# 删除编译服务器相关目录
Remove-Item -Path "$ideaLocalDir\compile-server" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$ideaLocalDir\log\build-log" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$ideaLocalDir\caches" -Recurse -Force -ErrorAction SilentlyContinue

# 3. 重新打开IntelliJ IDEA
# 4. 重新导入项目
```

### 方案B: 使用命令行构建（绕过IDE问题）

如果IDE构建持续失败，可以使用命令行构建：

```powershell
# 1. 先构建common模块（必须）
cd D:\IOE-DREAM
mvn clean install -pl microservices/microservices-common -am -DskipTests

# 2. 构建业务服务
mvn clean install -pl microservices/ioedream-access-service -am -DskipTests
```

### 方案C: 配置Java模块访问权限

如果出现模块访问权限错误，在`pom.xml`中添加JVM参数：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>--add-opens</arg>
            <arg>java.base/java.lang=ALL-UNNAMED</arg>
            <arg>--add-opens</arg>
            <arg>java.base/java.util=ALL-UNNAMED</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

---

## ✅ 验证修复

### 1. 检查编译服务器状态

在IntelliJ IDEA中：
- `Help → Show Log in Explorer`
- 查看最新的日志文件，确认没有连接错误

### 2. 测试项目构建

```powershell
# 在IDE中执行构建
# Build → Rebuild Project

# 或使用命令行验证
mvn clean compile -pl microservices/microservices-common -am
```

### 3. 检查依赖解析

- 打开任意Java文件
- 检查导入语句是否有红色错误提示
- 确认Maven依赖已正确下载

---

## 🚨 常见问题

### Q1: 修复后仍然连接超时

**解决方案**:
1. 检查防火墙是否阻止了本地端口
2. 检查是否有多个IntelliJ IDEA实例运行
3. 尝试重启计算机

### Q2: 清理缓存后项目无法识别

**解决方案**:
1. `File → Invalidate Caches / Restart...`
2. 重新导入Maven项目
3. 如果仍然失败，删除`.idea`文件夹并重新打开项目

### Q3: Maven项目导入失败

**解决方案**:
1. 检查Maven配置：`File → Settings → Build Tools → Maven`
2. 确认Maven Home路径正确
3. 检查网络连接（如果需要下载依赖）
4. 尝试手动执行：`mvn clean install -DskipTests`

### Q4: Java版本不匹配

**解决方案**:
1. 确认项目使用Java 17
2. 检查IDE配置：`File → Project Structure → Project`
3. 检查Maven配置：`File → Settings → Build Tools → Maven → Runner`

---

## 📚 相关文档

- [构建顺序强制标准](./BUILD_ORDER_MANDATORY_STANDARD.md)
- [编译环境配置](./COMPILATION_ENV_SETUP.md)
- [IDE依赖诊断脚本](../microservices/diagnose-ide-dependencies.ps1)

---

## 🔄 预防措施

### 1. 定期清理缓存

建议每月清理一次编译服务器缓存：

```powershell
.\scripts\fix-intellij-compile-server.ps1
```

### 2. 使用Maven构建

对于大型项目，建议使用Maven命令行构建：

```powershell
.\scripts\build-all.ps1
```

### 3. 监控编译服务器状态

定期检查编译服务器进程和端口占用情况。

---

**最后更新**: 2025-01-30  
**维护人**: IOE-DREAM 架构团队
