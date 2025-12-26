# Entity管理规范文档

**版本**: v1.0.0
**发布日期**: 2025-12-26
**适用范围**: IOE-DREAM所有微服务模块
**状态**: 生效

---

## 📋 文档概述

### 目的
本文档建立IOE-DREAM项目的Entity管理标准规范，确保Entity类的设计、迁移和管理遵循统一的企业级标准。

### 适用对象
- **架构师**: 系统架构设计和Entity边界划分
- **后端开发**: Entity类编写和维护
- **代码审查**: Entity合规性审查
- **测试工程师**: Entity相关测试用例设计

---

## 🏗️ Entity管理架构原则

### 核心原则

#### 原则1: 统一管理 > 重复定义
**规则**: 所有跨服务共享的Entity必须统一管理在`microservices-common-entity`模块

**理由**:
- ✅ 消除重复：避免业务服务间Entity重复定义
- ✅ 保证一致性：所有服务使用相同的Entity定义
- ✅ 简化维护：Entity字段变更只需修改一处

**反例**:
```
❌ access-service中有UserEntity
❌ attendance-service中也有UserEntity
❌ 两者字段定义不一致
```

**正例**:
```
✅ UserEntity统一在microservices-common-entity
✅ access-service依赖common-entity使用UserEntity
✅ attendance-service依赖common-entity使用UserEntity
```

---

#### 原则2: 业务专属 > 强制统一
**规则**: 仅被单一业务服务使用的Entity应保留在该服务模块内

**识别标准**:
- Entity仅被一个服务的DAO/Manager/Service使用
- Entity包含业务特定的复杂逻辑
- Entity表名使用业务前缀（如`t_consume_*`、`t_access_*`）

**示例**:
```
✅ ConsumeOfflineOrderEntity (仅consume-service使用)
   → 保留在consume-service模块内

✅ AccessAntiPassbackEntity (仅access-service使用)
   → 保留在access-service模块内
```

---

#### 原则3: 纯数据模型 > 业务逻辑
**规则**: Entity类应保持纯数据模型，避免包含业务逻辑

**Entity应包含**:
- ✅ 数据字段（使用Lombok @Data）
- ✅ 字段验证注解（@NotNull, @Size等）
- ✅ MyBatis-Plus注解（@TableName, @TableId等）
- ✅ Swagger文档注解（@Schema）

**Entity不应包含**:
- ❌ 复杂业务计算方法（如calculateOvertimePay()）
- ❌ 数据库访问操作（如insert(), update()）
- ❌ 服务调用（如gatewayServiceClient.call()）
- ❌ 静态工具方法

**正例**:
```java
@Data
@TableName("t_work_shift")
@Schema(description = "班次实体")
public class WorkShiftEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "班次ID")
    private Long shiftId;

    @Schema(description = "班次名称")
    @NotBlank @Size(max = 100)
    private String shiftName;

    @Schema(description = "工作开始时间")
    @NotNull
    private LocalTime workStartTime;

    // ✅ 仅包含数据字段，无业务逻辑
}
```

**反例**:
```java
@Data
@TableName("t_work_shift")
public class WorkShiftEntity extends BaseEntity {

    // ... 字段定义

    // ❌ 错误：包含业务计算方法
    public BigDecimal calculateOvertimePay(BigDecimal hours) {
        return hours.multiply(overtimeRate);
    }

    // ❌ 错误：包含数据库操作
    public void saveToDatabase() {
        this.insert();
    }

    // ❌ 错误：包含静态工具方法
    public static LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr);
    }
}
```

---

## 📊 Entity分类标准

### 分类决策树

```
是否被多个业务服务使用？
│
├─ 是 → 是否属于公共基础数据？
│       │
│       ├─ 是 → 【公共Entity】→ microservices-common-entity
│       │        例: UserEntity, DepartmentEntity, AreaEntity
│       │
│       └─ 否 → 【共享业务Entity】→ microservices-common-entity
│                例: DeviceEntity, ConsumeAccountEntity
│
└─ 否 → 是否属于跨模块业务概念？
        │
        ├─ 是 → 【领域共享Entity】→ microservices-common-entity
        │        例: EmployeeEntity（考勤+消费共用）
        │
        └─ 否 → 【业务专属Entity】→ 保留在业务服务模块内
                 例: ConsumeOfflineOrderEntity（仅消费服务）
```

