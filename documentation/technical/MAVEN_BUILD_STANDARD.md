# Maven 标准构建指南

## 📋 概述

本文档说明如何使用标准Maven命令按正确顺序构建、安装和验证整个项目，无需使用PowerShell脚本。

## 🎯 核心原则

1. **使用 `install` 而非 `compile`**: 确保每个模块都安装到本地Maven仓库
2. **使用 `-pl` 和 `-am`**: 指定模块并自动构建依赖
3. **分阶段构建**: 先构建公共模块，再构建业务服务
4. **最后完整构建**: 验证所有模块

## 🏗️ 标准构建流程

### 方式1: 分阶段构建（推荐，解决依赖问题）

#### 阶段1: 安装父POM

```bash
cd microservices
mvn clean install -N -DskipTests
```

**说明**:

- `-N`: 非递归模式，只构建当前POM，不构建子模块
- 确保父POM安装到本地仓库

#### 阶段2: 构建并安装核心模块

```bash
# Layer 1: 核心模块（无依赖）
mvn clean install -pl microservices-common-core -am -DskipTests

# Layer 2: 功能模块（依赖core）
mvn clean install -pl microservices-common-entity,microservices-common-storage -am -DskipTests
mvn clean install -pl microservices-common-security,microservices-common-data,microservices-common-cache -am -DskipTests
mvn clean install -pl microservices-common-monitor,microservices-common-export,microservices-common-workflow -am -DskipTests
mvn clean install -pl microservices-common-permission -am -DskipTests

# Layer 3: 业务公共模块
mvn clean install -pl microservices-common-business -am -DskipTests

# Layer 4: 公共模块聚合器
mvn clean install -pl microservices-common -am -DskipTests
```

**说明**:

- `-pl`: 指定要构建的模块（project list）
- `-am`: also-make，同时构建依赖的模块
- 每个命令都会安装模块到本地仓库

#### 阶段3: 构建并安装工具模块

```bash
mvn clean install -pl ioedream-db-init -am -DskipTests
```

#### 阶段4: 构建并安装业务服务

```bash
# 基础设施服务
mvn clean install -pl ioedream-gateway-service -am -DskipTests
mvn clean install -pl ioedream-common-service -am -DskipTests
mvn clean install -pl ioedream-device-comm-service -am -DskipTests
mvn clean install -pl ioedream-oa-service -am -DskipTests

# 业务服务
mvn clean install -pl ioedream-access-service -am -DskipTests
mvn clean install -pl ioedream-attendance-service -am -DskipTests
mvn clean install -pl ioedream-video-service -am -DskipTests
mvn clean install -pl ioedream-consume-service -am -DskipTests
mvn clean install -pl ioedream-visitor-service -am -DskipTests
mvn clean install -pl ioedream-database-service -am -DskipTests
mvn clean install -pl ioedream-biometric-service -am -DskipTests
```

#### 阶段5: 完整项目构建验证

```bash
# 最后构建整个项目，验证所有模块
mvn clean install -DskipTests
```

### 方式2: 一次性构建（如果依赖关系正确）

```bash
cd microservices

# 一次性构建所有模块（Maven会自动按依赖顺序构建）
mvn clean install -DskipTests
```

**注意**: 如果遇到依赖找不到的问题，使用方式1分阶段构建。

## 🔧 常用Maven命令

### 构建单个服务（自动构建依赖）

```bash
# 构建access-service及其所有依赖
mvn clean install -pl ioedream-access-service -am -DskipTests
```

### 只构建指定模块（不构建依赖）

```bash
# 只构建access-service，假设依赖已安装
mvn clean install -pl ioedream-access-service -DskipTests
```

### 从指定模块继续构建

```bash
# 从microservices-common-business开始继续构建
mvn clean install -rf microservices-common-business -DskipTests
```

### 查看构建顺序

```bash
# 显示Maven计算的构建顺序
mvn dependency:tree -Dverbose
```

### 验证依赖关系

```bash
# 分析依赖关系
mvn dependency:analyze

# 查看依赖树
mvn dependency:tree
```

## 📊 构建顺序说明

Maven会根据以下因素自动确定构建顺序：

1. **POM中的`<modules>`顺序**: `microservices/pom.xml`中定义的顺序
2. **依赖关系**: 模块之间的`<dependency>`关系
3. **Reactor排序**: Maven会自动计算最优构建顺序

当前`pom.xml`中的模块顺序（已按依赖关系排列）：

```xml
<modules>
  <!-- Layer 1: 核心模块 -->
  <module>microservices-common-core</module>
  <module>microservices-common-entity</module>
  
  <!-- Layer 2: 功能模块 -->
  <module>microservices-common-storage</module>
  <module>microservices-common-data</module>
  <module>microservices-common-security</module>
  <module>microservices-common-cache</module>
  <module>microservices-common-monitor</module>
  <module>microservices-common-export</module>
  <module>microservices-common-workflow</module>
  <module>microservices-common-business</module>
  <module>microservices-common-permission</module>
  
  <!-- Layer 3: 聚合模块 -->
  <module>microservices-common</module>
  
  <!-- Layer 4: 工具模块 -->
  <module>ioedream-db-init</module>
  
  <!-- Layer 5: 业务服务 -->
  <module>ioedream-gateway-service</module>
  <module>ioedream-common-service</module>
  <module>ioedream-device-comm-service</module>
  <module>ioedream-oa-service</module>
  <module>ioedream-access-service</module>
  <!-- ... 其他服务 ... -->
</modules>
```

