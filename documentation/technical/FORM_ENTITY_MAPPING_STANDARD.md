# 智能排班模块 - Form-Entity字段映射规范

**文档版本**: v1.0
**创建时间**: 2025-12-25
**适用范围**: 所有智能排班相关的Form、Entity、VO

---

## 一、核心原则 🎯

### 1.1 强制规则

**规则1: Form-Entity字段完全一致**
- ✅ Form字段必须与Entity字段一一对应
- ✅ 字段名、类型、注解保持一致
- ✅ 禁止Service调用Form不存在的方法
- ❌ 禁止"Form缺少字段导致编译错误"

**规则2: 类型强类型化**
- ✅ 日期统一使用`java.time.LocalDate`
- ✅ 禁止使用int表示日期
- ✅ 时间统一使用`java.time.LocalTime`
- ✅ 时间戳统一使用`java.time.LocalDateTime`

**规则3: JSON字段命名规范**
- ✅ List/Array类型必须序列化为JSON字符串存储
- ✅ 使用Jackson进行序列化/反序列化
- ❌ 禁止使用FastJSON（项目未引入依赖）

---

## 二、Form-Entity字段映射表 📋

### 2.1 SmartSchedulePlanAddForm ↔ SmartSchedulePlanEntity

| Form字段 | Entity字段 | 类型 | 默认值 | 说明 |
|---------|-----------|------|-------|------|
| planName | planName | String | 必填 | 计划名称 |
| description | description | String | null | 计划描述 |
| startDate | startDate | LocalDate | 必填 | 开始日期 ⚠️ 强类型 |
| endDate | endDate | LocalDate | 必填 | 结束日期 ⚠️ 强类型 |
| **periodDays** | **periodDays** | **Integer** | **计算/必填** | **排班周期（天）** |
| **employeeIds** | **employeeIds** | **List<String>** | **必填** | **员工ID列表（序列化）** |
| **shiftIds** | **shiftIds** | **List<String>** | **必填** | **班次ID列表（序列化）** |
| optimizationGoal | optimizationGoal | Integer | 5 | 优化目标 (1-5) |
| **minConsecutiveWorkDays** | **minConsecutiveWorkDays** | **Integer** | **1** | **最小连续工作天数** |
| maxConsecutiveWorkDays | maxConsecutiveWorkDays | Integer | 7 | 最大连续工作天数 |
| minRestDays | minRestDays | Integer | 2 | 最小休息天数 |
| **minDailyStaff** | **minDailyStaff** | **Integer** | **2** | **每日最少人员数** |
| **maxDailyStaff** | **maxDailyStaff** | **Integer** | **20** | **每日最多人员数** |
| fairnessWeight | fairnessWeight | Double | 0.4 | 公平性权重 (0.0-1.0) |
| costWeight | costWeight | Double | 0.3 | 成本权重 (0.0-1.0) |
| efficiencyWeight | efficiencyWeight | Double | 0.2 | 效率权重 (0.0-1.0) |
| satisfactionWeight | satisfactionWeight | Double | 0.1 | 满意度权重 (0.0-1.0) |
| algorithmType | algorithmType | Integer | 4 | 算法类型 (1-4) |
| populationSize | populationSize | Integer | 20 | 种群大小 |
| **maxGenerations** | **maxGenerations** | **Integer** | **50** | **最大迭代次数（遗传）** |
| **maxIterations** | **maxIterations** | **Integer** | **100** | **最大迭代次数（通用）** |
| crossoverRate | crossoverRate | Double | 0.8 | 交叉率 (0.0-1.0) |
| mutationRate | mutationRate | Double | 0.1 | 变异率 (0.0-1.0) |
| **selectionRate** | **selectionRate** | **Double** | **0.5** | **选择率** |
| **elitismRate** | **elitismRate** | **Double** | **0.1** | **精英保留率** |
| **overtimeCostPerShift** | **overtimeCostPerShift** | **Double** | **100.0** | **加班成本** |
| **weekendCostPerShift** | **weekendCostPerShift** | **Double** | **150.0** | **周末成本** |
| **holidayCostPerShift** | **holidayCostPerShift** | **Double** | **200.0** | **节假日成本** |

**字段统计**：
- Form字段: 27个
- Entity字段: 27个
- 映射完整度: 100% ✅

**新增字段（本次修复）**：9个
1. periodDays
2. minConsecutiveWorkDays
3. maxDailyStaff
4. maxIterations
5. selectionRate
6. elitismRate
7. overtimeCostPerShift
8. weekendCostPerShift
9. holidayCostPerShift

---

## 三、Service层实现规范 🔧

### 3.1 JSON序列化/反序列化

**序列化（Form → Entity）**：
```java
// ✅ 正确示例
SmartSchedulePlanEntity entity = SmartSchedulePlanEntity.builder()
    .planName(form.getPlanName())
    .employeeIds(form.getEmployeeIds() != null ?
        objectMapper.writeValueAsString(form.getEmployeeIds()) : "[]")
    .shiftIds(form.getShiftIds() != null ?
        objectMapper.writeValueAsString(form.getShiftIds()) : "[]")
    .build();

// ❌ 错误示例 - 直接传递List
.employeeIds(form.getEmployeeIds())  // 编译错误！
```

