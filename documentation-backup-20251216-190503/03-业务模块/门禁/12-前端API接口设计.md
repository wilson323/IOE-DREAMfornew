# 门禁模块前端API接口设计

## 📋 接口概述

本文档定义了门禁模块前端API接口规范，包括RESTful API设计和WebSocket实时通信接口。

## 🔐 认证授权

### 认证方式
- **Token认证**: Bearer Token
- **权限级别**: 五级安全控制（绝密/机密/秘密/内部/公开）
- **认证头**: `Authorization: Bearer {token}`

### 权限验证
- **接口级权限**: 基于角色和权限的访问控制
- **数据级权限**: 基于区域和时间的数据隔离
- **操作级权限**: 增删改查操作的细粒度控制

## 🌐 RESTful API设计
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
### 基础路径
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
```
Base URL: https://api.ioedream.com/access/v1
```

### 通用响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-12-02T10:30:00Z",
  "requestId": "req-123456"
}
```

### 错误响应格式
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": null,
  "errors": [
    {
      "field": "deviceId",
      "message": "设备ID不能为空"
    }
  ],
  "timestamp": "2025-12-02T10:30:00Z",
  "requestId": "req-123456"
}
```

## 📱 设备管理API

### 1. 设备列表查询

**接口路径**: `GET /devices`

**请求参数**:
```json
{
  "page": 1,
  "size": 20,
  "deviceType": "DOOR|CAMERA|READER",
  "areaId": 1001,
  "status": "ONLINE|OFFLINE|MAINTENANCE",
  "keyword": "门禁1"
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "total": 156,
    "page": 1,
    "size": 20,
    "records": [
      {
        "deviceId": "DEV-001",
        "deviceName": "主楼前门",
        "deviceType": "DOOR",
        "areaName": "主楼1层",
        "status": "ONLINE",
        "location": "东门",
        "ipAddress": "192.168.1.100",
        "lastHeartbeat": "2025-12-02T10:28:00Z",
        "onlineDuration": "2天5小时",
        "isConnected": true,
        "permissions": ["VIEW", "CONTROL"]
      }
    ]
  }
}
```

### 2. 设备详情查询

