# 考勤管理模块 - API接口设计文档

> **版本**: v2.0.0  
> **更新日期**: 2025-12-17  
> **服务端口**: 8091  
> **基础路径**: /api/v1/attendance

---

## 1. 接口设计规范

### 1.1 URL命名规范

- 使用小写字母和中划线
- RESTful风格
- 版本号在路径中体现

### 1.2 请求响应格式

**统一响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1702800000000
}
```

**分页响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  }
}
```

### 1.3 错误码规范

| 错误码范围 | 说明 |
|-----------|------|
| 200 | 成功 |
| 400-499 | 客户端错误 |
| 500-599 | 服务端错误 |
| 4001-4099 | 考勤模块业务错误 |

---

## 2. 班次时间管理接口

### 2.1 时间段管理

#### 2.1.1 创建时间段

**POST** `/api/v1/attendance/time-periods`

**请求参数**:
```json
{
  "periodCode": "PERIOD001",
  "periodName": "标准上午班",
  "startTime": "09:00:00",
  "endTime": "12:00:00",
  "lateTolerance": 15,
  "earlyTolerance": 15,
  "mustClockIn": true,
  "mustClockOut": true,
  "workDuration": 3.0,
  "overtimeRules": {}
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

#### 2.1.2 查询时间段列表

**GET** `/api/v1/attendance/time-periods`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| periodName | String | 否 | 名称模糊查询 |
| status | Integer | 否 | 状态 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "periodCode": "PERIOD001",
        "periodName": "标准上午班",
        "startTime": "09:00:00",
        "endTime": "12:00:00",
        "lateTolerance": 15,
        "earlyTolerance": 15,
        "mustClockIn": true,
        "mustClockOut": true,
        "workDuration": 3.0,
        "status": 1,
        "createTime": "2025-01-01 00:00:00"
      }
    ],
    "total": 10,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 2.1.3 获取时间段详情

**GET** `/api/v1/attendance/time-periods/{id}`

#### 2.1.4 更新时间段

**PUT** `/api/v1/attendance/time-periods/{id}`

#### 2.1.5 删除时间段

**DELETE** `/api/v1/attendance/time-periods/{id}`

---

### 2.2 班次管理

#### 2.2.1 创建班次

**POST** `/api/v1/attendance/shifts`

**请求参数**:
```json
{
  "shiftCode": "SHIFT001",
  "shiftName": "标准班",
  "shiftType": "regular",
  "cycleUnit": "day",
  "cycleCount": 1,
  "flexibleHours": null,
  "coreHours": null,
  "shiftConfig": {
    "crossDayEnabled": false,
    "maxContinuousDays": 6
  },
  "periodIds": [1, 2]
}
```

#### 2.2.2 查询班次列表

**GET** `/api/v1/attendance/shifts`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shiftName | String | 否 | 名称模糊查询 |
| shiftType | String | 否 | 班次类型 |
| status | Integer | 否 | 状态 |

#### 2.2.3 获取班次详情

**GET** `/api/v1/attendance/shifts/{id}`

#### 2.2.4 更新班次

**PUT** `/api/v1/attendance/shifts/{id}`

#### 2.2.5 删除班次

**DELETE** `/api/v1/attendance/shifts/{id}`

#### 2.2.6 复制班次

**POST** `/api/v1/attendance/shifts/{id}/copy`

#### 2.2.7 获取班次时间线

**GET** `/api/v1/attendance/shifts/{id}/timeline`

---

### 2.3 班次时间段关联

#### 2.3.1 获取班次的时间段

**GET** `/api/v1/attendance/shifts/{shiftId}/periods`

#### 2.3.2 添加时间段到班次

**POST** `/api/v1/attendance/shift-period-relations`

**请求参数**:
```json
{
  "shiftId": 1,
  "periodId": 2,
  "sequenceOrder": 1
}
```

#### 2.3.3 批量设置班次时间段

**PUT** `/api/v1/attendance/shifts/{shiftId}/periods`

**请求参数**:
```json
{
  "periodIds": [1, 2, 3]
}
```

#### 2.3.4 删除时间段关联

**DELETE** `/api/v1/attendance/shift-period-relations/{id}`

---

## 3. 排班管理接口

### 3.1 排班记录

#### 3.1.1 获取排班日历

**GET** `/api/v1/attendance/schedule-calendar`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 否 | 部门ID |
| employeeId | Long | 否 | 员工ID |
| month | String | 是 | 月份(YYYY-MM) |

**响应**:
```json
{
  "code": 200,
  "data": {
    "month": "2025-01",
    "schedules": [
      {
        "employeeId": 1,
        "employeeName": "张三",
        "days": [
          {
            "date": "2025-01-01",
            "shiftId": 1,
            "shiftName": "标准班",
            "isTemporary": false
          }
        ]
      }
    ]
  }
}
```

#### 3.1.2 创建排班记录

**POST** `/api/v1/attendance/schedule-records`

**请求参数**:
```json
{
  "employeeId": 1,
  "scheduleDate": "2025-01-15",
  "shiftId": 1,
  "scheduleType": "normal",
  "reason": ""
}
```

#### 3.1.3 批量创建排班

**POST** `/api/v1/attendance/schedule-records/batch`

**请求参数**:
```json
{
  "employeeIds": [1, 2, 3],
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "shiftId": 1,
  "conflictStrategy": "skip"
}
```

#### 3.1.4 查询员工排班

**GET** `/api/v1/attendance/schedule-records`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

#### 3.1.5 更新排班记录

**PUT** `/api/v1/attendance/schedule-records/{id}`

#### 3.1.6 删除排班记录

**DELETE** `/api/v1/attendance/schedule-records/{id}`

---

### 3.2 排班模板

#### 3.2.1 创建排班模板

**POST** `/api/v1/attendance/schedule-templates`

**请求参数**:
```json
{
  "templateName": "技术部标准排班",
  "templateType": "department",
  "departmentId": 1,
  "templateConfig": {
    "cycleType": "weekly",
    "cycleDays": 7,
    "schedulePattern": [
      {"dayOfWeek": 1, "shiftId": 1},
      {"dayOfWeek": 2, "shiftId": 1},
      {"dayOfWeek": 3, "shiftId": 1},
      {"dayOfWeek": 4, "shiftId": 1},
      {"dayOfWeek": 5, "shiftId": 1}
    ]
  }
}
```

#### 3.2.2 查询模板列表

**GET** `/api/v1/attendance/schedule-templates`

#### 3.2.3 应用排班模板

**POST** `/api/v1/attendance/schedule-templates/{id}/apply`

**请求参数**:
```json
{
  "employeeIds": [1, 2, 3],
  "startDate": "2025-01-01",
  "endDate": "2025-01-31"
}
```

#### 3.2.4 复制模板

**POST** `/api/v1/attendance/schedule-templates/{id}/copy`

---

### 3.3 临时排班覆盖

#### 3.3.1 创建临时排班

**POST** `/api/v1/attendance/schedule-overrides`

**请求参数**:
```json
{
  "employeeId": 1,
  "scheduleDate": "2025-01-15",
  "shiftId": 2,
  "priority": 10,
  "reason": "临时项目支援"
}
```

#### 3.3.2 查询临时排班

**GET** `/api/v1/attendance/schedule-overrides`

#### 3.3.3 删除临时排班

**DELETE** `/api/v1/attendance/schedule-overrides/{id}`

---

## 4. 考勤规则配置接口

### 4.1 考勤规则

#### 4.1.1 创建考勤规则

**POST** `/api/v1/attendance/rules`

**请求参数**:
```json
{
  "ruleCode": "RULE001",
  "ruleName": "标准考勤规则",
  "ruleType": "attendance",
  "description": "适用于正常员工的考勤规则",
  "configJson": {
    "lateTolerance": 10,
    "earlyTolerance": 10,
    "absentThreshold": 4
  },
  "applicableScope": {
    "departments": [1, 2],
    "positions": []
  }
}
```

#### 4.1.2 查询规则列表

**GET** `/api/v1/attendance/rules`

#### 4.1.3 获取规则详情

**GET** `/api/v1/attendance/rules/{id}`

#### 4.1.4 更新规则

**PUT** `/api/v1/attendance/rules/{id}`

#### 4.1.5 启用/禁用规则

**POST** `/api/v1/attendance/rules/{id}/toggle`

---

### 4.2 考勤点管理

#### 4.2.1 创建考勤点

**POST** `/api/v1/attendance/points`

**请求参数**:
```json
{
  "pointCode": "POINT001",
  "pointName": "总部大楼考勤点",
  "longitude": 116.404,
  "latitude": 39.915,
  "radius": 100,
  "deviceId": 1
}
```

#### 4.2.2 查询考勤点列表

**GET** `/api/v1/attendance/points`

#### 4.2.3 验证位置

**POST** `/api/v1/attendance/points/verify-location`

**请求参数**:
```json
{
  "longitude": 116.404,
  "latitude": 39.915
}
```

---

### 4.3 预警规则

#### 4.3.1 创建预警规则

**POST** `/api/v1/attendance/warning-rules`

#### 4.3.2 查询预警规则

**GET** `/api/v1/attendance/warning-rules`

#### 4.3.3 测试预警规则

**POST** `/api/v1/attendance/warning-rules/{id}/test`

---

### 4.4 通知规则

#### 4.4.1 创建通知规则

**POST** `/api/v1/attendance/notification-rules`

#### 4.4.2 查询通知规则

**GET** `/api/v1/attendance/notification-rules`

#### 4.4.3 发送测试通知

**POST** `/api/v1/attendance/notification-rules/{id}/test`

---

## 5. 异常管理接口

### 5.1 假种管理

#### 5.1.1 创建假种

**POST** `/api/v1/attendance/leave-types`

**请求参数**:
```json
{
  "leaveTypeCode": "ANNUAL",
  "leaveTypeName": "年假",
  "leaveCategory": "statutory",
  "description": "法定年假",
  "maxDaysPerYear": 15,
  "maxDaysPerApplication": 5,
  "requireCertificate": false,
  "deductSalary": false,
  "approvalWorkflow": {
    "approvalLevels": [
      {"level": 1, "approverType": "direct_manager"}
    ]
  }
}
```

#### 5.1.2 查询假种列表

**GET** `/api/v1/attendance/leave-types`

#### 5.1.3 获取员工假期余额

**GET** `/api/v1/attendance/leave-types/balance`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "leaveTypeId": 1,
      "leaveTypeName": "年假",
      "totalDays": 15,
      "usedDays": 5,
      "remainingDays": 10
    }
  ]
}
```

