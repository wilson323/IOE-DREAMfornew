# Task 2.6 加班管理 - 数据库设计文档

## 📋 文档信息

- **任务编号**: Task 2.6
- **任务名称**: 加班管理模块
- **创建时间**: 2025-01-30
- **设计原则**: 严格遵循CLAUDE.md全局架构规范

---

## 🎯 设计目标

构建完整的加班管理模块，支持：
1. **加班申请流程** - 员工提交加班申请，领导审批
2. **加班记录管理** - 记录实际加班时间和时长
3. **加班规则配置** - 灵活的加班计算规则
4. **加班统计分析** - 加班时长统计和报表

---

## 📊 数据库表设计

### 1. 加班申请表 (t_attendance_overtime_apply)

**表说明**: 存储员工加班申请记录

```sql
CREATE TABLE t_attendance_overtime_apply (
    -- 主键
    apply_id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '申请ID（主键）',

    -- 申请编号（业务主键）
    apply_no             VARCHAR(50)     NOT NULL COMMENT '申请编号（业务主键，格式：OT-YYYYMMDD-001）',

    -- 申请人信息
    applicant_id         BIGINT          NOT NULL COMMENT '申请人ID',
    applicant_name       VARCHAR(100)    NOT NULL COMMENT '申请人姓名',
    department_id        BIGINT          NOT NULL COMMENT '部门ID',
    department_name      VARCHAR(100)    NOT NULL COMMENT '部门名称',
    position_id          BIGINT          COMMENT '岗位ID',
    position_name        VARCHAR(100)    COMMENT '岗位名称',

    -- 加班信息
    overtime_type        VARCHAR(20)     NOT NULL COMMENT '加班类型：WORKDAY-工作日 OVERTIME-休息日 HOLIDAY-法定节假日',
    overtime_date        DATE            NOT NULL COMMENT '加班日期',
    start_time           TIME            NOT NULL COMMENT '加班开始时间',
    end_time             TIME            NOT NULL COMMENT '加班结束时间',
    planned_hours        DECIMAL(4,2)    NOT NULL COMMENT '计划加班时长（小时）',
    actual_hours         DECIMAL(4,2)    COMMENT '实际加班时长（小时）',

    -- 加班原因
    overtime_reason      VARCHAR(500)    NOT NULL COMMENT '加班原因',
    overtime_description  TEXT            COMMENT '加班详细说明',

    -- 加班补偿方式
    compensation_type    VARCHAR(20)     NOT NULL COMMENT '补偿方式：PAY-支付加班费 LEAVE-调休',
    leave_date           DATE            COMMENT '调休日期（补偿方式为调休时）',

    -- 审批信息
    apply_status         VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING-待审批 APPROVED-已批准 REJECTED-已驳回 CANCELLED-已撤销',
    approval_level       INT             NOT NULL DEFAULT 1 COMMENT '当前审批层级',
    approver_id          BIGINT          COMMENT '当前审批人ID',
    approver_name        VARCHAR(100)    COMMENT '当前审批人姓名',

    -- 最终审批信息
    final_approver_id     BIGINT          COMMENT '最终审批人ID',
    final_approver_name   VARCHAR(100)    COMMENT '最终审批人姓名',
    final_approval_time   DATETIME        COMMENT '最终审批时间',
    final_approval_comment VARCHAR(500)   COMMENT '最终审批意见',

    -- 工作流信息
    workflow_instance_id BIGINT          COMMENT '工作流实例ID',

    -- 审计字段
    create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id       BIGINT          COMMENT '创建人ID',
    update_user_id       BIGINT          COMMENT '更新人ID',
    deleted_flag         TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version              INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引
    PRIMARY KEY (apply_id),
    UNIQUE KEY uk_apply_no (apply_no),
    KEY idx_applicant_id (applicant_id),
    KEY idx_overtime_date (overtime_date),
    KEY idx_department_id (department_id),
    KEY idx_apply_status (apply_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';
```

---

### 2. 加班记录表 (t_attendance_overtime_record)

**表说明**: 存储实际加班记录（自动生成或手动记录）

