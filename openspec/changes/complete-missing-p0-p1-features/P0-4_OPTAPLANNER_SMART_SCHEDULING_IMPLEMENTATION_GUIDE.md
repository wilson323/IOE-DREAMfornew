# P0-4 OptaPlanner智能排班算法集成实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 6人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 集成OptaPlanner约束求解器，实现智能排班优化

---

## 📊 功能需求概述

### 核心功能
1. **智能排班优化** - 自动生成最优排班计划
2. **约束规则管理** - 灵活配置排班约束条件
3. **排班方案评估** - 多维度评估排班方案质量
4. **排班结果导出** - 导出排班计划到日历系统
5. **排班历史追踪** - 记录排班历史和调整记录

### 技术方案
- **OptaPlanner 9.x**: 约束求解引擎
- **约束定义**: 员工技能、班次需求、工时限制等
- **优化目标**: 公平性、合规性、成本最小化
- **求解策略**: Tabu Search + Simulated Annealing

---

## 🏗️ 系统架构设计

### OptaPlanner核心概念
```
1. Solution（解决方案）: 排班方案
2. Entity（实体）: 员工、班次、排班记录
3. Constraint（约束）: 硬约束（不可违反）、软约束（尽量满足）
4. Score（评分）: 方案质量分数
5. Solver（求解器）: 优化算法引擎
```

### 目录结构
```
ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/
├── controller/
│   └── scheduling/                      # 排班管理
│       └── SmartSchedulingController.java
├── service/
│   └── scheduling/                      # 排班服务
│       ├── SmartSchedulingService.java
│       └── impl/
│           └── SmartSchedulingServiceImpl.java
├── solver/
│   ├── model/                           # OptaPlanner模型
│   │   ├── AttendanceScheduleSolution.java    # 解决方案
│   │   ├── Employee.java                        # 员工实体
│   │   ├── Shift.java                            # 班次实体
│   │   ├── ShiftAssignment.java                 # 班次分配
│   │   └── AttendanceConstraintProvider.java    # 约束提供者
│   ├── score/
│   │   └── AttendanceEasyScoreCalculator.java     # 评分计算器
│   └── algorithm/
│       └── AttendanceSchedulingSolver.java        # 求解器配置
└── manager/
    └── scheduling/
        └── SmartSchedulingManager.java            # 排班管理器
```

---

## 📝 开发步骤

### 步骤1: OptaPlanner依赖配置（0.5天）
- [ ] 添加OptaPlanner依赖到pom.xml
- [ ] 配置求解器配置文件
- [ ] 验证依赖安装

### 步骤2: 数据库设计（0.5天）
- [ ] 创建排班方案表（t_smart_scheduling_plan）
- [ ] 创建排班记录表（t_shift_assignment）
- [ ] 创建约束规则表（t_scheduling_constraint）
- [ ] 创建排班历史表（t_scheduling_history）

### 步骤3: OptaPlanner模型层（1.5天）
- [ ] AttendanceScheduleSolution - 解决方案类
- [ ] Employee - 员工实体
- [ ] Shift - 班次实体
- [ ] ShiftAssignment - 班次分配实体
- [ ] AttendanceConstraintProvider - 约束提供者（硬/软约束）

### 步骤4: 评分计算器（1天）
- [ ] AttendanceEasyScoreCalculator - 评分计算器
- [ ] 硬约束惩罚（Hard Constraint Score）
- [ ] 软约束评分（Soft Constraint Score）
- [ ] 多维度权重配置

### 步骤5: 求解器配置（1天）
- [ ] AttendanceSchedulingSolver - 求解器配置
- [ ] Tabu Search算法配置
- [ ] Simulated Annealing算法配置
- [ ] 求解时间控制

### 步骤6: Service和Controller层（1.5天）
- [ ] SmartSchedulingManager - 排班管理器
- [ ] SmartSchedulingService - 排班服务
- [ ] SmartSchedulingController - REST API
- [ ] 排班结果导出功能

---

## 🔧 Maven依赖配置

```xml
<!-- OptaPlanner 9.x -->
<dependency>
    <groupId>org.optaplanner</groupId>
    <artifactId>optaplanner-core</artifactId>
    <version>9.44.0.Final</version>
</dependency>
<dependency>
    <groupId>org.optaplanner</groupId>
    <artifactId>optaplanner-persistence-jpa</artifactId>
    <version>9.44.0.Final</version>
</dependency>
```

---

## 🎨 OptaPlanner模型设计

### 1. 解决方案类（Solution）
```java
@PlanningSolution
public class AttendanceScheduleSolution {

    private List<Employee> employees;
    private List<Shift> shifts;
    private List<ShiftAssignment> shiftAssignments;
    private HardConstraintsScore hardScore;
    private SoftConstraintsScore softScore;
    private Integer score;

    @PlanningEntityCollectionProperty
    public List<ShiftAssignment> getShiftAssignments() {
        return shiftAssignments;
    }

    @ValueRangeProvider(providerType = Shift.class)
    public CountableValueRange<Shift> getShiftRange() {
        return ValueRangeFactory.createMutableValueRange(shifts);
    }
}
```

