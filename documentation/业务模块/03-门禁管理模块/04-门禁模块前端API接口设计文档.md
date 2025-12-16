# IOE-DREAM门禁管理模块 - 前端API接口设计文档

> **模块版本**: v2.0.0
> **创建时间**: 2025-12-16
> **最后更新**: 2025-12-16
> **API版本**: v1.0.0
> **协议版本**: HTTP/1.1, HTTPS
> **数据格式**: JSON

## 📋 接口概述

### 设计原则

遵循RESTful API设计规范，确保接口的一致性、易用性和可维护性：

- **统一响应格式**: 所有接口返回统一的数据格式
- **版本控制**: 支持API版本管理和向后兼容
- **安全认证**: 基于Sa-Token的身份认证和权限控制
- **参数验证**: 完整的参数验证和错误提示
- **文档规范**: 完整的API文档和示例代码

### 基础信息

- **Base URL**: `http://localhost:8090/api/access/v1`
- **协议**: HTTPS
- **字符编码**: UTF-8
- **数据格式**: JSON
- **认证方式**: Sa-Token (Bearer Token)

### 通用响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-12-16T10:30:00Z",
  "requestId": "req-123456789"
}
```

#### 错误响应
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": null,
  "errors": [
    {
      "field": "deviceId",
      "message": "设备ID不能为空",
      "code": "FIELD_REQUIRED"
    }
  ],
  "timestamp": "2025-12-16T10:30:00Z",
  "requestId": "req-123456789"
}
```

#### 分页响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 50,
    "records": []
  },
  "timestamp": "2025-12-16T10:30:00Z",
  "requestId": "req-123456789"
}
```

---

## 🔐 认证授权

### Token认证

#### 请求头
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 权限控制
- **接口级权限**: 基于角色的接口访问控制
- **数据级权限**: 基于用户和区域的数据访问控制
- **操作级权限**: 增删改查操作的细粒度控制

### 权限级别

```yaml
# 权限级别定义
security_levels:
  - level_1: "公开"      # 公开数据，无限制访问
  - level_2: "内部"      # 内部数据，需要身份认证
  - level_3: "机密"      # 机密数据，需要额外授权
  - level_4: "绝密"      # 绝密数据，需要高级授权
  - level_5: "最高机密"  # 最高机密数据，需要特殊授权
```

---

## 📱 设备管理API

### 1. 设备列表查询

**接口地址**: `GET /devices`

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "deviceType": 1,
  "areaId": 1001,
  "status": 1,
  "keyword": "主楼",
  "onlineStatus": 1
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "records": [
      {
        "deviceId": 1001,
        "deviceNo": "DEV-001",
        "deviceName": "主楼前门",
        "deviceType": 1,
        "deviceTypeName": "门禁控制器",
        "deviceModel": "ZKBio-ACC-200",
        "manufacturer": "ZKBio",
        "areaId": 1001,
        "areaName": "主楼1层",
        "locationDesc": "东门",
        "ipAddress": "192.168.1.100",
        "port": 8080,
        "protocolType": "TCP",
        "deviceStatus": 1,
        "deviceStatusName": "在线",
        "lastHeartbeat": "2025-12-16T10:25:00Z",
        "onlineDuration": 7200,
        "securityLevel": 2,
        "biometricSupport": "face,fingerprint",
        "createTime": "2025-12-01T10:00:00Z",
        "updateTime": "2025-12-16T10:25:00Z"
      }
    ]
  }
}
```

### 2. 设备详情查询

**接口地址**: `GET /devices/{deviceId}`