---

### 类型1: 公共Entity（Core Entities）

**定义**: 项目级别的基础数据实体，被几乎所有业务服务使用

**存放位置**: `microservices-common-entity/src/main/java/net/lab1024/sa/common/entity/`

**示例**:
```java
// 用户管理
net.lab1024.sa.common.entity.auth.UserEntity
net.lab1024.sa.common.entity.auth.DepartmentEntity

// 组织架构
net.lab1024.sa.common.entity.organization.AreaEntity
net.lab1024.sa.common.entity.organization.AreaDeviceRelationEntity

// 系统配置
net.lab1024.sa.common.entity.system.ConfigEntity
net.lab1024.sa.common.entity.system.DictEntity
```

**表命名规范**: `t_common_*`

**迁移标准**:
- 被≥3个业务服务依赖
- 属于系统级基础数据
- 字段变更影响多个模块

---

### 类型2: 共享业务Entity（Shared Business Entities）

**定义**: 被多个业务服务共享使用的业务领域实体

**存放位置**: `microservices-common-entity/src/main/java/net/lab1024/sa/common/entity/{module}/`

**示例**:
```java
// 消费管理（被多个服务使用账户信息）
net.lab1024.sa.common.entity.consume.ConsumeAccountEntity
net.lab1024.sa.common.entity.consume.ConsumeAccountKindEntity
net.lab1024.sa.common.entity.consume.ConsumeSubsidyTypeEntity
net.lab1024.sa.common.entity.consume.ConsumeSubsidyAccountEntity
net.lab1024.sa.common.entity.consume.ConsumeTransactionEntity
net.lab1024.sa.common.entity.consume.ConsumeDeviceEntity

// 设备管理（被多个业务服务使用）
net.lab1024.sa.common.entity.device.DeviceEntity
```

**表命名规范**:
- `t_{module}_*` (如`t_consume_*`, `t_access_*`)

**迁移标准**:
- 被2-3个业务服务依赖
- 属于核心业务概念
- 需要跨服务数据一致

---

### 类型3: 业务专属Entity（Service-Specific Entities）

**定义**: 仅被单一业务服务使用的专属实体

**存放位置**: `{service}/src/main/java/net/lab1024/sa/{service}/entity/`

**示例**:
```java
// 消费服务专属实体
net.lab1024.sa.consume.entity.ConsumeOfflineOrderEntity       // 离线消费订单
net.lab1024.sa.consume.entity.ConsumeRechargeEntity           // 充值记录
net.lab1024.sa.consume.entity.ConsumeRefundEntity             // 退款记录
net.lab1024.sa.consume.entity.ConsumeSubsidyGrantEntity       // 补贴发放记录
net.lab1024.sa.consume.entity.ConsumeSetmealEntity            // 套餐管理
net.lab1024.sa.consume.entity.ConsumeDiscountEntity           // 折扣规则
net.lab1024.sa.consume.entity.ConsumeReportEntity             // 报表配置

// 门禁服务专属实体
net.lab1024.sa.access.entity.AccessAntiPassbackEntity         // 反潜回记录
net.lab1024.sa.access.entity.AccessPermissionTemplateEntity   // 权限模板
```

**表命名规范**: `t_{service}_*`

**保留标准**:
- 仅被1个业务服务使用
- 包含服务特定业务逻辑
- 表结构变化不影响其他服务

---

## 🔄 Entity迁移流程

### 迁移前置检查

在执行Entity迁移前，必须完成以下检查：

#### 检查清单

**Step 1: 依赖分析**
```bash
# 使用IDE或脚本检查Entity被哪些文件导入
grep -r "import.*{EntityName}" microservices/ --include="*.java"
```

**Step 2: 影响范围评估**
- [ ] 确认Entity被哪些服务使用
- [ ] 评估字段变更影响范围
- [ ] 确认数据库表是否需要迁移

**Step 3: 测试覆盖**
- [ ] 确认相关单元测试已更新
- [ ] 确认集成测试已更新
- [ ] 执行完整测试套件验证

---

### 迁移执行步骤

#### 迁移至common-entity

**前提**: Entity被多个服务使用且符合"共享业务Entity"标准

**步骤**:

1. **创建Entity类文件**
   ```bash
   # 在common-entity模块创建Entity
   microservices-common-entity/src/main/java/net/lab1024/sa/common/entity/{module}/{EntityName}.java
   ```

