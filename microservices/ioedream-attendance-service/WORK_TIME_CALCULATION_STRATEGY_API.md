# 工时计算策略API文档

## 概述

工时计算策略模块为考勤服务提供灵活的工时计算能力，支持三种主要的工时制度：
- **标准工时制**（STANDARD）：固定上下班时间
- **轮班制**（ROTATING）：三班倒、四班三倒等
- **弹性工作制**（FLEXIBLE）：弹性上下班时间

---

## 基础信息

- **服务名称**: ioedream-attendance-service
- **服务端口**: 8091
- **API版本**: v1
- **包路径**: `net.lab1024.sa.attendance.strategy`

---

## 核心接口

### 1. WorkTimeCalculateStrategy - 工时计算策略接口

**接口描述**: 定义工时计算的核心方法

```java
public interface WorkTimeCalculateStrategy {
    String getStrategyType();
    String getStrategyName();
    CalculateResult calculate(CalculateContext context);
    boolean validatePunchRecords(List<PunchRecord> punchRecords, WorkShiftEntity workShift);
    Integer calculateLateMinutes(LocalDateTime punchTime, WorkShiftEntity workShift);
    Integer calculateEarlyLeaveMinutes(LocalDateTime punchTime, WorkShiftEntity workShift);
    Integer calculateOvertimeMinutes(LocalDateTime endTime, WorkShiftEntity workShift);
    List<WorkTimeSpan> calculateWorkTimeSpans(List<PunchRecord> punchRecords, WorkShiftEntity workShift);
    // ... 更多方法
}
```

---

## 策略实现

### 1. StandardWorkTimeStrategy - 标准工时制策略

**适用场景**:
- 白班（DAY_SHIFT）：09:00 - 18:00
- 夜班（NIGHT_SHIFT）：22:00 - 06:00
- 兼职班（PART_TIME_SHIFT）
- 特殊班（SPECIAL_SHIFT）

**核心计算规则**:
```yaml
上班时间: 固定时间（如09:00）
下班时间: 固定时间（如18:00）
迟到判断: 打卡时间 > 上班时间
早退判断: 打卡时间 < 下班时间
加班判断: 打卡时间 > 加班开始时间
```

**示例代码**:
```java
// 创建策略
WorkTimeCalculateStrategy strategy = new StandardWorkTimeStrategy();

// 构建上下文
CalculateContext context = CalculateContext.builder()
    .employeeId(1001L)
    .attendanceDate(LocalDate.of(2025, 1, 30))
    .workShift(workShift)
    .punchRecords(punchRecords)
    .build();

// 计算工时
CalculateResult result = strategy.calculate(context);

// 获取结果
Double actualWorkHours = result.getActualWorkHours();    // 实际工作时长
Integer lateMinutes = result.getLateMinutes();            // 迟到时长
Integer earlyLeaveMinutes = result.getEarlyLeaveMinutes(); // 早退时长
Double overtimeHours = result.getOvertimeHours();        // 加班时长
```

---

### 2. RotatingWorkTimeStrategy - 轮班制策略

**适用场景**:
- 三班倒：早班08:00-16:00、中班16:00-24:00、夜班00:00-08:00
- 四班三倒：8小时轮换
- 特殊轮班模式

**核心计算规则**:
```yaml
跨天计算: 自动识别夜班跨天
连续班次: 支持连续打卡计算
时段津贴: 夜班1.2倍、中班1.1倍
加班计算: 基于实际班次时间
```

**示例代码**:
```java
// 创建夜班配置
WorkShiftEntity nightShift = new WorkShiftEntity();
nightShift.setShiftId(102L);
nightShift.setShiftName("夜班");
nightShift.setShiftType(WorkShiftEntity.ShiftType.ROTATING_SHIFT.getCode());
nightShift.setStartTime(LocalTime.of(0, 0));
nightShift.setEndTime(LocalTime.of(8, 0));
nightShift.setIsOvernight(true);

// 使用轮班制策略
WorkTimeCalculateStrategy strategy = new RotatingWorkTimeStrategy();
CalculateResult result = strategy.calculate(context);
```

---

### 3. FlexibleWorkTimeStrategy - 弹性工作制策略

**适用场景**:
- 弹性工作制（FLEXIBLE_SHIFT）
- 核心工作时段：10:00-16:00必须在场
- 弹性时段：07:00-10:00和16:00-20:00灵活打卡