**路径参数**:
- `deviceId`: 设备ID

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "deviceId": 1001,
    "deviceNo": "DEV-001",
    "deviceName": "主楼前门",
    "deviceType": 1,
    "deviceTypeName": "门禁控制器",
    "deviceModel": "ZKBio-ACC-200",
    "manufacturer": "ZKBio",
    "serialNumber": "ACC200-2023-001",
    "firmwareVersion": "v2.1.0",
    "hardwareVersion": "v1.0.0",

    "area": {
      "areaId": 1001,
      "areaName": "主楼1层",
      "building": "主楼",
      "floor": "1层",
      "room": "大厅"
    },

    "location": {
      "latitude": 39.908823,
      "longitude": 116.397470,
      "altitude": 5.2
    },

    "network": {
      "ipAddress": "192.168.1.100",
      "port": 8080,
      "macAddress": "AA:BB:CC:DD:EE:FF",
      "protocolType": "TCP"
    },

    "config": {
      "maxUsers": 1000,
      "timeoutDuration": 30,
      "retryAttempts": 3,
      "antiPassback": true,
      "remoteUnlock": true
    },

    "features": {
      "cardSupport": true,
      "faceSupport": true,
      "fingerprintSupport": true,
      "passwordSupport": false,
      "qrCodeSupport": false
    },

    "security": {
      "securityLevel": 2,
      "antiPassback": true,
      "duressCode": false,
      "multiFactorAuth": true
    },

    "status": {
      "deviceStatus": 1,
      "deviceStatusName": "在线",
      "lastHeartbeat": "2025-12-16T10:25:00Z",
      "onlineDuration": 7200,
      "lastOfflineTime": null
    },

    "maintenance": {
      "installationDate": "2025-12-01",
      "warrantyDate": "2026-12-01",
      "maintenanceCycle": 90,
      "lastMaintenanceDate": "2025-11-15",
      "nextMaintenanceDate": "2026-02-13"
    },

    "statistics": {
      "totalAccess": 1250,
      "todayAccess": 42,
      "successRate": 98.5,
      "failureCount": 18,
      "avgResponseTime": 200
    }
  }
}
```

### 3. 设备添加

**接口地址**: `POST /devices`

**请求参数**:
```json
{
  "deviceNo": "DEV-002",
  "deviceName": "主楼后门",
  "deviceType": 1,
  "deviceModel": "ZKBio-ACC-200",
  "manufacturer": "ZKBio",
  "serialNumber": "ACC200-2023-002",
  "areaId": 1001,
  "locationDesc": "后门",
  "building": "主楼",
  "floor": "1层",
  "room": "后厅",
  "ipAddress": "192.168.1.101",
  "port": 8080,
  "protocolType": "TCP",
  "configData": {
    "maxUsers": 1000,
    "timeoutDuration": 30,
    "antiPassback": true
  },
  "securityLevel": 2,
  "biometricSupport": "face,fingerprint",
  "cardSupport": true,
  "faceSupport": true,
  "fingerprintSupport": true
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "设备添加成功",
  "data": {
    "deviceId": 1002,
    "deviceNo": "DEV-002"
  }
}
```

### 4. 设备更新

**接口地址**: `PUT /devices/{deviceId}`

**路径参数**:
- `deviceId`: 设备ID

**请求参数**:
```json
{
  "deviceName": "主楼后门(更新)",
  "locationDesc": "后门入口",
  "configData": {
    "maxUsers": 1200,
    "timeoutDuration": 25
  }
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "设备更新成功",
  "data": null
}
```

### 5. 设备删除

**接口地址**: `DELETE /devices/{deviceId}`

**路径参数**:
- `deviceId`: 设备ID

**响应数据**:
```json
{
  "code": 200,
  "message": "设备删除成功",
  "data": null
}
```

### 6. 设备远程控制

**接口地址**: `POST /devices/{deviceId}/control`

**路径参数**:
- `deviceId`: 设备ID

**请求参数**:
```json
{
  "action": "UNLOCK",
  "duration": 5000,
  "reason": "访客通行",
  "operatorId": 10001,
  "operatorName": "管理员"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "设备控制成功",
  "data": {
    "deviceId": 1001,
    "action": "UNLOCK",
    "executeTime": "2025-12-16T10:30:00Z",
    "result": "SUCCESS",
    "message": "门锁开启成功",
    "duration": 1200
  }
}
```

### 7. 设备配置同步

**接口地址**: `POST /devices/{deviceId}/sync-config`

**路径参数**:
- `deviceId`: 设备ID

**请求参数**:
```json
{
  "configItems": [
    {
      "configKey": "maxUsers",
      "configValue": "1500",
      "configDesc": "最大用户数"
    },
    {
      "configKey": "timeoutDuration",
      "configValue": "25",
      "configDesc": "超时时间(秒)"
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "配置同步成功",
  "data": {
    "deviceId": 1001,
    "syncTime": "2025-12-16T10:30:00Z",
    "syncResult": "SUCCESS",
    "successCount": 2,
    "failureCount": 0
  }
}
```

---

## 🔓 权限管理API

### 1. 权限列表查询

**接口地址**: `GET /permissions`

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "userId": 1001,
  "userName": "张三",
  "deviceType": 1,
  "areaId": 1001,
  "permissionType": 1,
  "permissionStatus": 1,
  "approvalStatus": 1
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "records": [
      {
        "permissionId": 1001,
        "permissionNo": "PERM-001",
        "userId": 1001,
        "userName": "张三",
        "userType": 1,
        "userTypeName": "员工",
        "userDept": "技术部",
        "userPosition": "高级工程师",
        "deviceId": 1001,
        "deviceName": "主楼前门",
        "areaId": 1001,
        "areaName": "主楼1层",
        "permissionType": 1,
        "permissionTypeName": "永久权限",
        "accessMethod": "card,face,fingerprint",
        "biometricRequired": true,
        "multiFactorRequired": false,
        "validStartTime": "2025-01-01T09:00:00Z",
        "validEndTime": "2025-12-31T18:00:00Z",
        "timeRestrictions": {
          "weekdays": [1,2,3,4,5],
          "startTime": "09:00",
          "endTime": "18:00"
        },
        "accessCount": 0,
        "usedCount": 156,
        "lastAccessTime": "2025-12-16T10:25:00Z",
        "permissionStatus": 1,
        "permissionStatusName": "启用",
        "approvalStatus": 1,
        "approvalStatusName": "已通过",
        "createTime": "2025-01-01T10:00:00Z",
        "updateTime": "2025-12-16T10:25:00Z"
      }
    ]
  }
}
```

### 2. 权限详情查询

**接口地址**: `GET /permissions/{permissionId}`

**路径参数**:
- `permissionId`: 权限ID

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "permissionId": 1001,
    "permissionNo": "PERM-001",
    "userId": 1001,
    "userName": "张三",
    "userType": 1,
    "userTypeName": "员工",
    "userDept": "技术部",
    "userPosition": "高级工程师",
    "deviceId": 1001,
    "deviceName": "主楼前门",
    "deviceType": 1,
    "deviceTypeName": "门禁控制器",
    "areaId": 1001,
    "areaName": "主楼1层",

    "permission": {
      "permissionType": 1,
      "permissionTypeName": "永久权限",
      "permissionLevel": 1,
      "accessMethod": "card,face,fingerprint",
      "biometricRequired": true,
      "multiFactorRequired": false,
      "emergencyAccess": false,
      "escortRequired": false
    },

    "timeRestrictions": {
      "validStartTime": "2025-01-01T09:00:00Z",
      "validEndTime": "2025-12-31T18:00:00Z",
      "weekdays": [1,2,3,4,5],
      "startTime": "09:00",
      "endTime": "18:00",
      "holidayAccess": false,
      "dateRestrictions": [],
      "customRules": []
    },

    "usage": {
      "accessCount": 0,
      "dailyCount": 0,
      "usedCount": 156,
      "lastAccessTime": "2025-12-16T10:25:00Z",
      "accessStatistics": {
        "monthlyAccess": 42,
        "weeklyAccess": 12,
        "todayAccess": 2
      }
    },

    "approval": {
      "approvalStatus": 1,
      "approvalStatusName": "已通过",
      "approverId": 10001,
      "approverName": "管理员",
      "approvalTime": "2025-01-01T10:30:00Z",
      "approvalComment": "权限申请已批准",
      "approvalProcessId": 1001
    },

    "security": {
      "riskLevel": 1,
      "securityLevel": 1,
      "accessReason": "正常工作需要",
      "additionalAuth": false
    },

    "createTime": "2025-01-01T10:00:00Z",
    "updateTime": "2025-12-16T10:25:00Z",
    "createUserId": 10001,
    "updateUserId": 1001
  }
}
```

### 3. 权限申请

**接口地址**: `POST /permissions/apply`

**请求参数**:
```json
{
  "userId": 1002,
  "userName": "李四",
  "userType": 1,
  "userDept": "市场部",
  "userPosition": "市场经理",
  "deviceId": 1001,
  "deviceName": "主楼前门",
  "permissionType": 2,
  "accessMethod": "card,face",
  "validStartTime": "2025-12-20T09:00:00Z",
  "validEndTime": "2025-12-20T18:00:00Z",
  "timeRestrictions": {
    "weekdays": [1,2,3,4,5],
    "startTime": "09:00",
    "endTime": "18:00"
  },
  "accessReason": "客户会议需要访问",
  "attachments": [
    {
      "fileName": "客户邀请函.pdf",
      "fileUrl": "/files/attachments/invite-letter.pdf",
      "fileSize": 1024000
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "权限申请提交成功",
  "data": {
    "permissionId": 1002,
    "permissionNo": "PERM-002",
    "processId": 1001,
    "processNo": "PROC-20251216-001",
    "status": "PENDING",
    "estimatedProcessTime": 2,
    "currentApprover": "王经理",
    "submitTime": "2025-12-16T10:30:00Z"
  }
}
```

### 4. 权限审批

**接口地址**: `POST /permissions/{permissionId}/approve`

**路径参数**:
- `permissionId`: 权限ID

**请求参数**:
```json
{
  "action": "APPROVE",
  "comment": "权限申请已审批通过，请注意安全规范",
  "conditions": {
    "escortRequired": true,
    "additionalVerification": false,
    "timeLimitations": {
      "maxDailyAccess": 10,
      "restrictedAreas": []
    }
  },
  "operatorId": 10001,
  "operatorName": "王经理"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "权限审批完成",
  "data": {
    "permissionId": 1002,
    "approvalResult": "APPROVED",
    "approvalTime": "2025-12-16T10:35:00Z",
    "effectiveTime": "2025-12-20T09:00:00Z",
    "operatorId": 10001,
    "operatorName": "王经理"
  }
}
```

### 5. 权限批量操作

**接口地址**: `POST /permissions/batch`

**请求参数**:
```json
{
  "operation": "APPROVE",
  "permissionIds": [1002, 1003, 1004],
  "comment": "批量审批权限申请",
  "operatorId": 10001,
  "operatorName": "管理员"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "批量操作完成",
  "data": {
    "totalCount": 3,
    "successCount": 3,
    "failureCount": 0,
    "results": [
      {
        "permissionId": 1002,
        "operation": "APPROVE",
        "result": "SUCCESS",
        "message": "权限审批成功"
      }
    ]
  }
}
```

---

## 📊 通行记录API

### 1. 通行记录查询

**接口地址**: `GET /records`

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2025-12-16T23:59:59Z",
  "deviceIds": [1001, 1002],
  "areaIds": [1001, 1002],
  "userIds": [1001, 1002],
  "userTypes": [1, 2],
  "accessTypes": [1, 2],
  "accessResults": [0, 1],
  "alertLevels": [0, 1, 2]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 50,
    "summary": {
      "totalCount": 1000,
      "successCount": 950,
      "failureCount": 50,
      "successRate": 95.0,
      "todayCount": 120,
      "hourlyCount": 8
    },
    "records": [
      {
        "recordId": 10001,
        "accessSn": "ACC-20251216-001",
        "userId": 1001,
        "userName": "张三",
        "userType": 1,
        "userTypeName": "员工",
        "device": {
          "deviceId": 1001,
          "deviceName": "主楼前门",
          "deviceType": 1,
          "deviceTypeName": "门禁控制器"
        },
        "area": {
          "areaId": 1001,
          "areaName": "主楼1层"
        },
        "accessType": 1,
        "accessTypeName": "进入",
        "accessMethod": "face",
        "accessResult": 0,
        "accessResultName": "成功",
        "verifyScore": 0.9850,
        "processDuration": 1200,
        "accessTime": "2025-12-16T09:30:00Z",
        "photoUrl": "/photos/records/2025-12-16/acc-001.jpg",
        "temperature": 36.5,
        "maskDetected": 1,
        "livenessDetected": 1,
        "riskScore": 0.1,
        "riskLevel": 0,
        "alertLevel": 0,
        "createTime": "2025-12-16T09:30:00Z"
      }
    ]
  }
}
```

### 2. 通行记录详情

**接口地址**: `GET /records/{recordId}`

**路径参数**:
- `recordId`: 记录ID

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "recordId": 10001,
    "accessSn": "ACC-20251216-001",
    "user": {
      "userId": 1001,
      "userName": "张三",
      "userType": 1,
      "userTypeName": "员工",
      "userDept": "技术部",
      "userPosition": "高级工程师",
      "userCardNo": "****-****-****-1234"
    },
    "device": {
      "deviceId": 1001,
      "deviceName": "主楼前门",
      "deviceType": 1,
      "deviceTypeName": "门禁控制器",
      "location": {
        "ipAddress": "192.168.1.100",
        "port": 8080,
        "macAddress": "AA:BB:CC:DD:EE:FF"
      }
    },
    "access": {
      "accessType": 1,
      "accessTypeName": "进入",
      "accessMethod": "face",
      "accessResult": 0,
      "accessResultName": "成功",
      "verifyScore": 0.9850,
      "processDuration": 1200,
      "accessTime": "2025-12-16T09:30:00Z"
    },
    "verification": {
      "verifyMethod": "face",
      "templateId": "TEMP-001",
      "templateMatchScore": 0.9850,
      "livenessCheck": true,
      "livenessScore": 0.9920,
      "biometricData": {
        "faceImage": "/faces/verify/2025-12-16/user-001.jpg",
        "faceFeatures": "encrypted-face-features",
        "comparisonResult": "match"
      }
    },
    "media": {
      "photoUrl": "/photos/records/2025-12-16/acc-001.jpg",
      "videoUrl": "/videos/records/2025-12-16/acc-001.mp4",
      "audioUrl": null
    },
    "environment": {
      "temperature": 36.5,
      "humidity": 45.2,
      "noiseLevel": 30.5,
      "lightLevel": 800
    },
    "security": {
      "riskScore": 0.1,
      "riskLevel": 0,
      "anomalyDetected": false,
      "securityCheck": "PASSED",
      "threatLevel": "LOW"
    },
    "location": {
      "latitude": 39.908823,
      "longitude": 116.397470,
      "altitude": 5.2,
      "building": "主楼",
      "floor": "1层",
      "room": "大厅",
      "gpsAccuracy": 2.0
    },
    "createTime": "2025-12-16T09:30:00Z"
  }
}
```

### 3. 通行统计查询

**接口地址**: `GET /records/statistics`

**请求参数**:
```json
{
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2025-12-16T23:59:59Z",
  "groupType": "DAY",
  "deviceIds": [1001, 1002],
  "areaIds": [1001, 1002],
  "userTypes": [1, 2],
  "statisticsType": "access,trend,user,device,area"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalAccess": 1000,
      "successCount": 950,
      "failureCount": 50,
      "successRate": 95.0,
      "avgDailyAccess": 500,
      "peakHour": "09:00",
      "peakAccess": 80
    },
    "trendData": [
      {
        "date": "2025-12-15",
        "totalAccess": 450,
        "successCount": 430,
        "failureCount": 20,
        "successRate": 95.6,
        "avgResponseTime": 210
      },
      {
        "date": "2025-12-16",
        "totalAccess": 550,
        "successCount": 520,
        "failureCount": 30,
        "successRate": 94.5,
        "avgResponseTime": 200
      }
    ],
    "userStatistics": [
      {
        "userId": 1001,
        "userName": "张三",
        "totalAccess": 120,
        "successCount": 115,
        "failureCount": 5,
        "successRate": 95.8,
        "avgDailyAccess": 6.0
      }
    ],
    "deviceStatistics": [
      {
        "deviceId": 1001,
        "deviceName": "主楼前门",
        "totalAccess": 600,
        "successCount": 580,
        "failureCount": 20,
        "successRate": 96.7,
        "avgDailyAccess": 30.0,
        "utilizationRate": 78.5
      }
    ],
    "areaStatistics": [
      {
        "areaId": 1001,
        "areaName": "主楼1层",
        "totalAccess": 800,
        "successCount": 760,
        "failureCount": 40,
        "successRate": 95.0,
        "peakHour": "09:00",
        "peakAccess": 60
      }
    ]
  }
}
```

### 4. 实时记录推送

**接口地址**: `WebSocket /records/realtime`

**连接认证**:
```json
{
  "type": "AUTH",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**订阅消息**:
```json
{
  "type": "SUBSCRIBE",
  "channels": [
    "access-events",
    "device-status",
    "alerts",
    "statistics"
  ]
}
```

**推送消息格式**:
```json
{
  "type": "ACCESS_EVENT",
  "data": {
    "recordId": 10002,
    "userId": 1002,
    "userName": "李四",
    "deviceId": 1001,
    "deviceName": "主楼前门",
    "accessType": "进入",
    "accessResult": "成功",
    "accessTime": "2025-12-16T10:30:00Z",
    "photoUrl": "/photos/records/2025-12-16/acc-002.jpg"
  }
}
```

---

## 📋 审批流程API

### 1. 审批流程列表

**接口地址**: `GET /approvals`

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "processType": 1,
  "processStatus": "PENDING",
  "applicantId": 1001,
  "applicantName": "张三",
  "priority": 2,
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2025-12-16T23:59:59Z"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 25,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 2,
    "records": [
      {
        "processId": 1001,
        "processNo": "PROC-20251216-001",
        "processTitle": "临时权限申请",
        "processType": 1,
        "processTypeName": "临时权限",
        "applicant": {
          "applicantId": 1001,
          "applicantName": "张三",
          "applicantDept": "技术部",
          "applicantPhone": "13800138000"
        },
        "applyContent": {
          "targetDevices": ["主楼前门", "主楼后门"],
          "accessReason": "客户会议需要",
          "startTime": "2025-12-20T14:00:00Z",
          "endTime": "2025-12-20T16:00:00Z"
        },
        "process": {
          "currentStep": 2,
          "totalSteps": 3,
          "processStatus": "IN_PROGRESS",
          "priority": 2,
          "estimatedProcessTime": 2
        },
        "approval": {
          "currentApproverId": 10002,
          "currentApproverName": "部门经理",
          "finalApproverId": null,
          "finalApproverName": null
        },
        "createTime": "2025-12-16T10:00:00Z",
        "updateTime": "2025-12-16T10:30:00Z"
      }
    ]
  }
}
```

### 2. 审批详情查询

**接口地址**: `GET /approvals/{processId}`

**路径参数**:
- `processId`: 流程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "processId": 1001,
    "processNo": "PROC-20251216-001",
    "processTitle": "临时权限申请",
    "processType": 1,
    "processTypeName": "临时权限",

    "applicant": {
      "applicantId": 1001,
      "applicantName": "张三",
      "applicantDept": "技术部",
      "applicantPhone": "13800138000",
      "applicantEmail": "zhangsan@company.com"
    },

    "applyContent": {
      "targetDevices": [
        {
          "deviceId": 1001,
          "deviceName": "主楼前门"
        }
      ],
      "accessReason": "客户会议需要临时访问权限",
      "startTime": "2025-12-20T14:00:00Z",
      "endTime": "2025-12-20T16:00:00Z",
      "attachments": [
        {
          "fileName": "客户邀请函.pdf",
          "fileUrl": "/files/approvals/invite-letter.pdf",
          "fileSize": 1024000
        }
      ]
    },

    "process": {
      "currentStep": 2,
      "totalSteps": 3,
      "processStatus": "IN_PROGRESS",
      "priority": 2,
      "urgencyLevel": 1,
      "estimatedProcessTime": 2,
      "actualProcessTime": 0.5,
      "autoApproval": false,
      "escalationConfig": {
        "escalationRules": [
          {
            "condition": "timeout > 2h",
            "action": "ESCALATE",
            "escalationTo": "部门总监"
          }
        ]
      }
    },

    "steps": [
      {
        "stepId": 1001,
        "stepName": "部门主管审批",
        "stepOrder": 1,
        "stepType": 1,
        "stepStatus": "COMPLETED",
        "approverId": 10002,
        "approverName": "部门经理",
        "approvalAction": "APPROVE",
        "approvalTime": "2025-12-16T10:15:00Z",
        "approvalComment": "申请合理，同意审批"
      },
      {
        "stepId": 1002,
        "stepName": "安全管理审批",
        "stepOrder": 2,
        "stepType": 1,
        "stepStatus": "PENDING",
        "approverId": 10003,
        "approverName": "安全主管"
      },
      {
        "stepId": 1003,
        "stepName": "最终审批",
        "stepOrder": 3,
        "stepType": 1,
        "stepStatus": "PENDING"
      }
    ],

    "approval": {
      "currentApproverId": 10003,
      "currentApproverName": "安全主管",
      "finalApproverId": null,
      "finalApproverName": null,
      "finalApproverTime": null
    },

    "notifications": {
      "createNotification": true,
      "updateNotification": true,
      "completeNotification": true,
      "escalationNotification": true
    },

    "createTime": "2025-12-16T10:00:00Z",
    "updateTime": "2025-12-16T10:30:00Z",
    "effectiveTime": null,
    "expireTime": null
  }
}
```

