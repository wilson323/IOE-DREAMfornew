# 方案C：实体类迁移实施计划

> **实施日期**: 2025-01-30  
> **方案类型**: 方案C - 重新定义模块职责，明确划分标准  
> **实施目标**: 将所有实体类统一迁移到 `microservices-common-entity` 模块

---

## 📋 一、实施目标

### 1.1 模块职责重新定义

**microservices-common-entity**:

- **定位**: 所有实体类的统一管理模块
- **职责**:
  - 基础实体（BaseEntity等）
  - 系统级实体（SystemConfigEntity, SystemDictEntity等）
  - 业务实体（AreaEntity, DeviceEntity, UserEntity, EmployeeEntity等）
- **原则**: 所有实体类都应在此模块中

**microservices-common-business**:

- **定位**: 业务公共组件模块
- **职责**:
  - DAO接口（数据访问层）
  - Manager类（业务编排层）
  - Service接口（服务接口层）
- **原则**: 不包含实体类，只包含业务逻辑组件

### 1.2 实体类划分标准

| 实体类型 | 归属模块 | 示例 |
|---------|---------|------|
| **基础实体** | `microservices-common-entity` | BaseEntity |
| **系统级实体** | `microservices-common-entity` | SystemConfigEntity, SystemDictEntity |
| **业务实体** | `microservices-common-entity` | AreaEntity, DeviceEntity, UserEntity, EmployeeEntity |
| **领域实体** | `microservices-common-entity` | 所有领域实体 |

**划分原则**:

- ✅ 所有实体类统一在 `microservices-common-entity` 中管理
- ✅ `microservices-common-business` 不包含实体类
- ✅ 实体类按业务域组织（organization, system, monitor等）

---

## 📊 二、迁移清单

### 2.1 当前实体类分布

**microservices-common-entity**（3个实体类）:

1. `BaseEntity` - 基础实体
2. `SystemConfigEntity` - 系统配置实体
3. `EmployeeEntity` - 员工实体

**microservices-common-business**（14个实体类，需要迁移）:

#### 组织架构相关实体（7个）

1. `AreaEntity` - 区域实体
2. `DeviceEntity` - 设备实体
3. `UserEntity` - 用户实体
4. `AreaDeviceEntity` - 区域设备关联实体
5. `AreaUserEntity` - 区域用户关联实体
6. `AreaAccessExtEntity` - 区域门禁扩展实体
7. `AntiPassbackRecordEntity` - 防潜回记录实体

#### 其他业务实体（7个）

8. `MenuEntity` - 菜单实体
9. `SystemDictEntity` - 字典实体
10. `AlertEntity` - 告警实体
11. `AlertRuleEntity` - 告警规则实体
12. `NotificationEntity` - 通知实体
13. `NotificationConfigEntity` - 通知配置实体
14. `UserPreferenceEntity` - 用户偏好实体

### 2.2 迁移后的实体类分布（目标状态）

**microservices-common-entity**（17个实体类）:

- 基础实体：BaseEntity
- 系统级实体：SystemConfigEntity, SystemDictEntity
- 组织架构实体：AreaEntity, DeviceEntity, UserEntity, AreaDeviceEntity, AreaUserEntity, AreaAccessExtEntity, AntiPassbackRecordEntity
- 业务实体：EmployeeEntity, MenuEntity, AlertEntity, AlertRuleEntity, NotificationEntity, NotificationConfigEntity, UserPreferenceEntity

**microservices-common-business**（0个实体类）:

- 只包含DAO、Manager、Service接口等业务组件

---

## 🔧 三、实施步骤

### 步骤1: 创建目标目录结构

在 `microservices-common-entity` 中创建对应的包结构：