```sql
CREATE TABLE t_attendance_overtime_record (
    -- 主键
    record_id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID（主键）',

    -- 关联申请
    apply_id             BIGINT          COMMENT '关联申请ID（如果有申请）',
    apply_no             VARCHAR(50)     COMMENT '申请编号',

    -- 员工信息
    employee_id          BIGINT          NOT NULL COMMENT '员工ID',
    employee_name        VARCHAR(100)    NOT NULL COMMENT '员工姓名',
    department_id        BIGINT          NOT NULL COMMENT '部门ID',
    department_name      VARCHAR(100)    NOT NULL COMMENT '部门名称',
    shift_id             BIGINT          COMMENT '班次ID',
    shift_name           VARCHAR(100)    COMMENT '班次名称',

    -- 加班信息
    overtime_date        DATE            NOT NULL COMMENT '加班日期',
    start_time           DATETIME        NOT NULL COMMENT '实际加班开始时间',
    end_time             DATETIME        NOT NULL COMMENT '实际加班结束时间',
    overtime_hours       DECIMAL(4,2)    NOT NULL COMMENT '实际加班时长（小时）',
    overtime_type        VARCHAR(20)     NOT NULL COMMENT '加班类型：WORKDAY-工作日 OVERTIME-休息日 HOLIDAY-法定节假日',

    -- 加班分类
    is_night_shift       TINYINT         NOT NULL DEFAULT 0 COMMENT '是否夜班 0-否 1-是',
    is_weekend           TINYINT         NOT NULL DEFAULT 0 COMMENT '是否周末 0-否 1-是',
    is_holiday           TINYINT         NOT NULL DEFAULT 0 COMMENT '是否法定节假日 0-否 1-是',

    -- 加班倍率
    overtime_multiplier  DECIMAL(3,2)    NOT NULL DEFAULT 1.0 COMMENT '加班倍率：1.0-1.5倍 2.0-3.0倍',
    normal_hours         DECIMAL(4,2)    NOT NULL DEFAULT 0.0 COMMENT '正常加班时长（小时）',
    multiplied_hours     DECIMAL(4,2)    NOT NULL DEFAULT 0.0 COMMENT '折算后时长（小时）',

    -- 补偿信息
    compensation_type    VARCHAR(20)     COMMENT '补偿方式：PAY-支付加班费 LEAVE-调休',
    compensation_status  VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '补偿状态：PENDING-待补偿 COMPLETED-已补偿',

    -- 计算信息
    calculation_rule_id  BIGINT          COMMENT '加班计算规则ID',
    calculation_base     VARCHAR(50)     COMMENT '计算依据：ACTUAL-实际打卡 SCHEDULE-排班时间',

    -- 备注信息
    remark               VARCHAR(500)    COMMENT '备注',
    description          TEXT            COMMENT '详细说明',

    -- 审计字段
    create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id       BIGINT          COMMENT '创建人ID',
    update_user_id       BIGINT          COMMENT '更新人ID',
    deleted_flag         TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version              INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引
    PRIMARY KEY (record_id),
    KEY idx_apply_id (apply_id),
    KEY idx_employee_id (employee_id),
    KEY idx_overtime_date (overtime_date),
    KEY idx_department_id (department_id),
    KEY idx_compensation_status (compensation_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班记录表';
```

---

### 3. 加班规则配置表 (t_attendance_overtime_rule)

**表说明**: 存储加班计算规则

