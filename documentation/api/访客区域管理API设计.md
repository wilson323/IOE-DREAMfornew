# 访客区域管理API设计

> **设计时间**: 2025-12-08
> **API版本**: v1.0
> **基础路径**: /api/v1/visitor/area
> **设计原则**: RESTful + 统一响应格式 + 完善的错误处理

---

## 📋 API基础规范

### 1. 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1702012800000
}
```

### 2. HTTP状态码规范

| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 200 | 成功 | 请求处理成功 |
| 400 | 客户端错误 | 参数错误、格式错误 |
| 401 | 未授权 | 缺少认证信息 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 资源ID错误 |
| 409 | 冲突 | 资源已存在 |
| 500 | 服务器错误 | 系统内部错误 |

### 3. 错误码规范

| 错误码 | 说明 | 场景 |
|--------|------|------|
| VISITOR_AREA_001 | 区域不存在 | 查询的区域不存在 |
| VISITOR_AREA_002 | 访客区域配置已存在 | 该区域已有访客配置 |
| VISITOR_AREA_003 | 区域不支持访客业务 | 区域未启用访客功能 |
| VISITOR_AREA_004 | 访客数量超出容量 | 超出最大访客数量 |
| VISITOR_AREA_005 | 区域当前不开放 | 超出开放时间范围 |
| VISITOR_AREA_006 | 权限不足 | 用户无权管理访客区域 |

---

## 🔗 访客区域管理API

### 1. 创建访客区域配置

**接口地址**: `POST /api/v1/visitor/area/create`

**请求参数**:
```json
{
  "areaId": 1001,
  "visitType": 1,
  "accessLevel": 2,
  "maxVisitors": 50,
  "receptionRequired": true,
  "receptionistId": 1001,
  "receptionistName": "张三",
  "photoAllowed": false,
  "videoAllowed": false,
  "visitTimeLimit": 120,
  "appointmentDaysLimit": 7,
  "healthCheckRequired": true,
  "healthCheckStandard": {
    "temperature": true,
    "temperatureThreshold": 37.3,
    "mask": true,
    "healthCode": true
  },
  "idCardRequired": true,
  "faceRecognitionRequired": false,
  "visitorDevices": {
    "registrationDevice": "DEV001",
    "accessDevice": "DEV002",
    "cameraDevice": "CAM001"
  },
  "safetyNotes": "请佩戴访客证件，禁止进入办公区域",
  "openHours": {
    "workdays": {
      "start": "09:00",
      "end": "18:00",
      "lunchBreak": {
        "start": "12:00",
        "end": "13:00"
      }
    },
    "weekends": {
      "start": "10:00",
      "end": "16:00"
    }
  },
  "approvalProcess": 2,
  "approverId": 2001,
  "approverName": "李四",
  "emergencyContact": {
    "name": "张三",
    "phone": "13800138000",
    "department": "安全部",
    "title": "安全管理员"
  },
  "visitorInstructions": "1. 请在前台登记 2. 佩戴访客证件 3. 不要进入限制区域",
  "visitorStatisticsConfig": {
    "enableStatistics": true,
    "reportInterval": "daily",
    "alerts": {
      "maxVisitors": true,
      "overstay": true
    }
  },
  "remark": "主要接待区域，需要严格安全管理"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "访客区域配置创建成功",
  "data": {
    "visitorAreaId": 1001,
    "areaId": 1001,
    "visitType": 1,
    "accessLevel": 2,
    "maxVisitors": 50,
    "currentVisitors": 0,
    "createTime": "2025-12-08T10:30:00"
  },
  "timestamp": 1702012800000
}
```

### 2. 更新访客区域配置

**接口地址**: `PUT /api/v1/visitor/area/update`

**请求参数**: 同创建接口，但包含 `visitorAreaId`

**响应示例**:
```json
{
  "code": 200,
  "message": "访客区域配置更新成功",
  "data": {
    "visitorAreaId": 1001,
    "updateTime": "2025-12-08T10:35:00"
  },
  "timestamp": 1702012800000
}
```

### 3. 删除访客区域配置

**接口地址**: `DELETE /api/v1/visitor/area/delete/{visitorAreaId}`

**路径参数**:
- `visitorAreaId`: 访客区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "访客区域配置删除成功",
  "data": null,
  "timestamp": 1702012800000
}
```

---

## 📊 访客区域查询API

### 1. 根据区域ID获取访客区域配置

