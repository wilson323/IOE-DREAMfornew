# 考勤管理模块 - API接口设计文档

## 📋 模块概述

考勤管理模块API基于RESTful设计规范，提供完整的考勤管理服务接口，支持考勤记录管理、排班管理、异常处理、报表统计等功能。

### 技术规范

- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **认证方式**: JWT Token
- **版本控制**: URL版本控制 (/api/v1/)
- **响应格式**: 统一ResponseDTO格式
- **接口文档**: OpenAPI 3.0规范

## 1. 接口设计规范

### 1.1 RESTful API规范

| HTTP方法 | 用途 | 示例URL |
|---------|------|---------|
| GET | 查询资源 | `/api/v1/attendance/records` |
| POST | 创建资源 | `/api/v1/attendance/records` |
| PUT | 更新资源 | `/api/v1/attendance/records/{id}` |
| DELETE | 删除资源 | `/api/v1/attendance/records/{id}` |
| PATCH | 部分更新 | `/api/v1/attendance/records/{id}/status` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体数据
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 1.3 错误码规范

| 错误码 | HTTP状态码 | 描述 |
|--------|-----------|------|
| 200 | 200 | 操作成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未授权访问 |
| 403 | 403 | 权限不足 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |
| 1001 | 400 | 参数验证失败 |
| 2001 | 400 | 考勤记录不存在 |
| 2002 | 400 | 考勤时间冲突 |
| 3001 | 400 | 排班冲突 |
| 4001 | 400 | 异常申请已存在 |

## 2. 考勤记录管理接口

### 2.1 考勤记录查询

#### 2.1.1 分页查询考勤记录

