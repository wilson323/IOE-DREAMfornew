# 班次时间管理 - API接口设计

> **版本**: v1.0.0  
> **微服务**: ioedream-attendance-service (8091)  
> **创建日期**: 2025-12-17

---

## 📋 接口概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/shifts | 获取班次列表 |
| GET | /api/v1/shifts/{id} | 获取班次详情 |
| POST | /api/v1/shifts | 创建班次 |
| PUT | /api/v1/shifts/{id} | 更新班次 |
| DELETE | /api/v1/shifts/{id} | 删除班次 |
| POST | /api/v1/shifts/{id}/toggle | 启用/禁用班次 |
| POST | /api/v1/shifts/batch-import | 批量导入 |

---

## 📖 接口详情

### 1. 获取班次列表

**请求**:
```http
GET /api/v1/shifts?pageNum=1&pageSize=10&shiftType=STANDARD&status=1
```

**响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 50,
    "list": [
      {
        "id": 1,
        "shiftName": "标准早班",
        "shiftCode": "SHIFT001",
        "shiftType": "STANDARD",
        "workStartTime": "08:30:00",
        "workEndTime": "17:30:00",
        "breakStartTime": "12:00:00",
        "breakEndTime": "13:00:00",
        "minWorkHours": 8.0,
        "status": 1,
        "createTime": "2025-01-01 00:00:00"
      }
    ]
  }
}
```

### 2. 创建班次

**请求**:
```http
POST /api/v1/shifts
Content-Type: application/json

{
  "shiftName": "标准早班",
  "shiftType": "STANDARD",
  "workStartTime": "08:30:00",
  "workEndTime": "17:30:00",
  "breakStartTime": "12:00:00",
  "breakEndTime": "13:00:00",
  "earlyClockMinutes": 30,
  "lateClockMinutes": 30,
  "minWorkHours": 8.0,
  "isCrossDay": false,
  "breakPeriods": [
    {
      "breakStart": "12:00:00",
      "breakEnd": "13:00:00"
    }
  ],
  "remark": "行政岗位标准班次"
}
```

**响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "shiftCode": "SHIFT20250117001"
  }
}
```

### 3. 更新班次

**请求**:
```http
PUT /api/v1/shifts/{id}
Content-Type: application/json

{
  "shiftName": "标准早班(修改)",
  "workStartTime": "09:00:00",
  "workEndTime": "18:00:00"
}
```

### 4. 删除班次

**请求**:
```http
DELETE /api/v1/shifts/{id}
```

**响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

### 5. 启用/禁用班次

**请求**:
```http
POST /api/v1/shifts/{id}/toggle
```

---

## 📊 数据模型

### ShiftAddForm

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shiftName | String | 是 | 班次名称,最长50字符 |
| shiftType | String | 是 | 班次类型:STANDARD/EARLY/MIDDLE/NIGHT/FLEX/OVERNIGHT |
| workStartTime | Time | 是 | 上班时间 |
| workEndTime | Time | 是 | 下班时间 |
| breakStartTime | Time | 否 | 休息开始时间 |
| breakEndTime | Time | 否 | 休息结束时间 |
| earlyClockMinutes | Integer | 否 | 提前打卡分钟数,默认30 |
| lateClockMinutes | Integer | 否 | 延后打卡分钟数,默认30 |
| flexStartTime | Time | 否 | 弹性开始时间(弹性班必填) |
| flexEndTime | Time | 否 | 弹性结束时间(弹性班必填) |
| minWorkHours | Decimal | 否 | 最低工作时长,默认8.0 |
| isCrossDay | Boolean | 否 | 是否跨天班次,默认false |
| breakPeriods | List | 否 | 多段休息时间配置 |
| remark | String | 否 | 备注,最长200字符 |

### ShiftVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 班次ID |
| shiftName | String | 班次名称 |
| shiftCode | String | 班次代码 |
| shiftType | String | 班次类型 |
| shiftTypeName | String | 班次类型名称 |
| workStartTime | Time | 上班时间 |
| workEndTime | Time | 下班时间 |
| workDuration | Decimal | 工作时长(小时) |
| breakDuration | Integer | 休息时长(分钟) |
| status | Integer | 状态 |
| statusName | String | 状态名称 |
| createTime | DateTime | 创建时间 |

---

## ⚠️ 错误码

| 错误码 | 说明 |
|--------|------|
| 50001 | 班次名称已存在 |
| 50002 | 班次代码已存在 |
| 50003 | 班次时间配置无效 |
| 50004 | 班次正在使用中,无法删除 |
| 50005 | 弹性班必须配置弹性时间 |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