**接口地址**: `GET /api/v1/visitor/area/by-area/{areaId}`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "visitorAreaId": 1001,
    "areaId": 1001,
    "areaName": "A栋1楼大厅",
    "areaCode": "A001-1F",
    "visitType": 1,
    "accessLevel": 2,
    "maxVisitors": 50,
    "currentVisitors": 15,
    "receptionRequired": true,
    "receptionistId": 1001,
    "receptionistName": "张三",
    "photoAllowed": false,
    "videoAllowed": false,
    "visitTimeLimit": 120,
    "healthCheckRequired": true,
    "healthCheckStandard": {
      "temperature": true,
      "temperatureThreshold": 37.3,
      "mask": true,
      "healthCode": true
    },
    "visitorDevices": {
      "registrationDevice": "DEV001",
      "accessDevice": "DEV002",
      "cameraDevice": "CAM001"
    },
    "openHours": {
      "workdays": {
        "start": "09:00",
        "end": "18:00"
      }
    },
    "enabled": true,
    "createTime": "2025-12-08T10:30:00"
  },
  "timestamp": 1702012800000
}
```

### 2. 根据访问类型获取访客区域列表

**接口地址**: `GET /api/v1/visitor/area/by-visit-type/{visitType}`

**路径参数**:
- `visitType`: 访问类型

**查询参数**:
- `pageNum`: 页码（默认1）
- `pageSize`: 每页大小（默认20）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "visitorAreaId": 1001,
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "visitType": 1,
        "accessLevel": 2,
        "maxVisitors": 50,
        "currentVisitors": 15,
        "receptionRequired": true
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1702012800000
}
```

### 3. 根据访问权限级别获取访客区域列表

**接口地址**: `GET /api/v1/visitor/area/by-access-level/{accessLevel}`

**路径参数**:
- `accessLevel`: 访问权限级别

**响应示例**: 同上，但只返回指定权限级别的区域

### 4. 获取需要接待人员的访客区域列表

**接口地址**: `GET /api/v1/visitor/area/reception-required`

**响应示例**: 返回所有需要接待人员的访客区域

### 5. 获取当前访客数量超限的区域列表

**接口地址**: `GET /api/v1/visitor/area/over-capacity`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "visitorAreaId": 1001,
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "maxVisitors": 50,
        "currentVisitors": 55,
        "overCapacity": 5,
        "occupancyRate": 110.0
      }
    ],
    "totalCount": 1
  },
  "timestamp": 1702012800000
}
```

### 6. 获取当前时段开放的访客区域列表

**接口地址**: `GET /api/v1/visitor/area/open-areas`

**响应示例**: 返回当前时段开放的访客区域

---

## 👤 用户权限和容量管理API

### 1. 检查用户是否有访客区域管理权限

**接口地址**: `GET /api/v1/visitor/area/permission/check`

**查询参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "hasPermission": true,
    "permissionLevel": "manage"
  },
  "timestamp": 1702012800000
}
```

### 2. 获取用户可管理的访客区域列表

**接口地址**: `GET /api/v1/visitor/area/user-manageable`

**查询参数**:
- `pageNum`: 页码（默认1）
- `pageSize`: 每页大小（默认20）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "visitorAreaId": 1001,
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "visitType": 1,
        "maxVisitors": 50,
        "currentVisitors": 15,
        "managePermission": true
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1702012800000
}
```

### 3. 更新区域当前访客数量

**接口地址**: `PUT /api/v1/visitor/area/{areaId}/visitors`

**路径参数**:
- `areaId`: 区域ID

**请求参数**:
```json
{
  "visitorCount": 25,
  "operation": "update"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "访客数量更新成功",
  "data": {
    "areaId": 1001,
    "oldVisitorCount": 15,
    "newVisitorCount": 25,
    "updateTime": "2025-12-08T10:40:00"
  },
  "timestamp": 1702012800000
}
```

### 4. 增加区域访客数量

**接口地址**: `POST /api/v1/visitor/area/{areaId}/visitors/increment`

**路径参数**:
- `areaId`: 区域ID

**请求参数**:
```json
{
  "increment": 5
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "访客数量增加成功",
  "data": {
    "areaId": 1001,
    "increment": 5,
    "oldVisitorCount": 25,
    "newVisitorCount": 30,
    "availableCapacity": 20,
    "success": true
  },
  "timestamp": 1702012800000
}
```

### 5. 减少区域访客数量

**接口地址**: `POST /api/v1/visitor/area/{areaId}/visitors/decrement`

**路径参数**:
- `areaId`: 区域ID

**请求参数**:
```json
{
  "decrement": 3
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "访客数量减少成功",
  "data": {
    "areaId": 1001,
    "decrement": 3,
    "oldVisitorCount": 30,
    "newVisitorCount": 27,
    "success": true
  },
  "timestamp": 1702012800000
}
```

### 6. 检查区域访客容量状态

**接口地址**: `GET /api/v1/visitor/area/{areaId}/capacity-status`

**路径参数**:
- `areaId`: 区域ID

**查询参数**:
- `additionalVisitors`: 额外访客数量（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "areaName": "A栋1楼大厅",
    "currentVisitors": 27,
    "maxVisitors": 50,
    "availableCapacity": 23,
    "additionalVisitors": 5,
    "status": "available",
    "message": "容量充足",
    "occupancyRate": 54.0
  },
  "timestamp": 1702012800000
}
```

