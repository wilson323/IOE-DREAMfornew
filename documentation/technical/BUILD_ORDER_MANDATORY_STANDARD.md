# IOE-DREAM 构建顺序强制标准

> **版本**: v1.0.0
> **生效日期**: 2025-01-30
> **适用范围**: IOE-DREAM所有微服务构建
> **规范级别**: 🔴 **强制执行**
> **制定人**: IOE-DREAM架构委员会
> **最后更新**: 2025-01-30（企业级统一计划实施）

---

## 🚨 黄金法则（强制执行）

> **microservices-common 必须在任何业务服务构建之前完成构建和安装**

**违反此规则将导致**:

- ❌ 依赖解析失败（`The import net.lab1024.sa.common.device cannot be resolved`）
- ❌ IDE无法识别类（`DeviceEntity cannot be resolved to a type`）
- ❌ 编译错误（200+ 错误）
- ❌ 构建失败

---

## 📋 强制构建顺序

### 第一阶段：基础设施构建（P0级）

```bash
# 1. 必须首先构建公共模块
microservices-common-core           ← 必须最先构建
microservices-common-entity         ← 依赖common-core
microservices-common-business       ← 依赖common-core+common-entity
microservices-common-data          ← 依赖common-core
microservices-common-security      ← 依赖common-core
microservices-common-cache         ← 依赖common-core
microservices-common-monitor       ← 依赖common-core
microservices-common-export        ← 依赖common-core
microservices-common-workflow      ← 依赖common-core
microservices-common-permission    ← 依赖common-core
microservices-common-gateway-client ← 依赖common-core

# 2. 公共配置容器（网关服务专用）
microservices-common                ← 仅网关服务依赖

# 3. 基础设施服务
ioedream-gateway-service            ← 基础设施服务
ioedream-common-service             ← 公共业务服务
ioedream-device-comm-service        ← 设备通讯服务
ioedream-oa-service                ← OA服务
ioedream-biometric-service          ← 生物识别服务
ioedream-database-service           ← 数据库管理服务
```

### 第二阶段：业务服务构建（可并行）

```bash
# 业务服务（依赖基础设施服务）
├── ioedream-access-service        ← 门禁管理服务
├── ioedream-attendance-service    ← 考勤管理服务
├── ioedream-consume-service       ← 消费管理服务
├── ioedream-visitor-service       ← 访客管理服务
└── ioedream-video-service         ← 视频监控服务
```

---

## 🔧 标准构建方法（强制执行）

### ✅ 方法1: 使用统一构建脚本（推荐）

```powershell
# 构建所有服务（自动确保顺序）
.\scripts\build-all.ps1

# 构建指定服务（自动先构建common）
.\scripts\build-all.ps1 -Service ioedream-access-service

# 清理并构建
.\scripts\build-all.ps1 -Clean

# 跳过测试
.\scripts\build-all.ps1 -SkipTests
```

### ✅ 方法2: Maven命令（手动）

```powershell
# 步骤1: 强制先构建 common（必须）
mvn clean install -pl microservices/microservices-common-core -am -DskipTests
mvn clean install -pl microservices/microservices-common-entity -am -DskipTests
mvn clean install -pl microservices/microservices-common-business -am -DskipTests
# ... 继续构建所有细粒度模块

# 步骤2: 构建公共配置容器
mvn clean install -pl microservices/microservices-common -am -DskipTests

# 步骤3: 构建基础设施服务
mvn clean install -pl microservices/ioedream-gateway-service -am -DskipTests
mvn clean install -pl microservices/ioedream-common-service -am -DskipTests

# 步骤4: 构建业务服务（可并行）
mvn clean install -pl microservices/ioedream-access-service -am -DskipTests
```

**关键参数说明**:

- `-pl`: 指定要构建的模块
- `-am`: also-make，同时构建依赖的模块
- `install`: 必须使用install而非compile，确保JAR安装到本地仓库
- `-DskipTests`: 跳过测试（开发阶段）

### ❌ 禁止事项

```powershell
# ❌ 禁止：直接构建业务服务（跳过common）
mvn clean install -pl microservices/ioedream-access-service

# ❌ 禁止：只编译不安装
mvn clean compile -pl microservices/microservices-common

# ❌ 禁止：跳过common构建检查
mvn clean install -rf microservices/ioedream-access-service

# ❌ 禁止：错误的依赖顺序
mvn clean install -pl microservices/ioedream-consume-service  # 在common构建之前
```

---

## 🔍 构建后验证

### 检查JAR文件存在性

```powershell
# 检查核心模块JAR是否存在
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar"

# 检查细粒度模块JAR是否存在
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-entity\1.0.0\microservices-common-entity-1.0.0.jar"

# 检查关键类是否存在
jar -tf "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-entity\1.0.0\microservices-common-entity-1.0.0.jar" | Select-String "DeviceEntity"
```

### 检查编译结果

```bash
# 检查编译错误数量
mvn clean compile | grep -E "(ERROR|WARN)" | wc -l

# 检查依赖解析
mvn dependency:tree | grep "FAILED"
```

### IDE集成验证

```bash
# IntelliJ IDEA刷新项目
mvn idea:idea

# Eclipse刷新项目
mvn eclipse:eclipse
```

