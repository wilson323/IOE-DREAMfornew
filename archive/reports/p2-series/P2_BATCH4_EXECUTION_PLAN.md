# P2-Batch4 执行计划

**执行日期**: 2025-12-26
**重构目标**: AttendanceRuleEngineImpl (875行)
**预期代码减少**: 目标70% (~612行 → ~260行)

---

## 📊 当前代码分析

### AttendanceRuleEngineImpl现状

**代码规模**: 875行 (超大型类)
**依赖注入**: 5个组件
- RuleLoader
- RuleValidator
- RuleCacheManager
- RuleEvaluatorFactory
- RuleExecutor

**方法统计**:
- 公共方法: 15个
- 私有方法: 15个
- 内部类: 2个 (CompiledCondition, CompiledActionObject)

### 职责分析

**当前职责** (6个维度):

| 职责 | 方法数 | 代码行数 | 问题 |
|------|--------|---------|------|
| **规则执行** | 5个 | ~250行 | 包含单个/批量/分类执行逻辑 |
| **条件/动作编译** | 4个 | ~200行 | 解析和编译逻辑混在一起 |
| **规则验证** | 5个 | ~150行 | 适用性检查、范围检查 |
| **缓存管理** | 3个 | ~80行 | 缓存清除、预热 |
| **统计管理** | 4个 | ~80行 | 统计收集、计算 |
| **规则覆盖** | 2个 | ~40行 | 覆盖逻辑 |

**核心问题**:
- ❌ 职责过多: 6个职责混在一起
- ❌ 代码过长: 875行难以维护
- ❌ 可测试性差: 方法耦合严重
- ❌ 可扩展性差: 新增功能困难

---

## 🎯 重构目标

### 设计原则

遵循P2-Batch3的成功经验:
- ✅ Facade Pattern: AttendanceRuleEngineImpl作为统一入口
- ✅ Delegation Pattern: 所有功能委托给专业服务
- ✅ Single Responsibility: 每个服务职责单一
- ✅ Constructor Injection: 构造函数注入依赖

### 服务拆分方案

**创建5个专业服务**:

| 服务 | 职责 | 预期代码行数 | 从原类提取的方法 |
|------|------|------------|----------------|
| **RuleExecutionService** | 规则执行 | ~200行 | evaluateRules, evaluateRule, batchEvaluateRules |
| **RuleCompilationService** | 条件/动作编译 | ~180行 | compileRuleCondition, compileRuleAction, parseCondition, parseAction |
| **RuleValidationService** | 规则验证 | ~150行 | validateRule, getApplicableRules, isRuleApplicable, 4个check方法 |
| **RuleCacheManagementService** | 缓存管理 | ~100行 | clearRuleCache, warmUpRuleCache, cache相关操作 |
| **RuleStatisticsService** | 统计管理 | ~100行 | getExecutionStatistics, 4个statistics方法 |

**保留Facade**:
- AttendanceRuleEngineImpl: 875行 → ~260行 (-70%)
- 只保留委托逻辑和协调逻辑

---

## 📋 重构实施步骤

### Phase 1: 服务创建 (P0优先级)

#### 1.1 RuleExecutionService
**文件路径**: `.../engine/rule/execution/RuleExecutionService.java`

**职责**:
- 单个规则评估
- 批量规则评估
- 按类别规则评估
- 规则优先级排序

**核心方法**:
```java
public RuleEvaluationResult evaluateRule(Long ruleId, RuleExecutionContext context)
public List<RuleEvaluationResult> evaluateRules(List<Long> ruleIds, RuleExecutionContext context)
public List<RuleEvaluationResult> evaluateRulesByCategory(String category, RuleExecutionContext context)
public List<RuleEvaluationResult> batchEvaluateRules(List<RuleExecutionContext> contexts)
private void sortByPriority(List<RuleEvaluationResult> results)
```

**依赖注入**:
- RuleEvaluatorFactory
- RuleExecutor

**单元测试**: 8个测试方法

---

#### 1.2 RuleCompilationService
**文件路径**: `.../engine/rule/compilation/RuleCompilationService.java`

**职责**:
- 规则条件编译
- 规则动作编译
- 条件表达式解析
- 动作表达式解析

**核心方法**:
```java
public CompiledRule compileRuleCondition(String ruleCondition)
public CompiledAction compileRuleAction(String ruleAction)
private CompiledCondition parseCondition(String conditionExpression)
private CompiledActionObject parseAction(String actionExpression)
```

**内部类**:
- CompiledCondition (从原类迁移)
- CompiledActionObject (从原类迁移)

**依赖注入**: 无 (纯编译逻辑)

**单元测试**: 6个测试方法

---

#### 1.3 RuleValidationService
**文件路径**: `.../engine/rule/validation/RuleValidationService.java`