---

## ⚙️ 访客区域配置API

### 1. 获取区域访客设备配置

**接口地址**: `GET /api/v1/visitor/area/{areaId}/devices`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "visitorDevices": {
      "registrationDevice": "DEV001",
      "accessDevice": "DEV002",
      "cameraDevice": "CAM001",
      "printerDevice": "PRINTER001",
      "temperatureDevice": "TEMP001",
      "qrCodeReader": "QR001"
    }
  },
  "timestamp": 1702012800000
}
```

### 2. 更新区域访客设备配置

**接口地址**: `PUT /api/v1/visitor/area/{areaId}/devices`

**路径参数**:
- `areaId`: 区域ID

**请求参数**:
```json
{
  "visitorDevices": {
    "registrationDevice": "DEV001",
    "accessDevice": "DEV002",
    "cameraDevice": "CAM001",
    "printerDevice": "PRINTER001",
    "temperatureDevice": "TEMP001",
    "qrCodeReader": "QR001"
  }
}
```

### 3. 获取区域健康检查标准

**接口地址**: `GET /api/v1/visitor/area/{areaId}/health-check`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "healthCheckStandard": {
      "temperature": true,
      "temperatureThreshold": 37.3,
      "mask": true,
      "healthCode": true,
      "travelHistory": false,
      "contactHistory": false,
      "symptomCheck": true
    }
  },
  "timestamp": 1702012800000
}
```

### 4. 获取区域开放时间配置

**接口地址**: `GET /api/v1/visitor/area/{areaId}/open-hours`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "openHours": {
      "workdays": {
        "start": "09:00",
        "end": "18:00",
        "lunchBreak": {
          "start": "12:00",
          "end": "13:00"
        }
      },
      "weekends": {
        "start": "10:00",
        "end": "16:00"
      },
      "holidays": {
        "enabled": false,
        "specialHours": []
      }
    }
  },
  "timestamp": 1702012800000
}
```

### 5. 检查区域当前是否开放

**接口地址**: `GET /api/v1/visitor/area/{areaId}/is-open`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "areaName": "A栋1楼大厅",
    "isOpen": true,
    "currentTime": "2025-12-08T14:30:00",
    "todayOpenHours": {
      "start": "09:00",
      "end": "18:00"
    },
    "nextClosingTime": "2025-12-08T18:00:00"
  },
  "timestamp": 1702012800000
}
```

---

## 📈 统计分析API

### 1. 获取访客区域统计信息

**接口地址**: `GET /api/v1/visitor/area/statistics`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalAreas": 15,
    "publicAreas": 5,
    "restrictedAreas": 8,
    "confidentialAreas": 2,
    "totalCapacity": 750,
    "currentVisitors": 245,
    "occupancyRate": 32.7,
    "enabledAreas": 15,
    "disabledAreas": 0
  },
  "timestamp": 1702012800000
}
```

### 2. 按访问类型统计访客区域分布

**接口地址**: `GET /api/v1/visitor/area/statistics/by-visit-type`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "statistics": [
      {
        "visitType": 1,
        "visitTypeName": "预约访问",
        "areaCount": 8,
        "totalCapacity": 400,
        "currentVisitors": 120
      },
      {
        "visitType": 2,
        "visitTypeName": "临时访问",
        "areaCount": 5,
        "totalCapacity": 250,
        "currentVisitors": 85
      },
      {
        "visitType": 3,
        "visitTypeName": "VIP访问",
        "areaCount": 2,
        "totalCapacity": 100,
        "currentVisitors": 40
      }
    ]
  },
  "timestamp": 1702012800000
}
```

### 3. 批量更新访客区域状态

**接口地址**: `PUT /api/v1/visitor/area/batch-status`

**请求参数**:
```json
{
  "areaIds": [1001, 1002, 1003],
  "enabled": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量状态更新成功",
  "data": {
    "totalCount": 3,
    "successCount": 3,
    "failedCount": 0,
    "results": [
      {
        "areaId": 1001,
        "success": true
      },
      {
        "areaId": 1002,
        "success": true
      },
      {
        "areaId": 1003,
        "success": true
      }
    ]
  },
  "timestamp": 1702012800000
}
```

---

## ✅ 验证和建议API

### 1. 验证访客区域配置

**接口地址**: `POST /api/v1/visitor/area/validate`

**请求参数**: 同创建访客区域配置接口

