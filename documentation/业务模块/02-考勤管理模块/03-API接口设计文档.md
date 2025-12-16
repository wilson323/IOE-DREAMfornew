# 考勤管理模块 - API接口设计文档

## 1. 概述

本文档定义了IOE-DREAM智慧园区安防综合管理平台中考勤管理模块的REST API接口规范。

### 1.1 接口基础信息

- **基础URL**: `http://localhost:8091/api/v1/attendance`
- **认证方式**: Bearer Token (Sa-Token)
- **数据格式**: JSON (UTF-8)
- **API版本**: v1

### 1.2 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2025-01-30T10:00:00.000Z"
}
```

### 1.3 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 2. 考勤记录管理接口

### 2.1 分页查询考勤记录

**接口地址**: `GET /record/query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |
| employeeId | Long | 否 | 员工ID |
| departmentId | Long | 否 | 部门ID |
| startDate | LocalDate | 否 | 开始日期，格式yyyy-MM-dd |
| endDate | LocalDate | 否 | 结束日期，格式yyyy-MM-dd |
| status | String | 否 | 考勤状态：NORMAL/LATE/EARLY/ABSENT |
| attendanceType | String | 否 | 考勤类型：CHECK_IN/CHECK_OUT |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 150,
    "list": [
      {
        "recordId": 1001,
        "userId": 1001,
        "userName": "张三",
        "departmentId": 10,
        "departmentName": "技术部",
        "attendanceDate": "2025-01-30",
        "punchTime": "2025-01-30T09:00:00",
        "attendanceStatus": "NORMAL",
        "attendanceType": "CHECK_IN",
        "deviceName": "一楼考勤机",
        "punchAddress": "一楼大厅"
      }
    ]
  }
}
```

### 2.2 创建考勤记录

**接口地址**: `POST /record/create`

**请求体**:
```json
{
  "userId": 1001,
  "deviceId": 1,
  "deviceCode": "DEV001",
  "punchType": 0,
  "punchTime": 1706582400,
  "punchAddress": "北京市朝阳区",
  "longitude": 116.404,
  "latitude": 39.915
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1001
}
```

### 2.3 获取考勤记录统计

**接口地址**: `GET /record/statistics`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | LocalDate | 是 | 开始日期 |
| endDate | LocalDate | 是 | 结束日期 |
| employeeId | Long | 否 | 员工ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDays": 22,
    "normalDays": 20,
    "lateCount": 2,
    "earlyCount": 1,
    "absentDays": 0,
    "overtimeHours": 16.5,
    "weekendOvertimeHours": 8.0
  }
}
```

---

## 3. 班次管理接口

### 3.1 分页查询班次列表

**接口地址**: `GET /shift/query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |
| shiftName | String | 否 | 班次名称 |
| shiftType | Integer | 否 | 班次类型 |
| status | Integer | 否 | 状态 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 5,
    "list": [
      {
        "shiftId": 1,
        "shiftName": "正常班",
        "shiftType": 1,
        "startTime": "09:00:00",
        "endTime": "18:00:00",
        "workHours": 8.0,
        "breakMinutes": 60,
        "flexibleStartTime": 30,
        "flexibleEndTime": 30,
        "status": 1
      }
    ]
  }
}
```

### 3.2 创建班次

**接口地址**: `POST /shift/create`

**请求体**:
```json
{
  "shiftName": "夜班",
  "shiftType": 2,
  "startTime": "22:00:00",
  "endTime": "06:00:00",
  "workHours": 8.0,
  "breakMinutes": 30,
  "isOvernight": true,
  "isFlexible": false,
  "colorCode": "#FF5722"
}
```

### 3.3 更新班次

**接口地址**: `PUT /shift/{shiftId}`

**请求体**: 同创建班次

### 3.4 删除班次

**接口地址**: `DELETE /shift/{shiftId}`

---

## 4. 排班管理接口

### 4.1 查询排班日历

