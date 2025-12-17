# 排班管理 - API接口设计

> **版本**: v1.0.0  
> **微服务**: ioedream-attendance-service (8091)  
> **创建日期**: 2025-12-17

---

## 📋 接口概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/schedules | 获取排班记录列表 |
| GET | /api/v1/schedules/calendar | 获取排班日历 |
| POST | /api/v1/schedules | 创建排班记录 |
| POST | /api/v1/schedules/batch | 批量创建排班 |
| PUT | /api/v1/schedules/{id} | 更新排班记录 |
| DELETE | /api/v1/schedules/{id} | 删除排班记录 |
| GET | /api/v1/schedule-templates | 获取模板列表 |
| POST | /api/v1/schedule-templates | 创建排班模板 |
| POST | /api/v1/schedule-templates/{id}/apply | 应用模板 |
| POST | /api/v1/smart-scheduling/start | 启动智能排班 |

---

## 📖 接口详情

### 1. 获取排班日历

**请求**:
```http
GET /api/v1/schedules/calendar?departmentId=1&month=2025-01
```

**响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "month": "2025-01",
    "departmentId": 1,
    "schedules": [
      {
        "employeeId": 101,
        "employeeName": "张三",
        "days": [
          {
            "date": "2025-01-01",
            "shiftId": 1,
            "shiftName": "早班",
            "isTemporary": false
          }
        ]
      }
    ]
  }
}
```

### 2. 批量创建排班

**请求**:
```http
POST /api/v1/schedules/batch
Content-Type: application/json

{
  "employeeIds": [101, 102, 103],
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "templateId": 1,
  "overwriteExisting": false
}
```

### 3. 应用排班模板

**请求**:
```http
POST /api/v1/schedule-templates/{id}/apply
Content-Type: application/json

{
  "employeeIds": [101, 102],
  "startDate": "2025-01-01",
  "endDate": "2025-01-31"
}
```

### 4. 启动智能排班

**请求**:
```http
POST /api/v1/smart-scheduling/start
Content-Type: application/json

{
  "departmentId": 1,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "constraints": {
    "maxConsecutiveDays": 6,
    "minRestHours": 12,
    "weekendBalance": true
  },
  "optimizationGoal": "COST_MIN"
}
```

**响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "taskId": "TASK20250117001",
    "status": "PROCESSING",
    "estimatedTime": 30
  }
}
```

---

## 📊 数据模型

### ScheduleRecordForm

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| scheduleDate | Date | 是 | 排班日期 |
| shiftId | Long | 是 | 班次ID |
| scheduleType | String | 否 | 排班类型,默认NORMAL |
| reason | String | 否 | 排班原因 |

### ScheduleTemplateForm

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateName | String | 是 | 模板名称 |
| templateType | String | 是 | 模板类型 |
| departmentId | Long | 否 | 部门ID |
| templateConfig | JSON | 是 | 模板配置 |

---

## ⚠️ 错误码

| 错误码 | 说明 |
|--------|------|
| 51001 | 排班日期冲突 |
| 51002 | 员工不存在 |
| 51003 | 班次不存在 |
| 51004 | 排班模板不存在 |
| 51005 | 智能排班计算超时 |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
