# 设计文档：考勤规则执行上下文优化

## 架构背景

### 当前实现分析

#### 1. RuleExecutionContext（手动Builder）

**位置**：`ioedream-attendance-service/src/main/java/.../engine/model/RuleExecutionContext.java`

**当前设计**：
- 使用 `@Data` 注解（Lombok生成getter/setter）
- 手动编写内部 `Builder` 类（第207-266行）
- 提供 `builder()` 静态工厂方法
- 包含别名方法（`getUserId()` → `getEmployeeId()`）

**问题**：
- 手动维护Builder类容易出错（字段新增时需同步更新）
- Builder方法不完整（缺少 `departmentName`、`executionId` 等）
- 与项目中其他模型类不一致（大多使用 `@Builder` 注解）

**字段清单**（当前）：
```java
// 基础字段
- employeeId (Long)
- employeeName (String)
- employeeNo (String)
- departmentId (Long)
- departmentName (String) ❌ Builder中缺失
- scheduleDate (LocalDate)
- shiftId (Long)
- shiftCode (String)
- shiftType (Integer)

// 统计字段
- consecutiveWorkDays (Integer)
- restDays (Integer)
- monthlyWorkDays (Integer)
- monthlyWorkHours (Double)
- skills (String)

// 执行字段
- customVariables (Map<String, Object>)
- executionId (String) ❌ Builder中缺失
- sessionId (String) ❌ Builder中缺失
```

#### 2. RuleTestServiceImpl（使用customVariables）

**位置**：`ioedream-attendance-service/src/main/java/.../service/impl/RuleTestServiceImpl.java`

**当前实现**（修复后）：
```java
private RuleExecutionContext buildExecutionContext(RuleTestRequest testRequest) {
    RuleExecutionContext.Builder builder = RuleExecutionContext.builder();

    // 基础字段
    builder.employeeId(testRequest.getUserId())
            .employeeName(testRequest.getUserName())
            .departmentId(testRequest.getDepartmentId())
            .scheduleDate(...);

    // 测试专用字段放入customVariables
    Map<String, Object> customVariables = new HashMap<>();
    customVariables.put("executionId", UUID.randomUUID().toString());
    customVariables.put("punchTime", testRequest.getPunchTime());
    customVariables.put("deviceId", testRequest.getDeviceId());
    // ... 更多字段

    for (Map.Entry<String, Object> entry : customVariables.entrySet()) {
        builder.customVariable(entry.getKey(), entry.getValue());
    }

    return builder.build();
}
```

**问题**：
- 类型不安全：`customVariables.get("punchTime")` 返回 `Object`，需要强制转换
- IDE无法自动补全：字段名是字符串，容易拼写错误
- 重构困难：字段名修改时无法利用IDE重构功能
- 可读性差：无法一眼看出测试上下文包含哪些字段

### 设计目标

1. **使用Lombok @Builder**：减少样板代码，提升可维护性
2. **类型安全**：测试专用字段使用强类型而非Map
3. **向后兼容**：不影响现有代码
4. **符合规范**：遵循CLAUDE.md和项目编码标准

## 解决方案设计

### 方案1：RuleExecutionContext添加@Builder（采用）

#### 设计决策

**选择**：在 `RuleExecutionContext` 上添加 `@Builder` 注解，移除手动Builder

**理由**：
- ✅ 减少样板代码（删除~60行手动Builder代码）
- ✅ 字段新增时自动同步（Lombok编译时生成）
- ✅ 与项目其他模型类保持一致
- ✅ 支持Builder继承和默认值

**实现步骤**：
1. 添加 `@Builder` 和 `@NoArgsConstructor`、`@AllArgsConstructor` 注解
2. 移除手动编写的内部 `Builder` 类（第207-266行）
3. 保留别名方法（`getUserId()`、`setAttendanceDate()`等）以兼容现有代码
4. 添加 `@Builder.Default` 注解为集合字段提供默认值