### 3. 审批处理

**接口地址**: `POST /approvals/{processId}/process`

**路径参数**:
- `processId`: 流程ID

**请求参数**:
```json
{
  "action": "APPROVE",
  "comment": "权限申请审批通过，请注意使用时间限制",
  "conditions": {
    "escortRequired": false,
    "additionalVerification": true,
    "timeLimitations": {
      "maxDailyAccess": 5,
      "restrictedAreas": ["会议室A", "服务器室"]
    }
  },
  "operatorId": 10003,
  "operatorName": "安全主管"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "审批处理完成",
  "data": {
    "processId": 1001,
    "action": "APPROVE",
    "result": "SUCCESS",
    "processStatus": "APPROVED",
    "finalApproverId": 10003,
    "finalApproverName": "安全主管",
    "approvalTime": "2025-12-16T10:35:00Z",
    "effectiveTime": "2025-12-20T14:00:00Z",
    "expireTime": "2025-12-20T16:00:00Z"
  }
}
```

### 4. 审批撤销

**接口地址**: `POST /approvals/{processId}/revoke`

**路径参数**:
- `processId`: 流程ID

**请求参数**:
```json
{
  "reason": "申请条件发生变化，需要撤销申请",
  "operatorId": 10001,
  "operatorName": "张三"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "审批流程已撤销",
  "data": {
    "processId": 1001,
    "processStatus": "CANCELLED",
    "revokeTime": "2025-12-16T10:40:00Z"
  }
}
```