2. **更新Entity注解**
   ```java
   @Data
   @EqualsAndHashCode(callSuper = true)
   @TableName("t_{module}_{table}")  // 确保表名正确
   @Schema(description = "{Entity描述}")
   public class {EntityName}Entity extends BaseEntity {

       @TableId(type = IdType.ASSIGN_ID)
       @Schema(description = "{主键描述}")
       private Long {idFieldName};

       // 其他字段...
   }
   ```

3. **更新所有服务导入**
   ```java
   // 旧导入
   import net.lab1024.sa.{service}.entity.{EntityName}Entity;

   // 新导入
   import net.lab1024.sa.common.entity.{module}.{EntityName}Entity;
   ```

4. **验证编译**
   ```bash
   # 编译common-entity模块
   mvn clean install -pl microservices/microservices-common-entity -am -DskipTests

   # 编译所有依赖服务
   mvn clean compile -pl microservices/ioedream-*service -am
   ```

5. **执行测试验证**
   ```bash
   # 运行相关服务的单元测试
   mvn test -pl microservices/ioedream-{service1}-service
   mvn test -pl microservices/ioedream-{service2}-service
   ```

6. **更新文档**
   - 更新数据库设计文档
   - 更新API契约文档
   - 更新架构设计文档

---

#### 保留在业务服务

**前提**: Entity仅被单一服务使用且符合"业务专属Entity"标准

**步骤**:

1. **确认Entity位置正确**
   ```bash
   # Entity应在服务模块的entity包中
   microservices/ioedream-{service}/src/main/java/net/lab1024/sa/{service}/entity/
   ```

2. **确保表名规范**
   ```java
   @TableName("t_{service}_{table}")  // 使用服务专属前缀
   ```

3. **验证无跨服务引用**
   ```bash
   # 确认其他服务没有导入此Entity
   grep -r "import.*{EntityName}Entity" microservices/ioedream-*/ --exclude-dir={current-service}
   ```

4. **文档化保留理由**
   ```markdown
   ## Entity保留说明

   ### {EntityName}Entity
   - **保留位置**: ioedream-{service}-service
   - **保留理由**: 仅被{service}服务使用，包含{specific}业务逻辑
   - **表名**: t_{service}_{table}
   - **依赖范围**: {service}服务内部
   ```

---

## 📏 Entity设计规范

### 命名规范

#### Entity类命名

| Entity类型 | 命名格式 | 示例 |
|-----------|---------|------|
| 通用业务实体 | `{Business}Entity` | `UserEntity`, `DepartmentEntity` |
| 关联表实体 | `{Entity1}{Entity2}Entity` | `AreaDeviceRelationEntity` |
| 配置实体 | `{Config}Entity` | `SystemConfigEntity` |
| 日志实体 | `{Log}Entity` | `OperationLogEntity` |

#### 表命名规范

| 表类别 | 前缀 | 示例 |
|-------|------|------|
| 公共表 | `t_common_` | `t_common_user` |
| 用户管理 | `t_user_` | `t_user_info`, `t_user_role` |
| 组织架构 | `t_org_` | `t_org_department` |
| 门禁管理 | `t_access_` | `t_access_record` |
| 考勤管理 | `t_attendance_` | `t_attendance_record` |
| 消费管理 | `t_consume_` | `t_consume_account` |
| 访客管理 | `t_visitor_` | `t_visitor_record` |
| 视频管理 | `t_video_` | `t_video_device` |

#### 字段命名规范

| 字段类型 | 命名格式 | 示例 |
|---------|---------|------|
| 主键 | `{table}_id` | `user_id`, `department_id` |
| 外键 | `{referenced_table}_id` | `department_id`, `area_id` |
| 状态字段 | `{entity}_status` | `user_status`, `account_status` |
| 类型字段 | `{entity}_type` | `account_type`, `device_type` |
| 标记字段 | `{entity}_flag` | `deleted_flag`, `disabled_flag` |
| 时间字段 | `{action}_time` | `create_time`, `update_time` |

---

### 字段设计规范

#### 主键字段

```java
@TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法
@Schema(description = "{实体}ID")
private Long {entityId};
```

**规则**:
- ✅ 使用`Long`类型（非`long`，允许null）
- ✅ 使用`IdType.ASSIGN_ID`（雪花算法）
- ✅ 字段名格式: `{entity}Id`（驼峰命名）
- ✅ 添加@Schema注解