**职责**:
- 规则验证
- 规则适用性检查
- 部门范围检查
- 用户属性检查
- 时间范围检查
- 规则过滤器检查

**核心方法**:
```java
public RuleValidationResult validateRule(Long ruleId)
public boolean isRuleApplicable(Long ruleId, RuleExecutionContext context)
public boolean checkDepartmentScope(Map<String, Object> ruleConfig, RuleExecutionContext context)
public boolean checkUserAttributes(Map<String, Object> ruleConfig, RuleExecutionContext context)
public boolean checkTimeScope(Map<String, Object> ruleConfig, RuleExecutionContext context)
public boolean checkRuleFilters(Map<String, Object> ruleConfig, RuleExecutionContext context)
```

**依赖注入**:
- RuleValidator

**单元测试**: 9个测试方法

---

#### 1.4 RuleCacheManagementService
**文件路径**: `.../engine/rule/cache/RuleCacheManagementService.java`

**职责**:
- 规则缓存清除
- 规则缓存预热
- 缓存状态查询

**核心方法**:
```java
public void clearRuleCache()
public void warmUpRuleCache(List<Long> ruleIds)
public CacheStatus getCacheStatus()
```

**依赖注入**:
- RuleCacheManager

**单元测试**: 4个测试方法

---

#### 1.5 RuleStatisticsService
**文件路径**: `.../engine/rule/statistics/RuleStatisticsService.java`

**职责**:
- 执行统计收集
- 统计信息计算
- 平均评估时间计算

**核心方法**:
```java
public RuleExecutionStatistics getExecutionStatistics(long startTime, long endTime)
private Long getStatisticsValue(String key)
private void setStatisticsValue(String key, Long value)
private Double calculateAverageEvaluationTime()
private void updateExecutionStatistics(String resultType)
```

**依赖注入**: 无 (纯统计逻辑)

**单元测试**: 5个测试方法

---

### Phase 2: Facade重构

#### 2.1 AttendanceRuleEngineImpl重构
**文件路径**: `.../engine/rule/impl/AttendanceRuleEngineImpl.java`

**重构内容**:
1. 注入5个专业服务
2. 所有公共方法改为委托调用
3. 保留规则覆盖逻辑 (handleRuleOverrides, shouldOverride)
4. 移除所有private方法 (已迁移到服务)
5. 移除内部类 (已迁移到RuleCompilationService)

**重构后代码**:
```java
@Slf4j
public class AttendanceRuleEngineImpl implements AttendanceRuleEngine {

    private final RuleExecutionService executionService;
    private final RuleCompilationService compilationService;
    private final RuleValidationService validationService;
    private final RuleCacheManagementService cacheService;
    private final RuleStatisticsService statisticsService;

    // 构造函数注入5个服务
    public AttendanceRuleEngineImpl(
            RuleExecutionService executionService,
            RuleCompilationService compilationService,
            RuleValidationService validationService,
            RuleCacheManagementService cacheService,
            RuleStatisticsService statisticsService) {
        this.executionService = executionService;
        this.compilationService = compilationService;
        this.validationService = validationService;
        this.cacheService = cacheService;
        this.statisticsService = statisticsService;
    }

    @Override
    public List<RuleEvaluationResult> evaluateRules(RuleExecutionContext context) {
        log.info("[规则引擎] 开始评估规则: userId={}, date={}",
                context.getUserId(), context.getAttendanceDate());

        // 委托给RuleExecutionService
        List<RuleEvaluationResult> results = executionService.evaluateRules(
            getApplicableRules(context), context
        );

        // 保留规则覆盖逻辑
        handleRuleOverrides(results);

        return results;
    }

    // 其他方法类似委托...

    // 保留规则覆盖逻辑
    private void handleRuleOverrides(List<RuleEvaluationResult> results) {
        // 原有逻辑保留
    }

    private boolean shouldOverride(RuleEvaluationResult higher, RuleEvaluationResult lower) {
        // 原有逻辑保留
    }
}
```

**预期代码行数**: ~260行 (-70%)

---

### Phase 3: Configuration类创建

#### 3.1 AttendanceRuleEngineConfiguration
**文件路径**: `.../config/AttendanceRuleEngineConfiguration.java`

**职责**: 注册6个Bean (5个服务 + 1个Facade)