---

## 🚨 监控告警API

### 1. 告警列表查询

**接口地址**: `GET /alerts`

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "alertType": "DEVICE_OFFLINE",
  "alertLevel": [2, 3, 4],
  "deviceId": 1001,
  "isHandled": false,
  "startTime": "2025-12-16T00:00:00Z",
  "endTime": "2025-12-16T23:59:59Z"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 15,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1,
    "summary": {
      "totalAlerts": 15,
      "unhandledAlerts": 8,
      "criticalAlerts": 3,
      "warningAlerts": 7,
      "infoAlerts": 5
    },
    "records": [
      {
        "alertId": 1001,
        "alertNo": "ALT-20251216-001",
        "alertType": "DEVICE_OFFLINE",
        "alertTypeName": "设备离线",
        "alertLevel": 3,
        "alertLevelName": "高风险",
        "deviceId": 1005,
        "deviceName": "侧门",
        "deviceType": 1,
        "areaName": "主楼2层",
        "alertMessage": "设备离线超过5分钟",
        "alertDescription": "侧门设备连接中断，请检查网络连接",
        "riskImpact": "medium",
        "isHandled": false,
        "handlerId": null,
        "handlerName": null,
        "handleTime": null,
        "handleComment": null,
        "alertTime": "2025-12-16T10:25:00Z",
        "createTime": "2025-12-16T10:25:00Z",
        "updateTime": "2025-12-16T10:25:00Z"
      }
    ]
  }
}
```

### 2. 告警处理

**接口地址**: `POST /alerts/{alertId}/handle`

**路径参数**:
- `alertId`: 告警ID

**请求参数**:
```json
{
  "action": "RESOLVE",
  "handleComment": "设备已重新上线，网络连接正常",
  "resolution": "重启设备网络服务",
  "operatorId": 10001,
  "operatorName": "运维工程师"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "告警处理完成",
  "data": {
    "alertId": 1001,
    "action": "RESOLVE",
    "handlerId": 10001,
    "handlerName": "运维工程师",
    "handleTime": "2025-12-16T10:35:00Z",
    "handleComment": "设备已重新上线，网络连接正常"
  }
}
```

### 3. 实时告警推送

**WebSocket地址**: `WebSocket /alerts/realtime`

**推送消息格式**:
```json
{
  "type": "ALERT",
  "data": {
    "alertId": 1002,
    "alertType": "MULTIPLE_FAILED_ATTEMPTS",
    "alertTypeName": "多次失败",
    "alertLevel": 2,
    "deviceId": 1001,
    "deviceName": "主楼前门",
    "alertMessage": "检测到多次认证失败",
    "alertTime": "2025-12-16T10:30:00Z",
    "requireAction": true,
    "suggestedActions": [
      "检查用户权限",
      "验证设备状态",
      "联系安全管理员"
    ]
  }
}
```

---

## 🔧 系统配置API

### 1. 系统参数查询

**接口地址**: `GET /config/parameters`

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "accessControl": {
      "maxAttempts": 3,
      "lockoutDuration": 300,
      "antiPassbackEnabled": true,
      "multiFactorRequired": true,
      "defaultTimeout": 30
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
      "auditLogEnabled": true,
      "encryptionEnabled": true
    },
    "performance": {
      "cacheEnabled": true,
      "cacheExpiration": 300,
      "connectionPoolMax": 50,
      "queryTimeout": 30
    },
    "notification": {
      "emailEnabled": true,
      "smsEnabled": true,
      "pushEnabled": true,
      "webhookEnabled": false
    }
  }
}
```

