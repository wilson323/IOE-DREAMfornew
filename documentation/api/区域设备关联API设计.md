# 区域-设备关联API设计

> **设计时间**: 2025-12-08
> **API版本**: v1.0
> **基础路径**: /api/v1/area-device
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
| AREA_DEVICE_001 | 区域不存在 | 查询的区域不存在 |
| AREA_DEVICE_002 | 设备不存在 | 查询的设备不存在 |
| AREA_DEVICE_003 | 关联已存在 | 设备已在区域中 |
| AREA_DEVICE_004 | 业务模块不支持 | 区域不支持指定业务模块 |
| AREA_DEVICE_005 | 设备类型不匹配 | 设备类型与业务模块不匹配 |
| AREA_DEVICE_006 | 权限不足 | 用户无权操作区域设备 |

---

## 🔗 区域设备关联管理API

### 1. 添加设备到区域

**接口地址**: `POST /api/v1/area-device/add`

**请求参数**:
```json
{
  "areaId": 1001,
  "deviceId": "DEV001",
  "deviceCode": "ACCESS_CTRL_001",
  "deviceName": "主入口门禁控制器",
  "deviceType": 1,
  "deviceSubType": 11,
  "businessModule": "access",
  "locationDesc": "主入口左侧",
  "installLocation": {
    "floor": "1F",
    "building": "A栋",
    "room": "大厅",
    "coordinates": {
      "x": 100,
      "y": 200
    }
  },
  "businessAttributes": {
    "accessMode": "card",
    "antiPassback": true,
    "openTime": 3000
  },
  "priority": 1,
  "remark": "主要出入口控制设备"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "设备添加成功",
  "data": {
    "relationId": "1001202512080001",
    "areaId": 1001,
    "deviceId": "DEV001",
    "deviceName": "主入口门禁控制器",
    "relationStatus": 1,
    "createTime": "2025-12-08T10:30:00"
  },
  "timestamp": 1702012800000
}
```

### 2. 从区域移除设备

**接口地址**: `DELETE /api/v1/area-device/remove`

**请求参数**:
```json
{
  "areaId": 1001,
  "deviceId": "DEV001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "设备移除成功",
  "data": null,
  "timestamp": 1702012800000
}
```

### 3. 批量添加设备到区域

**接口地址**: `POST /api/v1/area-device/batch-add`

**请求参数**:
```json
{
  "areaId": 1001,
  "devices": [
    {
      "deviceId": "DEV001",
      "deviceCode": "ACCESS_CTRL_001",
      "deviceName": "主入口门禁",
      "deviceType": 1,
      "businessModule": "access",
      "priority": 1
    },
    {
      "deviceId": "DEV002",
      "deviceCode": "CAMERA_001",
      "deviceName": "主入口摄像头",
      "deviceType": 4,
      "businessModule": "video",
      "priority": 2
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量添加完成",
  "data": {
    "totalCount": 2,
    "successCount": 2,
    "failedCount": 0,
    "results": [
      {
        "deviceId": "DEV001",
        "success": true,
        "relationId": "1001202512080001"
      },
      {
        "deviceId": "DEV002",
        "success": true,
        "relationId": "1001202512080002"
      }
    ]
  },
  "timestamp": 1702012800000
}
```

---

## 📊 区域设备查询API

### 1. 获取区域所有设备

**接口地址**: `GET /api/v1/area-device/area/{areaId}/devices`

**路径参数**:
- `areaId`: 区域ID

**查询参数**:
- `deviceType`: 设备类型（可选）
- `relationStatus`: 关联状态（可选）
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
        "relationId": "1001202512080001",
        "areaId": 1001,
        "deviceId": "DEV001",
        "deviceCode": "ACCESS_CTRL_001",
        "deviceName": "主入口门禁控制器",
        "deviceType": 1,
        "deviceSubType": 11,
        "businessModule": "access",
        "relationStatus": 1,
        "priority": 1,
        "locationDesc": "主入口左侧",
        "businessAttributes": {
          "accessMode": "card",
          "antiPassback": true,
          "openTime": 3000
        },
        "createTime": "2025-12-08T10:30:00"
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

### 2. 获取区域指定类型设备

**接口地址**: `GET /api/v1/area-device/area/{areaId}/devices/type/{deviceType}`

**路径参数**:
- `areaId`: 区域ID
- `deviceType`: 设备类型

**查询参数**:
- `pageNum`: 页码（默认1）
- `pageSize`: 每页大小（默认20）

**响应示例**: 同上，但只返回指定类型的设备

### 3. 获取区域业务模块设备

**接口地址**: `GET /api/v1/area-device/area/{areaId}/devices/module/{businessModule}`

**路径参数**:
- `areaId`: 区域ID
- `businessModule`: 业务模块

**响应示例**: 同上，但只返回指定业务模块的设备

### 4. 获取区域主设备

