# IOE-DREAM 本地CI构建系统

这个本地CI构建系统替代了GitHub Actions，支持在离线或内网环境中进行完整的构建、测试和部署。

## 🚀 快速开始

### PowerShell脚本 (推荐)

#### 快速构建
```powershell
# 构建所有服务
.\scripts\quick-build.ps1

# 构建指定服务
.\scripts\quick-build.ps1 -Service ioedream-access-service

# 清理后构建
.\scripts\quick-build.ps1 -Clean
```

#### 完整CI构建
```powershell
# 标准构建（包含测试和质量检查）
.\scripts\local-ci-build.ps1

# 跳过测试和质量检查
.\scripts\local-ci-build.ps1 -SkipTests -SkipQualityGate

# 构建指定服务
.\scripts\local-ci-build.ps1 -Service ioedream-access-service

# 清理构建
.\scripts\local-ci-build.ps1 -Clean

# 查看帮助
.\scripts\local-ci-build.ps1 -Help
```

### Windows批处理脚本

```cmd
# 构建所有服务
scripts\build-all.bat

# 清理并构建所有服务
scripts\build-all.bat -clean

# 构建指定服务
scripts\build-all.bat ioedream-access-service

# 查看帮助
scripts\build-all.bat -h
```

### 直接使用Maven

```bash
# 构建所有服务（跳过测试）
cd microservices && mvn clean install -DskipTests -Dpmd.skip=true

# 构建指定服务
cd microservices && mvn clean install -pl ioedream-access-service -am -DskipTests -Dpmd.skip=true

# 包含测试的完整构建
cd microservices && mvn clean install
```

## 📋 构建选项

### 常用参数

| 参数 | PowerShell | 批处理 | 说明 |
|------|------------|--------|------|
| 清理构建 | `-Clean` | `-clean` | 清理target目录后构建 |
| 指定服务 | `-Service <name>` | `<service-name>` | 只构建指定服务 |
| 跳过测试 | `-SkipTests` | N/A | 跳过单元测试执行 |
| 跳过PMD | `-SkipPMD` | N/A | 跳过代码质量检查 |
| 仅编译 | `-OnlyCompile` | N/A | 仅编译，不执行后续步骤 |
| 显示帮助 | `-Help` | `-h` | 显示使用帮助 |

### 构建场景

#### 1. 开发时快速构建
```powershell
# 快速验证代码编译
.\scripts\quick-build.ps1 -Service ioedream-access-service
```

#### 2. 提交前完整验证
```powershell
# 包含测试和质量检查
.\scripts\local-ci-build.ps1 -Service ioedream-access-service
```

#### 3. 生产环境构建
```powershell
# 清理环境，完整构建
.\scripts\local-ci-build.ps1 -Clean
```

#### 4. 批量构建多个服务
```powershell
# 构建核心服务
.\scripts\quick-build.ps1 -Service ioedream-access-service
.\scripts\quick-build.ps1 -Service ioedream-attendance-service
.\scripts\quick-build.ps1 -Service ioedream-consume-service
```

## 🔧 构建配置

### 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **内存**: 建议8GB以上（完整构建）
- **磁盘空间**: 建议10GB以上

### 构建优化

#### 快速构建优化
- 跳过测试 (`-DskipTests`)
- 跳过PMD检查 (`-Dpmd.skip=true`)
- 使用Maven并行构建 (`mvn -T 4`)
- 使用Maven Daemon (`mvnd`)

#### 内存优化
```bash
# 增加Maven内存
export MAVEN_OPTS="-Xmx2g -Xms1g"
```

#### 网络优化
```xml
<!-- 在settings.xml中配置国内镜像 -->
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

## 📊 构建产物

构建完成后，会在以下位置生成产物：

### JAR文件
```
microservices/
├── {service-name}/target/{service-name}-1.0.0.jar
└── {service-name}/target/{service-name}-1.0.0.jar.original
```

### 测试报告
```
microservices/
└── {service-name}/target/surefire-reports/
    └── TEST-*.xml
```

### 代码覆盖率报告
```
microservices/
└── {service-name}/target/site/jacoco/
    └── index.html
```

### PMD报告
```
microservices/
└── {service-name}/target/pmd.xml
```

## 🚨 常见问题

### 构建失败排查

1. **内存不足**
   ```bash
   # 增加Maven内存
   export MAVEN_OPTS="-Xmx4g -Xms2g"
   ```

2. **网络问题**
   ```bash
   # 使用离线模式
   mvn -o install
   ```

3. **依赖冲突**
   ```bash
   # 查看依赖树
   mvn dependency:tree
   ```

4. **权限问题**
   ```bash
   # 在Windows上以管理员身份运行PowerShell
   ```

### 性能优化建议

1. **使用SSD存储** - 显著提升I/O性能
2. **增加内存** - 减少GC停顿
3. **使用Maven并行构建** - `-T 4` 或 `-T 1C`
4. **配置Maven本地仓库** - 使用SSD存储

## 📝 构建脚本说明

### quick-build.ps1
- **用途**: 快速编译验证
- **特点**: 速度快，输出简洁
- **适用**: 开发时快速验证

### local-ci-build.ps1
- **用途**: 完整CI构建
- **特点**: 包含测试、质量检查、报告生成
- **适用**: 提交前验证、持续集成

### build-all.bat
- **用途**: Windows批处理构建
- **特点**: 兼容性好，无需PowerShell
- **适用**: CI/CD环境、自动化部署

## 🔄 替代GitHub Actions

这个本地CI系统完全替代了GitHub Actions的功能：

| GitHub Actions功能 | 本地CI替代方案 |
|-------------------|---------------|
| 工作流触发 | 手动执行或CI触发 |
| 构建步骤 | Maven构建流程 |
| 测试执行 | Maven Surefire插件 |
| 代码质量 | PMD、Checkstyle插件 |
| 产物上传 | 本地文件系统 |
| 环境变量 | 系统环境变量 |
| 并行执行 | Maven并行构建 |

## 🎯 下一步

1. **集成到IDE** - 配置IDE构建按钮
2. **设置CI服务器** - Jenkins/GitLab CI等
3. **自动化部署** - 结合部署脚本
4. **监控和报警** - 构建失败通知