**核心计算规则**:
```yaml
核心时段: 必须在场（如10:00-16:00）
弹性时段: 灵活打卡（如07:00-10:00和16:00-20:00）
迟到判断: 核心时段开始后打卡
早退判断: 核心时段结束前离开
加班判断: 超过弹性时段下班
满足标准: 实际工作时长 >= 核心工作时长
```

**示例代码**:
```java
// 创建弹性班次配置
WorkShiftEntity flexibleShift = new WorkShiftEntity();
flexibleShift.setShiftId(103L);
flexibleShift.setShiftName("弹性班");
flexibleShift.setShiftType(WorkShiftEntity.ShiftType.FLEXIBLE_SHIFT.getCode());
flexibleShift.setStartTime(LocalTime.of(9, 0));
flexibleShift.setEndTime(LocalTime.of(18, 0));
flexibleShift.setWorkHours(6.0); // 核心工作时长6小时
flexibleShift.setFlexibleStartTime(60); // 弹性开始1小时
flexibleShift.setFlexibleEndTime(120); // 弹性结束2小时

// 使用弹性工作制策略
WorkTimeCalculateStrategy strategy = new FlexibleWorkTimeStrategy();
CalculateResult result = strategy.calculate(context);
```

---

## 策略工厂

### WorkTimeCalculateStrategyFactory - 策略工厂

**功能**: 根据班次类型自动选择合适的计算策略

```java
@Autowired
private WorkTimeCalculateStrategyFactory strategyFactory;

// 自动选择策略
WorkTimeCalculateStrategy strategy = strategyFactory.getStrategy(workShift);

// 手动指定策略类型
WorkTimeCalculateStrategy standardStrategy = strategyFactory.getStrategyByType("STANDARD");
WorkTimeCalculateStrategy rotatingStrategy = strategyFactory.getStrategyByType("ROTATING");
WorkTimeCalculateStrategy flexibleStrategy = strategyFactory.getStrategyByType("FLEXIBLE");
```

**策略选择规则**:
```
if (workShift.getIsFlexible() == true) {
    return new FlexibleWorkTimeStrategy();
}

switch (workShift.getShiftType()) {
    case ROTATING_SHIFT -> return new RotatingWorkTimeStrategy();
    case FLEXIBLE_SHIFT -> return new FlexibleWorkTimeStrategy();
    default -> return new StandardWorkTimeStrategy();
}
```

---

## 数据模型

### CalculateContext - 计算上下文

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| workShift | WorkShiftEntity | 是 | 班次配置 |
| attendanceDate | LocalDate | 是 | 考勤日期 |
| punchRecords | List\<PunchRecord\> | 是 | 打卡记录列表 |
| scheduleId | Long | 否 | 排班ID |
| departmentId | Long | 否 | 部门ID |
| flexibleCalculation | Boolean | 否 | 是否启用灵活计算 |
| ignoreBreakTime | Boolean | 否 | 是否忽略休息时间 |
| autoFillMissedPunch | Boolean | 否 | 是否自动补卡 |
| enableOvernightCalculation | Boolean | 否 | 是否启用跨天计算 |

### CalculateResult - 计算结果

| 字段 | 类型 | 说明 |
|------|------|------|
| employeeId | Long | 员工ID |
| attendanceDate | LocalDate | 考勤日期 |
| actualWorkHours | Double | 实际工作时长（小时） |
| standardWorkHours | Double | 标准工作时长（小时） |
| lateMinutes | Integer | 迟到时长（分钟） |
| earlyLeaveMinutes | Integer | 早退时长（分钟） |
| overtimeHours | Double | 加班时长（小时） |
| workTimeSpans | List\<WorkTimeSpan\> | 工作时段列表 |
| breakMinutes | Integer | 休息时长（分钟） |
| coreWorkHours | Double | 核心工作时长（小时） |
| flexibleWorkHours | Double | 弹性工作时长（小时） |
| isOvernight | Boolean | 是否跨天班次 |
| attendanceStatus | AttendanceStatus | 考勤状态 |
| isValid | Boolean | 是否有效记录 |

**考勤状态枚举（AttendanceStatus）**:
```java
public enum AttendanceStatus {
    NORMAL("正常", "NORMAL"),
    LATE("迟到", "LATE"),
    EARLY_LEAVE("早退", "EARLY_LEAVE"),
    LATE_AND_EARLY_LEAVE("迟到早退", "LATE_AND_EARLY_LEAVE"),
    ABSENT("缺勤", "ABSENT"),
    OVERTIME("加班", "OVERTIME"),
    INVALID("无效", "INVALID");
}
```

### PunchRecord - 打卡记录

