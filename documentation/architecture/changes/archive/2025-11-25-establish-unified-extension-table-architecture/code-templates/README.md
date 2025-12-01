# IOE-DREAM 扩展表代码模板使用说明

**基于现有成功实践的代码模板库**

## 📋 概述

本代码模板库基于IOE-DREAM项目中已验证的成功实践，提供标准化的扩展表代码模板。所有模板都基于现有实体类、DAO、Service的成功实现进行增强和完善，避免从零创建，确保与项目现有架构完全兼容。

## 🎯 模板特色

### ✅ 基于现有成功实践
- **AreaEntity**: 完整的基础实体模板，支持无限层级结构
- **AccessAreaExtEntity**: 成功的扩展表实现，JSON配置避免字段冗余
- **现有DAO模式**: 高效查询、批量操作、JOIN查询优化
- **现有Service模式**: 缓存管理、事务处理、业务逻辑封装

### ✅ 避免代码冗余
- **统一BaseEntity继承**: 避免重复定义审计字段
- **JSON配置字段**: 避免为每个配置项创建独立字段
- **通用业务方法**: 避免重复的业务判断逻辑
- **统一命名规范**: 避免不一致的命名模式

### ✅ 标准化设计
- **四层架构**: Controller→Service→Manager→DAO严格遵循
- **缓存管理**: L1+Caffeine + L2+Redis分层缓存
- **事务处理**: 统一的事务边界和异常处理
- **参数验证**: 统一的参数校验和错误处理

## 📁 模板文件结构

```
code-templates/
├── base-entity-template.java      # 基础实体模板
├── extension-entity-template.java  # 扩展实体模板
├── extension-dao-template.java     # 扩展DAO模板
├── extension-service-template.java # 扩展Service模板
└── README.md                       # 使用说明（本文件）
```

## 🚀 快速开始

### 1. 基础实体模板使用

```bash
# 复制模板
cp base-entity-template.java AreaEntity.java

# 替换占位符（示例：区域模块）
{package} → net.lab1024.sa.base.module.area.domain.entity
{table_name} → area
{EntityName} → Area
{entity_comment} → 区域
{entity_id_field} → area_id
{entity_code_field} → area_code
{entity_name_field} → area_name
{entity_type_field} → area_type
```

### 2. 扩展实体模板使用

```bash
# 复制模板
cp extension-entity-template.java AreaAccessExtEntity.java

# 替换占位符（示例：区域门禁扩展）
{package} → net.lab1024.sa.base.module.area.domain.entity
{base_table} → area
{BaseEntityName} → Area
{module_name} → 门禁
{ModuleName} → Access
{base_entity_comment} → 区域
{base_table_id} → areaId
```

### 3. DAO模板使用

```bash
# 复制模板
cp extension-dao-template.java AreaAccessExtDao.java

# 替换占位符
{package} → net.lab1024.sa.base.module.area.dao
{BaseEntityName} → Area
{ModuleName} → Access
{base_table} → area
{base_table_id} → areaId
{base_entity_comment} → 区域
{module_name} → 门禁
```

### 4. Service模板使用

```bash
# 复制模板
cp extension-service-template.java AreaAccessExtService.java

# 替换占位符
{package} → net.lab1024.sa.base.module.area.service
{BaseEntityName} → Area
{ModuleName} → Access
{base_table} → area
{BaseTableId} → areaId
{base_entity_comment} → 区域
{module_name} → 门禁
```

## 📝 占位符替换说明

### 基础实体占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `{package}` | 实体包路径 | `net.lab1024.sa.base.module.area.domain.entity` |
| `{table_name}` | 数据库表名 | `area` |
| `{EntityName}` | 实体类名（驼峰） | `Area` |
| `{entity_comment}` | 实体中文注释 | `区域` |
| `{entity_id_field}` | 主键字段名 | `area_id` |
| `{entity_code_field}` | 编码字段名 | `area_code` |
| `{entity_name_field}` | 名称字段名 | `area_name` |
| `{entity_type_field}` | 类型字段名 | `area_type` |
| `{entity_type}` | 类型变量名 | `areaType` |
| `{entityCode}` | 编码变量名 | `areaCode` |
| `{entityName}` | 名称变量名 | `areaName` |

### 扩展实体占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `{module_name}` | 模块中文名 | `门禁` |
| `{ModuleName}` | 模块英文名（驼峰） | `Access` |
| `{module}` | 模块变量名（小写） | `access` |
| `{base_table}` | 基础表名 | `area` |
| `{BaseEntityName}` | 基础实体名 | `Area` |
| `{base_entity_comment}` | 基础实体注释 | `区域` |
| `{base_table_id}` | 基础表ID变量名 | `areaId` |
| `{BaseTableId}` | 基础表ID方法名 | `AreaId` |
| `{baseEntityName}` | 基础实体变量名 | `areaName` |
| `{BaseEntityName}` | 基础实体方法名 | `AreaName` |
| `{baseEntityCode}` | 基础实体编码变量名 | `areaCode` |

## 🛠️ 自动化脚本

### 模板替换脚本