```sql
CREATE TABLE t_attendance_overtime_rule (
    -- 主键
    rule_id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '规则ID（主键）',
    rule_code            VARCHAR(50)     NOT NULL COMMENT '规则编码',
    rule_name            VARCHAR(100)    NOT NULL COMMENT '规则名称',

    -- 规则分类
    rule_category        VARCHAR(50)     NOT NULL COMMENT '规则分类：CALCULATION-计算规则 COMPENSATION-补偿规则 LIMITATION-限制规则',
    rule_type            VARCHAR(50)     NOT NULL COMMENT '规则类型',

    -- 应用范围
    apply_scope          VARCHAR(20)     NOT NULL COMMENT '应用范围：GLOBAL-全局 DEPARTMENT-部门 SHIFT-班次 EMPLOYEE-员工',
    department_ids       TEXT            COMMENT '适用部门ID列表（JSON）',
    shift_ids            TEXT            COMMENT '适用班次ID列表（JSON）',
    user_ids             TEXT            COMMENT '适用员工ID列表（JSON）',

    -- 加班计算规则
    overtime_calculation INT             NOT NULL DEFAULT 1 COMMENT '加班计算方式：1-按小时 2-按半小时 3-按15分钟',
    min_overtime_duration INT            NOT NULL DEFAULT 30 COMMENT '最小加班时长（分钟）',
    overtime_precision   INT             NOT NULL DEFAULT 1 COMMENT '加班计算精度：1-分钟 2-5分钟 3-15分钟 4-30分钟',

    -- 加班时间阈值
    workday_start_threshold TIME         COMMENT '工作日加班开始阈值（超过此时间才算加班）',
    workday_end_threshold   TIME         COMMENT '工作日加班结束阈值（超过此时间才算加班）',
    restday_start_threshold  TIME         COMMENT '休息日加班开始阈值',
    restday_end_threshold    TIME         COMMENT '休息日加班结束阈值',

    -- 夜班定义
    night_shift_start     TIME            COMMENT '夜班开始时间',
    night_shift_end       TIME            COMMENT '夜班结束时间',
    night_shift_allowance DECIMAL(10,2)  COMMENT '夜班补贴（每小时）',

    -- 加班倍率
    workday_multiplier   DECIMAL(3,2)    NOT NULL DEFAULT 1.5 COMMENT '工作日加班倍率',
    weekend_multiplier   DECIMAL(3,2)    NOT NULL DEFAULT 2.0 COMMENT '周末加班倍率',
    holiday_multiplier   DECIMAL(3,2)    NOT NULL DEFAULT 3.0 COMMENT '法定节假日加班倍率',

    -- 加班限制
    max_daily_overtime   INT             COMMENT '最大加班时长（每天，分钟）',
    max_monthly_overtime INT             COMMENT '最大加班时长（每月，小时）',
    max_continuous_days INT             COMMENT '最大连续加班天数',

    -- 调休规则
    overtime_to_leave_rate DECIMAL(3,2) COMMENT '加班换休假率（如1.0表示1小时加班换1小时调休）',
    leave_valid_days     INT             COMMENT '调休有效期（天数）',

    -- 默认补偿方式
    default_compensation VARCHAR(20)     COMMENT '默认补偿方式：PAY-支付加班费 LEAVE-调休',

    -- 审批规则
    approval_required    TINYINT         NOT NULL DEFAULT 1 COMMENT '是否需要审批 0-否 1-是',
    approval_workflow_id BIGINT          COMMENT '审批工作流ID',
    auto_approve_hours   DECIMAL(4,2)    COMMENT '自动批准时长阈值（小时）',

    -- 规则状态
    rule_status          TINYINT         NOT NULL DEFAULT 1 COMMENT '规则状态：1-启用 0-禁用',
    execution_order      INT             NOT NULL DEFAULT 1 COMMENT '执行顺序（数字越小优先级越高）',
    rule_priority        INT             NOT NULL DEFAULT 1 COMMENT '规则优先级（数字越小优先级越高）',

    -- 生效时间
    effective_start_time DATETIME        COMMENT '生效开始时间',
    effective_end_time   DATETIME        COMMENT '生效结束时间',
    effective_days       VARCHAR(100)    COMMENT '生效日期（如：1,2,3,4,5表示工作日）',

    -- 其他信息
    description          TEXT            COMMENT '规则描述',
    sort_order           INT             COMMENT '排序号',

    -- 审计字段
    create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id       BIGINT          COMMENT '创建人ID',
    update_user_id       BIGINT          COMMENT '更新人ID',
    deleted_flag         TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version              INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_rule_code (rule_code),
    KEY idx_apply_scope (apply_scope),
    KEY idx_rule_status (rule_status),
    KEY idx_execution_order (execution_order),
    KEY idx_rule_priority (rule_priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班规则配置表';
```

---

### 4. 加班审批记录表 (t_attendance_overtime_approval)

**表说明**: 存储加班审批记录

```sql
CREATE TABLE t_attendance_overtime_approval (
    -- 主键
    approval_id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '审批ID（主键）',

    -- 关联申请
    apply_id             BIGINT          NOT NULL COMMENT '申请ID',
    apply_no             VARCHAR(50)     NOT NULL COMMENT '申请编号',

    -- 审批人信息
    approver_id          BIGINT          NOT NULL COMMENT '审批人ID',
    approver_name        VARCHAR(100)    NOT NULL COMMENT '审批人姓名',
    approver_position_id BIGINT          COMMENT '审批人岗位ID',
    approver_position_name VARCHAR(100)  COMMENT '审批人岗位名称',

    -- 审批信息
    approval_level       INT             NOT NULL COMMENT '审批层级',
    approval_action      VARCHAR(20)     NOT NULL COMMENT '审批操作：APPROVE-批准 REJECT-驳回 CANCEL-撤销',
    approval_comment     VARCHAR(500)    COMMENT '审批意见',
    approval_time        DATETIME        NOT NULL COMMENT '审批时间',

    -- 审批附件
    attachment_url       VARCHAR(500)    COMMENT '审批附件URL',

    -- 审计字段
    create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    PRIMARY KEY (approval_id),
    KEY idx_apply_id (apply_id),
    KEY idx_approver_id (approver_id),
    KEY idx_approval_time (approval_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班审批记录表';
```