### 2. 系统参数更新

**接口地址**: `PUT /config/parameters`

**请求参数**:
```json
{
  "accessControl.maxAttempts": 5,
  "monitoring.heartbeatInterval": 60,
  "security.sessionTimeout": 3600,
  "performance.cacheExpiration": 600
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "参数更新成功",
  "data": null
}
```

---

## 📊 统计报表API

### 1. 访问统计报表

**接口地址**: `GET /reports/access-statistics`

**请求参数**:
```json
{
  "startTime": "2025-12-01T00:00:00Z",
  "endTime": "2025-12-31T23:59:59Z",
  "groupType": "DAY",
  "areaIds": [1001, 1002],
  "deviceIds": [1001, 1002],
  "userTypes": [1, 2],
  "reportFormat": "JSON"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "统计报表生成成功",
  "data": {
    "summary": {
      "totalAccess": 12580,
      "successRate": 94.5,
      "failedRate": 5.5,
      "avgDailyAccess": 420
    },
    "timeSeries": [
      {
        "date": "2025-12-01",
        "totalAccess": 410,
        "successCount": 390,
        "failedCount": 20,
        "successRate": 95.1,
        "avgResponseTime": 210
      }
    ],
    "userStatistics": [
      {
        "userId": 1001,
        "userName": "张三",
        "totalAccess": 125,
        "successCount": 118,
        "failedCount": 7,
        "successRate": 94.4
      }
    ],
    "deviceStatistics": [
      {
        "deviceId": 1001,
        "deviceName": "主楼前门",
        "totalAccess": 1250,
        "utilizationRate": 78.5
      }
    ],
    "areaStatistics": [
      {
        "areaId": 1001,
        "areaName": "主楼1层",
        "totalAccess": 800,
        "peakHour": "09:00",
        "peakAccess": 60
      }
    ]
  }
}
```