```
microservices-common-entity/src/main/java/net/lab1024/sa/common/
├── entity/                           # 已存在
│   └── BaseEntity.java              # 已存在
├── organization/
│   └── entity/                      # 需要创建
│       ├── AreaEntity.java          # 从 common-business 迁移
│       ├── DeviceEntity.java
│       ├── UserEntity.java
│       ├── AreaDeviceEntity.java
│       ├── AreaUserEntity.java
│       ├── AreaAccessExtEntity.java
│       └── AntiPassbackRecordEntity.java
├── menu/
│   └── entity/                      # 需要创建
│       └── MenuEntity.java
├── monitor/
│   └── domain/
│       └── entity/                  # 需要创建
│           ├── AlertEntity.java
│           ├── AlertRuleEntity.java
│           └── NotificationEntity.java
├── notification/
│   └── domain/
│       └── entity/                  # 需要创建
│           └── NotificationConfigEntity.java
├── preference/
│   └── entity/                      # 需要创建
│       └── UserPreferenceEntity.java
└── system/
    └── domain/
        └── entity/                  # 已存在
            ├── SystemConfigEntity.java  # 已存在
            └── SystemDictEntity.java    # 从 common-business 迁移
```

### 步骤2: 迁移实体类文件

**迁移原则**:

- ✅ 保持包路径结构不变（`net.lab1024.sa.common.organization.entity` 等）
- ✅ 保持文件内容不变（只移动文件，不修改代码）
- ✅ 保持import语句不变

**迁移命令**（示例）:

```powershell
# 组织架构实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\organization\entity\*.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\organization\entity\"

# 菜单实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\menu\entity\*.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\menu\entity\"

# 监控实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\monitor\domain\entity\*.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\monitor\domain\entity\"

# 通知实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\notification\domain\entity\*.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\notification\domain\entity\"

# 偏好实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\preference\entity\*.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\preference\entity\"

# 系统字典实体类迁移
Move-Item "microservices-common-business\src\main\java\net\lab1024\sa\common\system\domain\entity\SystemDictEntity.java" "microservices-common-entity\src\main\java\net\lab1024\sa\common\system\domain\entity\"
```

### 步骤3: 更新依赖关系

**需要更新的pom.xml**:

- ✅ `microservices-common-entity/pom.xml`: 不需要更改（实体类本身不依赖其他模块）
- ✅ `microservices-common-business/pom.xml`: 保持对 `microservices-common-entity` 的依赖（DAO需要访问实体类）
- ✅ 业务服务的pom.xml: 需要显式声明 `microservices-common-entity` 依赖

### 步骤4: 验证和测试

**验证步骤**:

1. 编译 `microservices-common-entity` 模块
2. 编译 `microservices-common-business` 模块
3. 编译所有业务服务模块
4. 运行单元测试
5. 验证依赖关系（运行审计脚本）

---

## 📝 四、详细迁移清单

### 4.1 组织架构实体类（7个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| AreaEntity | `common-business/.../organization/entity/AreaEntity.java` | `common-entity/.../organization/entity/AreaEntity.java` | ⏳ 待迁移 |
| DeviceEntity | `common-business/.../organization/entity/DeviceEntity.java` | `common-entity/.../organization/entity/DeviceEntity.java` | ⏳ 待迁移 |
| UserEntity | `common-business/.../organization/entity/UserEntity.java` | `common-entity/.../organization/entity/UserEntity.java` | ⏳ 待迁移 |
| AreaDeviceEntity | `common-business/.../organization/entity/AreaDeviceEntity.java` | `common-entity/.../organization/entity/AreaDeviceEntity.java` | ⏳ 待迁移 |
| AreaUserEntity | `common-business/.../organization/entity/AreaUserEntity.java` | `common-entity/.../organization/entity/AreaUserEntity.java` | ⏳ 待迁移 |
| AreaAccessExtEntity | `common-business/.../organization/entity/AreaAccessExtEntity.java` | `common-entity/.../organization/entity/AreaAccessExtEntity.java` | ⏳ 待迁移 |
| AntiPassbackRecordEntity | `common-business/.../organization/entity/AntiPassbackRecordEntity.java` | `common-entity/.../organization/entity/AntiPassbackRecordEntity.java` | ⏳ 待迁移 |