---

## 🔗 表关系说明

### ER关系图

```
t_attendance_overtime_apply (加班申请表)
    ├─ 1:N ── t_attendance_overtime_approval (加班审批记录表)
    └─ 1:1 ── t_attendance_overtime_record (加班记录表)
            └─ N:1 ── t_attendance_overtime_rule (加班规则配置表)

WorkShiftEntity (班次表)
    └─ 1:1 ── WorkShiftOvertimeEntity (班次加班配置)
```

### 关键关联关系

1. **加班申请 → 加班记录**: 一对一（可选）
   - 通过 `apply_id` 关联
   - 有申请的加班记录（申请加班）
   - 无申请的加班记录（实际加班，自动生成）

2. **加班申请 → 加班审批记录**: 一对多
   - 通过 `apply_id` 关联
   - 每个申请可能有多个审批记录（多级审批）

3. **加班记录 → 加班规则**: 多对一
   - 通过 `calculation_rule_id` 关联
   - 加班记录按照规则进行计算

---

## 📊 数据字典

### 加班类型 (overtime_type)

| 值 | 说明 | 加班倍率 |
|----|----|----------|
| WORKDAY | 工作日加班 | 1.5倍 |
| OVERTIME | 休息日加班（周末） | 2.0倍 |
| HOLIDAY | 法定节假日加班 | 3.0倍 |

### 补偿方式 (compensation_type)

| 值 | 说明 | 备注 |
|----|----|----|
| PAY | 支付加班费 | 按照加班倍率计算加班费 |
| LEAVE | 调休 | 1小时加班换1小时调休（可配置） |

### 申请状态 (apply_status)

| 值 | 说明 | 备注 |
|----|----|----|
| PENDING | 待审批 | 初始状态 |
| APPROVED | 已批准 | 审批通过 |
| REJECTED | 已驳回 | 审批不通过 |
| CANCELLED | 已撤销 | 申请人撤销 |

### 补偿状态 (compensation_status)

| 值 | 说明 | 备注 |
|----|----|----|
| PENDING | 待补偿 | 未补偿 |
| COMPLETED | 已补偿 | 已支付加班费或已安排调休 |

---

## 🎯 设计原则遵循

### ✅ 架构规范遵循

1. **命名规范**
   - 表名: `t_attendance_overtime_*`（前缀统一）
   - 主键: `xxx_id`（统一使用Long类型）
   - 业务主键: `xxx_no`（申请编号）
   - 索引命名: `uk_*`（唯一索引）、`idx_*`（普通索引）

2. **字段规范**
   - 使用DECIMAL存储金额和时长（精度高）
   - 使用TINYINT存储状态和标记
   - 使用INT/DATETIME存储时间
   - JSON格式存储列表数据（department_ids, shift_ids, user_ids）

3. **审计字段**
   - 所有表包含标准审计字段（create_time, update_time, deleted_flag, version）
   - 支持乐观锁（version字段）
   - 支持逻辑删除（deleted_flag字段）

4. **索引设计**
   - 主键索引（PRIMARY KEY）
   - 唯一索引（UNIQUE KEY）
   - 外键索引（KEY）
   - 查询优化索引（常作为查询条件的字段）

---

## 🚀 后续实施步骤

1. ✅ 数据库设计完成
2. ⏳ 创建实体类（Entity）
3. ⏳ 创建DAO层（Mapper）
4. ⏳ 创建Service层（业务逻辑）
5. ⏳ 创建Controller层（API接口）
6. ⏳ 创建前端页面（Vue组件）
7. ⏳ 编写测试（单元测试 + 集成测试）

---

**文档创建时间**: 2025-01-30
**版本**: v1.0.0
**作者**: IOE-DREAM Team
**状态**: ✅ 数据库设计完成
