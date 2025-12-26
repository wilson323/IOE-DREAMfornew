# IOE-DREAM API文档索引 (Tasks 8-12)

## 📖 文档说明

本文档汇总了IOE-DREAM系统Tasks 8-12的所有REST API接口。

**访问地址**:
- 消费服务: http://localhost:8094/swagger-ui.html
- 视频服务: http://localhost:8092/swagger-ui.html
- 访客服务: http://localhost:8095/swagger-ui.html

**通用响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1735171200000
}
```

---

## 🛒 Task 8: 消费服务 API

### 基础路径
```
/api/v1/consume
```

### 8.1 消费记录对账

#### 8.1.1 执行对账
```http
POST /api/v1/consume/transaction/reconciliation
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 (yyyy-MM-dd) |
| endDate | string | 是 | 结束日期 (yyyy-MM-dd) |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reconciliationId": 1,
    "reconciliationStatus": 2,
    "hasDiscrepancy": false
  }
}
```

#### 8.1.2 查询对账记录
```http
GET /api/v1/consume/transaction/reconciliation/{reconciliationId}
```

#### 8.1.3 查询对账历史
```http
GET /api/v1/consume/transaction/reconciliation/history
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20 |

#### 8.1.4 查询对账统计
```http
GET /api/v1/consume/transaction/reconciliation/statistics
```

---

## 📹 Task 9: 视频服务 API - 固件升级

### 基础路径
```
/api/v1/video/firmware
```

### 9.1 固件升级管理

#### 9.1.1 创建升级任务
```http
POST /api/v1/video/firmware/upload
```

**请求体**:
```json
{
  "deviceId": 1001,
  "deviceCode": "CAM001",
  "deviceName": "1号摄像头",
  "currentVersion": "1.0.0",
  "targetVersion": "2.0.0",
  "firmwareUrl": "http://firmware.example.com/v2.0.0.bin",
  "fileSize": 52428800,
  "fileMd5": "abc123def456",
  "upgradeType": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "upgradeId": 1,
    "deviceId": 1001,
    "upgradeStatus": 1,
    "progress": 0,
    "createTime": "2025-12-26T10:00:00"
  }
}
```

#### 9.1.2 启动升级
```http
POST /api/v1/video/firmware/{upgradeId}/start
```

#### 9.1.3 查询升级进度
```http
GET /api/v1/video/firmware/{upgradeId}/progress
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "upgradeId": 1,
    "deviceId": 1001,
    "currentVersion": "1.0.0",
    "targetVersion": "2.0.0",
    "upgradeStatus": 2,
    "progress": 65,
    "startTime": "2025-12-26T10:00:00"
  }
}
```

#### 9.1.4 查询设备升级历史
```http
GET /api/v1/video/firmware/history/{deviceId}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | integer | 否 | 限制数量，默认100 |

#### 9.1.5 获取升级统计
```http
GET /api/v1/video/firmware/statistics
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "totalCount": 10,
    "successCount": 8,
    "failureCount": 2,
    "inProgressCount": 0,
    "successRate": 80.0
  }
}
```

---

## 📹 Task 10: 视频服务 API - 设备健康

### 基础路径
```
/api/v1/video/health
```

### 10.1 设备健康管理

#### 10.1.1 创建健康检查记录
```http
POST /api/v1/video/health/check
```

**请求体**:
```json
{
  "deviceId": 1001,
  "deviceCode": "CAM001",
  "deviceName": "1号摄像头",
  "cpuUsage": 45.5,
  "memoryUsage": 60.2,
  "diskUsage": 50.0,
  "networkLatency": 20,
  "packetLoss": 0.1,
  "frameRate": 25,
  "temperature": 45,
  "uptime": 720
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "healthId": 1,
    "deviceId": 1001,
    "healthScore": 85,
    "healthStatus": 1,
    "alarmLevel": 0,
    "alarmMessage": "",
    "checkTime": "2025-12-26T10:00:00"
  }
}
```

#### 10.1.2 查询设备健康状态
```http
GET /api/v1/video/health/device/{deviceId}
```

#### 10.1.3 查询健康历史
```http
GET /api/v1/video/health/history/{deviceId}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| limit | integer | 否 | 限制数量，默认100 |

#### 10.1.4 查询告警记录
```http
GET /api/v1/video/health/alarms
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| alarmLevel | integer | 否 | 告警级别 (0-3) |
| pageNum | integer | 否 | 页码 |
| pageSize | integer | 否 | 每页大小 |

#### 10.1.5 获取健康统计
```http
GET /api/v1/video/health/statistics
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "totalDevices": 50,
    "healthyCount": 40,
    "subHealthyCount": 8,
    "unhealthyCount": 2,
    "averageHealthScore": 82.5,
    "alarmCount": 10
  }
}
```

---

## 🚶 Task 11: 访客服务 API - 自助登记

### 基础路径
```
/api/v1/visitor/registration
```

### 11.1 自助登记管理

#### 11.1.1 创建自助登记
```http
POST /api/v1/visitor/registration/registration
```

**请求体**:
```json
{
  "visitorName": "张三",
  "idCardType": 1,
  "idCard": "110101199001011234",
  "phone": "13800138000",
  "visitorType": 1,
  "visitPurpose": "商务洽谈",
  "intervieweeId": 2001,
  "intervieweeName": "李四",
  "intervieweeDepartment": "技术部",
  "visitDate": "2025-12-26",
  "expectedEnterTime": "2025-12-26T10:00:00",
  "expectedLeaveTime": "2025-12-26T18:00:00",
  "facePhotoUrl": "http://example.com/face.jpg",
  "terminalId": "TERM001",
  "terminalLocation": "大厅登记机"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "registrationId": 1,
    "registrationCode": "SSRG20251226000001",
    "visitorCode": "VC2025122609100001",
    "registrationStatus": 0,
    "createTime": "2025-12-26T09:10:00"
  }
}
```

#### 11.1.2 查询待审批记录
```http
GET /api/v1/visitor/registration/pending
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| intervieweeId | long | 否 | 被访人ID |