---

### 5.2 异常申请

#### 5.2.1 提交请假申请

**POST** `/api/v1/attendance/exception-applications/leave`

**请求参数**:
```json
{
  "leaveTypeId": 1,
  "startDate": "2025-01-15",
  "endDate": "2025-01-17",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "duration": 3,
  "reason": "个人事务",
  "certificateUrl": ""
}
```

#### 5.2.2 提交加班申请

**POST** `/api/v1/attendance/exception-applications/overtime`

**请求参数**:
```json
{
  "overtimeType": "weekend",
  "startDate": "2025-01-18",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "duration": 8,
  "reason": "项目紧急"
}
```

#### 5.2.3 提交补签申请

**POST** `/api/v1/attendance/exception-applications/makeup`

**请求参数**:
```json
{
  "applicationDate": "2025-01-15",
  "clockInTime": "2025-01-15 09:00:00",
  "clockOutTime": "2025-01-15 18:00:00",
  "reason": "忘记打卡",
  "certificateUrl": ""
}
```

#### 5.2.4 提交销假申请

**POST** `/api/v1/attendance/exception-applications/cancel-leave`

**请求参数**:
```json
{
  "originalApplicationId": 1,
  "actualEndDate": "2025-01-16",
  "actualEndTime": "18:00:00"
}
```

