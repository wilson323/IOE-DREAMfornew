# 考勤模块前端API接口设计

## 概述

本文档详细描述了IOE-DREAM智能考勤管理系统的完整前端API接口设计，包括Web端和移动端的全功能接口支持。系统提供实时考勤监控、智能排班管理、异常处理、数据分析等核心功能。

### 技术架构
- **API协议**: RESTful API + WebSocket实时通信
- **认证方式**: Sa-Token + JWT
- **数据格式**: JSON
- **响应编码**: UTF-8
- **实时通信**: WebSocket双向消息推送

## API 基础配置

### 请求头配置
```http
Content-Type: application/json
Authorization: Bearer ${sa-token}
X-Client-Type: ${client_type} # web/mobile/mini-program
X-Device-Id: ${device_id} # 设备唯一标识
X-Platform-Version: ${version}
X-Location: ${latitude,longitude} # 移动端位置信息
```

### 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "timestamp": 1640995200000,
  "data": {},
  "pagination": {
    "current": 1,
    "size": 20,
    "total": 100,
    "pages": 5
  },
  "extra": {}
}
```

### 错误码定义
| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 40100 | 未登录或token失效 | 401 |
| 40300 | 权限不足 | 403 |
| 40400 | 资源不存在 | 404 |
| 40900 | 考勤冲突 | 409 |
| 42300 | 资源被锁定 | 423 |
| 42900 | 请求过于频繁 | 429 |
| 50000 | 服务器内部错误 | 500 |
| 80001 | 考勤记录已存在 | 400 |
| 80002 | 考勤时间冲突 | 400 |
| 80003 | 排班规则冲突 | 400 |
| 80004 | 设备离线 | 400 |
| 80005 | 位置验证失败 | 400 |

## 1. 考勤打卡管理
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
### 1.1 用户打卡
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
```http
POST /api/v1/attendance/clock
```

**请求参数:**
```json
{
  "clockType": "IN", // IN上班, OUT下班, BREAK外勤
  "clockMode": "QR_CODE", // QR_CODE二维码, FINGERPRINT指纹, FACE人脸, LOCATION定位, NFC近场
  "deviceId": null, // 设备ID，非设备打卡时为null
  "qrCodeData": "QR_CODE_DATA", // 二维码数据
  "locationData": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "上海市浦东新区张江高科技园区",
    "accuracy": 10.5
  },
  "faceFeature": null, // 人脸特征数据
  "fingerprintData": null, // 指纹数据
  "nfcData": null, // NFC数据
  "photoUrl": null, // 打卡照片
  "remark": "正常上班打卡"
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "recordId": 1672531200001,
    "userId": 1001,
    "userName": "张三",
    "userNo": "EMP001",
    "departmentId": 100,
    "departmentName": "技术研发部",
    "clockType": "IN",
    "clockTime": "2024-01-02 09:00:00",
    "clockLocation": {
      "name": "公司总部",
      "address": "上海市浦东新区张江高科技园区",
      "latitude": 31.2304,
      "longitude": 121.4737
    },
    "clockDevice": {
      "deviceId": "ATT001",
      "deviceName": "大门考勤机",
      "deviceType": "FACE_RECOGNITION"
    },
    "clockStatus": "NORMAL", // NORMAL正常, LATE迟到, EARLY早退, ABSENT缺勤, OVERTIME加班
    "scheduleInfo": {
      "scheduleId": 1001,
      "scheduleName": "标准工作时间",
      "workTime": "09:00-18:00",
      "shouldClockIn": "09:00:00",
      "shouldClockOut": "18:00:00"
    },
    "isAbnormal": false,
    "abnormalReason": null,
    "canEdit": true,
    "editDeadline": "2024-01-02 23:59:59"
  }
}
```

### 1.2 获取用户当日考勤状态
```http
GET /api/v1/attendance/user/today-status
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "userId": 1001,
    "date": "2024-01-02",
    "workSchedule": {
      "scheduleId": 1001,
      "scheduleName": "标准工作时间",
      "workTime": "09:00-18:00",
      "restTime": "12:00-13:00"
    },
    "clockRecords": [
      {
        "recordId": 1672531200001,
        "clockType": "IN",
        "clockTime": "2024-01-02 08:58:23",
        "location": "公司总部",
        "deviceName": "大门考勤机",
        "status": "NORMAL"
      },
      {
        "recordId": 1672531200002,
        "clockType": "OUT",
        "clockTime": "2024-01-02 18:05:12",
        "location": "公司总部",
        "deviceName": "大门考勤机",
        "status": "OVERTIME"
      }
    ],
    "todayStatus": {
      "hasClockIn": true,
      "hasClockOut": true,
      "isLate": false,
      "isEarlyLeave": false,
      "isAbsent": false,
      "workDuration": "9小时7分钟",
      "overtimeDuration": "5分钟",
      "status": "PRESENT" // PRESENT出勤, ABSENT缺勤, LEAVE请假, TRIP出差
    },
    "nextClockAction": {
      "action": "NONE", // IN上班, OUT下班, NONE今日已完成
      "canClock": false,
      "clockLocation": {
        "latitude": 31.2304,
        "longitude": 121.4737,
        "radius": 500
      }
    }
  }
}
```

### 1.3 获取考勤记录列表
```http
GET /api/v1/attendance/records
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
userId=1001
departmentId=100
status=NORMAL
clockType=IN,OUT
page=1
size=20
sortField=clockTime
sortOrder=desc
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "recordId": 1672531200001,
      "userId": 1001,
      "userName": "张三",
      "userNo": "EMP001",
      "departmentName": "技术研发部",
      "date": "2024-01-02",
      "clockType": "IN",
      "clockTime": "2024-01-02 08:58:23",
      "locationName": "公司总部",
      "deviceName": "大门考勤机",
      "status": "NORMAL",
      "isAbnormal": false,
      "canEdit": false,
      "editHistory": []
    }
  ],
  "pagination": {
    "current": 1,
    "size": 20,
    "total": 31,
    "pages": 2
  }
}
```

### 1.4 考勤记录申诉
```http
POST /api/v1/attendance/appeal
```

**请求参数:**
```json
{
  "recordId": 1672531200001,
  "appealType": "LATE", // LATE迟到, EARLY_LEAVE早退, ABSENT缺勤, FORGOT_FORGET_CARD忘打卡
  "originalStatus": "LATE",
  "expectedStatus": "NORMAL",
  "appealReason": "由于交通拥堵导致迟到，申请正常考勤",
  "evidenceFiles": [
    {
      "fileType": "IMAGE",
      "fileName": "交通拥堵证明.jpg",
      "fileUrl": "/files/appeal/2024/01/02/proof_001.jpg",
      "fileSize": 1024567
    },
    {
      "fileType": "DOCUMENT",
      "fileName": "请假申请.pdf",
      "fileUrl": "/files/appeal/2024/01/02/leave_001.pdf",
      "fileSize": 523456
    }
  ],
  "contactInfo": {
    "phone": "13800138000",
    "email": "zhangsan@company.com"
  }
}
```

## 2. 排班管理

### 2.1 获取排班列表
```http
GET /api/v1/attendance/schedules
```

**查询参数:**
```
departmentId=100
userId=1001
scheduleType=REGULAR
status=ACTIVE
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "scheduleId": 1001,
      "scheduleName": "标准工作时间",
      "scheduleType": "REGULAR", // REGULAR固定, FLEXIBLE弹性, SHIFT轮班
      "workDays": "1,2,3,4,5", // 1-7代表周一到周日
      "workTime": "09:00-18:00",
      "restTime": "12:00-13:00",
      "totalHours": 8,
      "overtimeRule": "WORKDAY_1.5,WEEKEND_2.0",
      "lateRule": {
        "lateThreshold": 9, // 迟到阈值（分钟）
        "absentThreshold": 30, // 缺勤阈值（分钟）
        "lateDeduction": 50, // 迟到扣款（元/次）
        "absentDeduction": 200 // 缺勤扣款（元/天）
      },
      "isDefault": true,
      "status": "ACTIVE",
      "assignedUsers": 25,
      "effectiveDate": "2024-01-01",
      "expiryDate": null
    }
  ]
}
```

### 2.2 创建排班
```http
POST /api/v1/attendance/schedules
```

**请求参数:**
```json
{
  "scheduleName": "研发部弹性工作时间",
  "scheduleType": "FLEXIBLE",
  "description": "研发部弹性工作制，核心工作时间10:00-16:00",
  "workDays": "1,2,3,4,5",
  "flexibleConfig": {
    "coreWorkTime": "10:00-16:00",
    "earliestClockIn": "08:00",
    "latestClockOut": "20:00",
    "requiredHours": 8,
    "lunchBreak": "12:00-13:00"
  },
  "shiftConfig": null, // 轮班配置（scheduleType为SHIFT时使用）
  "overtimeRule": {
    "workdayRate": 1.5,
    "weekendRate": 2.0,
    "holidayRate": 3.0,
    "maxDailyHours": 12,
    "maxMonthlyHours": 80
  },
  "lateRule": {
    "lateThreshold": 5,
    "absentThreshold": 30,
    "allowGracePeriod": true,
    "gracePeriodMinutes": 10
  },
  "holidayRule": {
    "holidayWorkRequired": false,
    "holidayCompensationType": "TIME_OFF", // TIME_OFF调休, MONEY补贴
    "holidayCompensationRate": 2.0
  },
  "effectiveDate": "2024-01-01",
  "expiryDate": null,
  "assignedUsers": [1001, 1002, 1003],
  "assignedDepartments": [100]
}
```

### 2.3 批量分配排班
```http
POST /api/v1/attendance/schedules/assign
```

**请求参数:**
```json
{
  "scheduleId": 1001,
  "assignType": "DEPARTMENT", // USER, DEPARTMENT, ROLE
  "assignTargets": [100, 101, 102], // 用户ID、部门ID或角色ID列表
  "effectiveDate": "2024-01-01",
  "sendNotification": true
}
```

### 2.4 获取用户排班日历
```http
GET /api/v1/attendance/schedules/calendar
```

**查询参数:**
```
userId=1001
startDate=2024-01-01
endDate=2024-01-31
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "userId": 1001,
    "userName": "张三",
    "month": "2024-01",
    "schedules": [
      {
        "date": "2024-01-01",
        "dayOfWeek": 1,
        "isWorkday": false,
        "holidayType": "NEW_YEAR",
        "schedule": null
      },
      {
        "date": "2024-01-02",
        "dayOfWeek": 2,
        "isWorkday": true,
        "holidayType": null,
        "schedule": {
          "scheduleId": 1001,
          "scheduleName": "标准工作时间",
          "workTime": "09:00-18:00",
          "restTime": "12:00-13:00"
        }
      }
    ],
    "statistics": {
      "totalDays": 31,
      "workdays": 22,
      "holidays": 2,
      "weekends": 8,
      "leaveDays": 1,
      "actualWorkdays": 20
    }
  }
}
```

## 3. 请假管理

### 3.1 请假申请
```http
POST /api/v1/attendance/leave/apply
```

**请求参数:**
```json
{
  "leaveType": "SICK", // SICK病假, PERSONAL事假, ANNUAL年假, MATERNITY产假, PATERNITY陪产假, MARRIAGE婚假, FUNERAL丧假
  "startDate": "2024-01-15",
  "endDate": "2024-01-16",
  "startTime": "2024-01-15 09:00:00",
  "endTime": "2024-01-16 18:00:00",
  "durationType": "DAYS", // HOURS小时, DAYS天, HALF_DAYS半天
  "duration": 2.0,
  "reason": "身体不适，需要休息治疗",
  "contactDuringLeave": {
    "phone": "13800138000",
    "email": "zhangsan@company.com",
    "emergencyContact": "李四 13900139000"
  },
  "attachments": [
    {
      "fileType": "MEDICAL_CERTIFICATE",
      "fileName": "病假证明.jpg",
      "fileUrl": "/files/leave/2024/01/15/medical_cert.jpg"
    }
  ],
  "substituteInfo": {
    "hasSubstitute": true,
    "substituteUserId": 1002,
    "substituteUserName": "李四",
    "workHandover": "工作交接内容说明"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "请假申请提交成功",
  "data": {
    "leaveId": 20240115001,
    "applicationNo": "LA-20240115-001",
    "status": "PENDING_APPROVAL", // PENDING_APPROVAL待审批, APPROVED已批准, REJECTED已拒绝, CANCELLED已取消
    "submissionTime": "2024-01-15 08:30:00",
    "estimatedApprovalTime": "2024-01-15 17:00:00",
    "nextApprover": {
      "userId": 2001,
      "userName": "王经理",
      "userRole": "部门经理"
    },
    "leaveBalance": {
      "annualLeaveRemaining": 12.5,
      "sickLeaveRemaining": 30,
      "personalLeaveRemaining": 5
    }
  }
}
```

### 3.2 获取请假记录
```http
GET /api/v1/attendance/leave/records
```

**查询参数:**
```
userId=1001
leaveType=SICK
status=APPROVED
startDate=2024-01-01
endDate=2024-12-31
page=1
size=20
```

### 3.3 请假审批
```http
POST /api/v1/attendance/leave/approve
```

**请求参数:**
```json
{
  "leaveId": 20240115001,
  "action": "APPROVE", // APPROVE批准, REJECT拒绝
  "comment": "同意病假申请，请好好休息",
  "conditions": [
    {
      "condition": "WORK_RECOVERY_REPORT",
      "description": "返岗后提交健康报告",
      "deadline": "2024-01-17 18:00:00"
    }
  ]
}
```

## 4. 加班管理

### 4.1 加班申请
```http
POST /api/v1/attendance/overtime/apply
```

**请求参数:**
```json
{
  "overtimeType": "WEEKDAY", // WEEKDAY工作日, WEEKEND周末, HOLIDAY节假日
  "startDate": "2024-01-15",
  "startTime": "2024-01-15 19:00:00",
  "endDate": "2024-01-15",
  "endTime": "2024-01-15 22:00:00",
  "estimatedHours": 3.0,
  "reason": "项目紧急发布，需要加班处理",
  "compensationType": "TIME_OFF", // TIME_OFF调休, MONEY补贴
  "projectInfo": {
    "projectId": 1001,
    "projectName": "智能考勤系统升级",
    "taskId": 2001,
    "taskName": "系统部署上线"
  },
  "approverId": 2001,
  "description": "负责系统部署和监控"
}
```

### 4.2 加班打卡
```http
POST /api/v1/attendance/overtime/clock
```

**请求参数:**
```json
{
  "overtimeId": 20240115001,
  "clockType": "START", // START开始, END结束
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "公司办公室"
  },
  "description": "开始加班工作"
}
```

### 4.3 获取加班记录
```http
GET /api/v1/attendance/overtime/records
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "overtimeId": 20240115001,
      "userId": 1001,
      "userName": "张三",
      "overtimeType": "WEEKDAY",
      "startTime": "2024-01-15 19:00:00",
      "endTime": "2024-01-15 22:30:00",
      "actualHours": 3.5,
      "estimatedHours": 3.0,
      "status": "COMPLETED", // PENDING待确认, IN_PROGRESS进行中, COMPLETED已完成, CANCELLED已取消
      "compensationType": "TIME_OFF",
      "compensationHours": 5.25, // 调休小时数（含倍率）
      "approvalStatus": "APPROVED",
      "reason": "项目紧急发布",
      "projectName": "智能考勤系统升级"
    }
  ]
}
```

## 5. 出差管理

### 5.1 出差申请
```http
POST /api/v1/attendance/trip/apply
```

**请求参数:**
```json
{
  "tripType": "BUSINESS", // BUSINESS商务, TRAINING培训, CONFERENCE会议, VISIT拜访
  "startDate": "2024-01-20",
  "endDate": "2024-01-22",
  "destinations": [
    {
      "city": "北京",
      "province": "北京市",
      "address": "北京市海淀区中关村大街1号",
      "purpose": "客户会议"
    },
    {
      "city": "天津",
      "province": "天津市",
      "address": "天津市滨海新区海河中路1号",
      "purpose": "项目考察"
    }
  ],
  "purpose": "北方区域客户拜访和项目考察",
  "budget": {
    "totalAmount": 5000.00,
    "transportation": 2000.00,
    "accommodation": 2000.00,
    "meals": 600.00,
    "other": 400.00
  },
  "transportation": ["TRAIN", "TAXI"],
  "accommodation": {
    "needBooking": true,
    "hotelLevel": "BUSINESS",
    "specialRequirements": "无烟房"
  },
  "contactInfo": {
    "mobile": "13800138000",
    "email": "zhangsan@company.com",
    "emergencyContact": "李四 13900139000"
  },
  "itinerary": [
    {
      "date": "2024-01-20",
      "time": "09:00",
      "activity": "乘坐高铁G102前往北京",
      "location": "上海虹桥站"
    },
    {
      "date": "2024-01-20",
      "time": "14:00",
      "activity": "客户会议",
      "location": "北京客户公司"
    }
  ]
}
```

## 6. 考勤统计报表

### 6.1 获取考勤月报表
```http
GET /api/v1/attendance/reports/monthly
```

**查询参数:**
```
year=2024
month=1
departmentId=100
userId=1001
groupBy=USER
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "reportPeriod": {
      "year": 2024,
      "month": 1,
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "totalDays": 31,
      "workDays": 22
    },
    "summary": {
      "totalEmployees": 150,
      "actualWorkDays": 3300,
      "averageAttendance": 95.8,
      "lateCount": 45,
      "earlyLeaveCount": 23,
      "absentCount": 8,
      "overtimeHours": 124.5,
      "leaveDays": 67
    },
    "details": [
      {
        "userId": 1001,
        "userName": "张三",
        "departmentName": "技术研发部",
        "position": "高级工程师",
        "workDays": 22,
        "actualDays": 21,
        "attendanceRate": 95.5,
        "lateCount": 2,
        "earlyLeaveCount": 0,
        "absentCount": 1,
        "leaveDays": 1,
        "overtimeHours": 8.5,
        "workHours": 176,
        "performanceScore": 92.5
      }
    ]
  }
}
```

### 6.2 获取部门考勤统计
```http
GET /api/v1/attendance/reports/department
```

**查询参数:**
```
departmentId=100
startDate=2024-01-01
endDate=2024-01-31
```

### 6.3 获取个人考勤汇总
```http
GET /api/v1/attendance/reports/personal
```

**查询参数:**
```
userId=1001
startDate=2024-01-01
endDate=2024-03-31
type=QUARTER
```

## 7. 移动端专用接口

### 7.1 获取附近打卡点
```http
GET /api/v1/attendance/mobile/nearby-points
```

**查询参数:**
```
latitude=31.2304
longitude=121.4737
radius=1000
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "pointId": 1001,
      "pointName": "公司总部",
      "pointType": "OFFICE", // OFFICE办公室, CONSTRUCTION工地, FACTORY工厂
      "address": "上海市浦东新区张江高科技园区",
      "latitude": 31.2304,
      "longitude": 121.4737,
      "radius": 500,
      "distance": 50.5,
      "devices": [
        {
          "deviceId": "ATT001",
          "deviceName": "大门考勤机",
          "deviceType": "FACE_RECOGNITION",
          "status": "ONLINE"
        }
      ],
      "availableClockTypes": ["QR_CODE", "FACE", "LOCATION"]
    }
  ]
}
```

### 7.2 验证打卡位置
```http
POST /api/v1/attendance/mobile/verify-location
```

**请求参数:**
```json
{
  "latitude": 31.2304,
  "longitude": 121.4737,
  "accuracy": 10.5,
  "timestamp": 1640995200000,
  "pointId": 1001
}
```

### 7.3 快速打卡
```http
POST /api/v1/attendance/mobile/quick-clock
```

**请求参数:**
```json
{
  "clockType": "IN",
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "上海市浦东新区张江高科技园区",
    "accuracy": 10.5
  },
  "pointId": 1001,
  "wifiInfo": {
    "ssid": "Company_WiFi",
    "bssid": "aa:bb:cc:dd:ee:ff",
    "signalStrength": -45
  },
  "photoUrl": null
}
```

### 7.4 获取考勤统计卡片
```http
GET /api/v1/attendance/mobile/stats-card
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "todayStatus": {
      "date": "2024-01-02",
      "status": "PRESENT",
      "clockInTime": "08:58",
      "clockOutTime": null,
      "workDuration": "7小时5分钟",
      "overtimeDuration": "0分钟"
    },
    "weekStats": {
      "workDays": 5,
      "actualDays": 4,
      "attendanceRate": 80,
      "lateCount": 0,
      "earlyLeaveCount": 1
    },
    "monthStats": {
      "workDays": 22,
      "actualDays": 20,
      "attendanceRate": 91,
      "lateCount": 2,
      "leaveDays": 1,
      "overtimeHours": 8.5
    },
    "leaveBalance": {
      "annualLeave": 12.5,
      "sickLeave": 30,
      "personalLeave": 5
    }
  }
}
```

## 8. WebSocket 实时推送

### 8.1 连接WebSocket
```
ws://localhost:8080/ws/attendance/{userId}?token={sa-token}
```

### 8.2 消息类型

#### 8.2.1 打卡成功通知
```json
{
  "type": "CLOCK_SUCCESS",
  "timestamp": 1640995200000,
  "data": {
    "userId": 1001,
    "userName": "张三",
    "clockType": "IN",
    "clockTime": "2024-01-02 09:00:00",
    "location": "公司总部",
    "message": "打卡成功！今天又是努力的一天 💪"
  }
}
```

#### 8.2.2 异常考勤提醒
```json
{
  "type": "ABNORMAL_ATTENDANCE",
  "timestamp": 1640995200000,
  "data": {
    "userId": 1001,
    "userName": "张三",
    "abnormalType": "LATE",
    "abnormalTime": "2024-01-02 09:15:00",
    "threshold": "09:00:00",
    "lateMinutes": 15,
    "message": "您今天已迟到15分钟，请注意考勤时间"
  }
}
```

#### 8.2.3 审批状态变更
```json
{
  "type": "APPROVAL_STATUS_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "applicationId": 20240115001,
    "applicationType": "LEAVE",
    "status": "APPROVED",
    "approverName": "王经理",
    "approverComment": "同意申请，好好休息",
    "message": "您的请假申请已获批准"
  }
}
```

#### 8.2.4 排班变更通知
```json
{
  "type": "SCHEDULE_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "userId": 1001,
    "scheduleId": 1002,
    "effectiveDate": "2024-01-20",
    "oldSchedule": "标准工作时间 09:00-18:00",
    "newSchedule": "弹性工作时间 10:00-19:00",
    "message": "您的排班从下周一调整为弹性工作时间"
  }
}
```

## 9. 批量操作接口

### 9.1 批量导入考勤记录
```http
POST /api/v1/attendance/batch/import
Content-Type: multipart/form-data
```

**请求参数:**
```
file: 考勤记录Excel文件
type: IMPORT_TYPE # RECORD记录, SCHEDULE排班, LEAVE请假
validateOnly: true # 仅验证不导入
```

### 9.2 批量导出考勤报表
```http
POST /api/v1/attendance/batch/export
```

**请求参数:**
```json
{
  "exportType": "MONTHLY_REPORT", // MONTHLY_REPORT月报, DAILY_DETAIL日报, LEAVE_SUMMARY请假汇总
  "filter": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "departmentIds": [100, 101],
    "userIds": [1001, 1002]
  },
  "format": "EXCEL", // EXCEL, PDF, CSV
  "emailTo": "manager@company.com"
}
```

### 9.3 批量审批
```http
POST /api/v1/attendance/batch/approve
```

**请求参数:**
```json
{
  "applicationType": "LEAVE",
  "applicationIds": [20240115001, 20240115002],
  "action": "APPROVE",
  "comment": "批量批准"
}
```

## 10. 系统配置接口

### 10.1 获取考勤规则配置
```http
GET /api/v1/attendance/config/rules
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "clockRules": {
      "allowEarlyClockIn": true,
      "earlyClockInMinutes": 30,
      "lateTolerance": 5,
      "autoOvertime": true,
      "overtimeMinimumMinutes": 30
    },
    "locationRules": {
      "enableLocationCheck": true,
      "allowWifiCheck": true,
      "wifiNetworks": ["Company_WiFi", "Office_5G"],
      "locationAccuracy": 100
    },
    "approvalRules": {
      "leaveApprovalRequired": true,
      "overtimeApprovalRequired": true,
      "multiLevelApproval": true,
      "autoApprovalConditions": []
    },
    "notificationRules": {
      "clockNotification": true,
      "abnormalAlert": true,
      "approvalNotification": true,
      "dailyReport": false
    }
  }
}
```

### 10.2 获取考勤日历配置
```http
GET /api/v1/attendance/config/calendar?year=2024
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "year": 2024,
    "holidays": [
      {
        "date": "2024-01-01",
        "name": "元旦",
        "type": "NATIONAL",
        "isWorkday": false
      },
      {
        "date": "2024-02-10",
        "name": "春节",
        "type": "NATIONAL",
        "isWorkday": false
      }
    ],
    "workdays": [
      {
        "date": "2024-02-04",
        "name": "春节调休",
        "type": "MAKEUP",
        "isWorkday": true,
        "originalType": "WEEKEND"
      }
    ]
  }
}
```

## 11. 移动端生物特征认证

### 11.1 人脸考勤
```http
POST /api/v1/attendance/biometric/face
```

**请求参数:**
```json
{
  "clockType": "IN",
  "faceImage": "BASE64_ENCODED_FACE_IMAGE",
  "faceFeature": null, // 客户端计算的人脸特征
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737
  },
  "deviceId": "MOBILE_001"
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "matchScore": 0.98,
    "matchThreshold": 0.85,
    "isMatch": true,
    "confidence": 99.2,
    "liveness": {
      "isLive": true,
      "score": 0.96,
      "antiSpoofing": true
    },
    "clockRecord": {
      "recordId": 1672531200001,
      "clockTime": "2024-01-02 09:00:00",
      "verificationMethod": "FACE_RECOGNITION"
    }
  }
}
```

### 11.2 指纹考勤
```http
POST /api/v1/attendance/biometric/fingerprint
```

### 11.3 声纹考勤
```http
POST /api/v1/attendance/biometric/voice
```

**请求参数:**
```json
{
  "clockType": "IN",
  "voiceData": "BASE64_ENCODED_VOICE_DATA",
  "verificationText": "今天是2024年1月2日，我张三上班打卡",
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737
  }
}
```

## 12. 移动端离线功能

### 12.1 离线数据同步
```http
POST /api/v1/attendance/mobile/sync
```

**请求参数:**
```json
{
  "lastSyncTime": 1640908800000,
  "offlineRecords": [
    {
      "recordId": "offline_001",
      "clockType": "IN",
      "clockTime": "2024-01-02 08:58:00",
      "location": {
        "latitude": 31.2304,
        "longitude": 121.4737
      },
      "deviceInfo": {
        "deviceId": "MOBILE_001",
        "deviceName": "iPhone 14 Pro"
      }
    }
  ]
}
```

### 12.2 获取离线配置
```http
GET /api/v1/attendance/mobile/offline-config
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "offlineEnabled": true,
    "maxOfflineDays": 7,
    "syncInterval": 1800,
    "offlineData": {
      "userSchedules": true,
      "attendancePoints": true,
      "holidayCalendar": true
    },
    "storageLimit": "100MB"
  }
}
```

---

## 接口权限矩阵

| 功能模块 | 员工 | 主管 | 部门经理 | HR | 系统管理员 |
|---------|------|------|----------|----|-----------|
| 打卡签到 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 个人考勤查询 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 部门考勤查询 | ✗ | ✓(部门) | ✓(部门) | ✓ | ✓ |
| 考勤申诉 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 申诉审批 | ✗ | ✓ | ✓ | ✓ | ✓ |
| 排班管理 | ✗ | ✗ | ✓(部门) | ✓ | ✓ |
| 请假申请 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 请假审批 | ✗ | ✓(下属) | ✓(部门) | ✓ | ✓ |
| 加班申请 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 加班审批 | ✗ | ✓(下属) | ✓(部门) | ✓ | ✓ |
| 考勤报表 | ✗ | ✓(部门) | ✓(部门) | ✓ | ✓ |
| 系统配置 | ✗ | ✗ | ✗ | ✓ | ✓ |

---

## 版本说明

- **当前版本**: v2.0.0
- **发布日期**: 2024-01-15
- **兼容性**: 向下兼容v1.x版本
- **更新内容**:
  - 新增生物特征认证接口
  - 增强移动端离线功能
  - 优化批量操作性能
  - 完善实时推送机制

---

## 技术支持

如有API使用问题，请联系：
- **技术支持**: tech-support@ioe-dream.com
- **API文档**: https://api.ioe-dream.com/docs/attendance
- **SDK下载**: https://github.com/ioe-dream/sdks
- **问题反馈**: https://github.com/ioe-dream/issues