# 考勤模块API接口文档

## 1. 概述

本文档详细描述了考勤模块的RESTful API接口，包括接口地址、请求方法、参数说明、返回结果等信息，供前端开发和第三方系统集成使用。

### 1.1 接口规范

- **基础URL**: `http://localhost:1024/api`
- **请求格式**: JSON
- **响应格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

### 1.2 公共参数

#### 1.2.1 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | JWT认证Token |
| Content-Type | string | 是 | application/json |

#### 1.2.2 响应结构

```json
{
  "success": true,
  "code": 0,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1632456789000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 请求是否成功 |
| code | integer | 状态码 |
| msg | string | 响应消息 |
| data | object/array | 响应数据 |
| timestamp | long | 时间戳 |

### 1.3 状态码说明

| 状态码 | 说明 |
|--------|------|
| 0 | 成功 |
| -1 | 系统错误 |
| 1001 | 参数错误 |
| 1002 | 数据不存在 |
| 1003 | 权限不足 |
| 1004 | 数据已存在 |

## 2. 考勤打卡接口

### 2.1 上班打卡

**接口地址**: `POST /attendance/punch-in`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| punchTime | string | 是 | 打卡时间(yyyy-MM-dd HH:mm:ss) |
| location | string | 否 | 打卡位置 |
| latitude | number | 否 | 纬度 |
| longitude | number | 否 | 经度 |
| photo | string | 否 | 照片Base64编码 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "上班打卡成功",
  "data": {
    "recordId": 1001,
    "punchTime": "2025-11-17 09:00:00",
    "location": "公司大楼",
    "status": "NORMAL"
  },
  "timestamp": 1632456789000
}
```

### 2.2 下班打卡

**接口地址**: `POST /attendance/punch-out`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| punchTime | string | 是 | 打卡时间(yyyy-MM-dd HH:mm:ss) |
| location | string | 否 | 打卡位置 |
| latitude | number | 否 | 纬度 |
| longitude | number | 否 | 经度 |
| photo | string | 否 | 照片Base64编码 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "下班打卡成功",
  "data": {
    "recordId": 1001,
    "punchTime": "2025-11-17 18:00:00",
    "location": "公司大楼",
    "workHours": 8.0,
    "status": "NORMAL"
  },
  "timestamp": 1632456789000
}
```

### 2.3 获取今日打卡记录

**接口地址**: `GET /attendance/today-punch`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "punchInTime": "2025-11-17 09:00:00",
    "punchOutTime": "2025-11-17 18:00:00",
    "workHours": 8.0,
    "status": "NORMAL",
    "punchRecords": [
      {
        "recordId": 1001,
        "punchTime": "2025-11-17 09:00:00",
        "punchType": "上班",
        "location": "公司大楼"
      },
      {
        "recordId": 1002,
        "punchTime": "2025-11-17 18:00:00",
        "punchType": "下班",
        "location": "公司大楼"
      }
    ]
  },
  "timestamp": 1632456789000
}
```

## 3. 考勤记录接口

### 3.1 查询考勤记录

**接口地址**: `GET /attendance/records`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 否 | 员工ID |
| departmentId | integer | 否 | 部门ID |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |
| status | string | 否 | 考勤状态(NORMAL/LATE/EARLY_LEAVE/ABSENCE) |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "list": [
      {
        "recordId": 1001,
        "employeeId": 1001,
        "employeeName": "张三",
        "departmentName": "技术部",
        "attendanceDate": "2025-11-17",
        "punchInTime": "2025-11-17 09:00:00",
        "punchOutTime": "2025-11-17 18:00:00",
        "workHours": 8.0,
        "status": "NORMAL",
        "createTime": "2025-11-17 09:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 10
  },
  "timestamp": 1632456789000
}
```

### 3.2 获取考勤记录详情

**接口地址**: `GET /attendance/record/{recordId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| recordId | integer | 是 | 考勤记录ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "recordId": 1001,
    "employeeId": 1001,
    "employeeName": "张三",
    "departmentName": "技术部",
    "attendanceDate": "2025-11-17",
    "punchInTime": "2025-11-17 09:00:00",
    "punchOutTime": "2025-11-17 18:00:00",
    "workHours": 8.0,
    "overtimeHours": 2.0,
    "status": "NORMAL",
    "location": "公司大楼",
    "photo": "base64_encoded_image",
    "createTime": "2025-11-17 09:00:00",
    "updateTime": "2025-11-17 18:00:00"
  },
  "timestamp": 1632456789000
}
```

## 4. 补卡申请接口

### 4.1 申请补卡

**接口地址**: `POST /attendance/correction/apply`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| correctionDate | string | 是 | 补卡日期(yyyy-MM-dd) |
| punchType | string | 是 | 打卡类型(PUNCH_IN/PUNCH_OUT) |
| reason | string | 是 | 补卡原因 |
| location | string | 否 | 打卡位置 |
| photo | string | 否 | 照片Base64编码 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "补卡申请提交成功",
  "data": {
    "correctionId": 1001,
    "status": "PENDING"
  },
  "timestamp": 1632456789000
}
```