#### 5.2.5 查询申请列表

**GET** `/api/v1/attendance/exception-applications`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 否 | 员工ID |
| applicationType | String | 否 | 申请类型 |
| approvalStatus | Integer | 否 | 审批状态 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

#### 5.2.6 获取申请详情

**GET** `/api/v1/attendance/exception-applications/{id}`

#### 5.2.7 撤销申请

**POST** `/api/v1/attendance/exception-applications/{id}/cancel`

#### 5.2.8 上传证明文件

**POST** `/api/v1/attendance/exception-applications/{id}/upload-certificate`

---

### 5.3 审批管理

#### 5.3.1 获取待审批列表

**GET** `/api/v1/attendance/pending-approvals`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approverId | Long | 是 | 审批人ID |
| applicationType | String | 否 | 申请类型 |

#### 5.3.2 审批通过

**POST** `/api/v1/attendance/exception-applications/{id}/approve`

**请求参数**:
```json
{
  "approvalComment": "同意"
}
```

#### 5.3.3 审批拒绝

**POST** `/api/v1/attendance/exception-applications/{id}/reject`

**请求参数**:
```json
{
  "approvalComment": "时间冲突，请重新申请"
}
```

#### 5.3.4 批量审批

**POST** `/api/v1/attendance/exception-applications/batch-approve`