```http
GET /api/v1/attendance/records/query
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认20 | 20 |
| employeeId | Long | 否 | 员工ID | 1001 |
| departmentId | Long | 否 | 部门ID | 10 |
| startDate | String | 否 | 开始日期，格式yyyy-MM-dd | 2025-01-01 |
| endDate | String | 否 | 结束日期，格式yyyy-MM-dd | 2025-01-31 |
| status | String | 否 | 考勤状态 | NORMAL |
| attendanceType | String | 否 | 考勤类型 | CHECK_IN |

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "id": 1,
        "employeeId": 1001,
        "employeeName": "张三",
        "employeeNo": "EMP001",
        "departmentId": 10,
        "departmentName": "技术部",
        "attendanceDate": "2025-01-30",
        "punchTime": "2025-01-30T09:00:00",
        "attendanceType": "CHECK_IN",
        "attendanceStatus": "NORMAL",
        "deviceName": "考勤机001",
        "location": "北京市朝阳区",
        "longitude": 116.4074,
        "latitude": 39.9042,
        "photoUrl": "https://example.com/photo/1.jpg",
        "createTime": "2025-01-30T09:00:00",
        "updateTime": "2025-01-30T09:00:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

#### 2.1.2 获取考勤记录详情

```http
GET /api/v1/attendance/records/{id}
```

**路径参数**:

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 考勤记录ID | 1 |

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "employeeId": 1001,
    "employeeName": "张三",
    "employeeNo": "EMP001",
    "departmentId": 10,
    "departmentName": "技术部",
    "shiftId": 1,
    "shiftName": "正常班",
    "attendanceDate": "2025-01-30",
    "punchTime": "2025-01-30T09:00:00",
    "attendanceType": "CHECK_IN",
    "attendanceStatus": "NORMAL",
    "workHours": 8.0,
    "lateMinutes": 0,
    "earlyMinutes": 0,
    "overtimeMinutes": 0,
    "deviceName": "考勤机001",
    "deviceCode": "DEVICE001",
    "location": "北京市朝阳区",
    "longitude": 116.4074,
    "latitude": 39.9042,
    "photoUrl": "https://example.com/photo/1.jpg",
    "verified": true,
    "abnormalReason": null,
    "createTime": "2025-01-30T09:00:00",
    "updateTime": "2025-01-30T09:00:00"
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 2.2 创建考勤记录

```http
POST /api/v1/attendance/records
```

**请求体**:

```json
{
  "userId": 1001,
  "deviceId": 1,
  "deviceCode": "DEVICE001",
  "punchType": 0,
  "punchTime": 1706582400,
  "punchAddress": "北京市朝阳区",
  "longitude": 116.4074,
  "latitude": 39.9042,
  "photoUrl": "https://example.com/photo/1.jpg"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| deviceId | Long | 是 | 设备ID |
| deviceCode | String | 是 | 设备编码 |
| punchType | Integer | 是 | 打卡类型：0-上班，1-下班 |
| punchTime | Long | 是 | 打卡时间戳 |
| punchAddress | String | 否 | 打卡地址 |
| longitude | Decimal | 否 | 经度 |
| latitude | Decimal | 否 | 纬度 |
| photoUrl | String | 否 | 打卡照片URL |

**响应示例**:

```json
{
  "code": 200,
  "message": "考勤记录创建成功",
  "data": 12345,
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 2.3 更新考勤记录

```http
PUT /api/v1/attendance/records/{id}
```

**请求体**:

```json
{
  "attendanceStatus": "LATE",
  "lateMinutes": 15,
  "abnormalReason": "交通拥堵",
  "verified": true,
  "verifierId": 1002,
  "verifyTime": "2025-01-30T10:00:00"
}
```

### 2.4 删除考勤记录

```http
DELETE /api/v1/attendance/records/{id}
```

## 3. 排班管理接口

### 3.1 排班日历查询

```http
GET /api/v1/attendance/schedule/calendar
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| year | Integer | 是 | 年份 | 2025 |
| month | Integer | 是 | 月份 | 1 |
| departmentId | Long | 否 | 部门ID | 10 |
| employeeId | Long | 否 | 员工ID | 1001 |

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "year": 2025,
    "month": 1,
    "schedules": [
      {
        "date": "2025-01-01",
        "schedules": [
          {
            "id": 1,
            "employeeId": 1001,
            "employeeName": "张三",
            "shiftId": 1,
            "shiftName": "正常班",
            "startTime": "09:00",
            "endTime": "18:00",
            "workHours": 8.0,
            "shiftType": "REGULAR"
          }
        ],
        "holidays": [],
        "exceptions": []
      }
    ]
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 3.2 批量排班

```http
POST /api/v1/attendance/schedule/batch
```

**请求体**:

```json
{
  "scheduleType": "DEPARTMENT",
  "departmentId": 10,
  "shiftId": 1,
  "dateRange": ["2025-02-01", "2025-02-07"],
  "employees": [1001, 1002, 1003]
}
```

### 3.3 智能排班

```http
POST /api/v1/attendance/schedule/intelligent
```

**请求体**:

```json
{
  "targetType": "DEPARTMENT",
  "targetId": 10,
  "dateRange": ["2025-02-01", "2025-02-28"],
  "scheduleRules": {
    "workDays": [1, 2, 3, 4, 5],
    "preferShifts": [1, 2],
    "avoidConsecutiveDays": true,
    "fairDistribution": true
  }
}
```

## 4. 异常管理接口

### 4.1 异常申请列表

```http
GET /api/v1/attendance/exceptions
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认20 | 20 |
| type | String | 否 | 异常类型 | LEAVE |
| status | String | 否 | 申请状态 | PENDING |
| applicantId | Long | 否 | 申请人ID | 1001 |
| startDate | String | 否 | 开始日期 | 2025-01-01 |
| endDate | String | 否 | 结束日期 | 2025-01-31 |

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "id": 1,
        "type": "LEAVE",
        "typeName": "请假申请",
        "applicantId": 1001,
        "applicantName": "张三",
        "departmentId": 10,
        "departmentName": "技术部",
        "startTime": "2025-01-30T09:00:00",
        "endTime": "2025-01-31T18:00:00",
        "duration": 2.0,
        "reason": "家中有事",
        "status": "PENDING",
        "approvers": [
          {
            "approverId": 1002,
            "approverName": "李四",
            "approvalTime": null,
            "approvalStatus": "PENDING",
            "comments": null
          }
        ],
        "attachments": [
          {
            "id": 1,
            "fileName": "请假证明.pdf",
            "fileUrl": "https://example.com/files/1.pdf",
            "fileSize": 1024000
          }
        ],
        "createTime": "2025-01-30T10:00:00",
        "updateTime": "2025-01-30T10:00:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 4.2 提交异常申请

```http
POST /api/v1/attendance/exceptions
```

**请求体**:

```json
{
  "type": "LEAVE",
  "subtype": "PERSONAL",
  "startTime": "2025-01-30T09:00:00",
  "endTime": "2025-01-31T18:00:00",
  "reason": "家中有急事",
  "approvers": [1002, 1003],
  "attachments": [
    {
      "fileName": "请假证明.pdf",
      "fileUrl": "https://example.com/files/1.pdf"
    }
  ]
}
```

### 4.3 审批异常申请

```http
POST /api/v1/attendance/exceptions/{id}/approve
```

**请求体**:

```json
{
  "approvalStatus": "APPROVED",
  "comments": "同意请假申请",
  "actualStartTime": "2025-01-30T09:00:00",
  "actualEndTime": "2025-01-31T18:00:00",
  "actualDuration": 2.0
}
```

## 5. 班次管理接口

### 5.1 班次列表

```http
GET /api/v1/attendance/shifts
```

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "shiftName": "正常班",
      "shiftCode": "NORMAL",
      "shiftType": "REGULAR",
      "startTime": "09:00",
      "endTime": "18:00",
      "workHours": 8.0,
      "breakTime": 1.0,
      "lateTolerance": 5,
      "earlyTolerance": 5,
      "requiredCheckIn": true,
      "requiredCheckOut": true,
      "overtimeEnabled": true,
      "weekendRate": 2.0,
      "holidayRate": 3.0,
      "description": "标准工作时间",
      "status": "ACTIVE",
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-01-01T00:00:00"
    }
  ],
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 5.2 创建班次

```http
POST /api/v1/attendance/shifts
```

**请求体**:

```json
{
  "shiftName": "夜班",
  "shiftCode": "NIGHT",
  "shiftType": "ROTATION",
  "startTime": "22:00",
  "endTime": "06:00",
  "workHours": 8.0,
  "breakTime": 1.0,
  "lateTolerance": 10,
  "earlyTolerance": 10,
  "requiredCheckIn": true,
  "requiredCheckOut": true,
  "overtimeEnabled": true,
  "weekendRate": 2.0,
  "holidayRate": 3.0,
  "description": "夜间工作时间"
}
```

## 6. 考勤统计接口

### 6.1 考勤统计概览

```http
GET /api/v1/attendance/statistics/overview
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| startDate | String | 是 | 开始日期 | 2025-01-01 |
| endDate | String | 是 | 结束日期 | 2025-01-31 |
| departmentId | Long | 否 | 部门ID | 10 |
| employeeId | Long | 否 | 员工ID | 1001 |

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalEmployees": 100,
    "attendanceDays": 22,
    "presentDays": 2090,
    "absentDays": 110,
    "lateDays": 85,
    "earlyDays": 45,
    "overtimeHours": 156.5,
    "attendanceRate": 95.0,
    "onTimeRate": 85.5,
    "departmentStats": [
      {
        "departmentId": 10,
        "departmentName": "技术部",
        "employeeCount": 30,
        "attendanceRate": 96.5,
        "onTimeRate": 87.2
      }
    ]
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 6.2 考勤详细统计

```http
GET /api/v1/attendance/statistics/detail
```

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalRecords": 2200,
      "normalRecords": 1980,
      "lateRecords": 85,
      "earlyRecords": 45,
      "absentRecords": 90,
      "overtimeRecords": 120
    },
    "trends": [
      {
        "date": "2025-01-01",
        "attendanceRate": 94.5,
        "onTimeRate": 86.2,
        "overtimeHours": 12.5
      }
    ],
    "topLates": [
      {
        "employeeId": 1001,
        "employeeName": "张三",
        "lateDays": 8,
        "totalLateMinutes": 120
      }
    ]
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

