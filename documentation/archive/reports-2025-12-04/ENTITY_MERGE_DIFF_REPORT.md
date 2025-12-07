# Entity差异对比与合并报告

**生成时间**: 2025-12-03
**对比范围**: microservices-common vs ioedream-access-service/advanced/domain/entity

---

## 📊 总体对比结果

| Entity名称 | Common版本 | Service版本 | 差异类型 | 合并策略 |
|-----------|-----------|------------|---------|---------|
| AntiPassbackRecordEntity | ✅ 完整 | 🔶 缺少BaseEntity继承 | 注解差异 | 保留Common |
| AntiPassbackRuleEntity | ✅ 完整 | 🔶 缺少BaseEntity继承 | 注解差异 | 保留Common |
| InterlockRuleEntity | ✅ 完整 | ⚠️ 表名不同 | 表名+字段 | 需合并 |
| InterlockLogEntity | ✅ 完整 | ⚠️ 表名不同 | 表名差异 | 保留Common |
| LinkageRuleEntity | ✅ 完整 | ⚠️ 枚举引用不同 | 字段差异 | 需合并 |
| EvacuationEventEntity | 🔶 不存在 | ✅ 完整 | Common缺失 | 需迁移 |
| EvacuationRecordEntity | ✅ 完整 | ⚠️ 字段差异 | 字段差异 | 需合并 |
| EvacuationPointEntity | ✅ 完整 | ⚠️ 字段差异 | 字段差异 | 需合并 |

---

## 🔍 详细差异分析

### 1️⃣ AntiPassbackRecordEntity

**Common版本**:
- ✅ 继承 `BaseEntity`（包含createTime, updateTime, deletedFlag, version等基础字段）
- ✅ 使用 `@TableId` + `@TableField` 完整注解
- ✅ 表名: `t_access_anti_passback_record`
- ✅ 重写 `getId()` 方法

**Service版本**:
- ❌ 不继承BaseEntity（直接在类中定义基础字段）
- ✅ 使用完整注解
- ✅ 表名: `t_access_anti_passback_record`
- ❌ 额外使用 `@Table(name = "...")` (JPA注解，冗余)

**合并决策**: ✅ **保留Common版本，无需修改**
- Common版本更符合架构规范（继承BaseEntity）
- Service版本的独立字段已在Common中通过BaseEntity包含

---

### 2️⃣ AntiPassbackRuleEntity

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 完整的 `@TableField` 注解
- ✅ 表名: `t_access_anti_passback_rule`
- ✅ 重写 `getId()` 方法

**Service版本**:
- ❌ 不继承BaseEntity
- ✅ 字段定义完整
- ✅ 表名: `t_access_anti_passback_rule`
- ⚠️ 独立定义了createUserId, updateUserId字段

**字段差异**:
```java
// Service独有字段（在Common中应通过BaseEntity处理）
private Long createUserId;  // 应使用 BaseEntity.createBy
private Long updateUserId;  // 应使用 BaseEntity.updateBy
```

**合并决策**: ✅ **保留Common版本，无需修改**
- BaseEntity已包含 createBy/updateBy 字段（语义相同）

---

### 3️⃣ InterlockRuleEntity ⚠️ **需要合并**

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 表名: `t_access_interlock_rule`
- ✅ 字段数: 17个核心字段

**Service版本**:
- ❌ 不继承BaseEntity
- ⚠️ 表名: `access_interlock_rule` （缺少 `t_` 前缀）
- ✅ 字段数: 25个字段（比Common多8个）

**Service独有字段**（需要合并到Common）:
```java
@TableField("timeout_seconds")
private Integer timeoutSeconds;  // 超时时间

@TableField("auto_release")
private Boolean autoRelease;  // 是否自动解除

@TableField("allow_preemption")
private Boolean allowPreemption;  // 是否允许抢占

@TableField("enable_logging")
private Boolean enableLogging;  // 是否启用日志

@TableField("continue_on_failure")
private Boolean continueOnFailure;  // 失败时是否继续

@TableField("max_retry_count")
private Integer maxRetryCount;  // 最大重试次数

@TableField("retry_interval")
private Integer retryInterval;  // 重试间隔

@Version
@TableField("version")
private Integer version;  // 乐观锁版本号
```

**合并决策**: 🔧 **需要将Service独有字段合并到Common**
- 保留Common的表名 `t_access_interlock_rule`
- 添加Service版本的8个独有字段

---

### 4️⃣ InterlockLogEntity

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 表名: `t_access_interlock_log`
- ✅ 完整的字段注解

**Service版本**:
- ❌ 不继承BaseEntity
- ⚠️ 表名: `access_interlock_execution` （完全不同！）
- ✅ 字段定义完整

**关键差异**: 
- **表名完全不同**: Common用 `t_access_interlock_log`，Service用 `access_interlock_execution`
- 这可能代表两个不同的业务表！

**合并决策**: ✅ **保留Common版本，Service版本表名不符合规范**
- 统一使用 `t_access_interlock_log`
- Service版本的 `access_interlock_execution` 表名违反命名规范（缺少t_前缀）

---

### 5️⃣ LinkageRuleEntity ⚠️ **需要处理枚举引用**

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 表名: `t_access_linkage_rule`
- ✅ 使用枚举: `net.lab1024.sa.common.access.enums.LinkageStatus`