**反序列化（Entity → OptimizationConfig）**：
```java
// ✅ 正确示例
OptimizationConfig config = OptimizationConfig.builder()
    .employeeIds(objectMapper.readValue(plan.getEmployeeIds(),
        new TypeReference<List<Long>>() {}))
    .shiftIds(plan.getShiftIds() != null ?
        objectMapper.readValue(plan.getShiftIds(),
            new TypeReference<List<Long>>() {}) : new ArrayList<>())
    .build();

// ❌ 错误示例 - 直接传递String
.employeeIds(plan.getEmployeeIds())  // 类型不匹配！
```

### 3.2 Object Mapper配置

**必需字段**：
```java
@Service
public class XxxServiceImpl implements XxxService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 初始化时配置（可选）
    @PostConstruct
    public void init() {
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }
}
```

---

## 四、API设计规范 📐

### 4.1 日期时间类型规范（P0级强制）

**⚠️ 禁止使用int表示日期**

```java
// ❌ 严格禁止
public void method(int date, int year, int month, int day)

// ✅ 必须使用强类型
public void method(LocalDate date)
public void method(LocalDateTime timestamp)
```

**常见错误模式**：
```java
// ❌ 错误：int表示日期
detectShiftConflicts(int startDate, int endDate, Chromosome chromosome)

// ✅ 正确：LocalDate表示日期
detectShiftConflicts(LocalDate startDate, LocalDate endDate, Chromosome chromosome)
```

### 4.2 Controller API规范

**请求参数**：
```java
// ✅ 正确：使用LocalDate
@GetMapping("/api/plans")
public ResponseDTO<PageResult<SmartSchedulePlanVO>> queryPlans(
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate
) {
    // ...
}
```

**响应数据**：
```java
// ✅ 正确：VO包含LocalDate字段
@Data
public class SmartSchedulePlanVO {
    private Long planId;
    private String planName;
    private LocalDate startDate;  // ✅ 强类型
    private LocalDate endDate;    // ✅ 强类型
    private Integer periodDays;   // ✅ 计算字段
}
```

### 4.3 数据库字段映射

**Entity字段映射**：
```java
@Data
@TableName("t_smart_schedule_plan")
public class SmartSchedulePlanEntity {

    // ✅ 日期类型直接映射
    @TableField("start_date")
    private LocalDate startDate;  // MySQL: DATE

    // ✅ List类型序列化存储
    @TableField("employee_ids")
    private String employeeIds;  // MySQL: JSON/TEXT

    // ✅ 数值类型
    @TableField("period_days")
    private Integer periodDays;  // MySQL: INT
}
```

**数据库表结构**：
```sql
CREATE TABLE t_smart_schedule_plan (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    period_days INT NOT NULL COMMENT '排班周期（天）',
    employee_ids JSON COMMENT '员工ID列表',
    INDEX idx_date_range (start_date, end_date)
);
```

---

## 五、类型转换工具类 🛠️

### 5.1 推荐工具类

**日期转换**：
```java
// ✅ 使用Java Time API
LocalDate date = LocalDate.now();
LocalDate date = LocalDate.of(2025, 12, 25);
LocalDate date = LocalDate.parse("2025-12-25");

// ❌ 禁止使用
int date = 20251225;  // 不清晰，容易出错
```

**JSON转换**：
```java
// ✅ 使用Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(list);
List<Long> list = mapper.readValue(json, new TypeReference<List<Long>>() {});

// ❌ 禁止使用（项目无依赖）
String json = JSON.toJSONString(list);
```

---

## 六、验证清单 ✓

### 6.1 Form设计检查清单

- [ ] Form字段与Entity字段一一对应
- [ ] 所有字段都有@NotNull或@Min/@Max验证注解
- [ ] 所有字段都有默认值
- [ ] 日期字段使用LocalDate
- [ ] List字段使用泛型（List<Long>而非List）
- [ ] 所有字段都有Javadoc注释

### 6.2 Service实现检查清单

- [ ] JSON序列化使用Jackson
- [ ] 所有List字段都有null检查
- [ ] 反序列化使用TypeReference
- [ ] ObjectMapper配置正确
- [ ] 无"找不到符号"编译错误

### 6.3 API接口检查清单

- [ ] Controller使用LocalDate参数
- [ ] 禁止使用int表示日期
- [ ] VO字段使用强类型
- [ ] 接口文档完整（Swagger注解）
- [ ] 参数验证完整

---

## 七、常见错误模式 ❌

### 7.1 编译错误模式

**错误1: Form缺少字段**
```
[ERROR] 找不到符号
  符号:   方法 getPeriodDays()
```
**原因**: Service调用Form不存在的方法
**修复**: 向Form添加对应字段

**错误2: 类型不匹配**
```
[ERROR] 不兼容的类型: int无法转换为java.time.LocalDate
```
**原因**: 使用int表示日期
**修复**: 改用LocalDate类型