### 4.2 菜单实体类（1个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| MenuEntity | `common-business/.../menu/entity/MenuEntity.java` | `common-entity/.../menu/entity/MenuEntity.java` | ⏳ 待迁移 |

### 4.3 监控实体类（3个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| AlertEntity | `common-business/.../monitor/domain/entity/AlertEntity.java` | `common-entity/.../monitor/domain/entity/AlertEntity.java` | ⏳ 待迁移 |
| AlertRuleEntity | `common-business/.../monitor/domain/entity/AlertRuleEntity.java` | `common-entity/.../monitor/domain/entity/AlertRuleEntity.java` | ⏳ 待迁移 |
| NotificationEntity | `common-business/.../monitor/domain/entity/NotificationEntity.java` | `common-entity/.../monitor/domain/entity/NotificationEntity.java` | ⏳ 待迁移 |

### 4.4 通知实体类（1个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| NotificationConfigEntity | `common-business/.../notification/domain/entity/NotificationConfigEntity.java` | `common-entity/.../notification/domain/entity/NotificationConfigEntity.java` | ⏳ 待迁移 |

### 4.5 偏好实体类（1个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| UserPreferenceEntity | `common-business/.../preference/entity/UserPreferenceEntity.java` | `common-entity/.../preference/entity/UserPreferenceEntity.java` | ⏳ 待迁移 |

### 4.6 系统实体类（1个）

| 实体类 | 源路径 | 目标路径 | 状态 |
|--------|--------|---------|------|
| SystemDictEntity | `common-business/.../system/domain/entity/SystemDictEntity.java` | `common-entity/.../system/domain/entity/SystemDictEntity.java` | ⏳ 待迁移 |

---

## ✅ 五、执行检查清单

### 迁移前检查

- [ ] 备份当前代码
- [ ] 确认所有实体类列表
- [ ] 确认目标目录结构
- [ ] 确认依赖关系

### 迁移执行

- [ ] 创建目标目录结构
- [ ] 迁移组织架构实体类（7个）
- [ ] 迁移菜单实体类（1个）
- [ ] 迁移监控实体类（3个）
- [ ] 迁移通知实体类（1个）
- [ ] 迁移偏好实体类（1个）
- [ ] 迁移系统实体类（1个）

### 迁移后验证

- [ ] 编译 `microservices-common-entity` 模块
- [ ] 编译 `microservices-common-business` 模块
- [ ] 编译所有业务服务模块
- [ ] 运行单元测试
- [ ] 验证依赖关系（运行审计脚本）
- [ ] 更新文档

---

## 📚 六、相关文档更新

### 6.1 需要更新的文档

1. **CLAUDE.md**
   - 更新模块职责定义
   - 明确实体类分布规则

2. **COMMON_LIBRARY_SPLIT.md**
   - 更新模块说明
   - 明确实体类统一在 `microservices-common-entity` 中

3. **GLOBAL_ENTITY_DISTRIBUTION_ROOT_CAUSE_ANALYSIS.md**
   - 更新状态为"已执行方案C"
   - 记录迁移结果

4. **audit-dependencies.ps1**
   - 修正推断逻辑，使其符合新的实体类分布

---

## 🚀 七、风险评估和回滚计划

### 7.1 风险评估

**风险点**:

- ⚠️ 迁移过程中可能出现编译错误
- ⚠️ 依赖关系更新可能遗漏某些服务
- ⚠️ 测试覆盖不足可能导致运行时错误

**缓解措施**:

- ✅ 分阶段迁移（先迁移一个包，验证后再继续）
- ✅ 充分测试每个阶段
- ✅ 保持备份以便回滚

### 7.2 回滚计划

如果迁移过程中出现问题，可以：

1. 使用Git回滚到迁移前状态
2. 恢复备份文件
3. 重新执行迁移步骤

---

**制定人**: IOE-DREAM 架构委员会  
**制定日期**: 2025-01-30  
**状态**: ⏳ 待执行  
**版本**: v1.0.0