### 2. 员工实体（Entity）
```java
@PlanningEntity
public class Employee {

    private Long id;
    private String name;
    private String employeeCode;
    private List<String> skills;  // 技能列表
    private Integer maxShiftsPerDay;  // 每天最多班次
    private Integer maxConsecutiveShifts;  // 最多连续班次
    private Integer minRestHours;  // 最少休息小时
    private Boolean available;  // 是否可用
}
```

### 3. 班次实体（Problem Fact）
```java
@PlanningFact
public class Shift {

    private Long id;
    private String shiftName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<String> requiredSkills;  // 需要的技能
    private Integer requiredEmployees;  // 需要的员工数
    private ShiftType shiftType;  // 班次类型
}
```

### 4. 班次分配（Planning Entity）
```java
@PlanningEntity
public class ShiftAssignment {

    private Long id;
    private Shift shift;
    private Employee employee;

    @PlanningVariableValueRange(providerType = Employee.class)
    public List<Employee> getEmployees() {
        return employees;
    }
}
```

### 5. 约束提供者
```java
public class AttendanceConstraintProvider implements ConstraintProvider {

    @Override
    public void defineConstraints(ConstraintVerifier<ShiftAssignment> constraintVerifier) {
        // 硬约束
        constraintVerifier.forEach(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee().getAvailable())
            .penalize("Employee not available")
            .filter(assignment -> !hasSkillConflict(assignment))
            .penalize("Employee missing required skills");

        // 软约束
        constraintVerifier.forEach(ShiftAssignment.class)
            .reward("Fair distribution", score -> ...)
            .reward("Preferred shifts", score -> ...);
    }
}
```

---

## 📊 约束规则设计

### 硬约束（Hard Constraints - 不可违反）
1. **员工可用性**: 不可用员工不能排班
2. **技能匹配**: 员工必须具备班次所需技能
3. **时间冲突**: 员工同一时间只能排一个班次
4. **班次时长**: 每天工作时长不超过法定限制
5. **休息时间**: 两个班次之间必须有足够休息

### 软约束（Soft Constraints - 尽量满足）
1. **公平性**: 员工排班次数尽量均衡
2. **偏好匹配**: 优先满足员工班次偏好
3. **连续排班**: 避免过度连续排班
4. **技能利用率**: 最大化员工技能利用率
5. **成本优化**: 最小化人力成本

---

## 🔌 REST API设计

### 排班优化API
```java
@RestController
@RequestMapping("/api/scheduling/smart")
public class SmartSchedulingController {

    /**
     * 执行智能排班优化
     */
    @PostMapping("/optimize")
    public ResponseDTO<Map<String, Object>> optimizeSchedule(
        @RequestBody ScheduleOptimizeForm form);

    /**
     * 获取排班方案详情
     */
    @GetMapping("/{planId}")
    public ResponseDTO<Map<String, Object>> getScheduleDetail(
        @PathVariable Long planId);

    /**
     * 获取排班结果列表
     */
    @GetMapping("/{planId}/assignments")
    public ResponseDTO<List<Map<String, Object>>> getAssignments(
        @PathVariable Long planId);

    /**
     * 导出排班计划
     */
    @PostMapping("/{planId}/export")
    public ResponseDTO<String> exportSchedule(
        @PathVariable Long planId,
        @RequestParam String format); // ical/excel/json

    /**
     * 确认排班方案
     */
    @PostMapping("/{planId}/confirm")
    public ResponseDTO<Void> confirmSchedule(
        @PathVariable Long planId);
}
```

---

## ✅ 验收标准

### 功能验收
- [ ] OptaPlanner求解器成功集成
- [ ] 能够生成排班方案
- [ ] 约束规则正确执行
- [ ] 评分算法合理有效
- [ ] 求解时间可接受（< 5分钟）

### 性能验收
- [ ] 求解时间 < 5分钟（100员工/30天）
- [ ] 内存占用合理（< 2GB）
- [ ] 可生成可行解（满足硬约束）

### 代码质量
- [ ] 严格遵循四层架构规范
- [ ] OptaPlanner模型设计规范
- [ ] 约束定义清晰完整
- [ ] 代码注释完整

---

## 🚀 实施优先级

**P0核心功能（必须完成）**:
1. OptaPlanner依赖集成
2. 核心模型定义（Solution/Entity/Assignment）
3. 基本约束定义（5个硬约束 + 3个软约束）
4. 简单评分计算器
5. REST API接口

**P1增强功能（可选）**:
1. 复杂约束规则（业务规则引擎）
2. 多目标优化（Pareto前沿）
3. 实时求解监控
4. 求解结果可视化

**P2优化功能（可选）**:
1. 约束规划器配置
2. 自定义算法配置
3. 分布式求解
4. 增量求解

---

**📅 预计完成时间**: 6个工作日
**👥 开发人员**: 后端工程师（熟悉OptaPlanner）
**🎯 里程碑**: 每日下班前提交代码并演示进度