### 2. 设备使用统计

**接口地址**: `GET /reports/device-usage`

**请求参数**:
```json
{
  "startTime": "2025-12-01T00:00:00Z",
  "endTime": "2025-12-31T23:59:59Z",
  "deviceIds": [1001, 1002],
  "statisticsType": "usage,trend,failure"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "设备使用统计查询成功",
  "data": {
    "deviceUsage": [
      {
        "deviceId": 1001,
        "deviceName": "主楼前门",
        "deviceType": 1,
        "totalAccess": 1250,
        "dailyAverage": 41.7,
        "peakHour": "09:00",
        "utilizationRate": 78.5,
        "avgResponseTime": 200,
        "successRate": 96.8,
        "failureCount": 40,
        "onlineDuration": 7200,
        "maintenanceCount": 2
      }
    ],
    "usageTrend": [
      {
        "date": "2025-12-01",
        "accessCount": 420,
        "successRate": 95.1,
        "avgResponseTime": 210
      }
    ],
    "failureAnalysis": [
      {
        "failureType": "AUTHENTICATION_FAILED",
        "failureCount": 25,
        "failureRate": 2.0,
        "mainReasons": ["无效卡号", "权限不足", "设备故障"]
      }
    ]
  }
}
```

### 3. 用户行为分析