**接口地址**: `GET /schedule/calendar`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| year | Integer | 是 | 年份 |
| month | Integer | 是 | 月份 |
| departmentId | Long | 否 | 部门ID |
| employeeId | Long | 否 | 员工ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "year": 2025,
    "month": 1,
    "schedules": [
      {
        "date": "2025-01-01",
        "dayOfWeek": 3,
        "isHoliday": false,
        "employees": [
          {
            "employeeId": 1001,
            "employeeName": "张三",
            "shiftId": 1,
            "shiftName": "正常班",
            "scheduleType": "NORMAL"
          }
        ]
      }
    ]
  }
}
```

### 4.2 批量排班

**接口地址**: `POST /schedule/batch`

**请求体**:
```json
{
  "scheduleType": "TEMPLATE",
  "templateId": 1,
  "employees": [1001, 1002, 1003],
  "startDate": "2025-02-01",
  "endDate": "2025-02-28",
  "shiftId": 1
}
```

### 4.3 智能排班

**接口地址**: `POST /schedule/intelligent`

**请求体**:
```json
{
  "employees": [1001, 1002, 1003],
  "startDate": "2025-02-01",
  "endDate": "2025-02-28",
  "useHistoricalData": true,
  "optimizeFor": "BALANCE"
}
```

---

## 5. 异常管理接口

### 5.1 分页查询异常申请

**接口地址**: `GET /exception/query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |
| applicationType | String | 否 | 申请类型 |
| approvalStatus | Integer | 否 | 审批状态 |
| employeeId | Long | 否 | 员工ID |
| startDate | LocalDate | 否 | 开始日期 |
| endDate | LocalDate | 否 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 15,
    "list": [
      {
        "applicationId": 1,
        "employeeId": 1001,
        "employeeName": "张三",
        "applicationType": "LEAVE",
        "leaveType": "事假",
        "startDate": "2025-01-15",
        "endDate": "2025-01-17",
        "duration": 3.0,
        "reason": "家中有事",
        "approvalStatus": 1,
        "approvalTime": "2025-01-14T10:00:00",
        "approverName": "李经理"
      }
    ]
  }
}
```

### 5.2 提交请假申请

**接口地址**: `POST /exception/leave/apply`

**请求体**:
```json
{
  "leaveType": "事假",
  "startDate": "2025-02-01",
  "endDate": "2025-02-03",
  "duration": 3.0,
  "reason": "家中有急事处理",
  "certificateUrl": "https://example.com/cert.jpg"
}
```

### 5.3 提交补签申请

**接口地址**: `POST /exception/supplement/apply`

**请求体**:
```json
{
  "supplementType": "CHECK_IN",
  "supplementDate": "2025-01-30",
  "supplementTime": "09:15:00",
  "reason": "忘记打卡",
  "certificateUrl": "https://example.com/proof.jpg"
}
```

### 5.4 提交加班申请

**接口地址**: `POST /exception/overtime/apply`

**请求体**:
```json
{
  "overtimeType": "WEEKEND",
  "startDate": "2025-02-08",
  "endDate": "2025-02-08",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "duration": 8.0,
  "reason": "项目紧急上线",
  "certificateUrl": "https://example.com/overtime.jpg"
}
```

### 5.5 审批异常申请

**接口地址**: `POST /exception/{applicationId}/approve`

**请求体**:
```json
{
  "approvalResult": "APPROVE",
  "approvalComment": "同意申请",
  "isEarlyReturn": false
}
```

---

## 6. 考勤计算接口

### 6.1 触发考勤计算

**接口地址**: `POST /calculation/trigger`

**请求体**:
```json
{
  "calculationType": "RECALCULATE",
  "dateRange": {
    "startDate": "2025-01-01",
    "endDate": "2025-01-31"
  },
  "scope": {
    "type": "DEPARTMENT",
    "departmentIds": [10, 20, 30]
  },
  "options": {
    "includeOvertime": true,
    "includeException": true,
    "useIntelligentMatching": true
  }
}
```

### 6.2 获取计算进度

**接口地址**: `GET /calculation/progress/{taskId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": "task_123456",
    "status": "RUNNING",
    "progress": 65,
    "totalEmployees": 1000,
    "processedEmployees": 650,
    "estimatedCompletion": "2025-01-30T11:00:00",
    "errorCount": 2
  }
}
```

---

## 7. 考勤报表接口

### 7.1 生成日报表

**接口地址**: `GET /report/daily`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportDate | LocalDate | 是 | 报表日期 |
| departmentId | Long | 否 | 部门ID |
| employeeId | Long | 否 | 员工ID |
| format | String | 否 | 导出格式：JSON/EXCEL/PDF |

### 7.2 生成月报表

**接口地址**: `GET /report/monthly`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportMonth | String | 是 | 报表月份，格式：yyyy-MM |
| departmentId | Long | 否 | 部门ID |
| employeeId | Long | 否 | 员工ID |
| format | String | 否 | 导出格式：JSON/EXCEL/PDF |

### 7.3 生成汇总统计

**接口地址**: `GET /report/summary`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | LocalDate | 是 | 开始日期 |
| endDate | LocalDate | 是 | 结束日期 |
| groupBy | String | 否 | 分组类型：DEPARTMENT/EMPLOYEE |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summaryType": "DEPARTMENT",
    "summaryPeriod": "2025-01-01至2025-01-31",
    "departments": [
      {
        "departmentId": 10,
        "departmentName": "技术部",
        "employeeCount": 25,
        "totalWorkDays": 550,
        "actualWorkDays": 535,
        "attendanceRate": 0.973,
        "lateCount": 15,
        "earlyCount": 8,
        "overtimeHours": 120.5,
        "leaveDays": 12.5
      }
    ]
  }
}
```