### 4.2 查询补卡申请记录

**接口地址**: `GET /attendance/correction/list`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 否 | 员工ID |
| departmentId | integer | 否 | 部门ID |
| status | string | 否 | 申请状态(PENDING/APPROVED/REJECTED) |
| startDate | string | 否 | 开始日期(yyyy-MM-dd) |
| endDate | string | 否 | 结束日期(yyyy-MM-dd) |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "list": [
      {
        "correctionId": 1001,
        "employeeId": 1001,
        "employeeName": "张三",
        "departmentName": "技术部",
        "correctionDate": "2025-11-16",
        "punchType": "PUNCH_IN",
        "reason": "忘记打卡",
        "status": "APPROVED",
        "approveTime": "2025-11-16 10:00:00",
        "createTime": "2025-11-16 09:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  },
  "timestamp": 1632456789000
}
```

### 4.3 获取补卡申请详情

**接口地址**: `GET /attendance/correction/{correctionId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| correctionId | integer | 是 | 补卡申请ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "correctionId": 1001,
    "employeeId": 1001,
    "employeeName": "张三",
    "departmentName": "技术部",
    "correctionDate": "2025-11-16",
    "punchType": "PUNCH_IN",
    "reason": "忘记打卡",
    "location": "公司大楼",
    "photo": "base64_encoded_image",
    "status": "APPROVED",
    "approveRemark": "同意补卡",
    "approveTime": "2025-11-16 10:00:00",
    "createTime": "2025-11-16 09:00:00",
    "updateTime": "2025-11-16 10:00:00"
  },
  "timestamp": 1632456789000
}
```

### 4.4 撤销补卡申请

**接口地址**: `DELETE /attendance/correction/{correctionId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| correctionId | integer | 是 | 补卡申请ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "撤销成功",
  "data": null,
  "timestamp": 1632456789000
}
```

## 5. 排班管理接口

### 5.1 获取员工排班信息

**接口地址**: `GET /attendance/schedule`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| date | string | 是 | 日期(yyyy-MM-dd) |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "scheduleId": 1001,
    "employeeId": 1001,
    "scheduleDate": "2025-11-17",
    "scheduleType": "NORMAL",
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "remark": "正常班"
  },
  "timestamp": 1632456789000
}
```

### 5.2 获取月度排班信息

**接口地址**: `GET /attendance/schedule/month`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| year | integer | 是 | 年份 |
| month | integer | 是 | 月份 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": [
    {
      "scheduleId": 1001,
      "employeeId": 1001,
      "scheduleDate": "2025-11-01",
      "scheduleType": "NORMAL",
      "workStartTime": "09:00",
      "workEndTime": "18:00"
    },
    {
      "scheduleId": 1002,
      "employeeId": 1001,
      "scheduleDate": "2025-11-02",
      "scheduleType": "NORMAL",
      "workStartTime": "09:00",
      "workEndTime": "18:00"
    }
  ],
  "timestamp": 1632456789000
}
```

### 5.3 获取排班详情

**接口地址**: `GET /attendance/schedule/{scheduleId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| scheduleId | integer | 是 | 排班ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "scheduleId": 1001,
    "employeeId": 1001,
    "employeeName": "张三",
    "departmentName": "技术部",
    "scheduleDate": "2025-11-17",
    "scheduleType": "NORMAL",
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "remark": "正常班",
    "createTime": "2025-11-01 09:00:00",
    "updateTime": "2025-11-01 09:00:00"
  },
  "timestamp": 1632456789000
}
```

### 5.4 批量设置排班

**接口地址**: `POST /attendance/schedule/batch`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |
| scheduleType | string | 是 | 排班类型 |
| workStartTime | string | 是 | 工作开始时间(HH:mm) |
| workEndTime | string | 是 | 工作结束时间(HH:mm) |
| remark | string | 否 | 备注 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "排班设置成功",
  "data": {
    "successCount": 20,
    "failCount": 0
  },
  "timestamp": 1632456789000
}
```