| 字段 | 类型 | 说明 |
|------|------|------|
| recordId | Long | 打卡记录ID |
| employeeId | Long | 员工ID |
| punchTime | LocalDateTime | 打卡时间 |
| punchType | PunchType | 打卡类型（IN/OUT/BREAK_IN/BREAK_OUT） |
| deviceId | String | 设备ID |
| punchMethod | String | 打卡方式（FACE/FINGER/CARD/PASSWORD/MANUAL） |
| locationInfo | String | 位置信息（JSON格式） |
| verified | Boolean | 是否验证通过 |

### WorkTimeSpan - 工作时段

| 字段 | 类型 | 说明 |
|------|------|------|
| spanId | Integer | 时段ID |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| workMinutes | Integer | 工作时长（分钟） |
| breakMinutes | Integer | 休息时长（分钟） |
| isValid | Boolean | 是否有效时段 |
| timeSpanType | TimeSpanType | 时段类型（NORMAL/OVERTIME/BREAK/FLEXIBLE） |

---

## REST API接口

### 1. 计算工时

**接口描述**: 根据班次配置和打卡记录计算工时

**请求方式**: `POST`

**请求路径**: `/api/v1/attendance/calculate`

**请求体**:
```json
{
  "employeeId": 1001,
  "attendanceDate": "2025-01-30",
  "shiftId": 101,
  "punchRecords": [
    {
      "recordId": 1,
      "employeeId": 1001,
      "punchTime": "2025-01-30 08:55:00",
      "punchType": "IN",
      "deviceId": "DEV001",
      "verified": true
    },
    {
      "recordId": 2,
      "employeeId": 1001,
      "punchTime": "2025-01-30 18:05:00",
      "punchType": "OUT",
      "deviceId": "DEV001",
      "verified": true
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "employeeId": 1001,
    "attendanceDate": "2025-01-30",
    "shiftId": 101,
    "shiftName": "正常班",
    "actualWorkHours": 8.17,
    "standardWorkHours": 8.0,
    "lateMinutes": 0,
    "earlyLeaveMinutes": 0,
    "overtimeHours": 0.08,
    "attendanceStatus": "NORMAL",
    "isValid": true
  }
}
```

---

## 使用示例

### Spring Service集成示例

```java
@Service
@Slf4j
public class AttendanceCalculationService {

    @Autowired
    private WorkTimeCalculateStrategyFactory strategyFactory;

    /**
     * 计算员工工时
     */
    public CalculateResult calculateEmployeeWorkTime(Long employeeId, LocalDate date) {
        // 1. 获取员工排班
        ScheduleEntity schedule = scheduleDao.getByEmployeeAndDate(employeeId, date);
        if (schedule == null) {
            throw new BusinessException("未找到排班信息");
        }

        // 2. 获取班次配置
        WorkShiftEntity workShift = workShiftDao.selectById(schedule.getShiftId());
        if (workShift == null) {
            throw new BusinessException("未找到班次配置");
        }

        // 3. 获取打卡记录
        List<PunchRecord> punchRecords = punchRecordDao.getByEmployeeAndDate(employeeId, date);

        // 4. 选择合适的计算策略
        WorkTimeCalculateStrategy strategy = strategyFactory.getStrategy(workShift);

        // 5. 构建计算上下文
        CalculateContext context = CalculateContext.builder()
                .employeeId(employeeId)
                .attendanceDate(date)
                .scheduleId(schedule.getScheduleId())
                .workShift(workShift)
                .punchRecords(punchRecords)
                .enableOvernightCalculation(true)
                .build();

        // 6. 计算工时
        CalculateResult result = strategy.calculate(context);

        log.info("[考勤计算] 员工工时计算完成: employeeId={}, date={}, actualWorkHours={}, status={}",
                employeeId, date, result.getActualWorkHours(), result.getAttendanceStatus());

        return result;
    }
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 1001 | 员工ID不能为空 |
| 1002 | 考勤日期不能为空 |
| 1003 | 班次配置不存在 |
| 1004 | 打卡记录无效或不完整 |
| 1005 | 计算策略不支持 |

---

## 测试

### 单元测试

```bash
# 运行所有工时计算策略测试
mvn test -Dtest=WorkTimeCalculateStrategyTest

# 运行标准工时制测试
mvn test -Dtest=StandardWorkTimeStrategyTest

# 运行轮班制测试
mvn test -Dtest=RotatingWorkTimeStrategyTest

# 运行弹性工作制测试
mvn test -Dtest=FlexibleWorkTimeStrategyTest
```

---

**文档维护**: IOE-DREAM Attendance Team
**最后更新**: 2025-01-30