**Service版本**:
- ✅ 继承 `BaseEntity`
- ⚠️ 表名: `access_linkage_rule` （缺少 `t_` 前缀）
- ⚠️ 使用枚举: `net.lab1024.sa.access.advanced.domain.enums.LinkageStatus`

**字段差异**:
```java
// Service版本多了这些字段（BaseEntity已包含）
@TableField("created_by")
private Long createdBy;

@TableField("updated_by")
private Long updatedBy;

@Version
@TableField("version")
private Integer version;
```

**合并决策**: ✅ **保留Common版本，修正枚举引用路径即可**
- Common的表名 `t_access_linkage_rule` 符合规范
- Service独有字段已在BaseEntity中包含

---

### 6️⃣ EvacuationEventEntity ⚠️ **Common中不存在，需迁移**

**Common版本**: ❌ **文件不存在**

**Service版本**:
- ✅ 字段完整（43个字段）
- ⚠️ 表名: `access_evacuation_event` （缺少 `t_` 前缀）
- ✅ 包含业务方法（getEvacuationCompletionRate等）

**合并决策**: 🚀 **需要从Service迁移到Common**
- 修正表名为 `t_access_evacuation_event`
- 改为继承 `BaseEntity`
- 移除冗余的基础字段（使用BaseEntity）
- 保留业务方法

---

### 7️⃣ EvacuationRecordEntity

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 表名: `t_access_evacuation_record`
- ✅ 字段完整

**Service版本**:
- ❌ 不继承BaseEntity
- ⚠️ 表名: `access_evacuation_record` （缺少 `t_` 前缀）
- ✅ 包含业务方法

**合并决策**: 🔧 **Common需补充业务方法**
- 保留Common版本的继承结构
- 从Service复制3个业务方法:
  - `getEvacuationDurationMinutes()`
  - `isArrived()`
  - `needMedicalHelp()`
  - `getEvacuationLevelNumber()`

---

### 8️⃣ EvacuationPointEntity

**Common版本**:
- ✅ 继承 `BaseEntity`
- ✅ 表名: `t_access_evacuation_point`
- ✅ 字段完整

**Service版本**:
- ❌ 不继承BaseEntity
- ⚠️ 表名: `access_evacuation_point` （缺少 `t_` 前缀）
- ✅ 包含业务方法

**合并决策**: 🔧 **Common需补充业务方法**
- 保留Common版本的继承结构
- 从Service复制4个业务方法:
  - `getUsageRate()`
  - `isOverThreshold()`
  - `isFull()`
  - `getRemainingCapacity()`

---

## 🔧 合并执行计划

### 必须执行的合并操作

#### ✅ 不需要修改（3个）
1. ✅ AntiPassbackRecordEntity - Common版本完整
2. ✅ AntiPassbackRuleEntity - Common版本完整
3. ✅ InterlockLogEntity - Common版本完整
4. ✅ LinkageRuleEntity - Common版本完整

#### 🔧 需要补充字段（1个）
5. 🔧 **InterlockRuleEntity** - 添加8个独有字段到Common

#### 🔧 需要补充业务方法（2个）
6. 🔧 **EvacuationRecordEntity** - 添加4个业务方法到Common
7. 🔧 **EvacuationPointEntity** - 添加4个业务方法到Common

#### 🚀 需要迁移（1个）
8. 🚀 **EvacuationEventEntity** - 从Service完整迁移到Common（不存在）

---

## ⚠️ 关键注意事项

### 表名规范问题
Service版本的多个Entity使用了不符合规范的表名（缺少`t_`前缀）：
- ❌ `access_interlock_rule` → ✅ `t_access_interlock_rule`
- ❌ `access_interlock_execution` → ✅ `t_access_interlock_log`
- ❌ `access_linkage_rule` → ✅ `t_access_linkage_rule`
- ❌ `access_evacuation_event` → ✅ `t_access_evacuation_event`
- ❌ `access_evacuation_record` → ✅ `t_access_evacuation_record`
- ❌ `access_evacuation_point` → ✅ `t_access_evacuation_point`

**所有合并后的Entity必须使用Common版本的表名（带`t_`前缀）**

### 枚举引用问题
LinkageRuleEntity需要确保使用正确的枚举路径：
- ✅ `net.lab1024.sa.common.access.enums.LinkageStatus`
- ❌ `net.lab1024.sa.access.advanced.domain.enums.LinkageStatus`

### BaseEntity继承
所有Common版本的Entity都正确继承了BaseEntity，这是架构规范要求：
- ✅ 自动包含: createTime, updateTime, createBy, updateBy, deletedFlag, version
- ✅ 自动支持MyBatis-Plus的自动填充和逻辑删除

---

## 📋 下一步操作清单

### Phase 1.2: 合并Entity字段（4个需要修改）

1. ✅ 修改 `InterlockRuleEntity` - 添加8个字段
2. ✅ 修改 `EvacuationRecordEntity` - 添加4个业务方法  
3. ✅ 修改 `EvacuationPointEntity` - 添加4个业务方法
4. ✅ 创建 `EvacuationEventEntity` - 从Service迁移

### Phase 1.3: 删除Service重复目录

```bash
# 删除整个重复Entity目录
删除: D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\advanced\domain\entity\
```

**预期效果**: 编译错误从77,064行降至约40,000行

---

**生成完毕** ✅