**代码示例**：
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionContext {
    @Builder.Default
    private Map<String, Object> customVariables = new HashMap<>();

    // 保留别名方法
    public Long getUserId() {
        return employeeId;
    }

    public void setUserId(Long userId) {
        this.employeeId = userId;
    }

    // ... 其他别名方法
}
```

**潜在问题与缓解**：
- ⚠️ Lombok @Builder生成的字段名可能与手动Builder不一致
  - **缓解**：使用 `@Builder.Default` 设置默认值，保留别名方法

#### 影响范围分析

**受影响的类**：
- `RuleTestServiceImpl.buildExecutionContext()` - 需要更新Builder调用
- 其他使用 `RuleExecutionContext.builder()` 的地方 - 需要验证兼容性

**向后兼容性**：
- ✅ 字段名不变
- ✅ Getter/Setter方法不变
- ✅ 别名方法保留
- ✅ `builder()` 静态方法仍然存在

### 方案2：引入RuleTestContext专用类（采用）

#### 设计决策

**选择**：创建新的 `RuleTestContext` 类，继承或组合 `RuleExecutionContext`

**理由**：
- ✅ 类型安全：测试字段使用强类型
- ✅ 职责分离：测试上下文与生产上下文分离
- ✅ 可扩展性：未来可以添加更多测试专用功能
- ✅ 不影响生产代码：`RuleExecutionContext` 保持简洁

#### 实现方案A：继承方式（推荐）

```java
/**
 * 规则测试上下文
 * <p>
 * 继承自RuleExecutionContext，添加测试专用字段
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTestContext extends RuleExecutionContext {

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 执行时间戳
     */
    private LocalDateTime executionTimestamp;

    /**
     * 执行模式（TEST/PRODUCTION）
     */
    private String executionMode;

    /**
     * 打卡时间
     */
    private LocalTime punchTime;

    /**
     * 打卡类型（IN/OUT）
     */
    private String punchType;

    /**
     * 排班开始时间
     */
    private LocalTime scheduleStartTime;

    /**
     * 排班结束时间
     */
    private LocalTime scheduleEndTime;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 用户属性（JSON格式）
     */
    private Map<String, Object> userAttributes;

    /**
     * 考勤属性（JSON格式）
     */
    private Map<String, Object> attendanceAttributes;

    /**
     * 环境参数（JSON格式）
     */
    private Map<String, Object> environmentParams;

    /**
     * 转换为RuleExecutionContext
     * <p>
     * 将测试字段合并到customVariables中，用于规则引擎
     * </p>
     */
    public RuleExecutionContext toRuleExecutionContext() {
        RuleExecutionContext context = RuleExecutionContext.builder()
                .employeeId(this.getEmployeeId())
                .employeeName(this.getEmployeeName())
                .departmentId(this.getDepartmentId())
                .scheduleDate(this.getScheduleDate())
                .build();

        // 合并测试字段到customVariables
        Map<String, Object> customVars = new HashMap<>();
        if (this.getCustomVariables() != null) {
            customVars.putAll(this.getCustomVariables());
        }

        // 添加测试专用字段
        customVars.put("executionId", this.executionId);
        customVars.put("executionTimestamp", this.executionTimestamp);
        customVars.put("executionMode", this.executionMode);

        if (this.punchTime != null) {
            customVars.put("punchTime", this.punchTime);
        }
        // ... 其他字段

        context.setCustomVariables(customVars);
        return context;
    }
}
```

**优点**：
- ✅ 代码复用：继承基础字段和方法
- ✅ 类型安全：测试字段有明确类型
- ✅ IDE支持：自动补全、重构、类型检查

**缺点**：
- ⚠️ 继承耦合：`RuleTestContext` 依赖 `RuleExecutionContext` 的实现细节
- ⚠️ 字段隐藏：子类字段可能与父类 `customVariables` 冲突

#### 实现方案B：组合方式（备选）

```java
/**
 * 规则测试上下文
 * <p>
 * 组合RuleExecutionContext，避免继承耦合
 * </p>
 */
@Data
@Builder
public class RuleTestContext {
    /**
     * 基础规则执行上下文
     */
    private final RuleExecutionContext baseContext;

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 打卡时间
     */
    private LocalTime punchTime;

    // ... 其他测试字段

    /**
     * 转换为RuleExecutionContext
     */
    public RuleExecutionContext toRuleExecutionContext() {
        Map<String, Object> customVars = new HashMap<>();
        if (baseContext.getCustomVariables() != null) {
            customVars.putAll(baseContext.getCustomVariables());
        }

        // 添加测试字段
        customVars.put("executionId", this.executionId);
        customVars.put("punchTime", this.punchTime);
        // ...

        baseContext.setCustomVariables(customVars);
        return baseContext;
    }
}
```

**优点**：
- ✅ 松耦合：不依赖继承
- ✅ 灵活性：可以独立演化

**缺点**：
- ⚠️ 代码冗余：需要委托方法访问基础字段

#### 最终选择：方案A（继承方式）

**理由**：
- 代码更简洁，易于理解
- 测试场景专用，不需要过度设计
- 继承关系合理（测试上下文**是**一种规则执行上下文）

### 方案3：单元测试策略

#### 测试覆盖范围

**核心类**：
- `RuleExecutionContext` - Builder模式、别名方法、getVariables()
- `RuleTestContext` - 字段访问、toRuleExecutionContext()
- `RuleTestServiceImpl.buildExecutionContext()` - 上下文构建逻辑

**测试框架**：
- JUnit 5
- Mockito（Mock依赖）
- AssertJ（流式断言）

#### 测试用例示例

```java
@Test
@DisplayName("测试RuleExecutionContext Builder模式")
void testRuleContextBuilder() {
    // Given & When
    RuleExecutionContext context = RuleExecutionContext.builder()
            .employeeId(1001L)
            .employeeName("张三")
            .departmentId(10L)
            .scheduleDate(LocalDate.of(2024, 1, 1))
            .build();

    // Then
    assertThat(context.getEmployeeId()).isEqualTo(1001L);
    assertThat(context.getUserId()).isEqualTo(1001L); // 别名方法
}