### 5.5 复制排班

**接口地址**: `POST /attendance/schedule/copy`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sourceEmployeeId | integer | 是 | 源员工ID |
| targetEmployeeId | integer | 是 | 目标员工ID |
| sourceDate | string | 是 | 源日期(yyyy-MM-dd) |
| targetDate | string | 是 | 目标日期(yyyy-MM-dd) |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "排班复制成功",
  "data": null,
  "timestamp": 1632456789000
}
```

## 6. 考勤统计接口

### 6.1 个人考勤统计

**接口地址**: `GET /attendance/statistics/personal`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "summary": {
      "shouldAttend": 22,
      "actualAttend": 20,
      "lateCount": 2,
      "earlyLeaveCount": 1,
      "absenceCount": 2,
      "overtimeHours": 15.5,
      "workHours": 160.0
    },
    "details": [
      {
        "date": "2025-11-01",
        "status": "NORMAL",
        "punchInTime": "09:00",
        "punchOutTime": "18:00",
        "workHours": 8.0
      }
    ]
  },
  "timestamp": 1632456789000
}
```

### 6.2 部门考勤统计

**接口地址**: `GET /attendance/statistics/department`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| departmentId | integer | 是 | 部门ID |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "summary": {
      "employeeCount": 25,
      "shouldAttend": 550,
      "actualAttend": 520,
      "lateCount": 15,
      "earlyLeaveCount": 8,
      "absenceCount": 30,
      "overtimeHours": 120.5
    },
    "details": [
      {
        "employeeId": 1001,
        "employeeName": "张三",
        "shouldAttend": 22,
        "actualAttend": 20,
        "lateCount": 2,
        "earlyLeaveCount": 1,
        "absenceCount": 2,
        "overtimeHours": 15.5
      }
    ]
  },
  "timestamp": 1632456789000
}
```

### 6.3 公司考勤统计

**接口地址**: `GET /attendance/statistics/company`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "summary": {
      "departmentCount": 8,
      "employeeCount": 200,
      "shouldAttend": 4400,
      "actualAttend": 4200,
      "lateCount": 85,
      "earlyLeaveCount": 45,
      "absenceCount": 200,
      "overtimeHours": 850.0
    },
    "details": [
      {
        "departmentId": 101,
        "departmentName": "技术部",
        "employeeCount": 25,
        "shouldAttend": 550,
        "actualAttend": 520,
        "lateCount": 15,
        "earlyLeaveCount": 8,
        "absenceCount": 30,
        "overtimeHours": 120.5
      }
    ]
  },
  "timestamp": 1632456789000
}
```

### 6.4 导出考勤统计

**接口地址**: `GET /attendance/statistics/export`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | string | 是 | 统计类型(personal/department/company) |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |
| employeeId | integer | 否 | 员工ID(type=personal时必填) |
| departmentId | integer | 否 | 部门ID(type=department时必填) |

**响应**: Excel文件流

### 6.5 获取统计图表数据

**接口地址**: `GET /attendance/statistics/chart`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | string | 是 | 统计类型(trend/distribution) |
| startDate | string | 是 | 开始日期(yyyy-MM-dd) |
| endDate | string | 是 | 结束日期(yyyy-MM-dd) |
| employeeId | integer | 否 | 员工ID |
| departmentId | integer | 否 | 部门ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "categories": ["2025-11-01", "2025-11-02", "2025-11-03"],
    "series": [
      {
        "name": "正常",
        "data": [20, 22, 21]
      },
      {
        "name": "迟到",
        "data": [2, 1, 0]
      }
    ]
  },
  "timestamp": 1632456789000
}
```

## 7. 考勤规则接口

### 7.1 获取员工考勤规则

**接口地址**: `GET /attendance/rule/employee/{employeeId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | integer | 是 | 员工ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "ruleId": 1001,
    "employeeId": 1001,
    "workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "lateMinutes": 5,
    "earlyLeaveMinutes": 5,
    "overtimeMinutes": 60,
    "punchRange": 100,
    "allowCorrection": true,
    "correctionLimit": 3
  },
  "timestamp": 1632456789000
}
```

### 7.2 获取部门考勤规则

**接口地址**: `GET /attendance/rule/department/{departmentId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| departmentId | integer | 是 | 部门ID |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "查询成功",
  "data": {
    "ruleId": 1002,
    "departmentId": 101,
    "workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "lateMinutes": 5,
    "earlyLeaveMinutes": 5,
    "overtimeMinutes": 60,
    "punchRange": 100,
    "allowCorrection": true,
    "correctionLimit": 3
  },
  "timestamp": 1632456789000
}
```

### 7.3 获取考勤规则详情

**接口地址**: `GET /attendance/rule/{ruleId}`

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
    "employeeId": 1001,
    "workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "lateMinutes": 5,
    "earlyLeaveMinutes": 5,
    "overtimeMinutes": 60,
    "punchRange": 100,
    "allowCorrection": true,
    "correctionLimit": 3,
    "createTime": "2025-01-01 00:00:00",
    "updateTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1632456789000
}
```