**响应示例**:
```json
{
  "code": 200,
  "message": "验证完成",
  "data": {
    "status": "valid",
    "message": "访客区域配置验证通过",
    "areaId": 1001,
    "warnings": [],
    "suggestions": [
      "建议设置合理的访问时间限制",
      "建议配置紧急联系人信息"
    ]
  },
  "timestamp": 1702012800000
}
```

### 2. 获取访客区域访问建议

**接口地址**: `GET /api/v1/visitor/area/{areaId}/suggestions`

**路径参数**:
- `areaId`: 区域ID

**查询参数**:
- `visitType`: 访问类型

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "visitType": 1,
    "areaName": "A栋1楼大厅",
    "suggestions": [
      {
        "type": "booking",
        "priority": "high",
        "suggestion": "建议提前至少1个工作日进行预约",
        "reason": "确保接待人员有时间准备"
      },
      {
        "type": "security",
        "priority": "medium",
        "suggestion": "建议准备有效身份证件进行登记",
        "reason": "满足访客管理安全要求"
      },
      {
        "type": "health",
        "priority": "high",
        "suggestion": "建议做好健康检查准备工作",
        "reason": "当前区域启用了健康检查要求"
      }
    ],
    "currentCapacity": {
      "current": 15,
      "max": 50,
      "available": 35,
      "occupancyRate": 30.0
    }
  },
  "timestamp": 1702012800000
}
```

### 3. 获取区域访客须知

**接口地址**: `GET /api/v1/visitor/area/{areaId}/instructions`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "areaName": "A栋1楼大厅",
    "visitorInstructions": "1. 请在前台登记 2. 佩戴访客证件 3. 不要进入限制区域 4. 离开时请归还访客证件",
    "safetyNotes": "请佩戴访客证件，禁止进入办公区域",
    "emergencyContact": {
      "name": "张三",
      "phone": "13800138000",
      "department": "安全部"
    }
  },
  "timestamp": 1702012800000
}
```

---

## 🔒 API权限控制

### 1. 权限要求

| API分组 | 权限要求 | 说明 |
|---------|----------|------|
| 访客区域管理 | visitor:area:manage | 需要访客区域管理权限 |
| 访客区域查询 | visitor:area:view | 需要访客区域查看权限 |
| 访客容量管理 | visitor:area:capacity | 需要访客容量管理权限 |
| 访客统计查看 | visitor:area:statistics | 需要访客统计查看权限 |

### 2. 权限验证头

```http
Authorization: Bearer {jwt_token}
X-User-Id: {user_id}
X-Area-Ids: {accessible_area_ids}
```

---

## 📝 API使用示例

### JavaScript/TypeScript示例

```typescript
// 访客区域管理服务
class VisitorAreaService {
  private baseUrl = '/api/v1/visitor/area';

  // 创建访客区域配置
  async createVisitorArea(params: CreateVisitorAreaRequest): Promise<ApiResponse<VisitorAreaEntity>> {
    const response = await fetch(`${this.baseUrl}/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      },
      body: JSON.stringify(params)
    });
    return response.json();
  }

  // 检查区域容量状态
  async checkCapacityStatus(areaId: number, additionalVisitors?: number): Promise<ApiResponse<CapacityStatus>> {
    const params = additionalVisitors ? `?additionalVisitors=${additionalVisitors}` : '';
    const response = await fetch(`${this.baseUrl}/${areaId}/capacity-status${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    });
    return response.json();
  }

  // 增加访客数量
  async incrementVisitors(areaId: number, increment: number): Promise<ApiResponse<VisitorCountResult>> {
    const response = await fetch(`${this.baseUrl}/${areaId}/visitors/increment`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      },
      body: JSON.stringify({ increment })
    });
    return response.json();
  }

  // 获取访客区域统计
  async getStatistics(): Promise<ApiResponse<VisitorAreaStatistics>> {
    const response = await fetch(`${this.baseUrl}/statistics`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    });
    return response.json();
  }

  private getToken(): string {
    return localStorage.getItem('jwt_token') || '';
  }
}

// 使用示例
const visitorAreaService = new VisitorAreaService();

// 创建访客区域
const createResult = await visitorAreaService.createVisitorArea({
  areaId: 1001,
  visitType: 1,
  accessLevel: 2,
  maxVisitors: 50,
  receptionRequired: true
});

// 检查容量状态
const capacityStatus = await visitorAreaService.checkCapacityStatus(1001, 5);

// 增加访客数量
const incrementResult = await visitorAreaService.incrementVisitors(1001, 3);

// 获取统计信息
const statistics = await visitorAreaService.getStatistics();
```

---

**设计完成时间**: 2025-12-08
**API版本**: v1.0
**设计原则**: RESTful + 统一响应 + 完善错误处理
**测试建议**: 使用Postman或Swagger进行接口测试