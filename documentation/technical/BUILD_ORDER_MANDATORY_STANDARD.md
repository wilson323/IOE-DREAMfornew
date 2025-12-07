# 构建顺序强制标准

**版本**: v1.0.0  
**生效日期**: 2025-12-05  
**优先级**: 🔴 P0 - 强制执行  
**适用范围**: IOE-DREAM 所有微服务

---

## 🎯 核心原则

### 黄金法则
> **microservices-common 必须在任何业务服务构建之前完成构建和安装**

违反此规则将导致：
- ❌ 依赖解析失败
- ❌ IDE无法识别类
- ❌ 编译错误
- ❌ 构建失败

---

## 📋 强制构建顺序

### 标准构建顺序（强制执行）

```
1. microservices-common          ← 必须先构建
   ↓
2. ioedream-gateway-service      ← 基础设施服务
   ↓
3. ioedream-common-service       ← 公共业务服务
   ↓
4. ioedream-device-comm-service  ← 设备通讯服务
   ↓
5. ioedream-oa-service          ← OA服务
   ↓
6. 业务服务（并行构建）
   ├── ioedream-access-service
   ├── ioedream-attendance-service
   ├── ioedream-video-service
   ├── ioedream-consume-service
   └── ioedream-visitor-service
```

---

## 🔧 构建方法

### 方法1: 使用统一构建脚本（推荐）✅

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

**优势**:
- ✅ 自动确保构建顺序
- ✅ 自动验证JAR文件
- ✅ 自动检查关键类
- ✅ 错误提示清晰

---

### 方法2: Maven命令（手动）

```powershell
# 步骤1: 强制先构建 common（必须）
mvn clean install -pl microservices/microservices-common -am -DskipTests

# 步骤2: 验证JAR文件存在
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

# 步骤3: 构建业务服务
mvn clean install -pl microservices/ioedream-access-service -am -DskipTests
```

**注意**: 
- ⚠️ 必须使用 `-am` 参数（also-make），确保依赖模块一起构建
- ⚠️ 必须使用 `install` 而非 `compile`，确保JAR安装到本地仓库

---

### 方法3: IDE构建（需要配置）

#### IntelliJ IDEA
1. **配置Maven Runner**:
   ```
   File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner
   ✅ Delegate IDE build/run actions to Maven
   ✅ Run tests using: Maven
   ```

2. **配置构建顺序**:
   ```
   File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Importing
   ✅ Use Maven wrapper
   ✅ Automatically download: Sources and Documentation
   ```

3. **重新导入项目**:
   ```
   Maven工具窗口 -> Reload All Maven Projects
   ```

#### VS Code
1. **安装Java扩展包**:
   - Extension Pack for Java
   - Maven for Java

2. **配置Maven**:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.import.maven.enabled": true
   }
   ```

3. **重新加载窗口**:
   ```
   Ctrl+Shift+P -> Java: Clean Java Language Server Workspace
   Ctrl+Shift+P -> Reload Window
   ```

---

## 🚨 禁止事项

### ❌ 禁止直接构建业务服务
```powershell
# ❌ 错误：直接构建业务服务
mvn clean install -pl microservices/ioedream-access-service

# ✅ 正确：先构建common，再构建业务服务
mvn clean install -pl microservices/microservices-common -am
mvn clean install -pl microservices/ioedream-access-service -am
```

### ❌ 禁止跳过common构建
```powershell
# ❌ 错误：跳过common构建
mvn clean install -pl microservices/ioedream-access-service -rf microservices/ioedream-access-service

# ✅ 正确：使用统一构建脚本
.\scripts\build-all.ps1 -Service ioedream-access-service
```

### ❌ 禁止使用compile而非install
```powershell
# ❌ 错误：只编译不安装
mvn clean compile -pl microservices/microservices-common

# ✅ 正确：编译并安装到本地仓库
mvn clean install -pl microservices/microservices-common -am
```

---

## 🔍 验证检查

### 构建后验证清单

- [ ] `microservices-common` JAR文件存在于本地仓库
- [ ] JAR文件包含所有关键类
- [ ] IDE可以正确识别依赖
- [ ] 编译无错误
- [ ] 类型可以正确解析

### 验证命令

```powershell
# 检查JAR文件
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"
Test-Path $jarPath