### 7.4 更新考勤规则

**接口地址**: `PUT /attendance/rule/{ruleId}`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | integer | 是 | 规则ID |
| workDays | array | 否 | 工作日 |
| workStartTime | string | 否 | 工作开始时间(HH:mm) |
| workEndTime | string | 否 | 工作结束时间(HH:mm) |
| lateMinutes | integer | 否 | 迟到判定分钟数 |
| earlyLeaveMinutes | integer | 否 | 早退判定分钟数 |
| overtimeMinutes | integer | 否 | 加班判定分钟数 |
| punchRange | integer | 否 | 打卡范围(米) |
| allowCorrection | boolean | 否 | 是否允许补卡 |
| correctionLimit | integer | 否 | 补卡次数限制 |

**响应示例**:

```json
{
  "success": true,
  "code": 0,
  "msg": "规则更新成功",
  "data": null,
  "timestamp": 1632456789000
}
```

## 8. 错误码说明

### 8.1 通用错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| -1 | 系统错误 |
| 1001 | 参数错误 |
| 1002 | 数据不存在 |
| 1003 | 权限不足 |
| 1004 | 数据已存在 |
| 1005 | 数据验证失败 |

### 8.2 考勤模块错误码

| 错误码 | 说明 |
|--------|------|
| 2001 | 考勤记录不存在 |
| 2002 | 已经打过卡 |
| 2003 | 不在打卡时间范围内 |
| 2004 | 不在打卡位置范围内 |
| 2005 | 考勤规则不存在 |
| 2006 | 补卡申请次数超限 |
| 2007 | 补卡申请已审批 |
| 2008 | 排班信息不存在 |

## 9. 接口调用示例

### 9.1 JavaScript调用示例

```javascript
// 上班打卡
async function punchIn(employeeId, punchTime, location) {
  const response = await fetch('/api/attendance/punch-in', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      employeeId: employeeId,
      punchTime: punchTime,
      location: location
    })
  });

  const result = await response.json();
  if (result.success) {
    console.log('打卡成功:', result.data);
  } else {
    console.error('打卡失败:', result.msg);
  }
}

// 查询考勤记录
async function queryAttendanceRecords(employeeId, startDate, endDate) {
  const response = await fetch(`/api/attendance/records?employeeId=${employeeId}&startDate=${startDate}&endDate=${endDate}`, {
    method: 'GET',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });

  const result = await response.json();
  if (result.success) {
    console.log('考勤记录:', result.data);
  } else {
    console.error('查询失败:', result.msg);
  }
}
```

### 9.2 Java调用示例

```java
// 使用RestTemplate调用
@Service
public class AttendanceService {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseDTO<PunchRecordVO> punchIn(PunchRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PunchRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ResponseDTO<PunchRecordVO>> response = restTemplate.postForEntity(
            "/api/attendance/punch-in", entity, new ParameterizedTypeReference<ResponseDTO<PunchRecordVO>>() {});

        return response.getBody();
    }

    public ResponseDTO<PageResult<AttendanceRecordVO>> queryRecords(AttendanceQueryRequest request) {
        String url = String.format("/api/attendance/records?employeeId=%d&startDate=%s&endDate=%s",
            request.getEmployeeId(), request.getStartDate(), request.getEndDate());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<ResponseDTO<PageResult<AttendanceRecordVO>>> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, new ParameterizedTypeReference<ResponseDTO<PageResult<AttendanceRecordVO>>>() {});

        return response.getBody();
    }
}
```

## 10. 注意事项

1. **认证安全**: 所有接口调用都需要有效的JWT Token认证
2. **参数验证**: 请求参数需要进行有效性验证
3. **频率限制**: 避免频繁调用接口，建议增加适当的延时
4. **错误处理**: 需要妥善处理接口返回的错误信息
5. **数据保护**: 敏感数据需要进行加密传输和存储
6. **版本兼容**: 注意API版本变化，及时更新调用方式

---
**文档版本**: v1.0.0
**更新时间**: 2025-11-17
**适用系统**: SmartAdmin v3