**请求参数**:
```json
{
  "applicationIds": [1, 2, 3],
  "approvalResult": "approved",
  "approvalComment": "批量通过"
}
```

#### 5.3.5 获取审批历史

**GET** `/api/v1/attendance/approval-history`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| applicationId | Long | 是 | 申请ID |

---

## 6. 考勤数据接口

### 6.1 打卡记录

#### 6.1.1 员工打卡

**POST** `/api/v1/attendance/clock`

**请求参数**:
```json
{
  "employeeId": 1,
  "clockType": "clock_in",
  "longitude": 116.404,
  "latitude": 39.915,
  "deviceId": 1,
  "photoUrl": ""
}
```

#### 6.1.2 查询打卡记录

**GET** `/api/v1/attendance/clock-records`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

#### 6.1.3 获取今日打卡状态

**GET** `/api/v1/attendance/clock-records/today`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |

---

### 6.2 考勤结果

#### 6.2.1 查询考勤结果

**GET** `/api/v1/attendance/results`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 否 | 员工ID |
| departmentId | Long | 否 | 部门ID |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| attendanceStatus | String | 否 | 考勤状态 |

#### 6.2.2 获取员工月度考勤

**GET** `/api/v1/attendance/results/monthly`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| month | String | 是 | 月份(YYYY-MM) |

#### 6.2.3 重新计算考勤

**POST** `/api/v1/attendance/results/recalculate`

**请求参数**:
```json
{
  "employeeId": 1,
  "date": "2025-01-15"
}
```

---

## 7. 汇总报表接口

### 7.1 月度汇总

#### 7.1.1 查询月度汇总

**GET** `/api/v1/attendance/summaries`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 否 | 部门ID |
| month | String | 是 | 月份(YYYY-MM) |

#### 7.1.2 获取员工月度汇总

**GET** `/api/v1/attendance/summaries/employee`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| month | String | 是 | 月份(YYYY-MM) |

---

### 7.2 统计分析

#### 7.2.1 部门出勤统计

**GET** `/api/v1/attendance/statistics/department`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 是 | 部门ID |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

#### 7.2.2 异常统计

**GET** `/api/v1/attendance/statistics/exceptions`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 否 | 部门ID |
| month | String | 是 | 月份(YYYY-MM) |

---

### 7.3 报表导出

#### 7.3.1 导出考勤明细

**GET** `/api/v1/attendance/export/details`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 否 | 部门ID |
| month | String | 是 | 月份 |
| format | String | 否 | 格式(excel/pdf) |

#### 7.3.2 导出月度汇总

**GET** `/api/v1/attendance/export/summary`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| departmentId | Long | 否 | 部门ID |
| month | String | 是 | 月份 |
| format | String | 否 | 格式(excel/pdf) |

---

## 8. 预警记录接口

### 8.1 查询预警记录

**GET** `/api/v1/attendance/warning-records`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 否 | 员工ID |
| warningType | String | 否 | 预警类型 |
| isHandled | Integer | 否 | 是否已处理 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

### 8.2 处理预警

**POST** `/api/v1/attendance/warning-records/{id}/handle`

**请求参数**:
```json
{
  "handleComment": "已与员工沟通"
}
```

---

*本文档持续更新中*