#### 审计字段

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class {EntityName}Entity extends BaseEntity {
    // BaseEntity已包含:
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - createUserId (创建人ID)
    // - updateUserId (更新人ID)
}
```

**规则**:
- ✅ 所有Entity必须继承`BaseEntity`
- ✅ 使用`@TableField(fill = FieldFill.INSERT)`
- ✅ 使用`@TableField(fill = FieldFill.INSERT_UPDATE)`

#### 状态字段

```java
@Schema(description = "{实体}状态(1-启用 0-禁用)")
@TableField("{entity}_status")
private Integer {entity}Status;
```

**规则**:
- ✅ 使用`Integer`类型（便于扩展状态值）
- ✅ 添加@Schema描述所有状态值
- ✅ 字段名格式: `{entity}Status`

#### 逻辑删除字段

```java
@TableLogic
@TableField("deleted_flag")
@Schema(description = "删除标记(0-未删除 1-已删除)")
private Integer deletedFlag;
```

**规则**:
- ✅ 使用`@TableLogic`注解
- ✅ 字段名: `deletedFlag`
- ✅ 默认值: 0（未删除）

#### 乐观锁字段

```java
@Version
@TableField("version")
@Schema(description = "乐观锁版本号")
private Integer version;
```

**规则**:
- ✅ 使用`@Version`注解
- ✅ 字段名: `version`
- ✅ 类型: `Integer`

---

### 注解使用规范

#### Entity类注解

```java
@Data  // Lombok数据类注解
@EqualsAndHashCode(callSuper = true)  // 包含父类字段
@TableName("t_{table}")  // MyBatis-Plus表映射
@Schema(description = "{Entity描述}")  // Swagger文档
public class {EntityName}Entity extends BaseEntity {
    // 字段定义
}
```

#### 字段注解

```java
// 主键字段
@TableId(type = IdType.ASSIGN_ID)
@Schema(description = "{主键描述}")
private Long {idField};

// 必填字段
@Schema(description = "{字段描述}")
@NotNull(message = "{字段}不能为空")
private {Type} {field};

// 字符串长度验证
@Schema(description = "{字段描述}")
@NotBlank(message = "{字段}不能为空")
@Size(max = 100, message = "{字段}长度不能超过100个字符")
private String {field};

// 数据库字段映射
@TableField("{column_name}")
@Schema(description = "{字段描述}")
private {Type} {field};

// 自动填充字段
@TableField(fill = FieldFill.INSERT)
@Schema(description = "创建时间")
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
@Schema(description = "更新时间")
private LocalDateTime updateTime;

// 逻辑删除
@TableLogic
@TableField("deleted_flag")
@Schema(description = "删除标记(0-未删除 1-已删除)")
private Integer deletedFlag;

// 乐观锁
@Version
@TableField("version")
@Schema(description = "乐观锁版本号")
private Integer version;

// 排除字段
@TableField(exist = false)
@Schema(description = "非数据库字段")
private {Type} {transientField};
```

---

## ⚠️ Entity反模式（禁止事项）

### 反模式1: 超大Entity

**反例**:
```java
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {
    // ❌ 80+字段，772行代码
    // ❌ 包含基础信息、工作时间、弹性时间、加班规则、
    //    休息规则、午休规则、考勤规则、节假日规则等
}
```

**问题**:
- 违反单一职责原则
- 难以维护和理解
- 性能问题（总是加载全部字段）

**正例**:
```java
// 拆分为多个Entity
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {
    // ✅ 仅包含核心字段（~20字段）
    private Long shiftId;
    private String shiftName;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
}