```bash
#!/bin/bash
# generate-extension-module.sh
# 使用模板快速生成扩展模块

# 参数配置
BASE_ENTITY_NAME=$1  # 基础实体名（如：Area）
MODULE_NAME=$2      # 模块名（如：Access）
MODULE_CN_NAME=$3   # 模块中文名（如：门禁）
PACKAGE_PATH=$4     # 包路径（如：net.lab1024.sa.base.module.area）

# 生成扩展实体
sed -e "s/{BaseEntityName}/$BASE_ENTITY_NAME/g" \
    -e "s/{ModuleName}/$MODULE_NAME/g" \
    -e "s/{module_name}/$MODULE_CN_NAME/g" \
    -e "s/{package}/$PACKAGE_PATH.domain.entity/g" \
    extension-entity-template.java > ${BASE_ENTITY_NAME}${MODULE_NAME}ExtEntity.java

# 生成DAO
sed -e "s/{BaseEntityName}/$BASE_ENTITY_NAME/g" \
    -e "s/{ModuleName}/$MODULE_NAME/g" \
    -e "s/{module_name}/$MODULE_CN_NAME/g" \
    -e "s/{package}/$PACKAGE_PATH.dao/g" \
    extension-dao-template.java > ${BASE_ENTITY_NAME}${MODULE_NAME}ExtDao.java

# 生成Service
sed -e "s/{BaseEntityName}/$BASE_ENTITY_NAME/g" \
    -e "s/{ModuleName}/$MODULE_NAME/g" \
    -e "s/{module_name}/$MODULE_CN_NAME/g" \
    -e "s/{package}/$PACKAGE_PATH.service/g" \
    extension-service-template.java > ${BASE_ENTITY_NAME}${MODULE_NAME}ExtService.java

echo "扩展模块生成完成：${BASE_ENTITY_NAME}${MODULE_NAME}"
```

### 使用示例

```bash
# 生成区域门禁扩展模块
./generate-extension-module.sh Area Access 门禁 net.lab1024.sa.base.module.area

# 生成设备消费扩展模块
./generate-extension-module.sh Device Consume 消费 net.lab1024.sa.base.module.device

# 生成账户考勤扩展模块
./generate-extension-module.sh Account Attendance 考勤 net.lab1024.sa.base.module.account
```

## 🎨 最佳实践

### 1. 字段设计最佳实践

**✅ 推荐做法**：
```java
// 使用JSON配置避免字段冗余
@TableField("time_restrictions")
private Map<String, Object> timeRestrictions;  // {"workdays":["07:00-09:00"]}

// 提供业务方法封装
public boolean hasTimeRestrictions() {
    return this.timeRestrictions != null && !this.timeRestrictions.isEmpty();
}
```

**❌ 避免做法**：
```java
// 避免为每个配置项创建独立字段
private String workdayStartTime;  // ❌ 冗余
private String workdayEndTime;    // ❌ 冗余
private String weekendStartTime;  // ❌ 冗余
private String weekendEndTime;    // ❌ 冗余
```

### 2. 缓存使用最佳实践

**✅ 推荐做法**：
```java
// 统一的缓存管理
@Resource
private AreaAccessCacheManager cacheManager;

// 缓存优先查询
VO cachedResult = cacheManager.getInfo(areaId);
if (cachedResult != null) {
    return ResponseDTO.ok(cachedResult);
}

// 查询后缓存
cacheManager.setInfo(areaId, result);
```

### 3. 批量操作最佳实践

**✅ 推荐做法**：
```java
// 使用批量查询避免N+1问题
List<Long> areaIds = updateList.stream()
    .map(AreaAccessUpdateForm::getAreaId)
    .collect(Collectors.toList());

Map<Long, AreaEntity> areaMap = areaDao.selectBatchIds(areaIds)
    .stream()
    .collect(Collectors.toMap(AreaEntity::getAreaId, entity -> entity));
```

### 4. 异常处理最佳实践

**✅ 推荐做法**：
```java
// 统一异常处理
try {
    // 业务逻辑
    return ResponseDTO.ok(result);
} catch (Exception e) {
    log.error("操作失败，参数: {}", param, e);
    return ResponseDTO.error("System", "系统异常");
}

// 抛出业务异常
if (invalidCondition) {
    throw new SmartException("业务逻辑错误");
}
```

## 🔄 版本更新

### v1.0.0 (2025-11-25)
- 基于现有成功实践创建基础模板
- 支持基础实体+扩展表设计模式
- 包含完整的DAO、Service层模板
- 提供自动化生成脚本

### 后续版本规划
- 支持Controller层模板
- 添加单元测试模板
- 支持更多业务场景模板
- 集成代码生成工具

## 📞 技术支持

如有模板使用问题，请联系：
- **技术团队**: SmartAdmin架构治理委员会
- **文档维护**: 扩展表架构规范项目组
- **问题反馈**: 项目Git Issues

---

**重要提醒**: 本模板库基于现有成功实践，所有模板都已经过验证。使用时请确保与项目现有架构保持一致，避免不必要的定制化修改。