---

## 8. 考勤规则配置接口

### 8.1 查询考勤规则列表

**接口地址**: `GET /rules/query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleType | String | 否 | 规则类型 |
| isEnabled | Boolean | 否 | 是否启用 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "ruleId": 1,
      "ruleName": "标准考勤规则",
      "ruleCode": "STANDARD_001",
      "ruleType": "ATTENDANCE",
      "description": "企业标准考勤规则",
      "configJson": {
        "lateThreshold": 10,
        "earlyThreshold": 10,
        "overtimeThreshold": 60
      },
      "isEnabled": true,
      "createTime": "2025-01-01T00:00:00"
    }
  ]
}
```

### 8.2 创建考勤规则

**接口地址**: `POST /rules/create`

**请求体**:
```json
{
  "ruleName": "弹性工作制规则",
  "ruleCode": "FLEXIBLE_001",
  "ruleType": "ATTENDANCE",
  "description": "弹性工作时间规则",
  "configJson": {
    "lateThreshold": 60,
    "earlyThreshold": 30,
    "coreStartTime": "10:00",
    "coreEndTime": "16:00",
    "flexibleStartTime": 120,
    "flexibleEndTime": 60
  },
  "applicableScope": {
    "departments": [10, 20],
    "excludeEmployees": [1001]
  }
}
```

---

## 9. 假种管理接口

### 9.1 查询假种列表

**接口地址**: `GET/leave/types`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "leaveTypeId": 1,
      "leaveTypeCode": "ANNUAL",
      "leaveTypeName": "年假",
      "leaveCategory": "WELFARE",
      "maxDaysPerYear": 15.0,
      "maxDaysPerApplication": 5.0,
      "requireCertificate": false,
      "isEnabled": true
    },
    {
      "leaveTypeId": 2,
      "leaveTypeCode": "SICK",
      "leaveTypeName": "病假",
      "leaveCategory": "SICK",
      "maxDaysPerYear": 30.0,
      "maxDaysPerApplication": 10.0,
      "requireCertificate": true,
      "isEnabled": true
    }
  ]
}
```

---

## 10. 移动端接口

### 10.1 移动端打卡

**接口地址**: `POST /mobile/punch`

**请求体**:
```json
{
  "punchType": "CHECK_IN",
  "location": {
    "longitude": 116.404,
    "latitude": 39.915,
    "address": "北京市朝阳区"
  },
  "deviceId": "MOBILE_001",
  "photoBase64": "base64_encoded_image_data"
}
```

### 10.2 获取今日考勤状态

**接口地址**: `GET /mobile/today-status`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "employeeId": 1001,
    "employeeName": "张三",
    "shiftName": "正常班",
    "workStartTime": "09:00:00",
    "workEndTime": "18:00:00",
    "hasClockIn": true,
    "hasClockOut": false,
    "clockInTime": "2025-01-30T09:00:00",
    "clockOutTime": null,
    "attendanceStatus": "NORMAL",
    "remainingWorkTime": "7h30m"
  }
}
```