**接口地址**: `GET /api/v1/area-device/area/{areaId}/devices/primary`

**路径参数**:
- `areaId`: 区域ID

**响应示例**: 返回区域中所有优先级为1的主设备

---

## 👤 用户设备权限API

### 1. 获取用户可访问设备

**接口地址**: `GET /api/v1/area-device/user/devices`

**查询参数**:
- `businessModule`: 业务模块（可选）
- `deviceType`: 设备类型（可选）
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
        "relationId": "1001202512080001",
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "deviceId": "DEV001",
        "deviceName": "主入口门禁控制器",
        "deviceType": 1,
        "businessModule": "access",
        "relationStatus": 1,
        "canAccess": true,
        "accessLevel": "normal"
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

### 2. 检查设备是否在区域中

**接口地址**: `GET /api/v1/area-device/check`

**查询参数**:
- `areaId`: 区域ID
- `deviceId`: 设备ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "deviceId": "DEV001",
    "inArea": true,
    "relationId": "1001202512080001",
    "relationStatus": 1,
    "businessModule": "access"
  },
  "timestamp": 1702012800000
}
```

---

## ⚙️ 设备业务属性API

### 1. 获取设备业务属性

**接口地址**: `GET /api/v1/area-device/device/{deviceId}/attributes`

**路径参数**:
- `deviceId`: 设备ID

**查询参数**:
- `areaId`: 区域ID（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "DEV001",
    "areaId": 1001,
    "businessAttributes": {
      "accessMode": "card",
      "antiPassback": true,
      "openTime": 3000,
      "closeTime": 5000,
      "autoLock": true
    }
  },
  "timestamp": 1702012800000
}
```

### 2. 设置设备业务属性

**接口地址**: `PUT /api/v1/area-device/device/{deviceId}/attributes`

**路径参数**:
- `deviceId`: 设备ID

**请求参数**:
```json
{
  "areaId": 1001,
  "businessAttributes": {
    "accessMode": "biometric",
    "antiPassback": true,
    "openTime": 3500,
    "faceRecognitionEnabled": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "业务属性设置成功",
  "data": {
    "deviceId": "DEV001",
    "areaId": 1001,
    "updateTime": "2025-12-08T10:35:00"
  },
  "timestamp": 1702012800000
}
```

### 3. 获取设备属性模板

**接口地址**: `GET /api/v1/area-device/template/{deviceType}`

**路径参数**:
- `deviceType`: 设备类型

**查询参数**:
- `deviceSubType`: 设备子类型（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceType": 1,
    "deviceSubType": 11,
    "template": {
      "accessMode": "card",
      "accessLevel": "normal",
      "antiPassback": true,
      "openTime": 3000,
      "closeTime": 5000,
      "autoLock": true
    }
  },
  "timestamp": 1702012800000
}
```

---

## 📈 统计分析API

### 1. 获取区域设备统计

**接口地址**: `GET /api/v1/area-device/area/{areaId}/statistics`

**路径参数**:
- `areaId`: 区域ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "onlineDeviceCount": 8,
    "totalDeviceCount": 10,
    "onlineRate": 80.0,
    "typeStatistics": [
      {
        "deviceType": 1,
        "deviceSubType": 11,
        "deviceCount": 2,
        "onlineCount": 2,
        "primaryCount": 1
      },
      {
        "deviceType": 4,
        "deviceSubType": 41,
        "deviceCount": 5,
        "onlineCount": 4,
        "primaryCount": 0
      }
    ],
    "primaryDeviceCount": 3,
    "moduleStatistics": {
      "access": 2,
      "video": 5,
      "attendance": 1,
      "consume": 2
    }
  },
  "timestamp": 1702012800000
}
```

### 2. 获取业务模块设备分布

**接口地址**: `GET /api/v1/area-device/module/{businessModule}/distribution`

