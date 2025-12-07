# 考勤规则API

<cite>
**本文档引用的文件**   
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java)
- [考勤规则配置.md](file://documentation\03-业务模块\考勤\考勤规则配置.md)
- [考勤系统数据库ER图设计.md](file://documentation\03-业务模块\考勤\考勤系统数据库ER图设计.md)
- [考勤模块API接口文档.md](file://documentation\06-模板工具\API文档\考勤模块API接口文档.md)
</cite>

## 目录
1. [简介](#简介)
2. [核心API接口](#核心api接口)
3. [考勤规则数据模型](#考勤规则数据模型)
4. [规则应用与优先级处理](#规则应用与优先级处理)
5. [复杂考勤规则配置示例](#复杂考勤规则配置示例)
6. [错误码说明](#错误码说明)

## 简介
考勤规则API提供了完整的考勤规则管理功能，支持创建、修改、查询和删除考勤规则。系统通过灵活的JSON配置结构，支持各种复杂的考勤场景，包括标准考勤、弹性工作制、跨天考勤、加班计算等。考勤规则可应用于不同部门和员工群体，并支持优先级管理，确保规则的准确应用。

## 核心API接口

### 创建考勤规则
**接口地址**: `POST /api/attendance/rules`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleName | string | 是 | 规则名称 |
| ruleCode | string | 是 | 规则编码 |
| description | string | 否 | 规则描述 |
| configJson | json | 是 | 规则配置JSON |
| applicableScope | json | 是 | 适用范围JSON |
| isEnabled | boolean | 否 | 是否启用 |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "创建考勤规则成功",
  "data": 1001,
  "timestamp": 1632456789000
}
```

**接口说明**:
创建新的考勤规则，系统会自动进行规则冲突检测，确保新规则不会与现有规则产生冲突。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L66-L87)

### 更新考勤规则
**接口地址**: `PUT /api/attendance/rules/{ruleId}`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | integer | 是 | 规则ID |
| ruleName | string | 否 | 规则名称 |
| description | string | 否 | 规则描述 |
| configJson | json | 否 | 规则配置JSON |
| applicableScope | json | 否 | 适用范围JSON |
| isEnabled | boolean | 否 | 是否启用 |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "规则更新成功",
  "data": true,
  "timestamp": 1632456789000
}
```

**接口说明**:
更新指定的考勤规则配置，支持部分字段更新。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L96-L122)

### 删除考勤规则
**接口地址**: `DELETE /api/attendance/rules/{ruleId}`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | integer | 是 | 规则ID |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "考勤规则删除成功",
  "data": true,
  "timestamp": 1632456789000
}
```

**接口说明**:
删除指定的考勤规则，系统会检查规则是否正在被使用。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L130-L151)

### 批量删除考勤规则
**接口地址**: `DELETE /api/attendance/rules/batch`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleIds | array | 是 | 规则ID列表 |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "批量删除考勤规则成功",
  "data": 3,
  "timestamp": 1632456789000
}
```

**接口说明**:
批量删除多个考勤规则，返回实际删除的数量。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L159-L175)

### 获取考勤规则详情
**接口地址**: `GET /api/attendance/rules/{ruleId}`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | integer | 是 | 规则ID |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "ruleId": 1001,
    "ruleName": "标准考勤规则",
    "ruleCode": "RULE001",
    "description": "标准工作日考勤规则",
    "configJson": {
      "attendance_settings": {
        "late_tolerance_minutes": 10,
        "early_tolerance_minutes": 10,
        "absent_threshold_hours": 4,
        "min_work_hours": 8.0,
        "break_inclusion": false,
        "overtime_calculation_method": "daily",
        "weekend_overtime_multiplier": 2.0,
        "holiday_overtime_multiplier": 3.0
      },
      "work_time_rules": {
        "flexible_start_time": "08:00",
        "flexible_end_time": "20:00",
        "core_start_time": "10:00",
        "core_end_time": "16:00",
        "break_settings": {
          "auto_deduct": true,
          "break_duration": 60,
          "break_start_time": "12:00",
          "break_end_time": "13:00"
        }
      },
      "special_rules": {
        "holiday_handling": "double_pay",
        "weekend_handling": "normal_overtime",
        "night_shift_settings": {
          "night_start": "22:00",
          "night_end": "06:00",
          "night_shift_bonus": 0.2
        }
      }
    },
    "applicableScope": {
      "departmentIds": [101, 102],
      "employeeIds": [],
      "employeeTypes": ["full_time"]
    },
    "isEnabled": true,
    "createTime": "2025-01-01 00:00:00",
    "updateTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1632456789000
}
```

**接口说明**:
查询指定考勤规则的详细信息，包括完整的配置参数。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L183-L203)

### 获取所有考勤规则
**接口地址**: `GET /api/attendance/rules`

**请求参数**:
无

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": [
    {
      "ruleId": 1001,
      "ruleName": "标准考勤规则",
      "ruleCode": "RULE001",
      "description": "标准工作日考勤规则",
      "isEnabled": true,
      "createTime": "2025-01-01 00:00:00"
    },
    {
      "ruleId": 1002,
      "ruleName": "弹性工作制",
      "ruleCode": "RULE002",
      "description": "弹性工作时间考勤规则",
      "isEnabled": true,
      "createTime": "2025-01-02 00:00:00"
    }
  ],
  "timestamp": 1632456789000
}
```

**接口说明**:
查询所有有效的考勤规则列表，返回简要信息。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L210-L226)

### 获取员工适用的考勤规则
**接口地址**: `GET /api/attendance/rules/applicable`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| departmentId | integer | 否 | 部门ID |
| employeeType | string | 否 | 员工类型 |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "ruleId": 1001,
    "ruleName": "标准考勤规则",
    "ruleCode": "RULE001",
    "description": "标准工作日考勤规则",
    "configJson": {
      "attendance_settings": {
        "late_tolerance_minutes": 10,
        "early_tolerance_minutes": 10,
        "absent_threshold_hours": 4,
        "min_work_hours": 8.0,
        "break_inclusion": false,
        "overtime_calculation_method": "daily",
        "weekend_overtime_multiplier": 2.0,
        "holiday_overtime_multiplier": 3.0
      },
      "work_time_rules": {
        "flexible_start_time": "08:00",
        "flexible_end_time": "20:00",
        "core_start_time": "10:00",
        "core_end_time": "16:00",
        "break_settings": {
          "auto_deduct": true,
          "break_duration": 60,
          "break_start_time": "12:00",
          "break_end_time": "13:00"
        }
      },
      "special_rules": {
        "holiday_handling": "double_pay",
        "weekend_handling": "normal_overtime",
        "night_shift_settings": {
          "night_start": "22:00",
          "night_end": "06:00",
          "night_shift_bonus": 0.2
        }
      }
    },
    "priority": 1,
    "isEnabled": true
  },
  "timestamp": 1632456789000
}
```

**接口说明**:
根据员工信息获取最适用的考勤规则，考虑规则优先级。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L322-L346)

### 更新考勤规则状态
**接口地址**: `PUT /api/attendance/rules/{ruleId}/status`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | integer | 是 | 规则ID |
| enabled | boolean | 是 | 是否启用 |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "考勤规则状态更新成功",
  "data": true,
  "timestamp": 1632456789000
}
```

**接口说明**:
启用或禁用指定的考勤规则。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L290-L312)

