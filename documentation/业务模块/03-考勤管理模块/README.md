# 考勤管理模块 - 开发设计文档

> **版本**: v2.0.0  
> **更新日期**: 2025-12-17  
> **模块归属**: ioedream-attendance-service (8091)  
> **技术架构**: Spring Boot 3.5.8 + MyBatis-Plus + MySQL 8.0

---

## 📋 文档目录

### 核心设计文档

| 序号 | 文档名称 | 说明 |
|------|----------|------|
| 01 | [功能说明文档](./01-功能说明/README.md) | 模块功能概述、子模块划分 |
| 02 | [用户故事](./02-用户故事/README.md) | 用户故事、用例描述 |
| 03 | [数据库设计](./03-数据库设计/README.md) | ER图、表结构设计、索引策略 |
| 04 | [业务流程图](./04-业务流程图/README.md) | 业务流程、时序图、状态图 |
| 05 | [API接口设计](./05-API接口设计/README.md) | RESTful接口规范、请求响应 |
| 06 | [非功能需求](./06-非功能需求/README.md) | 性能、安全、可靠性指标 |
| 07 | [验收标准](./07-验收标准/README.md) | 功能验收、测试用例 |

### 子模块文档

| 子模块 | 文档路径 | 说明 |
|--------|----------|------|
| 班次时间管理 | [./子模块/01-班次时间管理/](./子模块/01-班次时间管理/) | 时间段、班次配置 |
| 排班管理 | [./子模块/02-排班管理/](./子模块/02-排班管理/) | 排班日历、智能排班 |
| 考勤规则配置 | [./子模块/03-考勤规则配置/](./子模块/03-考勤规则配置/) | 规则、预警、通知 |
| 异常管理 | [./子模块/04-异常管理/](./子模块/04-异常管理/) | 请假、加班、补签 |
| 考勤数据 | [./子模块/05-考勤数据/](./子模块/05-考勤数据/) | 打卡记录、考勤计算 |
| 汇总报表 | [./子模块/06-汇总报表/](./子模块/06-汇总报表/) | 统计分析、报表导出 |

---

## 🎯 模块概述

### 业务定位

考勤管理模块是IOE-DREAM智慧园区一卡通管理平台的核心业务模块之一，负责员工考勤全生命周期管理，包括：

- **班次时间管理**: 灵活配置各类班次和时间段
- **排班管理**: 支持多种排班模式（固定班次、弹性班次、轮班制）
- **考勤规则配置**: 考勤规则、预警规则、通知规则
- **异常管理**: 请假、加班、调班、补签、销假等异常申请
- **考勤数据**: 打卡记录采集、考勤结果计算
- **汇总报表**: 月度统计、出勤率分析、异常统计

### 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                        │
├─────────────────────────────────────────────────────────────┤
│              ioedream-attendance-service (8091)              │
├──────────────┬──────────────┬──────────────┬────────────────┤
│  Controller  │   Service    │   Manager    │      DAO       │
│  (接口层)    │   (业务层)   │   (编排层)   │   (数据层)     │
├──────────────┴──────────────┴──────────────┴────────────────┤
│                    MySQL 8.0 + Redis                         │
└─────────────────────────────────────────────────────────────┘
```

### 四层架构

| 层级 | 职责 | 规范 |
|------|------|------|
| **Controller** | HTTP请求处理、参数验证 | @RestController、@Valid |
| **Service** | 核心业务逻辑、事务管理 | @Service、@Transactional |
| **Manager** | 复杂流程编排、多数据组装 | 纯Java类，构造函数注入 |
| **DAO** | 数据库CRUD操作 | @Mapper、继承BaseMapper |

---

## 📊 数据表概览

### 核心表清单

| 表名 | 说明 | 子模块 |
|------|------|--------|
| `t_attendance_time_period` | 时间段表 | 班次时间管理 |
| `t_attendance_work_shift` | 班次表 | 班次时间管理 |
| `t_attendance_shift_period_relation` | 班次时间段关联表 | 班次时间管理 |
| `t_attendance_schedule_record` | 排班记录表 | 排班管理 |
| `t_attendance_schedule_template` | 排班模板表 | 排班管理 |
| `t_attendance_schedule_override` | 临时排班覆盖表 | 排班管理 |
| `t_attendance_rule` | 考勤规则表 | 考勤规则配置 |
| `t_attendance_point` | 考勤点表 | 考勤规则配置 |
| `t_attendance_warning_rule` | 预警规则表 | 考勤规则配置 |
| `t_attendance_notification_rule` | 通知规则表 | 考勤规则配置 |
| `t_attendance_mobile_config` | 移动端配置表 | 考勤规则配置 |
| `t_attendance_leave_type` | 假种配置表 | 异常管理 |
| `t_attendance_exception_application` | 异常申请表 | 异常管理 |
| `t_attendance_exception_approval` | 异常审批表 | 异常管理 |
| `t_attendance_clock_record` | 打卡记录表 | 考勤数据 |
| `t_attendance_result` | 考勤结果表 | 考勤数据 |
| `t_attendance_summary` | 考勤汇总表 | 汇总报表 |
| `t_attendance_warning_record` | 预警记录表 | 汇总报表 |

---

## 🔗 模块依赖关系

### 依赖的模块

| 模块 | 依赖内容 |
|------|----------|
| **ioedream-common-service** | 员工信息、部门信息、区域信息 |
| **ioedream-device-comm-service** | 考勤设备、门禁设备联动 |
| **ioedream-oa-service** | 审批流程、工作流引擎 |

### 被依赖的模块

| 模块 | 依赖内容 |
|------|----------|
| **ioedream-access-service** | 门禁通行与考勤联动 |
| **ioedream-consume-service** | 消费与考勤数据关联 |

---

## ⚠️ 开发规范

### 强制执行规则

```java
// ✅ 必须使用 @Resource 注入
@Resource
private AttendanceRecordDao attendanceRecordDao;

// ✅ 必须使用 @Mapper + Dao 后缀
@Mapper
public interface AttendanceRecordDao extends BaseMapper<AttendanceRecordEntity> {}

// ✅ 必须使用 @Valid 参数校验
@PostMapping
public ResponseDTO<Long> add(@Valid @RequestBody AttendanceRuleAddForm form) {}

// ✅ 必须返回统一 ResponseDTO 格式
public ResponseDTO<AttendanceRecordVO> getById(Long id) {
    return ResponseDTO.ok(attendanceRecordService.getById(id));
}
```

### 严格禁止事项

- ❌ 禁止使用 @Autowired 注入
- ❌ 禁止使用 @Repository 注解
- ❌ 禁止使用 Repository 后缀命名
- ❌ 禁止跨层访问（Controller直接调用DAO）
- ❌ 禁止在Controller中包含业务逻辑

---

## 📈 开发优先级

### P0 - 核心功能（第一阶段）

1. 班次时间管理 - 基础班次配置
2. 排班管理 - 基础排班功能
3. 考勤数据 - 打卡记录采集
4. 考勤计算 - 基础考勤结果计算

### P1 - 重要功能（第二阶段）

1. 异常管理 - 请假、加班申请
2. 考勤规则配置 - 基础规则
3. 汇总报表 - 月度统计

### P2 - 扩展功能（第三阶段）

1. 智能排班算法
2. 设备联动预警
3. 高级报表分析
4. 移动端优化

---

## 📞 联系方式

- **模块负责人**: 考勤团队
- **技术架构师**: IOE-DREAM架构委员会
- **文档维护**: 持续更新中

---

*本文档遵循IOE-DREAM项目统一架构规范，详见 [CLAUDE.md](../../../CLAUDE.md)*