@Test
@DisplayName("测试RuleTestContext类型安全")
void testRuleTestContextTypeSafety() {
    // Given & When
    RuleTestContext testContext = RuleTestContext.builder()
            .employeeId(1001L)
            .punchTime(LocalTime.of(8, 35))
            .deviceId("DEV001")
            .executionMode("TEST")
            .build();

    // Then - 类型安全，无需强制转换
    LocalTime punchTime = testContext.getPunchTime();
    assertThat(punchTime).isEqualTo(LocalTime.of(8, 35));
}

@Test
@DisplayName("测试toRuleExecutionContext转换")
void testToRuleExecutionContextConversion() {
    // Given
    RuleTestContext testContext = RuleTestContext.builder()
            .employeeId(1001L)
            .punchTime(LocalTime.of(8, 35))
            .build();

    // When
    RuleExecutionContext context = testContext.toRuleExecutionContext();

    // Then - 测试字段应合并到customVariables
    assertThat(context.getCustomVariables())
            .containsKey("punchTime")
            .containsValue(LocalTime.of(8, 35));
}
```

## 权衡讨论

### 权衡1：Lombok vs 手动代码

| 维度 | Lombok @Builder | 手动Builder |
|------|-----------------|-------------|
| 样板代码 | ✅ 少 | ❌ 多（~60行） |
| 可维护性 | ✅ 高（字段自动同步） | ❌ 低（手动维护） |
| 可读性 | ✅ 高（简洁） | ⚠️ 中（冗长） |
| 调试难度 | ⚠️ 中（生成代码） | ✅ 低（直接代码） |
| 灵活性 | ⚠️ 中（注解限制） | ✅ 高（完全控制） |

**决策**：采用Lombok @Builder
- 理由：减少样板代码、提升可维护性的收益远大于调试成本

### 权衡2：继承 vs 组合

| 维度 | 继承方式 | 组合方式 |
|------|----------|----------|
| 代码复用 | ✅ 高 | ⚠️ 中（需要委托） |
| 耦合度 | ⚠️ 高（紧耦合） | ✅ 低（松耦合） |
| 类型安全 | ✅ 高 | ✅ 高 |
| 复杂度 | ✅ 低 | ⚠️ 中 |

**决策**：采用继承方式
- 理由：测试场景专用，继承关系合理；代码简洁性优先

### 权衡3：立即重构 vs 渐进迁移

**选项A：立即重构**（推荐）
- 一次性修改所有相关代码
- 优点：避免技术债累积
- 缺点：变更范围较大

**选项B：渐进迁移**
- 先创建新类，旧代码逐步迁移
- 优点：风险低
- 缺点：维护两套代码

**决策**：采用选项A（立即重构）
- 理由：变更范围可控（仅考勤服务），且有完整单元测试保护

## 实施计划

1. **阶段1**：重构 RuleExecutionContext（使用@Builder）
2. **阶段2**：创建 RuleTestContext 类
3. **阶段3**：重构 RuleTestServiceImpl
4. **阶段4**：编写单元测试
5. **阶段5**：验证和审查

每个阶段完成后进行编译和测试验证。

## 风险缓解

| 风险 | 缓解措施 |
|------|---------|
| Lombok生成的方法名不一致 | 保留别名方法，确保向后兼容 |
| 继承导致字段冲突 | 明确字段命名规则，避免与父类customVariables冲突 |
| 单元测试覆盖不足 | 使用JaCoCo工具检查覆盖率，目标≥80% |
| 破坏现有功能 | 完整的单元测试 + 集成测试验证 |

## 参考资料

- [Lombok @Builder文档](https://projectlombok.org/features/Builder)
- [CLAUDE.md](../../../CLAUDE.md) - 项目架构规范
- [JUnit 5用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ文档](https://assertj.github.io/doc/)