@Data
@TableName("t_work_shift_rule")
public class WorkShiftRuleEntity {
    // ✅ 规则配置独立Entity（~15字段）
    private Long ruleId;
    private Long shiftId;  // 外键关联
    private Integer flexibleEnabled;
    private LocalTime flexibleStartTime;
    // ... 其他规则字段
}
```

**标准**:
- ✅ Entity行数 ≤ 200行（理想）
- ⚠️ Entity行数 ≤ 400行（上限）
- ❌ Entity行数 > 400行（必须拆分）

---

### 反模式2: 包含业务逻辑

**反例**:
```java
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {

    // ❌ 错误：包含业务计算方法
    public BigDecimal calculateOvertimePay(BigDecimal overtimeHours) {
        return overtimeHours.multiply(this.overtimeRate);
    }

    // ❌ 错误：包含数据库操作
    public void saveToDatabase() {
        this.insert();
    }

    // ❌ 错误：包含静态工具方法
    public static LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr);
    }
}
```

**问题**:
- 违反Entity纯数据模型原则
- 业务逻辑应在Manager层
- 工具方法应在util包

**正例**:
```java
// Entity保持纯数据模型
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {
    private Long shiftId;
    private String shiftName;
    private BigDecimal overtimeRate;
    // ✅ 仅包含数据字段
}

// 业务逻辑在Manager层
@Component
public class WorkShiftManager {
    public BigDecimal calculateOvertimePay(Long shiftId, BigDecimal hours) {
        WorkShiftEntity shift = shiftDao.selectById(shiftId);
        return hours.multiply(shift.getOvertimeRate());
    }
}

// 工具方法在util包
public class DateTimeUtils {
    public static LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr);
    }
}
```

---

### 反模式3: 使用Repository注解

**反例**:
```java
@Repository  // ❌ 错误：MyBatis-Plus DAO不能使用@Repository
public interface UserDao extends BaseMapper<UserEntity> {
}
```

**正例**:
```java
@Mapper  // ✅ 正确：使用@Mapper注解
public interface UserDao extends BaseMapper<UserEntity> {
}
```

**理由**:
- `@Mapper`是MyBatis-Plus标准注解
- `@Repository`是Spring JPA注解
- 项目统一使用MyBatis-Plus

---

### 反模式4: 使用@Autowired注入

**反例**:
```java
@Service
public class UserServiceImpl {
    @Autowired  // ❌ 错误：违反依赖注入规范
    private UserDao userDao;
}
```

**正例**:
```java
@Service
public class UserServiceImpl {
    @Resource  // ✅ 正确：使用@Resource注解
    private UserDao userDao;
}
```

**理由**:
- `@Resource`是JSR-250标准注解
- `@Autowired`是Spring特有注解
- 项目规范要求使用@Resource

---

## 🔍 Entity合规性检查

### 检查清单

#### 编译前检查

- [ ] Entity继承BaseEntity
- [ ] 主键使用@IdType.ASSIGN_ID
- [ ] 表名使用标准前缀
- [ ] 字段命名符合规范
- [ ] 使用@Mapper注解（DAO层）
- [ ] 使用@Resource注解（Service层）
- [ ] 导入路径正确

#### 代码审查检查

- [ ] Entity行数 ≤ 200行（理想）或 ≤ 400行（上限）
- [ ] 字段数 ≤ 30个
- [ ] 无业务逻辑方法
- [ ] 无静态工具方法
- [ ] 无@Repository注解
- [ ] 无@Autowired注解
- [ ] 审计字段完整

#### 运行时检查

- [ ] 所有测试通过
- [ ] 无编译警告
- [ ] 无运行时异常
- [ ] 性能测试通过

---

## 📚 相关文档

### 核心规范
- [CLAUDE.md](../../CLAUDE.md) - 项目全局架构规范
- [BUILD_ORDER_MANDATORY_STANDARD.md](./BUILD_ORDER_MANDATORY_STANDARD.md) - 构建顺序强制标准

### 数据库设计
- [数据库设计README](../业务模块/04-消费管理模块/03-数据库设计/README.md) - 数据库设计文档

### 架构设计
- [系统架构设计文档](../architecture/01-系统架构设计文档.md) - 架构设计文档

### API文档
- [API契约规范](../api/README.md) - API设计规范

---

## 📞 规范执行支持

### 架构委员会

- **规范制定**: 负责Entity管理规范的制定和维护
- **架构审查**: 重要Entity设计的架构评审
- **争议解决**: Entity边界划分的争议处理

### 质量保障

- **代码审查**: PR中Entity合规性强制检查
- **CI/CD检查**: 自动化架构合规性检查
- **质量报告**: 定期Entity管理质量报告

---

**👥 制定人**: IOE-DREAM架构委员会
**🏗️ 技术架构师**: SmartAdmin核心团队
**✅ 最终解释权**: IOE-DREAM项目架构委员会
**📅 版本**: v1.0.0
**🔄 下次更新**: 根据项目实践持续优化