**错误3: JSON序列化错误**
```
[ERROR] 找不到符号
  符号:   方法 JSON.toJSONString(...)
```
**原因**: 使用了FastJSON但项目未引入
**修复**: 改用Jackson ObjectMapper

### 7.2 运行时错误模式

**错误1: JSON解析异常**
```java
// ❌ 错误
List<Long> ids = mapper.readValue(idsString, List.class);  // 原始类型

// ✅ 正确
List<Long> ids = mapper.readValue(idsString,
    new TypeReference<List<Long>>() {});  // 泛型保留
```

**错误2: 空指针异常**
```java
// ❌ 错误
String json = mapper.writeValueAsString(form.getEmployeeIds());

// ✅ 正确
String json = form.getEmployeeIds() != null ?
    mapper.writeValueAsString(form.getEmployeeIds()) : "[]";
```

---

## 八、最佳实践示例 ✨

### 8.1 完整的Service实现示例

```java
@Slf4j
@Service
public class SmartScheduleServiceImpl implements SmartScheduleService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(SmartSchedulePlanAddForm form) {
        log.info("[智能排班] 创建排班计划: name={}, startDate={}, endDate={}",
            form.getPlanName(), form.getStartDate(), form.getEndDate());

        // 1. 构建实体
        SmartSchedulePlanEntity entity = SmartSchedulePlanEntity.builder()
            .planName(form.getPlanName())
            .description(form.getDescription())
            .startDate(form.getStartDate())  // ✅ LocalDate强类型
            .endDate(form.getEndDate())      // ✅ LocalDate强类型
            .periodDays(form.getPeriodDays())
            .employeeIds(form.getEmployeeIds() != null ?  // ✅ null检查
                objectMapper.writeValueAsString(form.getEmployeeIds()) : "[]")
            .shiftIds(form.getShiftIds() != null ?  // ✅ null检查
                objectMapper.writeValueAsString(form.getShiftIds()) : "[]")
            // ... 其他字段
            .build();

        // 2. 保存到数据库
        smartSchedulePlanDao.insert(entity);

        log.info("[智能排班] 排班计划创建成功: planId={}", entity.getPlanId());
        return entity.getPlanId();
    }
}
```

### 8.2 完整的Controller示例

```java
@Slf4j
@RestController
@RequestMapping("/api/attendance/smart-schedule")
public class SmartScheduleController {

    @Resource
    private SmartScheduleService smartScheduleService;

    @PostMapping("/plan")
    public ResponseDTO<Long> createPlan(@RequestBody @Valid SmartSchedulePlanAddForm form) {
        log.info("[智能排班] 接收创建计划请求: form={}", form);
        Long planId = smartScheduleService.createPlan(form);
        return ResponseDTO.ok(planId);
    }

    @GetMapping("/plans")
    public ResponseDTO<PageResult<SmartSchedulePlanVO>> queryPlans(
        @RequestParam @NotNull LocalDate startDate,  // ✅ LocalDate强类型
        @RequestParam @NotNull LocalDate endDate,   // ✅ LocalDate强类型
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<SmartSchedulePlanVO> result = smartScheduleService.queryPlans(
            startDate, endDate, pageNum, pageSize);
        return ResponseDTO.ok(result);
    }
}
```

---

## 九、维护机制 🔄

### 9.1 Form-Entity同步检查

**自动化检查脚本**（建议添加到pre-commit hook）：
```bash
#!/bin/bash
# check-form-entity-sync.sh

# 检查Form和Entity字段数量一致性
FORM_COUNT=$(grep "private " SmartSchedulePlanAddForm.java | wc -l)
ENTITY_COUNT=$(grep "private " SmartSchedulePlanEntity.java | wc -l)

if [ $FORM_COUNT -ne $ENTITY_COUNT ]; then
    echo "❌ Form和Entity字段数量不一致！"
    echo "Form字段数: $FORM_COUNT"
    echo "Entity字段数: $ENTITY_COUNT"
    exit 1
fi

echo "✅ Form和Entity字段数量一致: $FORM_COUNT个"
```

### 9.2 类型检查规则

**IDEA Inspection规则**：
- 禁止int表示日期
- 禁止使用FastJSON
- 强制null检查
- 强制泛型类型保留

**Checkstyle规则**：
```xml
<module name="Regexp">
    <property name="format" value="int.*date|int.*Date"/>
    <property name="message" value="禁止使用int表示日期，请使用LocalDate"/>
</module>
```

---

## 十、培训与文档 📚

### 10.1 开发者培训要点

1. **强类型优先**：日期、时间必须使用java.time包
2. **Form-Entity一致**：设计Form时必须先查看Entity
3. **JSON序列化规范**：统一使用Jackson
4. **null安全**：所有对象操作前必须检查null

### 10.2 参考文档

- [Java 8 Date/Time API](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
- [Jackson JSON Guide](https://github.com/FasterXML/jackson-docs)
- [Bean Validation Specification](https://beanvalidation.org/2.0/spec/)

---

**文档维护**: 架构委员会
**最后更新**: 2025-12-25
**下次审查**: 2026-01-01

**变更记录**:
- 2025-12-25: 初始版本，建立Form-Entity映射机制