**接口地址**: `GET /reports/user-behavior`

**请求参数**:
```json
{
  "startTime": "2025-12-01T00:00:00Z",
  "endTime": "2025-12-31T23:59:59Z",
  "userIds": [1001, 1002],
  "analysisType": "pattern,anomaly,risk"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "用户行为分析完成",
  "data": {
    "userBehavior": [
      {
        "userId": 1001,
        "userName": "张三",
        "totalAccess": 125,
        "accessPattern": {
          "peakHours": ["09:00", "12:00", "18:00"],
          "preferredDevices": ["主楼前门", "主楼后门"],
          "avgAccessInterval": 4.5,
          "consistencyScore": 0.85
        },
        "anomalyScore": 0.2,
        "riskLevel": "LOW",
        "behaviorTrend": "STABLE"
      }
    ],
    "patternAnalysis": {
      "regularUsers": 80,
      "irregularUsers": 15,
      "highFrequencyUsers": 5
    },
    "riskAssessment": {
      "lowRiskUsers": 85,
      "mediumRiskUsers": 12,
      "highRiskUsers": 3
    }
  }
}
```

---

## 🔍 生物识别API

### 1. 生物识别模板管理

#### 模板注册

**接口地址**: `POST /biometric/templates`

**请求参数**:
```json
{
  "userId": 1001,
  "userName": "张三",
  "biometricType": 1,
  "biometricData": "base64-encoded-biometric-data",
  "deviceInfo": {
    "deviceId": 1001,
    "deviceName": "主楼前门"
  },
  "qualityScore": 0.95,
  "algorithm": "face-recognition-v2.0"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "生物识别模板注册成功",
  "data": {
    "templateId": 1001,
    "templateNo": "BIO-001",
    "biometricType": 1,
    "biometricTypeName": "人脸",
    "templateQuality": 0.95,
    "registrationTime": "2025-12-16T10:30:00Z"
  }
}
```

#### 模板查询

**接口地址**: `GET /biometric/templates`

**请求参数**:
```json
{
  "userId": 1001,
  "biometricType": 1,
  "templateStatus": 1
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "templates": [
      {
        "templateId": 1001,
        "templateNo": "BIO-001",
        "biometricType": 1,
        "biometricTypeName": "人脸",
        "templateVersion": 1,
        "templateQuality": 0.95,
        "isPrimary": true,
        "createTime": "2025-12-16T10:30:00Z",
        "lastVerifyTime": "2025-12-16T10:25:00Z",
        "verifyCount": 156,
        "successCount": 152,
        "failureCount": 4
      }
    ]
  }
}
```

### 2. 生物识别验证

**接口地址**: `POST /biometric/verify`

**请求参数**:
```json
  "userId": 1001,
  "biometricType": 1,
  "biometricData": "base64-encoded-biometric-data",
  "deviceId": 1001,
  "verifyScenario": "access",
  "livenessCheck": true
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "生物识别验证完成",
  "data": {
    "verifyId": 1001,
    "templateId": 1001,
    "userId": 1001,
    "userName": "张三",
    "biometricType": 1,
    "biometricTypeName": "人脸",
    "matchScore": 0.9850,
    "matchThreshold": 0.8000,
    "matchResult": true,
    "livenessCheck": true,
    "livenessScore": 0.9920,
    "verifyResult": "SUCCESS",
    "verifyDuration": 800,
    "confidence": 0.98
  }
}
```

---

## 📱� 移动端专用API

### 1. 移动端设备信息

**接口地址**: `GET /mobile/device-info`