## ⚠️ 常见问题解决

### 问题1: 找不到依赖类（如BusinessException）

**原因**: Maven在reactor模式下可能使用`target/classes`而不是本地仓库的JAR

**解决方案**:

```bash
# 方案1: 分阶段构建，确保每个模块都安装
mvn clean install -pl microservices-common-core -am -DskipTests
mvn clean install -pl microservices-common-storage -am -DskipTests

# 方案2: 强制使用本地仓库
mvn clean install -DskipTests -U
```

### 问题2: spring-boot-maven-plugin:repackage 错误

**原因**: 库模块没有主类，但尝试repackage

**解决方案**: 已在库模块的`pom.xml`中配置`<skip>true</skip>`，如果仍有问题：

```bash
# 跳过repackage
mvn clean install -DskipTests -Dspring-boot.repackage.skip=true
```

### 问题3: 构建速度慢

**解决方案**:

```bash
# 跳过测试和质量检查
mvn clean install -DskipTests -Dpmd.skip=true -Dcheckstyle.skip=true

# 使用并行构建（谨慎使用）
mvn clean install -DskipTests -T 4
```

## 🎯 最佳实践

### 日常开发

```bash
# 只构建正在开发的服务
mvn clean install -pl ioedream-access-service -am -DskipTests
```

### CI/CD构建

```bash
# 完整构建，包含测试
cd microservices
mvn clean install
```

### 问题排查

```bash
# 详细输出
mvn clean install -X -DskipTests

# 只编译不安装（快速检查）
mvn clean compile -DskipTests
```

## 📝 一键构建脚本（可选）

如果经常需要分阶段构建，可以创建一个简单的批处理文件：

**Windows (build.bat)**:

```batch
@echo off
cd microservices

echo [1/5] 安装父POM...
call mvn clean install -N -DskipTests
if errorlevel 1 exit /b 1

echo [2/5] 构建核心模块...
call mvn clean install -pl microservices-common-core -am -DskipTests
if errorlevel 1 exit /b 1

echo [3/5] 构建功能模块...
call mvn clean install -pl microservices-common-entity,microservices-common-storage,microservices-common-security,microservices-common-data,microservices-common-cache,microservices-common-monitor,microservices-common-export,microservices-common-workflow,microservices-common-permission -am -DskipTests
if errorlevel 1 exit /b 1

echo [4/5] 构建业务模块...
call mvn clean install -pl microservices-common-business,microservices-common -am -DskipTests
if errorlevel 1 exit /b 1

echo [5/5] 构建业务服务...
call mvn clean install -pl ioedream-gateway-service,ioedream-common-service,ioedream-device-comm-service,ioedream-oa-service,ioedream-access-service,ioedream-attendance-service,ioedream-video-service,ioedream-consume-service,ioedream-visitor-service,ioedream-database-service,ioedream-biometric-service -am -DskipTests
if errorlevel 1 exit /b 1

echo [完成] 完整项目构建验证...
call mvn clean install -DskipTests

echo.
echo ========================================
echo 构建完成！
echo ========================================
```

**Linux/Mac (build.sh)**:

```bash
#!/bin/bash
set -e

cd microservices

echo "[1/5] 安装父POM..."
mvn clean install -N -DskipTests

echo "[2/5] 构建核心模块..."
mvn clean install -pl microservices-common-core -am -DskipTests

echo "[3/5] 构建功能模块..."
mvn clean install -pl microservices-common-entity,microservices-common-storage,microservices-common-security,microservices-common-data,microservices-common-cache,microservices-common-monitor,microservices-common-export,microservices-common-workflow,microservices-common-permission -am -DskipTests

echo "[4/5] 构建业务模块..."
mvn clean install -pl microservices-common-business,microservices-common -am -DskipTests

echo "[5/5] 构建业务服务..."
mvn clean install -pl ioedream-gateway-service,ioedream-common-service,ioedream-device-comm-service,ioedream-oa-service,ioedream-access-service,ioedream-attendance-service,ioedream-video-service,ioedream-consume-service,ioedream-visitor-service,ioedream-database-service,ioedream-biometric-service -am -DskipTests

echo "[完成] 完整项目构建验证..."
mvn clean install -DskipTests

echo ""
echo "========================================"
echo "构建完成！"
echo "========================================"
```

## 🔗 相关文档

- [构建顺序强制标准](./BUILD_ORDER_MANDATORY_STANDARD.md)
- [CLAUDE.md 规范](../../CLAUDE.md)