**路径参数**:
- `businessModule`: 业务模块

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "businessModule": "access",
    "distribution": [
      {
        "deviceType": 1,
        "deviceCount": 15,
        "areaCount": 8
      },
      {
        "deviceType": 2,
        "deviceCount": 10,
        "areaCount": 5
      }
    ]
  },
  "timestamp": 1702012800000
}
```

---

## 🔧 设备状态管理API

### 1. 更新设备关联状态

**接口地址**: `PUT /api/v1/area-device/relation/{relationId}/status`

**路径参数**:
- `relationId`: 关联ID

**请求参数**:
```json
{
  "status": 2,
  "remark": "设备维护中"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "relationId": "1001202512080001",
    "oldStatus": 1,
    "newStatus": 2,
    "updateTime": "2025-12-08T10:40:00"
  },
  "timestamp": 1702012800000
}
```

### 2. 批量更新区域设备状态

**接口地址**: `PUT /api/v1/area-device/area/{areaId}/batch-status`

**路径参数**:
- `areaId`: 区域ID

**请求参数**:
```json
{
  "status": 2,
  "deviceTypes": [1, 4],
  "remark": "批量维护"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量状态更新成功",
  "data": {
    "areaId": 1001,
    "totalCount": 5,
    "updatedCount": 5,
    "newStatus": 2,
    "updateTime": "2025-12-08T10:45:00"
  },
  "timestamp": 1702012800000
}
```

### 3. 同步设备状态

**接口地址**: `POST /api/v1/area-device/device/{deviceId}/sync-status`

**路径参数**:
- `deviceId`: 设备ID

**请求参数**:
```json
{
  "deviceStatus": "online",
  "syncTime": "2025-12-08T10:50:00"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "状态同步成功",
  "data": {
    "deviceId": "DEV001",
    "deviceStatus": "online",
    "relationStatus": 1,
    "syncedAreas": [
      {
        "areaId": 1001,
        "areaName": "A栋1楼大厅",
        "oldStatus": 4,
        "newStatus": 1
      }
    ],
    "syncTime": "2025-12-08T10:50:00"
  },
  "timestamp": 1702012800000
}
```

---

## ✅ 验证和建议API

### 1. 验证设备关联配置

**接口地址**: `POST /api/v1/area-device/validate`

**请求参数**:
```json
{
  "areaId": 1001,
  "deviceId": "DEV001",
  "deviceType": 1,
  "businessModule": "access"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证完成",
  "data": {
    "status": "valid",
    "message": "配置验证通过",
    "areaId": 1001,
    "deviceId": "DEV001",
    "businessModule": "access",
    "warnings": [],
    "suggestions": [
      "建议配置备用门禁设备",
      "建议启用反潜回功能"
    ]
  },
  "timestamp": 1702012800000
}
```

### 2. 获取区域设备部署建议

**接口地址**: `GET /api/v1/area-device/area/{areaId}/suggestions`

**路径参数**:
- `areaId`: 区域ID

**查询参数**:
- `businessModule`: 业务模块

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "areaId": 1001,
    "businessModule": "access",
    "suggestions": [
      {
        "type": "device_deployment",
        "priority": "high",
        "suggestion": "建议部署门禁控制器在主要出入口",
        "reason": "主要出入口是安全控制的关键节点"
      },
      {
        "type": "device_deployment",
        "priority": "medium",
        "suggestion": "建议部署读卡器在通道两侧",
        "reason": "便于用户操作，提高通行效率"
      },
      {
        "type": "configuration",
        "priority": "high",
        "suggestion": "建议配置备用门禁设备",
        "reason": "确保系统高可用性"
      }
    ],
    "currentDevices": [
      {
        "deviceId": "DEV001",
        "deviceType": 1,
        "deviceName": "主入口门禁",
        "priority": 1
      }
    ]
  },
  "timestamp": 1702012800000
}
```

---

## 🔒 API权限控制

### 1. 权限要求

| API分组 | 权限要求 | 说明 |
|---------|----------|------|
| 设备关联管理 | area:device:manage | 需要区域设备管理权限 |
| 设备查询 | area:device:view | 需要区域设备查看权限 |
| 设备统计 | area:device:statistics | 需要区域设备统计权限 |
| 状态管理 | area:device:status | 需要设备状态管理权限 |
| 验证建议 | area:device:validate | 需要设备验证权限 |

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
// 区域设备管理服务
class AreaDeviceService {
  private baseUrl = '/api/v1/area-device';

  // 添加设备到区域
  async addDeviceToArea(params: AddDeviceRequest): Promise<ApiResponse<AreaDeviceRelation>> {
    const response = await fetch(`${this.baseUrl}/add`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      },
      body: JSON.stringify(params)
    });
    return response.json();
  }

  // 获取区域设备
  async getAreaDevices(areaId: number, options?: QueryOptions): Promise<ApiResponse<PageResult<AreaDeviceRelation>>> {
    const params = new URLSearchParams();
    if (options?.deviceType) params.append('deviceType', options.deviceType.toString());
    if (options?.pageNum) params.append('pageNum', options.pageNum.toString());
    if (options?.pageSize) params.append('pageSize', options.pageSize.toString());

    const response = await fetch(`${this.baseUrl}/area/${areaId}/devices?${params}`, {
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
const areaDeviceService = new AreaDeviceService();

// 添加设备
const addResult = await areaDeviceService.addDeviceToArea({
  areaId: 1001,
  deviceId: 'DEV001',
  deviceCode: 'ACCESS_CTRL_001',
  deviceName: '主入口门禁',
  deviceType: 1,
  businessModule: 'access'
});

// 获取设备列表
const devices = await areaDeviceService.getAreaDevices(1001, {
  deviceType: 1,
  pageNum: 1,
  pageSize: 20
});
```

---

**设计完成时间**: 2025-12-08
**API版本**: v1.0
**设计原则**: RESTful + 统一响应 + 完善错误处理
**测试建议**: 使用Postman或Swagger进行接口测试