# 检查关键类
jar -tf $jarPath | Select-String "DeviceEntity"

# 验证依赖解析
mvn dependency:tree -pl microservices/ioedream-access-service -Dincludes=net.lab1024.sa:microservices-common
```

---

## 🔄 CI/CD集成

### GitLab CI配置

```yaml
build:common:
  stage: build
  script:
    - echo "=== 构建 microservices-common ==="
    - mvn clean install -pl microservices/microservices-common -am -DskipTests
  artifacts:
    paths:
      - "microservices/microservices-common/target/*.jar"
    expire_in: 1 hour

build:services:
  stage: build
  dependencies:
    - build:common
  script:
    - echo "=== 构建业务服务 ==="
    - mvn clean install -DskipTests
  needs:
    - build:common
```

### GitHub Actions配置

```yaml
- name: Build microservices-common
  run: mvn clean install -pl microservices/microservices-common -am -DskipTests

- name: Build all services
  run: mvn clean install -DskipTests
  needs: build-common
```

---

## 📚 开发规范更新

### 新开发者入职检查清单

- [ ] 阅读本文档
- [ ] 执行 `.\scripts\build-all.ps1` 验证构建流程
- [ ] 配置IDE（参考方法3）
- [ ] 验证依赖解析正常

### 日常开发流程

1. **拉取代码后**:
   ```powershell
   .\scripts\pre-build-check.ps1
   ```

2. **修改common模块后**:
   ```powershell
   mvn clean install -pl microservices/microservices-common -am
   ```

3. **构建业务服务前**:
   ```powershell
   .\scripts\build-all.ps1 -Service <service-name>
   ```

---

## 🛠️ 故障排查

### 问题1: IDE无法识别类

**症状**: IDE显示红色错误，提示类无法解析

**解决方案**:
1. 执行 `.\scripts\build-all.ps1`
2. IDE: File -> Invalidate Caches / Restart
3. Maven: Reload All Maven Projects

### 问题2: Maven构建失败

**症状**: `[ERROR] The import net.lab1024.sa.common.device cannot be resolved`

**解决方案**:
1. 检查common是否已构建: `Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"`
2. 如果不存在，执行: `mvn clean install -pl microservices/microservices-common -am`
3. 重新构建业务服务

### 问题3: JAR文件存在但类找不到

**症状**: JAR文件存在，但IDE仍无法识别类

**解决方案**:
1. 检查JAR文件内容: `jar -tf <jar-path> | Select-String "DeviceEntity"`
2. 如果类不存在，重新构建common
3. 清理IDE缓存并重新导入

---

## 📊 监控和告警

### 构建监控指标

- **构建成功率**: 应 ≥ 95%
- **构建时间**: common模块 < 2分钟
- **依赖解析时间**: < 30秒

### 告警规则

- ⚠️ common构建失败 → 立即告警
- ⚠️ 依赖解析失败 → 立即告警
- ⚠️ 构建时间超过5分钟 → 警告

---

## ✅ 合规检查

### 代码提交前检查

```powershell
# 执行预构建检查
.\scripts\pre-build-check.ps1

# 如果检查失败，自动构建common
if ($LASTEXITCODE -ne 0) {
    Write-Host "自动构建 microservices-common..." -ForegroundColor Yellow
    mvn clean install -pl microservices/microservices-common -am -DskipTests
}
```

### Git预提交钩子

创建 `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# 检查 common 是否已构建
JAR_PATH="$HOME/.m2/repository/net/lab1024/sa/microservices-common/1.0.0/microservices-common-1.0.0.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "❌ microservices-common 未构建！"
    echo "请先执行: mvn clean install -pl microservices/microservices-common -am"
    exit 1
fi

echo "✅ microservices-common 已构建"
exit 0
```

---

## 📝 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|---------|
| v1.0.0 | 2025-12-05 | 初始版本，建立强制构建顺序标准 |

---

**制定人**: IOE-DREAM 架构委员会  
**维护人**: 架构师团队  
**审核人**: 技术负责人  
**生效日期**: 2025-12-05