## 7. 移动端接口

### 7.1 移动端打卡

```http
POST /api/v1/attendance/mobile/checkin
```

**请求体**:

```json
{
  "userId": 1001,
  "checkType": "CHECK_IN",
  "location": {
    "longitude": 116.4074,
    "latitude": 39.9042,
    "address": "北京市朝阳区",
    "accuracy": 10.0
  },
  "deviceInfo": {
    "deviceId": "device123",
    "deviceType": "Android",
    "osVersion": "12.0"
  },
  "photoBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ..."
}
```

### 7.2 获取今日考勤状态

```http
GET /api/v1/attendance/mobile/status
```

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "todayDate": "2025-01-30",
    "checkInRecord": {
      "time": "2025-01-30T09:05:00",
      "status": "LATE",
      "lateMinutes": 5,
      "location": "北京市朝阳区"
    },
    "checkOutRecord": null,
    "todayStats": {
      "workHours": 0,
      "status": "WORKING",
      "nextSchedule": {
        "shiftName": "正常班",
        "endTime": "18:00"
      }
    }
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 7.3 位置验证

```http
POST /api/v1/attendance/mobile/verify-location
```

**请求体**:

```json
{
  "longitude": 116.4074,
  "latitude": 39.9042,
  "accuracy": 10.0
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "位置验证成功",
  "data": {
    "valid": true,
    "locationInfo": {
      "address": "北京市朝阳区",
      "distance": 50.5,
      "withinRange": true
    },
    "nearestAttendancePoint": {
      "id": 1,
      "name": "公司总部",
      "radius": 100
    }
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

## 8. 系统配置接口

### 8.1 获取考勤配置

```http
GET /api/v1/attendance/config
```

**响应示例**:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "workDays": [1, 2, 3, 4, 5],
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "lateThreshold": 5,
    "earlyThreshold": 5,
    "absentThreshold": 120,
    "overtimeThreshold": 60,
    "mobileCheckIn": {
      "enabled": true,
      "locationVerification": true,
      "photoRequired": true,
      "maxDistance": 200
    },
    "autoApproval": {
      "enabled": false,
      "autoApproveOvertime": true,
      "maxOvertimeHours": 8
    }
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 8.2 更新考勤配置

```http
PUT /api/v1/attendance/config
```

## 9. 批量操作接口

### 9.1 批量导入考勤记录

```http
POST /api/v1/attendance/records/import
```

**请求体**:

```json
{
  "fileType": "EXCEL",
  "data": [
    {
      "employeeNo": "EMP001",
      "employeeName": "张三",
      "attendanceDate": "2025-01-30",
      "checkInTime": "2025-01-30T09:00:00",
      "checkOutTime": "2025-01-30T18:00:00",
      "status": "NORMAL"
    }
  ]
}
```

### 9.2 批量导出考勤记录

```http
GET /api/v1/attendance/records/export
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| format | String | 否 | 导出格式：EXCEL、PDF、CSV | EXCEL |
| startDate | String | 是 | 开始日期 | 2025-01-01 |
| endDate | String | 是 | 结束日期 | 2025-01-31 |
| departmentId | Long | 否 | 部门ID | 10 |
| employeeId | Long | 否 | 员工ID | 1001 |