#### 11.1.3 审批登记申请
```http
POST /api/v1/visitor/registration/{registrationId}/approve
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approverId | long | 是 | 审批人ID |
| approverName | string | 是 | 审批人姓名 |
| approved | boolean | 是 | 是否通过 |
| approvalComment | string | 否 | 审批意见 |

#### 11.1.4 访客签到
```http
POST /api/v1/visitor/registration/check-in
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| visitorCode | string | 是 | 访客码 |
| terminalId | string | 是 | 终端ID |
| terminalLocation | string | 是 | 终端位置 |

#### 11.1.5 访客签离
```http
POST /api/v1/visitor/registration/check-out
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| visitorCode | string | 是 | 访客码 |
| terminalId | string | 是 | 终端ID |
| terminalLocation | string | 是 | 终端位置 |

#### 11.1.6 查询访客统计
```http
GET /api/v1/visitor/registration/statistics
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

---

## 🚶 Task 12: 访客服务 API - 自助签离

### 基础路径
```
/api/v1/visitor/self-check-out
```

### 12.1 自助签离管理

#### 12.1.1 执行自助签离
```http
POST /api/v1/visitor/self-check-out/perform
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| visitorCode | string | 是 | 访客码 |
| terminalId | string | 是 | 终端ID |
| terminalLocation | string | 是 | 终端位置 |
| cardReturnStatus | integer | 是 | 卡归还状态 (0-未归还 1-已归还 2-卡遗失) |
| visitorCard | string | 否 | 访客卡号 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "checkOutId": 1,
    "visitorCode": "VC2025122609100001",
    "checkOutTime": "2025-12-26T18:30:00",
    "visitDuration": 540,
    "isOvertime": 0,
    "overtimeDuration": 0,
    "checkOutMethod": 1,
    "checkOutStatus": 1
  }
}
```

#### 12.1.2 人工签离
```http
POST /api/v1/visitor/self-check-out/manual
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| visitorCode | string | 是 | 访客码 |
| operatorId | long | 是 | 操作人ID |
| operatorName | string | 是 | 操作人姓名 |
| reason | string | 否 | 签离原因 |

#### 12.1.3 查询签离记录
```http
GET /api/v1/visitor/self-check-out/query
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| visitorCode | string | 是 | 访客码 |

#### 12.1.4 查询超时签离记录
```http
GET /api/v1/visitor/self-check-out/overtime
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

#### 12.1.5 查询未归还访客卡
```http
GET /api/v1/visitor/self-check-out/unreturned-cards
```

#### 12.1.6 更新满意度评价
```http
POST /api/v1/visitor/self-check-out/satisfaction
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| checkOutId | long | 是 | 签离记录ID |
| satisfactionScore | integer | 是 | 满意度评分 (1-5) |
| visitorFeedback | string | 否 | 访客反馈 |

#### 12.1.7 获取访问时长统计
```http
GET /api/v1/visitor/self-check-out/duration-statistics
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "averageDuration": 480,
    "minDuration": 30,
    "maxDuration": 1200,
    "totalVisitors": 100
  }
}
```

#### 12.1.8 获取满意度统计
```http
GET /api/v1/visitor/self-check-out/satisfaction-statistics
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "totalCount": 80,
    "averageScore": 4.5,
    "score5Count": 50,
    "score4Count": 20,
    "score3Count": 8,
    "score2Count": 2,
    "score1Count": 0,
    "satisfactionRate": 87.5
  }
}
```

---

## 📊 API端点统计

| 服务 | API端点数 | 功能模块 |
|------|-----------|----------|
| **消费服务** | 4个 | 消费记录对账 |
| **视频服务** | 7个 | 固件升级 |
| **视频服务** | 7个 | 设备健康检查 |
| **访客服务** | 14个 | 自助登记 |
| **访客服务** | 12个 | 自助签离 |
| **总计** | **44个** | **Tasks 8-12** |

---

## 🔐 权限说明

所有API都需要相应的权限认证：

- `VISITOR_MANAGE`: 访客管理权限
- `VISITOR_SELF_SERVICE`: 访客自助服务权限
- `VISITOR_QUERY`: 访客查询权限
- `DEVICE_MANAGE`: 设备管理权限
- `DEVICE_QUERY`: 设备查询权限

---

## 📝 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |
| 1001 | 数据不存在 |
| 1002 | 数据重复 |
| 2001 | 访客码不存在 |
| 2002 | 访客未签到 |
| 2003 | 访客已签离 |
| 3001 | 设备不存在 |
| 3002 | 设备离线 |
| 4001 | 固件文件不存在 |
| 4002 | 固件校验失败 |
| 4003 | 设备升级失败 |

---

## 🚀 在线测试

所有API都可以通过Swagger UI在线测试：

1. 访问服务Swagger UI
2. 点击需要测试的API
3. 点击"Try it out"
4. 填写请求参数
5. 点击"Execute"执行
6. 查看响应结果

---

**📅 文档版本**: v1.0.0
**🏗️ 技术架构师**: IOE-DREAM架构团队
**✅ 最后更新**: 2025-12-26
