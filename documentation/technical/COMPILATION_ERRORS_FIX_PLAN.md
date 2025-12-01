# 编译错误修复计划

## 问题概述

当前 `sa-base` 模块存在大量编译错误，主要问题包括：

1. **包导入错误**：`DataSourceConfig.java` 中可能存在编码问题导致编译错误
2. **重复方法定义**：多个类中存在重复的方法定义
3. **缺失类引用**：引用了不存在的类或包
4. **模块依赖错误**：`sa-base` 模块引用了 `sa-admin` 模块的类

## 错误分类

### 1. 包导入错误
- `DataSourceConfig.java`: 找不到 `jakarta.sql` 包（应该是 `java.sql` 或编码问题）

### 2. 重复方法定义
- `TcpDeviceProtocolAdapter.java`: `formatCommand` 和 `parseResponse` 方法重复
- `AreaService.java`: `validateAreaCode` 和 `getChildrenAreas` 方法重复

### 3. 缺失类引用
- `SmartDeviceDao` - 应该在 `sa-admin` 模块
- `AccessPermissionValidator` - 应该在 `sa-admin` 模块
- `ServiceImpl` (MyBatis-Plus) - 包路径错误
- `AreaService` - 应该在 `sa-admin` 模块
- `SmartPersonService` - 应该在 `sa-admin` 模块
- 多个 DAO 类缺失

### 4. 接口实现不完整
- `SystemErrorCode` 和 `UserErrorCode` 未实现 `getLevel()` 方法
- 多个枚举类未实现 `getDesc()` 方法

## 修复策略

### 阶段1：修复包导入和编码问题
1. 检查并修复 `DataSourceConfig.java` 的编码问题
2. 确保所有导入使用正确的包名（`java.sql` 而非 `jakarta.sql`）

### 阶段2：修复重复方法定义
1. 检查并删除重复的方法定义
2. 确保每个方法签名唯一

### 阶段3：修复缺失类引用
1. 将引用 `sa-admin` 模块的代码移到 `sa-admin` 模块
2. 或者创建接口在 `sa-base` 模块，实现在 `sa-admin` 模块

### 阶段4：修复接口实现
1. 为所有枚举类实现 `getDesc()` 方法
2. 为所有错误码类实现 `getLevel()` 方法

## 执行步骤

1. 先修复编码问题
2. 修复重复方法定义
3. 修复缺失类引用
4. 修复接口实现
5. 重新编译验证

