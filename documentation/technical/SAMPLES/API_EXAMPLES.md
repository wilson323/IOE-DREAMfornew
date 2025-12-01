# API 示例合集

> 版本: v1.0.0  
> 责任人: 后端组 × 前端组  
> 最后更新: 2025-11-12

所有示例均基于 `docs/COMMON_MODULES/api-contracts.md` 中的接口定义, 字段命名与 `docs/DATA_DICTIONARY.md` 保持一致。

## 1. 角色列表查询
```
GET /api/admin/permission/roles HTTP/1.1
Host: api.smartadmin.internal
satoken: {{token}}
```

响应示例:
```json
{
  "code": "OK",
  "data": [
    {
      "roleId": 1001,
      "roleCode": "ROLE_SUPER",
      "roleName": "超级管理员",
      "dataScope": "ALL"
    }
  ],
  "traceId": "req-2025-11-12-0002"
}
```

## 2. 设备注册
```
POST /api/admin/device/register HTTP/1.1
Host: api.smartadmin.internal
satoken: {{token}}
Idempotent-Key: 2025-11-12-0003
Content-Type: application/json
```

请求体:
```json
{
  "deviceId": 9001,
  "deviceSn": "SN-AC-001",
  "deviceName": "北门闸机",
  "deviceType": "ACCESS",
  "firmwareVersion": "3.2.1",
  "signature": "BASE64_SIGNATURE"
}
```

响应体:
```json
{
  "code": "OK",
  "data": 9001,
  "traceId": "req-2025-11-12-0003"
}
```

## 3. 区域树查询
```
GET /api/admin/area/tree HTTP/1.1
Host: api.smartadmin.internal
satoken: {{token}}
```

响应体:
```json
{
  "code": "OK",
  "data": [
    {
      "areaId": 1,
      "areaName": "总部园区",
      "areaLevel": 1,
      "children": [
        {
          "areaId": 11,
          "areaName": "A栋",
          "areaLevel": 2,
          "children": []
        }
      ]
    }
  ],
  "traceId": "req-2025-11-12-0004"
}
```

## 4. 人员授权
```
POST /api/admin/person/permission HTTP/1.1
Host: api.smartadmin.internal
satoken: {{token}}
Content-Type: application/json
```

请求体:
```json
{
  "personId": 2001,
  "areaIds": [1, 11],
  "validStartTime": "2025-11-12T00:00:00+08:00",
  "validEndTime": "2025-12-12T23:59:59+08:00"
}
```

响应体:
```json
{
  "code": "OK",
  "traceId": "req-2025-11-12-0005"
}
```

## 5. 告警确认
```
POST /api/admin/alert/event/8001/ack HTTP/1.1
Host: api.smartadmin.internal
satoken: {{token}}
Content-Type: application/json
```

请求体:
```json
{
  "ackUserId": 10001,
  "ackComment": "已派人处理",
  "ackTime": "2025-11-12T10:05:00+08:00"
}
```

响应体:
```json
{
  "code": "OK",
  "traceId": "req-2025-11-12-0006"
}
```

## 6. WebSocket 订阅
```
GET wss://api.smartadmin.internal/ws/realtime?token={{token}}
```

客户端示例:
```javascript
const socket = new WebSocket("wss://api.smartadmin.internal/ws/realtime?token=" + token);
socket.onopen = () => socket.send(JSON.stringify({ type: "SUBSCRIBE", topic: "device-status" }));
socket.onmessage = (event) => {
  const payload = JSON.parse(event.data);
  console.log(payload.deviceId, payload.onlineStatus);
};
```

> 所有示例中的 {{token}} 请替换为实际登陆凭证, 不得写入代码仓库。
