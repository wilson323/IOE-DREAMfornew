# IOE-DREAM 构建脚本使用指南

## 🚀 快速开始

### 标准构建流程（推荐）

```powershell
# 1. 构建所有服务（自动确保顺序）
.\scripts\build-all.ps1

# 2. 构建指定服务（自动先构建common）
.\scripts\build-all.ps1 -Service ioedream-access-service

# 3. 清理并构建
.\scripts\build-all.ps1 -Clean

# 4. 跳过测试
.\scripts\build-all.ps1 -SkipTests
```

---

## 📋 脚本说明

### build-all.ps1 - 统一构建脚本

**功能**: 强制先构建 `microservices-common`，然后构建其他服务

**参数**:
- `-Clean`: 清理target目录后构建
- `-SkipTests`: 跳过测试
- `-Service <name>`: 只构建指定服务

**示例**:
```powershell
# 构建所有服务
.\scripts\build-all.ps1

# 构建门禁服务（自动先构建common）
.\scripts\build-all.ps1 -Service ioedream-access-service -Clean
```

---

### pre-build-check.ps1 - 预构建检查

**功能**: 检查 `microservices-common` 是否已构建

**使用场景**:
- IDE构建前
- Git提交前
- 手动验证

**示例**:
```powershell
# 检查common是否已构建
.\scripts\pre-build-check.ps1
```

---

## ⚠️ 常见问题

### 问题1: 脚本执行失败

**症状**: PowerShell执行策略错误

**解决**:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 问题2: Maven命令找不到

**症状**: `mvn: command not found`

**解决**:
1. 检查Maven是否安装: `mvn -version`
2. 检查PATH环境变量
3. 使用完整路径: `C:\Program Files\Apache\maven\bin\mvn`

### 问题3: JAR文件验证失败

**症状**: JAR文件存在但类找不到

**解决**:
1. 重新构建common: `mvn clean install -pl microservices/microservices-common -am`
2. 检查JAR文件内容: `jar -tf <jar-path>`
3. 清理Maven本地仓库缓存

---

## 📚 相关文档

- [构建顺序强制标准](../documentation/technical/BUILD_ORDER_MANDATORY_STANDARD.md)
- [根源性解决方案总结](../documentation/technical/ROOT_CAUSE_SOLUTION_SUMMARY.md)
- [编译错误分析报告](../documentation/technical/ACCESS_SERVICE_COMPILATION_ERROR_ROOT_CAUSE_ANALYSIS.md)