**接口路径**: `GET /devices/{deviceId}`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "deviceId": "DEV-001",
    "deviceName": "主楼前门",
    "deviceType": "DOOR",
    "model": "ZKBio-ACC-200",
    "manufacturer": "ZKBio",
    "serialNumber": "ACC200-2023-001",
    "areaId": 1001,
    "areaName": "主楼1层",
    "status": "ONLINE",
    "location": {
      "building": "主楼",
      "floor": "1层",
      "description": "东门",
      "coordinates": {
        "latitude": 39.908823,
        "longitude": 116.397470
      }
    },
    "network": {
      "ipAddress": "192.168.1.100",
      "macAddress": "AA:BB:CC:DD:EE:FF",
      "port": 8080,
      "protocol": "TCP"
    },
    "features": {
      "remoteUnlock": true,
      "intercom": true,
      "cardReader": true,
      "faceRecognition": true,
      "fingerprint": true,
      "password": false
    },
    "security": {
      "antiPassback": true,
      "duressCode": false,
      "multiFactor": true
    },
    "statusHistory": [
      {
        "status": "ONLINE",
        "changeTime": "2025-12-02T10:28:00Z",
        "reason": "设备上线"
      }
    ]
  }
}
```

### 3. 设备远程控制

**接口路径**: `POST /devices/{deviceId}/control`

**请求参数**:
```json
{
  "action": "UNLOCK|LOCK|RESTART",
  "duration": 5000,
  "reason": "访客通行"
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "deviceId": "DEV-001",
    "action": "UNLOCK",
    "executeTime": "2025-12-02T10:30:00Z",
    "result": "SUCCESS",
    "message": "门锁开启成功"
  }
}
```

## 🔓 区域空间管理API

### 1. 区域树结构查询

**接口路径**: `GET /areas/tree`

**响应数据**:
```json
{
  "code": 200,
  "data": [
    {
      "areaId": 1000,
      "areaName": "总部大楼",
      "areaCode": "HQ",
      "parentId": null,
      "level": 1,
      "areaType": "BUILDING",
      "children": [
        {
          "areaId": 1001,
          "areaName": "主楼",
          "areaCode": "HQ-MAIN",
          "parentId": 1000,
          "level": 2,
          "areaType": "FLOOR",
          "children": [
            {
              "areaId": 10011,
              "areaName": "1层大厅",
              "areaCode": "HQ-MAIN-1F",
              "parentId": 1001,
              "level": 3,
              "areaType": "ROOM"
            }
          ]
        }
      ]
    }
  ]
}
```

### 2. 区域权限分配

**接口路径**: `POST /areas/{areaId}/permissions`

**请求参数**:
```json
{
  "userIds": [1001, 1002],
  "roleIds": [2001, 2002],
  "permissions": {
    "enter": true,
    "viewDevices": true,
    "controlDevices": false,
    "viewRecords": true
  },
  "validity": {
    "startTime": "2025-12-02T09:00:00Z",
    "endTime": "2025-12-02T18:00:00Z",
    "weekdays": [1,2,3,4,5],
    "holidays": false
  }
}
```

## 📊 实时监控API

### 1. 实时监控数据

**接口路径**: `GET /monitoring/realtime`

**请求参数**:
```json
{
  "areaIds": [1001, 1002],
  "deviceTypes": ["DOOR", "CAMERA"],
  "limit": 50
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "summary": {
      "totalDevices": 156,
      "onlineDevices": 142,
      "offlineDevices": 14,
      "onlineRate": 91.0
    },
    "devices": [
      {
        "deviceId": "DEV-001",
        "deviceName": "主楼前门",
        "status": "ONLINE",
        "lastUpdate": "2025-12-02T10:30:00Z",
        "currentUsers": 2,
        "events": [
          {
            "eventId": "EVT-001",
            "eventType": "ACCESS_GRANTED",
            "userId": 1001,
            "userName": "张三",
            "timestamp": "2025-12-02T10:29:30Z",
            "result": "SUCCESS"
          }
        ],
        "alerts": []
      }
    ]
  }
}
```

### 2. 实时告警信息

**接口路径**: `GET /monitoring/alerts`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "total": 3,
    "alerts": [
      {
        "alertId": "ALT-001",
        "deviceId": "DEV-005",
        "deviceName": "侧门",
        "alertType": "DEVICE_OFFLINE",
        "severity": "HIGH",
        "message": "设备离线超过5分钟",
        "timestamp": "2025-12-02T10:25:00Z",
        "isHandled": false,
        "handler": null,
        "handleTime": null
      }
    ]
  }
}
```

## 📋 事件记录查询API

### 1. 事件列表查询

**接口路径**: `GET /events`

**请求参数**:
```json
{
  "page": 1,
  "size": 20,
  "startTime": "2025-12-01T00:00:00Z",
  "endTime": "2025-12-02T23:59:59Z",
  "deviceIds": ["DEV-001", "DEV-002"],
  "areaIds": [1001, 1002],
  "eventTypes": ["ACCESS_GRANTED", "ACCESS_DENIED", "DOOR_OPEN"],
  "userIds": [1001, 1002],
  "result": "SUCCESS|FAILED|TIMEOUT"
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "total": 2345,
    "page": 1,
    "size": 20,
    "summary": {
      "successCount": 2100,
      "failedCount": 245,
      "successRate": 89.5
    },
    "records": [
      {
        "eventId": "EVT-12345",
        "deviceId": "DEV-001",
        "deviceName": "主楼前门",
        "areaName": "主楼1层大厅",
        "eventType": "ACCESS_GRANTED",
        "userId": 1001,
        "userName": "张三",
        "userType": "EMPLOYEE",
        "accessMethod": "CARD",
        "cardNumber": "****-****-****-1234",
        "timestamp": "2025-12-02T10:15:30Z",
        "result": "SUCCESS",
        "duration": 1200,
        "photoUrl": "https://api.ioedream.com/photos/evt-12345.jpg",
        "description": "门禁卡刷卡成功"
      }
    ]
  }
}
```

### 2. 事件详情查询