**响应示例**:

```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "downloadUrl": "https://example.com/files/attendance_records_20250130.xlsx",
    "fileSize": 2048576,
    "recordCount": 220
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

## 10. 实时通知接口

### 10.1 WebSocket连接

```javascript
// 连接WebSocket
const ws = new WebSocket('wss://api.example.com/ws/attendance');

// 订阅考勤事件
ws.send(JSON.stringify({
  type: 'subscribe',
  channels: ['attendance.records', 'attendance.exceptions']
}));
```

### 10.2 实时考勤事件推送

```json
{
  "type": "attendance.record",
  "event": "CREATED",
  "data": {
    "id": 12345,
    "employeeId": 1001,
    "employeeName": "张三",
    "punchTime": "2025-01-30T09:00:00",
    "status": "NORMAL"
  },
  "timestamp": 1706582400000
}
```

## 11. 接口安全规范

### 11.1 认证机制

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 11.2 权限控制

| 权限代码 | 说明 | 接口示例 |
|---------|------|---------|
| ATTENDANCE_RECORD_VIEW | 查看考勤记录 | GET /api/v1/attendance/records |
| ATTENDANCE_RECORD_EDIT | 编辑考勤记录 | PUT /api/v1/attendance/records |
| ATTENDANCE_SCHEDULE_MANAGE | 排班管理 | POST /api/v1/attendance/schedule |
| ATTENDANCE_EXCEPTION_APPROVE | 异常审批 | POST /api/v1/attendance/exceptions/{id}/approve |
| ATTENDANCE_STATISTICS_VIEW | 查看统计 | GET /api/v1/attendance/statistics |

### 11.3 请求限流

| 接口类型 | 限流规则 | 说明 |
|---------|---------|------|
| 查询接口 | 100次/分钟 | 普通用户 |
| 创建接口 | 20次/分钟 | 普通用户 |
| 批量接口 | 5次/分钟 | 普通用户 |
| 审批接口 | 50次/分钟 | 管理员 |

## 12. 接口测试用例

### 12.1 正常用例

```http
### 考勤记录查询
GET /api/v1/attendance/records/query?pageNum=1&pageSize=20&startDate=2025-01-01&endDate=2025-01-31