```java
@Slf4j
@Configuration
public class AttendanceRuleEngineConfiguration {

    @Bean
    public RuleExecutionService ruleExecutionService(
            RuleEvaluatorFactory evaluatorFactory,
            RuleExecutor ruleExecutor) {
        return new RuleExecutionService(evaluatorFactory, ruleExecutor);
    }

    @Bean
    public RuleCompilationService ruleCompilationService() {
        return new RuleCompilationService();
    }

    @Bean
    public RuleValidationService ruleValidationService(RuleValidator ruleValidator) {
        return new RuleValidationService(ruleValidator);
    }

    @Bean
    public RuleCacheManagementService ruleCacheManagementService(RuleCacheManager cacheManager) {
        return new RuleCacheManagementService(cacheManager);
    }

    @Bean
    public RuleStatisticsService ruleStatisticsService() {
        return new RuleStatisticsService();
    }

    @Bean
    public AttendanceRuleEngine attendanceRuleEngine(
            RuleExecutionService executionService,
            RuleCompilationService compilationService,
            RuleValidationService validationService,
            RuleCacheManagementService cacheService,
            RuleStatisticsService statisticsService) {
        return new AttendanceRuleEngineImpl(
                executionService, compilationService, validationService,
                cacheService, statisticsService);
    }
}
```

**预期代码行数**: ~120行

---

### Phase 4: 单元测试创建

#### 4.1 测试类清单

| 测试类 | 测试方法数 | 覆盖目标 | 状态 |
|--------|-----------|---------|------|
| **RuleExecutionServiceTest** | 8个 | ~85% | 待创建 |
| **RuleCompilationServiceTest** | 6个 | ~80% | 待创建 |
| **RuleValidationServiceTest** | 9个 | ~90% | 待创建 |
| **RuleCacheManagementServiceTest** | 4个 | ~80% | 待创建 |
| **RuleStatisticsServiceTest** | 5个 | ~85% | 待创建 |
| **AttendanceRuleEngineConfigurationTest** | 6个 | 100% | 待创建 |
| **AttendanceRuleEngineImplTest** | 8个 | 100% | 待创建 |

**总计**: 7个测试类, 46个测试方法
**目标覆盖率**: 85%+

---

## 📦 包结构优化

### 重构前

```
engine/rule/
└── impl/
    └── AttendanceRuleEngineImpl.java (875行)
```

### 重构后

```
engine/rule/
├── impl/
│   └── AttendanceRuleEngineImpl.java (~260行, Facade)
├── execution/
│   └── RuleExecutionService.java (~200行)
├── compilation/
│   └── RuleCompilationService.java (~180行)
├── validation/
│   └── RuleValidationService.java (~150行)
├── cache/
│   └── RuleCacheManagementService.java (~100行)
└── statistics/
    └── RuleStatisticsService.java (~100行)

config/
└── AttendanceRuleEngineConfiguration.java (~120行)
```

---

## ✅ 验收标准

### 重构完成标准

- [ ] 5个专业服务创建完成
- [ ] AttendanceRuleEngineImpl重构为Facade
- [ ] 代码减少70% (875行 → ~260行)
- [ ] API 100%向后兼容
- [ ] 代码规范符合标准
- [ ] 单元测试覆盖85%+

### 代码质量标准

- [ ] 所有公共方法100%兼容
- [ ] 无TODO残留
- [ ] 无编译警告
- [ ] 遵循四层架构规范
- [ ] 统一日志规范 (@Slf4j)

---

## 🎯 预期成果

### 代码减少效果

| 指标 | 重构前 | 重构后 | 改进 |
|------|--------|--------|------|
| **Facade行数** | 875行 | ~260行 | -70% ⭐ |
| **专业服务** | 0个 | 5个 | +∞ ⭐ |
| **职责数量** | 6个 | 1个 | -83% ⭐ |
| **测试方法数** | 0个 | 46个 | +∞ ⭐ |
| **测试覆盖率** | 0% | 85%+ | +∞ ⭐ |

### 可维护性提升

| 维度 | 改进效果 |
|------|---------|
| **代码可读性** | +400% |
| **可测试性** | +500% |
| **可扩展性** | +350% |
| **可维护性** | +400% |

---

## 📅 实施时间表

| 阶段 | 任务 | 预计时间 | 状态 |
|------|------|---------|------|
| **Phase 1** | 创建5个专业服务 | 2-3小时 | 待开始 |
| **Phase 2** | Facade重构 | 1-2小时 | 待开始 |
| **Phase 3** | Configuration类创建 | 0.5小时 | 待开始 |
| **Phase 4** | 单元测试创建 | 2-3小时 | 待开始 |
| **Phase 5** | 文档生成 | 0.5小时 | 待开始 |

**总计**: 6-9小时

---

## 🚀 后续计划

完成P2-Batch4后,继续P2系列重构:
- **P2-Batch5**: RuleCacheManagerImpl重构 (514行)
- **P2-Batch6**: 其他大型Engine类重构

---

**制定人**: IOE-DREAM架构团队
**制定时间**: 2025-12-26
**文档版本**: v1.0
**状态**: ✅ 计划完成,待执行
