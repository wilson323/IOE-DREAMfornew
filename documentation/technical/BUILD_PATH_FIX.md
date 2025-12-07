# 构建路径问题修复指南

## 问题描述

**错误信息**:
```
The project was not built since its build path is incomplete. 
Cannot find the class file for EmployeeEntity. 
Fix the build path then try building this project
```

**原因分析**:
- `ioedream-attendance-service` 依赖 `microservices-common` 模块中的 `EmployeeEntity` 类
- `microservices-common` 模块未正确构建或未安装到本地Maven仓库
- IDE的类路径缓存未刷新

## 解决方案

### 方案1: 命令行构建（推荐）

**步骤1**: 构建 `microservices-common` 模块
```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests
```

**步骤2**: 编译 `ioedream-attendance-service`
```powershell
cd D:\IOE-DREAM\microservices\ioedream-attendance-service
mvn clean compile -DskipTests
```

**步骤3**: 使用自动化脚本
```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\fix-attendance-build.ps1"
```

### 方案2: IDE刷新（IntelliJ IDEA）

1. **刷新Maven项目**
   - 右键点击项目根目录
   - 选择 `Maven` -> `Reload Project`

2. **清理IDE缓存**
   - `File` -> `Invalidate Caches / Restart`
   - 选择 `Invalidate and Restart`

3. **重新导入项目**
   - `File` -> `Project Structure`
   - 检查 `Modules` 中是否正确识别了所有模块
   - 检查 `Libraries` 中是否包含 `microservices-common`

### 方案3: 完整重新构建

**在项目根目录执行**:
```powershell
cd D:\IOE-DREAM\microservices
mvn clean install -DskipTests
```

这会按顺序构建所有模块，确保依赖关系正确。

## 验证步骤

### 1. 检查类文件是否存在
```powershell
Test-Path "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\hr\entity\EmployeeEntity.java"
```

### 2. 检查Maven本地仓库
```powershell
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"
```

### 3. 检查依赖关系
```powershell
cd D:\IOE-DREAM\microservices\ioedream-attendance-service
mvn dependency:tree -Dincludes=net.lab1024.sa:microservices-common
```

## 相关文件位置

- **EmployeeEntity**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/hr/entity/EmployeeEntity.java`
- **EmployeeDao**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/hr/dao/EmployeeDao.java`
- **pom.xml**: `microservices/ioedream-attendance-service/pom.xml`

## 常见问题

### Q1: 构建后IDE仍然报错？
**A**: 执行IDE缓存清理（方案2的步骤2）

### Q2: Maven构建成功但IDE找不到类？
**A**: 检查IDE的Maven设置，确保使用项目中的Maven配置

### Q3: 多个模块都有类似问题？
**A**: 执行完整重新构建（方案3）

## 预防措施

1. **构建顺序**: 始终先构建 `microservices-common`，再构建其他服务
2. **依赖管理**: 确保所有服务的 `pom.xml` 中正确声明了对 `microservices-common` 的依赖
3. **IDE同步**: 修改 `pom.xml` 后及时刷新Maven项目

## 相关文档

- [CLAUDE.md - 全局架构规范](../../CLAUDE.md)
- [Maven构建指南](./Maven构建指南.md)

---

**最后更新**: 2025-12-02
**维护人**: IOE-DREAM 架构团队