---

## 🚨 错误排查指南

### 常见错误1: 依赖解析失败

**错误信息**:
```
[ERROR] Failed to execute goal on project ioedream-access-service:
Could not resolve dependencies for project ...
The import net.lab1024.sa.common.device cannot be resolved
```

**解决方案**:
```bash
# 1. 确保common模块已构建
mvn clean install -pl microservices/microservices-common -am

# 2. 检查本地仓库
ls -la ~/.m2/repository/net/lab1024/sa/

# 3. 重新构建
mvn clean install -pl microservices/ioedream-access-service -am
```

### 常见错误2: Entity类找不到

**错误信息**:
```
DeviceEntity cannot be resolved to a type
```

**解决方案**:
```bash
# 1. 确保entity模块已构建
mvn clean install -pl microservices/microservices-common-entity -am

# 2. 检查依赖声明
# 确保pom.xml中有正确依赖：
# <dependency>
#     <groupId>net.lab1024.sa</groupId>
#     <artifactId>microservices-common-entity</artifactId>
# </dependency>
```

### 常见错误3: Manager类无法注入

**错误信息**:
```
Could not autowire field: private UserManager userManager
```

**解决方案**:
```bash
# 1. 确保business模块已构建
mvn clean install -pl microservices/microservices-common-business -am

# 2. 检查Bean注册配置
# 确保Configuration类中正确注册了Manager Bean
```

---

## 📊 构建性能优化

### 并行构建

```bash
# 使用多线程构建
mvn clean install -T 4  # 4个线程并行

# 按模块并行构建
mvn clean install -pl microservices/microservices-common-core,microservices/microservices-common-entity -am
```

### 增量构建

```bash
# 只构建变更的模块
mvn compile -pl microservices/ioedream-access-service -am

# 跳过测试加快构建
mvn install -DskipTests
```

### 网络优化

```xml
<!-- settings.xml 配置镜像 -->
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>Aliyun Maven Mirror</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

---

## 🔧 开发环境配置

### IDE设置

#### IntelliJ IDEA
```properties
# File > Settings > Build, Execution, Deployment > Build Tools > Maven
- Importing: Automatically download sources and documentation
- Runner: Delegate IDE build/run actions to Maven
- Importing: Keep Maven and generated sources roots separate
```

#### Eclipse
```properties
# Window > Preferences > Maven
- Download Artifact Sources: Enabled
- Download Artifact Javadoc: Enabled
- Update Maven projects on startup: Enabled
```

### 构建工具版本要求

```xml
<!-- Maven版本要求 -->
<properties>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  <maven.compiler.release>17</maven.compiler.release>
</properties>
```

---

## 📋 检查清单

### 构建前检查

- [ ] Maven版本 >= 3.8.0
- [ ] Java版本 = 17
- [ ] 网络连接正常
- [ ] 本地仓库空间充足
- [ ] IDE配置正确

### 构建中监控

- [ ] 依赖下载进度
- [ ] 编译错误数量
- [ ] 测试通过率
- [ ] 构建时间统计

### 构建后验证

- [ ] 目标JAR文件生成
- [ ] 关键类可访问
- [ ] 服务可正常启动
- [ ] API接口可访问

---

## 🎯 质量门禁

### CI/CD集成

```yaml
# .github/workflows/build-check.yml
name: Build Order Check
on: [push, pull_request]

jobs:
  build-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Check Build Order
        run: |
          ./scripts/build-order-check.sh

      - name: Build Common Modules
        run: |
          mvn clean install -pl microservices/microservices-common -am -DskipTests

      - name: Build All Services
        run: |
          ./scripts/build-all.ps1
```

### 自动化验证

```bash
#!/bin/bash
# scripts/build-order-check.sh

echo "检查构建顺序合规性..."

# 检查common模块是否先构建
if [ ! -f "$HOME/.m2/repository/net/lab1024/sa/microservices-common/1.0.0/microservices-common-1.0.0.jar" ]; then
    echo "❌ microservices-common未构建"
    exit 1
fi

echo "✅ 构建顺序检查通过"
```

---

## 📚 相关文档

- **[完整架构方案](./documentation/architecture/ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md)** - 企业级架构重构完整方案
- **[依赖分析报告](./documentation/technical/DEPENDENCY_ANALYSIS_REPORT.md)** - 详细的依赖关系分析
- **[构建脚本](./scripts/build-all.ps1)** - 统一构建脚本
- **[企业级架构统一计划](./企业级架构统一计划.md)** - 完整的统一实施方案

---

## 🚨 重要提醒

⚠️ **本标准为项目唯一构建规范，所有开发人员必须严格遵循**

- ✅ **强制执行**: 任何违反本标准的构建操作都将导致失败
- ✅ **构建顺序**: 必须严格遵循构建顺序，违反将导致构建失败
- ✅ **质量保障**: 所有构建必须通过质量门禁检查
- ✅ **持续监控**: CI/CD流水线自动监控构建合规性
- ✅ **团队协作**: 遵循构建标准是团队协作的基础和保障

**让我们一起确保IOE-DREAM项目的稳定构建和持续集成！** 🚀