---

## 11. 数据导入导出接口

### 11.1 导出考勤数据

**接口地址**: `POST /export/attendance`

**请求体**:
```json
{
  "exportType": "EXCEL",
  "dateRange": {
    "startDate": "2025-01-01",
    "endDate": "2025-01-31"
  },
  "filters": {
    "departmentIds": [10, 20],
    "employeeIds": [1001, 1002],
    "attendanceStatus": ["NORMAL", "LATE"]
  },
  "fields": [
    "employeeName",
    "departmentName",
    "attendanceDate",
    "clockInTime",
    "clockOutTime",
    "workDuration",
    "attendanceStatus"
  ]
}
```

### 11.2 批量导入排班

**接口地址**: `POST /import/schedule`

**请求体**: multipart/form-data
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | Excel文件 |
| overwriteExisting | Boolean | 否 | 是否覆盖现有数据 |

---

## 12. 系统通知接口

### 12.1 获取考勤提醒配置

**接口地址**: `GET /notification/reminders`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "ruleId": 1,
      "ruleName": "上班打卡提醒",
      "reminderType": "CLOCK_IN",
      "timeOffsetMinutes": -30,
      "message": "提醒：您还有30分钟就要上班了，请及时打卡！",
      "isEnabled": true
    }
  ]
}
```

### 12.2 配置提醒规则

**接口地址**: `POST /notification/reminders/config`

**请求体**:
```json
{
  "reminderType": "CLOCK_IN",
  "timeOffsetMinutes": -30,
  "message": "上班打卡提醒",
  "isEnabled": true,
  "recipients": ["EMPLOYEE", "MANAGER"]
}
```

---

## 13. 错误码说明

### 13.1 业务错误码

| 错误码 | 说明 |
|--------|------|
| ATTENDANCE_001 | 考勤记录不存在 |
| ATTENDANCE_002 | 考勤记录重复 |
| ATTENDANCE_003 | 班次不存在 |
| ATTENDANCE_004 | 排班冲突 |
| ATTENDANCE_005 | 申请已存在 |
| ATTENDANCE_006 | 假期不足 |
| ATTENDANCE_007 | 审批权限不足 |
| ATTENDANCE_008 | 计算失败 |

### 13.2 系统错误码

| 错误码 | 说明 |
|--------|------|
| SYS_001 | 系统繁忙 |
| SYS_002 | 数据库连接失败 |
| SYS_003 | 文件上传失败 |
| SYS_004 | 导出任务失败 |

---

## 14. 接口调用示例

### 14.1 JavaScript/Axios示例

```javascript
// 考勤记录查询
const queryAttendanceRecords = async (params) => {
  try {
    const response = await axios.get('/api/v1/attendance/record/query', {
      params,
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });
    return response.data;
  } catch (error) {
    console.error('查询考勤记录失败:', error);
    throw error;
  }
};

// 提交打卡
const submitPunch = async (punchData) => {
  try {
    const response = await axios.post('/api/v1/attendance/mobile/punch', punchData, {
      headers: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('打卡提交失败:', error);
    throw error;
  }
};
```

### 14.2 cURL示例

```bash
# 查询考勤记录
curl -X GET "http://localhost:8091/api/v1/attendance/record/query?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer your-token-here"

# 提交请假申请
curl -X POST "http://localhost:8091/api/v1/attendance/exception/leave/apply" \
  -H "Authorization: Bearer your-token-here" \
  -H "Content-Type: application/json" \
  -d '{
    "leaveType": "事假",
    "startDate": "2025-02-01",
    "endDate": "2025-02-03",
    "duration": 3.0,
    "reason": "家中有事"
  }'
```

---

## 15. 版本更新说明

### v1.0.0 (2025-01-30)
- 初始版本发布
- 包含考勤记录、班次管理、排班管理、异常管理等核心功能
- 支持移动端打卡和报表导出
- 提供完整的API文档

---

*本文档由IOE-DREAM团队维护，如有疑问请联系技术支持。*