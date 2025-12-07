# 编译顺序问题快速修复指南

**问题**: 编译顺序导致的"找不到符号"错误  
**解决方案**: 先编译 `microservices-common`，再编译 `ioedream-common-service`

---

## ⚡ 快速修复（3步）

### 步骤1: 编译 microservices-common

```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests -U
```

**关键**: 必须使用 `install` 而不是 `compile`，这样才能安装到本地Maven仓库

### 步骤2: 验证安装成功

```powershell
# 检查本地仓库
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common"
if (Test-Path $jarPath) {
    Write-Host "✓ 已安装" -ForegroundColor Green
} else {
    Write-Host "✗ 未安装" -ForegroundColor Red
}
```

### 步骤3: 编译 ioedream-common-service

```powershell
cd D:\IOE-DREAM\microservices\ioedream-common-service
mvn clean compile -DskipTests -U
```

---

## 🚀 或使用自动化脚本

```powershell
cd D:\IOE-DREAM
.\scripts\fix-compilation-errors.ps1
```

脚本会自动按正确顺序编译所有模块。

---

## ❓ 为什么是编译顺序问题？

1. `ioedream-common-service` 依赖 `microservices-common`
2. `UserDetailVO` 类在 `microservices-common` 中定义
3. 如果 `microservices-common` 未先安装，编译时找不到类定义
4. 即使类定义正确，也会报"找不到符号"错误

**解决**: 先 `mvn install` 基础模块，再编译依赖它的模块。

---

**详细文档**: [COMPILATION_ORDER_FIX.md](./COMPILATION_ORDER_FIX.md)
