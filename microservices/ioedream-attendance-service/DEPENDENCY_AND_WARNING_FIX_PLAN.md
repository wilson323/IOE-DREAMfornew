# 依赖问题和警告修复计划

## 问题分析

### 1. 依赖问题
- **问题**：`net.lab1024.sa.common` 无法解析
- **原因**：`microservices-common` 模块未构建或未安装到本地 Maven 仓库
- **解决方案**：
  1. 先构建 `microservices-common` 模块：`mvn clean install -DskipTests`
  2. 然后刷新 `ioedream-attendance-service` 项目依赖

### 2. 警告问题分类

#### A. 未使用的导入和变量
- 未使用的导入：需要删除
- 未使用的字段：可以删除或标记为 `@SuppressWarnings("unused")`
- 未使用的局部变量：删除或使用

#### B. 废弃方法调用
- `deleteBatchIds()` - MyBatis-Plus 废弃方法，需要使用新方法
- `BigDecimal.divide()` 使用 `ROUND_HALF_UP` - Java 9+ 已废弃，使用 `RoundingMode.HALF_UP`
- `Schema.required()` - OpenAPI 废弃方法，使用新注解

#### C. BaseEntity 方法问题
- `setCreateTime()`, `setUpdateTime()`, `setDeletedFlag()` 等方法应该由 Lombok 自动生成
- 如果编译错误，可能需要检查 Lombok 配置

## 修复步骤

### 步骤1：构建 common 模块
```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests
```

### 步骤2：修复废弃方法
- 替换 `deleteBatchIds()` 为新的批量删除方法
- 替换 `BigDecimal.divide()` 的废弃参数
- 替换 `Schema.required()` 为新的注解方式

### 步骤3：清理未使用的导入和变量
- 删除未使用的导入
- 删除或注释未使用的变量

### 步骤4：修复 BaseEntity 相关问题
- 确保所有实体类正确继承 BaseEntity
- 检查 Lombok 配置是否正确

### 步骤5：全局一致性检查
- 统一导入路径
- 统一代码风格
- 避免冗余代码

## 优先级

1. **高优先级**：依赖问题（影响编译）
2. **中优先级**：BaseEntity 方法问题（影响功能）
3. **低优先级**：警告（不影响编译和运行）

## 预计工作量

- 依赖问题：30分钟
- 废弃方法修复：1小时
- 清理警告：1小时
- 全局一致性：1小时
- 总计：约3-4小时