### 验证考勤规则冲突
**接口地址**: `POST /api/attendance/rules/validate/conflict`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configJson | json | 是 | 规则配置JSON |
| applicableScope | json | 是 | 适用范围JSON |

**响应示例**:
```json
{
  "success": true,
  "code": 0,
  "msg": "验证成功",
  "data": false,
  "timestamp": 1632456789000
}
```

**接口说明**:
检查考勤规则是否存在冲突，返回true表示有冲突，false表示无冲突。

**Section sources**
- [AttendanceRuleController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-attendance-service_src_main_java_net_lab1024_sa_attendance_controller_AttendanceRuleController.java#L354-L374)

## 考勤规则数据模型

### 考勤规则表 (attendance_rules)
| 字段名 | 数据类型 | 长度 | 是否为空 | 默认值 | 说明 |
|--------|----------|------|----------|--------|------|
| id | bigint | - | NOT NULL | AUTO_INCREMENT | 主键ID |
| rule_name | varchar | 100 | NOT NULL | - | 规则名称 |
| rule_code | varchar | 50 | NOT NULL | - | 规则编码 |
| rule_type | varchar | 50 | NOT NULL | - | 规则类型(考勤规则/预警规则) |
| description | text | - | NULL | - | 规则描述 |
| config_json | json | - | NULL | - | 规则配置JSON |
| warning_config | json | - | NULL | - | 预警配置JSON(包含预警类型、连续天数阈值、预警级别等) |
| notification_config | json | - | NULL | - | 通知配置JSON |
| applicable_scope | json | - | NULL | - | 适用范围JSON |
| is_enabled | tinyint | - | NOT NULL | 1 | 是否启用(0:禁用,1:启用) |
| status | tinyint | - | NOT NULL | 1 | 状态(0:删除,1:正常) |
| create_time | datetime | - | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | - | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

### config_json（考勤规则配置）JSON结构
```json
{
  "rule_name": "标准考勤规则",
  "attendance_settings": {
    "late_tolerance_minutes": 10,           // 迟到容忍时间
    "early_tolerance_minutes": 10,          // 早退容忍时间
    "absent_threshold_hours": 4,            // 旷工阈值小时数
    "min_work_hours": 8.0,                  // 最小工作时长
    "break_inclusion": false,               // 是否包含休息时间
    "overtime_calculation_method": "daily", // 加班计算方式
    "weekend_overtime_multiplier": 2.0,     // 周末加班倍数
    "holiday_overtime_multiplier": 3.0      // 节假日加班倍数
  },
  "work_time_rules": {
    "flexible_start_time": "08:00",         // 弹性开始时间
    "flexible_end_time": "20:00",           // 弹性结束时间
    "core_start_time": "10:00",             // 核心开始时间
    "core_end_time": "16:00",               // 核心结束时间
    "break_settings": {
      "auto_deduct": true,                  // 自动扣除休息
      "break_duration": 60,                 // 休息时长（分钟）
      "break_start_time": "12:00",          // 休息开始时间
      "break_end_time": "13:00"             // 休息结束时间
    }
  },
  "special_rules": {
    "holiday_handling": "double_pay",       // 节假日处理方式
    "weekend_handling": "normal_overtime",  // 周末处理方式
    "night_shift_settings": {
      "night_start": "22:00",               // 夜班开始时间
      "night_end": "06:00",                 // 夜班结束时间
      "night_shift_bonus": 0.2              // 夜班补贴比例
    }
  }
}
```

### applicable_scope（适用范围）JSON结构
```json
{
  "departmentIds": [101, 102],              // 适用部门ID列表
  "employeeIds": [1001, 1002],              // 适用员工ID列表
  "employeeTypes": ["full_time", "part_time"], // 适用员工类型
  "priority": 1                             // 规则优先级，数值越小优先级越高
}
```

**Section sources**
- [考勤规则配置.md](file://documentation\03-业务模块\考勤\考勤规则配置.md#L8-L24)
- [考勤系统数据库ER图设计.md](file://documentation\03-业务模块\考勤\考勤系统数据库ER图设计.md#L1332-L1347)

## 规则应用与优先级处理

### 规则优先级处理逻辑
考勤规则的优先级处理遵循以下原则：

1. **优先级数值越小，优先级越高**：优先级1的规则优先于优先级2的规则
2. **精确匹配优先**：员工ID匹配的规则优先于部门匹配的规则
3. **类型匹配**：员工类型匹配的规则优先于通用规则
4. **时间范围**：特定时间段的规则优先于常规规则

### 规则应用流程
```
触发考勤事件 -> 加载适用规则 -> 执行规则判断 -> 生成考勤结果 -> 触发预警通知 -> 更新考勤记录
```

### 适用范围配置
考勤规则可以通过以下方式指定适用范围：
- **部门范围**：指定规则适用于哪些部门
- **员工范围**：指定规则适用于哪些具体员工
- **员工类型**：指定规则适用于哪些类型的员工（如全职、兼职、实习生等）
- **优先级设置**：当多个规则都适用时，按优先级确定最终应用的规则

**Section sources**
- [考勤规则配置.md](file://documentation\03-业务模块\考勤\考勤规则配置.md#L467-L476)

## 复杂考勤规则配置示例

### 跨天考勤规则
```json
{
  "rule_name": "夜班考勤规则",
  "attendance_settings": {
    "late_tolerance_minutes": 15,
    "early_tolerance_minutes": 15,
    "absent_threshold_hours": 4,
    "min_work_hours": 8.0,
    "break_inclusion": true,
    "overtime_calculation_method": "daily"
  },
  "work_time_rules": {
    "flexible_start_time": "20:00",
    "flexible_end_time": "08:00",
    "core_start_time": "22:00",
    "core_end_time": "06:00",
    "break_settings": {
      "auto_deduct": true,
      "break_duration": 60,
      "break_start_time": "00:00",
      "break_end_time": "01:00"
    }
  },
  "special_rules": {
    "holiday_handling": "double_pay",
    "weekend_handling": "normal_overtime",
    "night_shift_settings": {
      "night_start": "22:00",
      "night_end": "06:00",
      "night_shift_bonus": 0.3
    }
  }
}
```

### 午休时间扣除规则
```json
{
  "rule_name": "标准考勤规则（含午休）",
  "attendance_settings": {
    "late_tolerance_minutes": 10,
    "early_tolerance_minutes": 10,
    "absent_threshold_hours": 4,
    "min_work_hours": 8.0,
    "break_inclusion": false,
    "overtime_calculation_method": "daily"
  },
  "work_time_rules": {
    "flexible_start_time": "08:00",
    "flexible_end_time": "18:00",
    "core_start_time": "09:00",
    "core_end_time": "17:00",
    "break_settings": {
      "auto_deduct": true,
      "break_duration": 60,
      "break_start_time": "12:00",
      "break_end_time": "13:00"
    }
  },
  "special_rules": {
    "holiday_handling": "double_pay",
    "weekend_handling": "normal_overtime"
  }
}
```

### 弹性工作制规则
```json
{
  "rule_name": "弹性工作制",
  "attendance_settings": {
    "late_tolerance_minutes": 30,
    "early_tolerance_minutes": 30,
    "absent_threshold_hours": 4,
    "min_work_hours": 8.0,
    "break_inclusion": false,
    "overtime_calculation_method": "daily"
  },
  "work_time_rules": {
    "flexible_start_time": "07:00",
    "flexible_end_time": "21:00",
    "core_start_time": "10:00",
    "core_end_time": "16:00",
    "break_settings": {
      "auto_deduct": true,
      "break_duration": 60,
      "break_start_time": "12:00",
      "break_end_time": "13:00"
    }
  },
  "special_rules": {
    "holiday_handling": "double_pay",
    "weekend_handling": "normal_overtime"
  }
}
```

**Section sources**
- [考勤规则配置.md](file://documentation\03-业务模块\考勤\考勤规则配置.md#L289-L325)

## 错误码说明

### 通用错误码
| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| -1 | 系统错误 |
| 1001 | 参数错误 |
| 1002 | 数据不存在 |
| 1003 | 权限不足 |
| 1004 | 数据已存在 |
| 1005 | 数据验证失败 |

### 考勤规则专用错误码
| 错误码 | 说明 |
|--------|------|
| 2001 | 规则编码已存在 |
| 2002 | 规则名称已存在 |
| 2003 | 规则配置无效 |
| 2004 | 规则存在冲突 |
| 2005 | 规则正在被使用，无法删除 |
| 2006 | 适用范围配置无效 |
| 2007 | 规则优先级冲突 |

**Section sources**
- [考勤模块API接口文档.md](file://documentation\06-模板工具\API文档\考勤模块API接口文档.md#L944-L954)