**接口路径**: `GET /events/{eventId}`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "eventId": "EVT-12345",
    "deviceId": "DEV-001",
    "deviceName": "主楼前门",
    "areaId": 1001,
    "areaName": "main-building-1f-hall",
    "eventType": "ACCESS_GRANTED",
    "userId": 1001,
    "userName": "张三",
    "userType": "EMPLOYEE",
    "department": "技术部",
    "position": "高级工程师",
    "accessMethod": "CARD",
    "credentialInfo": {
      "cardNumber": "****-****-****-1234",
      "cardType": "EMPLOYEE_CARD",
      "cardHolder": "张三"
    },
    "verificationData": {
      "method": "CARD",
      "result": "SUCCESS",
      "matchScore": 0.95,
      "verificationTime": 200
    },
    "timestamp": "2025-12-02T10:15:30Z",
    "result": "SUCCESS",
    "duration": 1200,
    "media": {
      "photoUrl": "https://api.ioedream.com/photos/evt-12345.jpg",
      "videoUrl": "https://api.ioedream.com/videos/evt-12345.mp4",
      "audioUrl": null
    },
    "location": {
      "latitude": 39.908823,
      "longitude": 116.397470,
      "altitude": 5.2
    },
    "additionalInfo": {
      "entryDirection": "IN",
      "temperature": 36.5,
      "deviceBattery": 85,
      "networkSignal": -45
    }
  }
}
```

## 🔄 审批流程管理API

### 1. 审批申请提交

**接口路径**: `POST /approvals/requests`

**请求参数**:
```json
{
  "requestType": "TEMPORARY_ACCESS",
  "applicantId": 1001,
  "applicantName": "李四",
  "applicantDept": "市场部",
  "targetAreaIds": [1001, 1002],
  "accessPurpose": "客户洽谈",
  "startTime": "2025-12-03T14:00:00Z",
  "endTime": "2025-12-03T16:00:00Z",
  "reason": "接待重要客户",
  "attachments": [
    {
      "fileName": "客户邀请函.pdf",
      "fileUrl": "https://api.ioedream.com/files/invite-letter.pdf",
      "fileSize": 1024000
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "requestId": "REQ-001",
    "requestNumber": "APR-202512-001",
    "status": "PENDING",
    "submitTime": "2025-12-02T10:30:00Z",
    "estimatedProcessTime": 2,
    "currentApprover": "王经理"
  }
}
```

### 2. 审批列表查询

**接口路径**: `GET /approvals/requests`

**请求参数**:
```json
{
  "page": 1,
  "size": 20,
  "status": "PENDING|APPROVED|REJECTED",
  "requestType": "TEMPORARY_ACCESS|AREA_ACCESS",
  "applicantId": 1001,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}
```

### 3. 审批处理

**接口路径**: `POST /approvals/{requestId}/process`

**请求参数**:
```json
{
  "action": "APPROVE|REJECT|REVOKE",
  "comment": "审批通过，请注意安全规范",
  "conditions": {
    "requireEscort": true,
    "additionalVerification": true
  }
}
```

## ⚙️ 系统配置API

### 1. 系统参数配置

**接口路径**: `GET /config/parameters`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "accessControl": {
      "maxAttempts": 3,
      "lockoutDuration": 300,
      "antiPassbackEnabled": true,
      "multiFactorRequired": true
    },
    "monitoring": {
      "heartbeatInterval": 30,
      "offlineThreshold": 300,
      "alertThreshold": 100,
      "logRetentionDays": 365
    },
    "security": {
      "sessionTimeout": 1800,
      "passwordComplexity": true,
      "twoFactorEnabled": true,
      "auditLogEnabled": true
    }
  }
}
```

### 2. 用户权限查询

**接口路径**: `GET /users/{userId}/permissions`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "userId": 1001,
    "userName": "张三",
    "roles": ["EMPLOYEE", "MANAGER"],
    "permissions": {
      "devices": ["VIEW", "CONTROL", "CONFIG"],
      "areas": [1001, 1002, 1003],
      "events": ["VIEW", "EXPORT"],
      "approvals": ["APPLY", "APPROVE", "MANAGE"]
    },
    "securityLevel": "INTERNAL",
    "validAreas": [
      {
        "areaId": 1001,
        "areaName": "主楼1层",
        "accessTime": [
          {
            "dayOfWeek": 1,
            "startTime": "09:00",
            "endTime": "18:00"
          }
        ]
      }
    ]
  }
}
```

## 🔔 WebSocket实时通信

### 连接端点
```
ws://api.ioedream.com/access/v1/ws
```

### 认证
```json
{
  "type": "AUTH",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 订阅消息

```json
{
  "type": "SUBSCRIBE",
  "channels": [
    "device-status",
    "access-events",
    "alerts",
    "approvals"
  ]
}
```

### 实时消息格式

#### 设备状态变更
```json
{
  "type": "DEVICE_STATUS",
  "data": {
    "deviceId": "DEV-001",
    "status": "OFFLINE",
    "timestamp": "2025-12-02T10:30:00Z",
    "reason": "网络连接中断"
  }
}
```

#### 访问事件通知
```json
{
  "type": "ACCESS_EVENT",
  "data": {
    "eventId": "EVT-12346",
    "deviceId": "DEV-001",
    "eventType": "ACCESS_GRANTED",
    "userId": 1001,
    "userName": "张三",
    "timestamp": "2025-12-02T10:30:00Z",
    "result": "SUCCESS"
  }
}
```

#### 告警通知
```json
{
  "type": "ALERT",
  "data": {
    "alertId": "ALT-002",
    "deviceId": "DEV-005",
    "alertType": "MULTIPLE_FAILED_ATTEMPTS",
    "severity": "MEDIUM",
    "message": "门禁多次验证失败",
    "timestamp": "2025-12-02T10:30:00Z",
    "requireAction": true
  }
}
```

## 📱 移动端专用API

### 移动端设备信息获取

**接口路径**: `GET /mobile/device-info`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "deviceId": "MOB-001",
    "platform": "iOS|Android",
    "appVersion": "2.1.0",
    "osVersion": "15.1",
    "deviceModel": "iPhone 13",
    "pushToken": "aps-abc123",
    "biometricSupport": {
      "faceId": true,
      "touchId": true,
      "fingerprint": false
    }
  }
}
```

### 移动端推送令牌注册

**接口路径**: `POST /mobile/register-push-token`

**请求参数**:
```json
{
  "userId": 1001,
  "pushToken": "aps-abc123-def456",
  "platform": "ios",
  "deviceType": "mobile"
}
```

### 移动端生物识别验证

**接口路径**: `POST /mobile/biometric/verify`

**请求参数**:
```json
{
  "biometricType": "FACE|FINGERPRINT",
  "biometricData": "base64-encoded-biometric-data",
  "deviceId": "DEV-001",
  "userId": 1001
}
```

## 📊 统计报表API

### 1. 访问统计报表

**接口路径**: `GET /reports/access-statistics`

**请求参数**:
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "groupType": "DAY|WEEK|MONTH",
  "areaIds": [1001, 1002],
  "deviceIds": ["DEV-001"],
  "userTypes": ["EMPLOYEE", "VISITOR"]
}
```

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "summary": {
      "totalAccess": 12580,
      "successRate": 94.5,
      "failedRate": 5.5,
      "avgDailyAccess": 420
    },
    "chartData": [
      {
        "date": "2025-12-01",
        "successCount": 410,
        "failedCount": 25,
        "totalCount": 435,
        "successRate": 94.3
      }
    ]
  }
}
```

### 2. 设备使用统计

**接口路径**: `GET /reports/device-statistics`

**响应数据**:
```json
{
  "code": 200,
  "data": {
    "deviceUsage": [
      {
        "deviceId": "DEV-001",
        "deviceName": "主楼前门",
        "totalAccess": 1250,
        "dailyAverage": 41.7,
        "peakHour": "09:00",
        "utilizationRate": 78.5
      }
    ]
  }
}
```

## 🔒 安全接口

### 安全级别验证

**接口路径**: `POST /security/verify-security-level`

**请求参数**:
```json
{
  "userId": 1001,
  "operation": "VIEW_SENSITIVE_AREA",
  "areaId": 1001,
  "additionalAuth": {
    "password": "encrypted-password",
    "biometric": "biometric-hash"
  }
}
```

### 操作审计日志

**接口路径**: `POST /audit/log`

**请求参数**:
```json
{
  "userId": 1001,
  "operation": "UNLOCK_DOOR",
  "resourceId": "DEV-001",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2025-12-02T10:30:00Z",
  "result": "SUCCESS"
}
```

## 📝 版本控制

### API版本信息
- **当前版本**: v1.0.0
- **版本策略**: 语义化版本控制
- **兼容性**: 向后兼容至少2个大版本

### 版本历史
- v1.0.0: 初始版本发布
- v0.9.0: 测试版本
- v0.8.0: 开发版本

---

## 🔗 相关文档

- [门禁模块数据库设计](./05-数据库设计与ER图.md)
- [门禁模块系统架构](./01-系统整体架构流程图.md)
- [前端界面原型设计](./前端原型设计.md)
- [移动端适配规范](./移动端适配规范.md)

---

*本文档遵循RESTful API设计规范，确保接口的一致性和易用性。*