**请求头**:
```http
User-Agent: IOE-DREAM-Mobile/2.0.0 (iOS; iPhone 14.0)
X-Device-ID: device-unique-id
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取移动端设备信息成功",
  "data": {
    "deviceId": "MOB-001",
    "platform": "iOS",
    "appVersion": "2.0.0",
    "osVersion": "14.0",
    "deviceModel": "iPhone 12",
    "pushToken": "aps-abc123-def456",
    "biometricSupport": {
      "faceId": true,
      "touchId": true,
      "fingerprint": false
    }
  }
}
```

### 2. 移动端推送令牌注册

**接口地址**: `POST /mobile/register-push-token`

**请求参数**:
```json
{
  "userId": 1001,
  "pushToken": "aps-abc123-def456",
  "platform": "ios",
  "deviceType": "mobile",
  "deviceInfo": {
    "platform": "iOS",
    "appVersion": "2.0.0",
    "osVersion": "14.0",
    "deviceModel": "iPhone 12"
  }
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "推送令牌注册成功",
  "data": {
    "token": "token-123456",
    "expireTime": "2025-12-17T10:30:00Z"
  }
}
```

### 3. 移动端生物识别验证

**接口地址**: `POST /mobile/biometric/verify`

**请求参数**:
```json
{
  "userId": 1001,
  "biometricType": 1,
  "biometricData": "base64-encoded-biometric-data",
  "deviceId": 1001,
  "scenario": "mobile-access",
  "additionalAuth": {
    "password": "encrypted-password"
  }
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "移动端生物识别验证完成",
  "data": {
    "verifyId": 1002,
    "matchScore": 0.9800,
    "verifyResult": "SUCCESS",
    "additionalAuthRequired": false,
    "verifyDuration": 1500
  }
}
```

---

## 📚 API版本管理

### 版本信息

#### 当前版本信息
- **API版本**: v1.0.0
- **版本策略**: 语义化版本控制
- **兼容性**: 向后兼容至少2个大版本
- **弃用通知**: 提前30天通知API弃用

### 版本历史
- v1.0.0: 初始版本发布（2025-12-16）
- v0.9.0: 测试版本
- v0.8.0: 开发版本

### 版本兼容性

#### 新版本发布流程
1. **影响评估**: 评估新版本对现有功能的影响
2. **测试验证**: 完整的功能测试和兼容性测试
3. **通知公告**: 提前30天发布公告和文档更新
4. **灰度发布**: 分批次进行灰度发布
5. **全量发布**: 确认无问题后进行全量发布

---

## 🛡️ 安全规范

### 1. 访问控制

#### 认证要求
- **Token认证**: 所有接口必须提供有效的Sa-Token
- **权限验证**: 根据接口要求进行权限验证
- **会话管理**: 会话超时自动登出

#### 权限控制
```java
// 接口级权限注解
@SaCheckPermission("access:device:query")
public ResponseDTO getDevices() {
    // 设备查询权限
}

// 数据级权限控制
@DataScope(value = "user_area", type = "BY_ORG")
public ResponseDTO getUserDevices() {
    // 只能查询用户所属区域的设备
}
```

### 2. 参数验证

#### 验证注解
```java
@PostMapping("/devices")
public ResponseDTO addDevice(@Valid @RequestBody DeviceAddForm form) {
    // 使用@Valid进行参数验证
}

// 自定义验证
public class DeviceAddForm {
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @Pattern(regexp = "^DEV-[0-9]{3}$", message = "设备编号格式不正确")
    private String deviceNo;

    @Range(min = 1, max = 5, message = "安全等级必须在1-5之间")
    private Integer securityLevel;
}
```

### 3. 数据脱敏

#### 脱敏策略
```java
@Component
public class DataMaskingService {

    public String maskSensitiveData(String data, String dataType) {
        switch (dataType) {
            case "phone":
                return maskPhone(data);
            case "idCard":
                return maskIdCard(data);
            case "cardNo":
                return maskCardNo(data);
            default:
                return data;
        }
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
```

---

## 📈 性能优化

### 1. 分页优化

#### 游标分页
```java
// 游标分页查询，避免深度分页
public Page<AccessRecordVO> getAccessRecords(AccessRecordQueryForm queryForm) {
    Page<AccessRecordEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());

    LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.le(AccessRecordEntity::getAccessTime, queryForm.getLastRecordTime());

    return accessRecordMapper.selectPage(page, wrapper);
}
```

### 2. 缓存策略

#### Redis缓存
```java
@Service
public class AccessPermissionService {

    @Cacheable(value = "access:permission", key = "#userId + ':' + #deviceId")
    public AccessPermissionVO getPermission(Long userId, Long deviceId) {
        // 权限查询逻辑
    }

    @CacheEvict(value = "access:permission", key = "#userId + ':' + #deviceId")
    public void evictPermission(Long userId, Long deviceId) {
        // 权限清除逻辑
    }
}
```

### 3. 数据库优化

#### 索引优化
```sql
-- 为常用查询条件创建复合索引
CREATE INDEX idx_record_user_time ON access_record(user_id, access_time DESC);
CREATE INDEX idx_record_device_time ON access_record(device_id, access_time DESC);
CREATE INDEX idx_permission_user_device ON access_permission(user_id, device_id, permission_status, deleted_flag);
```

---

**📞 技术支持**: 如有API接口相关问题，请联系IOE-DREAM开发团队。

---

*本文档遵循RESTful API设计规范，确保接口设计的一致性和易用性。*