### 创建考勤记录
POST /api/v1/attendance/records
Content-Type: application/json

{
  "userId": 1001,
  "deviceId": 1,
  "deviceCode": "DEVICE001",
  "punchType": 0,
  "punchTime": 1706582400,
  "punchAddress": "北京市朝阳区"
}
```

### 12.2 异常用例

```http
### 参数验证错误
GET /api/v1/attendance/records/query?pageNum=0
# 预期返回：400 Bad Request

### 权限不足
DELETE /api/v1/attendance/records/1
# 预期返回：403 Forbidden

### 资源不存在
GET /api/v1/attendance/records/99999
# 预期返回：404 Not Found
```

## 13. 接口版本管理

### 13.1 版本策略

- **当前版本**: v1.0.0
- **版本兼容**: 向后兼容两个版本
- **废弃通知**: 提前3个月通知版本废弃

### 13.2 版本升级

```http
# v1.0版本
GET /api/v1/attendance/records

# v1.1版本（新增参数）
GET /api/v1.1/attendance/records?includePhoto=true
```

## 14. 性能优化

### 14.1 缓存策略

| 数据类型 | 缓存时间 | 说明 |
|---------|---------|------|
| 员工信息 | 30分钟 | 员工基本信息缓存 |
| 部门信息 | 1小时 | 部门结构缓存 |
| 班次信息 | 2小时 | 班次配置缓存 |
| 考勤配置 | 10分钟 | 系统配置缓存 |

### 14.2 数据分页

- **默认页大小**: 20条记录
- **最大页大小**: 100条记录
- **游标分页**: 支持大数据量查询

## 15. 错误处理

### 15.1 业务异常

```json
{
  "code": 2001,
  "message": "考勤记录已存在",
  "data": {
    "errorCode": "RECORD_EXISTS",
    "errorDetail": "该员工在此时间段已有打卡记录"
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

### 15.2 系统异常

```json
{
  "code": 500,
  "message": "系统内部错误",
  "data": {
    "errorCode": "INTERNAL_ERROR",
    "errorDetail": "数据库连接超时"
  },
  "timestamp": 1706582400000,
  "traceId": "trace-id-123456"
}
```

---

## 📋 总结

考勤管理模块API接口设计遵循RESTful规范，提供完整的考勤管理功能，包括：

1. **标准化接口**: 统一的URL设计、参数规范、响应格式
2. **安全性保障**: JWT认证、权限控制、请求限流
3. **性能优化**: 分页查询、缓存策略、异步处理
4. **扩展性**: 版本管理、模块化设计
5. **易用性**: 详细文档、丰富示例、错误处理

---

*文档版本: v1.0.0*
*创建时间: 2025-01-30*
*更新时间: 2025-01-30*
*维护人员: IOE-DREAM后端团队*