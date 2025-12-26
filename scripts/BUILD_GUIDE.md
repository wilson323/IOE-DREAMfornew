# IOE-DREAM 构建指南

## 📋 快速开始

### 方式1: 使用标准Maven命令（推荐，无需脚本）

```bash
# Windows
cd microservices
build.bat

# Linux/Mac
cd microservices
chmod +x build.sh
./build.sh
```

**或者手动执行标准Maven命令**:

```bash
cd microservices

# 阶段1: 安装父POM
mvn clean install -N -DskipTests

# 阶段2: 构建核心模块
mvn clean install -pl microservices-common-core -am -DskipTests

# 阶段3: 构建功能模块
mvn clean install -pl microservices-common-entity,microservices-common-storage,microservices-common-security,microservices-common-data,microservices-common-cache,microservices-common-monitor,microservices-common-export,microservices-common-workflow,microservices-common-permission -am -DskipTests

# 阶段4: 构建业务模块
mvn clean install -pl microservices-common-business,microservices-common,ioedream-db-init -am -DskipTests

# 阶段5: 构建业务服务
mvn clean install -pl ioedream-gateway-service,ioedream-common-service,ioedream-device-comm-service,ioedream-oa-service,ioedream-access-service,ioedream-attendance-service,ioedream-video-service,ioedream-consume-service,ioedream-visitor-service,ioedream-database-service,ioedream-biometric-service -am -DskipTests

# 最后: 完整项目构建验证
mvn clean install -DskipTests
```

**详细说明**: 参见 [Maven标准构建指南](../documentation/technical/MAVEN_BUILD_STANDARD.md)

### 方式2: 使用PowerShell构建脚本

```powershell
# 从项目根目录执行
.\scripts\build-all.ps1

# 跳过测试（更快）
.\scripts\build-all.ps1 -SkipTests

# 清理后构建
.\scripts\build-all.ps1 -Clean -SkipTests

# 包含质量检查
.\scripts\build-all.ps1 -SkipTests -SkipQuality:$false
```

### 方式2: 使用详细构建脚本

```powershell
# 完整构建（按顺序构建所有模块，最后构建整个项目）
.\scripts\build-ordered.ps1 -BuildMode full -SkipTests

# 只构建公共模块
.\scripts\build-ordered.ps1 -BuildMode common -SkipTests

# 只构建业务服务（假设公共模块已安装）
.\scripts\build-ordered.ps1 -BuildMode services -SkipTests

# 构建单个服务
.\scripts\build-ordered.ps1 -BuildMode single -Service ioedream-access-service -SkipTests
```

## 🏗️ 构建顺序说明

构建脚本会按照以下顺序自动构建和安装所有模块：

### 阶段1: 安装父POM

- `ioedream-microservices-parent`

### 阶段2: 按依赖顺序构建并安装模块

**Layer 1: 核心模块（无依赖）**

- `microservices-common-core`

**Layer 2: 功能模块（依赖core）**

- `microservices-common-entity`
- `microservices-common-storage`
- `microservices-common-security`
- `microservices-common-data`
- `microservices-common-cache`
- `microservices-common-export`
- `microservices-common-workflow`
- `microservices-common-monitor`
- `microservices-common-permission`

**Layer 3: 业务公共模块**

- `microservices-common-business`

**Layer 4: 公共模块聚合器**

- `microservices-common`

**Layer 5: 工具模块**

- `ioedream-db-init`

**Layer 6: 业务服务**

- `ioedream-gateway-service`
- `ioedream-common-service`
- `ioedream-device-comm-service`
- `ioedream-oa-service`
- `ioedream-access-service`
- `ioedream-attendance-service`
- `ioedream-video-service`
- `ioedream-consume-service`
- `ioedream-visitor-service`
- `ioedream-database-service`
- `ioedream-biometric-service`

### 阶段3: 完整项目构建验证

- 最后执行 `mvn clean install` 构建整个项目，验证所有模块

## ⚙️ 参数说明

### build-all.ps1 参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-Clean` | 构建前清理所有target目录 | false |
| `-SkipTests` | 跳过测试 | false |
| `-SkipQuality` | 跳过质量检查（PMD、Checkstyle等） | false |
| `-Verbose` | 显示详细输出 | false |

### build-ordered.ps1 参数

| 参数 | 说明 | 可选值 |
|------|------|--------|
| `-BuildMode` | 构建模式 | `full`, `common`, `services`, `single` |
| `-Service` | 单服务模式时指定服务名 | 任意服务名 |
| `-SkipTests` | 跳过测试 | switch |
| `-SkipQuality` | 跳过质量检查 | switch |
| `-Clean` | 清理后构建 | switch |
| `-Verbose` | 详细输出 | switch |

## 🔧 常见问题

### 1. 构建失败：找不到依赖类

**问题**: 编译时提示找不到 `net.lab1024.sa.common.exception.BusinessException` 等类

**解决方案**:

```powershell
# 确保按顺序构建，先构建 common-core
.\scripts\build-ordered.ps1 -BuildMode common -SkipTests

# 然后再构建整个项目
.\scripts\build-all.ps1 -SkipTests
```

### 2. 构建失败：spring-boot-maven-plugin:repackage 错误

**问题**: 库模块报错 "Unable to find main class"

**解决方案**: 已为所有库模块配置了 `<skip>true</skip>`，如果仍有问题，检查对应模块的 `pom.xml`

### 3. 构建速度慢

**解决方案**:

```powershell
# 跳过测试和质量检查
.\scripts\build-all.ps1 -SkipTests -SkipQuality
```

### 4. 只想重新构建某个服务

**解决方案**:

```powershell
# 构建单个服务（会自动构建依赖）
.\scripts\build-ordered.ps1 -BuildMode single -Service ioedream-access-service -SkipTests
```

## 📊 构建输出说明

构建脚本会显示：

- ✅ **[OK]** - 模块构建成功
- ⚠️ **[SKIP]** - 模块被跳过（目录不存在）
- ❌ **[FAIL]** - 模块构建失败（会显示错误摘要）
- 🔵 **[BUILD]** - 正在构建模块

## 🎯 最佳实践

1. **首次构建**: 使用 `.\scripts\build-all.ps1 -SkipTests` 快速构建
2. **日常开发**: 使用 `.\scripts\build-ordered.ps1 -BuildMode single -Service <服务名> -SkipTests`
3. **CI/CD**: 使用 `.\scripts\build-ordered.ps1 -BuildMode full`（包含测试）
4. **问题排查**: 使用 `-Verbose` 参数查看详细输出

## 📝 注意事项

1. **构建顺序很重要**: 必须按照依赖顺序构建，否则会出现找不到类的错误
2. **Maven本地仓库**: 确保 `~/.m2/repository` 有足够空间
3. **网络连接**: 首次构建需要下载依赖，确保网络畅通
4. **Java版本**: 确保使用 Java 17+

## 🔗 相关文档

- [构建顺序强制标准](../documentation/technical/BUILD_ORDER_MANDATORY_STANDARD.md)
- [CLAUDE.md 规范](../CLAUDE.md)
