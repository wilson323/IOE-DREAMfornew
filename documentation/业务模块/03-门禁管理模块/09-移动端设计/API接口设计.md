# 门禁模块移动端API接口设计

> **版本**: v1.0.0  
> **创建日期**: 2025-12-17  
> **基础路径**: `/api/access/v1/mobile`

---

## 🔐 认证方式

- **Token认证**: Bearer Token
- **认证头**: `Authorization: Bearer {token}`

---

## 📱 设备管理API

### 1. 获取设备列表

```
GET /devices
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| areaId | Long | 否 | 区域ID筛选 |
| status | String | 否 | 状态: ONLINE/OFFLINE |
| keyword | String | 否 | 搜索关键字 |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "deviceId": "DEV-001",
      "deviceName": "主楼前门",
      "areaName": "主楼1层",
      "online": true,
      "lastHeartbeat": "2025-12-17T10:30:00Z"
    }
  ]
}
```

### 2. 远程开锁

```
POST /devices/{deviceId}/unlock
```

**请求参数**:
```json
{
  "reason": "紧急开锁",
  "duration": 5000
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "success": true,
    "message": "开锁成功"
  }
}
```

---

## 🏢 区域管理API

### 1. 获取用户区域列表

```
GET /areas
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "areaId": 1001,
      "areaName": "主楼1层",
      "areaType": "FLOOR",
      "deviceCount": 5,
      "permissionCount": 3
    }
  ]
}
```

---

## 📊 实时监控API

### 1. 获取统计数据

```
GET /statistics
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "onlineDevices": 142,
    "offlineDevices": 14,
    "todayAccess": 1250,
    "activeAlerts": 3
  }
}
```

### 2. 获取实时事件

```
GET /events
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | Integer | 否 | 返回条数(默认20) |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "eventId": "EVT-001",
      "eventType": "ACCESS_GRANTED",
      "eventTime": "2025-12-17T10:30:00Z",
      "description": "张三 刷卡通过 主楼前门",
      "deviceName": "主楼前门"
    }
  ]
}
```

---

## 📝 通行记录API

### 1. 获取通行记录

```
GET /records
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码(默认1) |
| pageSize | Integer | 否 | 每页条数(默认20) |
| status | String | 否 | 状态: success/fail |
| dateRange | String | 否 | today/week/month |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 156,
    "records": [
      {
        "id": "record_001",
        "userName": "张三",
        "deptName": "技术部",
        "areaName": "主门",
        "deviceName": "门禁终端A1",
        "accessTime": "2025-12-17T09:15:30Z",
        "success": true,
        "accessMethod": "人脸识别",
        "failReason": null
      }
    ]
  }
}
```

---

## 🔔 WebSocket实时通信

### 连接端点

```
ws://api.ioedream.com/access/v1/ws
```

### 消息格式

**订阅消息**:
```json
{
  "type": "SUBSCRIBE",
  "channels": ["device-status", "access-events", "alerts"]
}
```

**设备状态变更**:
```json
{
  "type": "DEVICE_STATUS",
  "data": {
    "deviceId": "DEV-001",
    "status": "OFFLINE",
    "timestamp": "2025-12-17T10:30:00Z"
  }
}
```

**告警通知**:
```json
{
  "type": "ALERT",
  "data": {
    "alertId": "ALT-001",
    "alertType": "DEVICE_OFFLINE",
    "severity": "HIGH",
    "message": "设备离线超过5分钟",
    "timestamp": "2025-12-17T10:30:00Z"
  }
}
```

---

## ❌ 错误码

| 错误码 | 说明 |
|--------|------|
| 40001 | 参数验证失败 |
| 40101 | 未授权访问 |
| 40301 | 无操作权限 |
| 40401 | 资源不存在 |
| 50001 | 服务器内部错误 |
| 50002 | 设备通讯失败 |

---

**📝 文档维护**
- **创建人**: IOE-DREAM架构团队
- **最后更新**: 2025-12-17
