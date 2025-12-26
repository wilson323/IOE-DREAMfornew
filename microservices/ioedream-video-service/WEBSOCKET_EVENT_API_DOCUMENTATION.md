# WebSocket事件推送API文档

## 概述

WebSocket事件推送模块为边缘AI识别结果提供实时推送能力，实现毫秒级的事件展示和告警响应。

## 基础信息

- **服务名称**: ioedream-video-service
- **服务端口**: 8092
- **API版本**: v1
- **WebSocket端点**: `ws://localhost:8092/ws/video-events`

---

## WebSocket连接

### 连接端点

**端点**: `/ws/video-events`

**协议**: WebSocket with STOMP over SockJS

**连接示例**:
```javascript
// 使用SockJS + STOMP
const socket = new SockJS('http://localhost:8092/ws/video-events');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('WebSocket连接成功:', frame);
});
```

---

## 订阅主题

### 1. 订阅所有视频事件

**主题**: `/topic/video-events`

**说明**: 订阅后接收所有边缘设备的AI识别事件

**示例**:
```javascript
stompClient.subscribe('/topic/video-events', function (message) {
    const event = JSON.parse(message.body);
    console.log('收到事件:', event);
    handleEvent(event);
});
```

**事件数据格式**:
```json
{
  "eventId": "CAM001_FACE_DETECTED_1706592000000",
  "deviceId": "CAM001",
  "deviceName": "A栋1楼大厅摄像头",
  "eventType": "FACE_DETECTED",
  "eventData": {
    "faceId": 12345,
    "age": 25,
    "gender": "male"
  },
  "confidence": 0.95,
  "eventTime": "2025-01-30 12:00:00",
  "timestamp": 1706592000000,
  "imageUrl": "https://minio.example.com/video-events/2025/01/30/evt_001.jpg",
  "areaId": 2001,
  "areaName": "A栋1楼大厅",
  "longitude": 116.397128,
  "latitude": 39.916527
}
```

---

### 2. 订阅指定设备事件

**主题**: `/topic/device-events/{deviceId}`

**说明**: 只接收指定设备的AI识别事件

**示例**:
```javascript
const deviceId = 'CAM001';
stompClient.subscribe(`/topic/device-events/${deviceId}`, function (message) {
    const event = JSON.parse(message.body);
    console.log(`收到设备${deviceId}的事件:`, event);
});
```

---

## 事件类型

| 事件类型 | 说明 | eventData字段 |
|---------|------|---------------|
| `FACE_DETECTED` | 人脸检测 | faceId, bbox |
| `FACE_RECOGNIZED` | 人脸识别 | faceId, personId, name, similarity |
| `PERSON_COUNT` | 人数统计 | count, areaId |
| `ABNORMAL_BEHAVIOR` | 异常行为 | behaviorType, description |
| `AREA_INTRUSION` | 区域入侵 | intrusionArea, duration |
| `LOITERING_DETECTED` | 徘徊检测 | duration, trajectory |
| `FALL_DETECTED` | 跌倒检测 | fallPosition, severity |
| `FIGHT_DETECTED` | 打架检测 | participantsCount, severity |

---

## REST API接口

### 1. 上报AI事件

**接口描述**: 边缘设备上报AI识别事件

**请求方式**: `POST`

**请求路径**: `/api/v1/video/edge/event`

**请求头**:
- `Content-Type`: `application/json`

**请求体**:
```json
{
  "eventId": "CAM001_FACE_DETECTED_1706592000000",
  "deviceId": "CAM001",
  "deviceName": "A栋1楼大厅摄像头",
  "eventType": "FACE_DETECTED",
  "eventData": {
    "faceId": 12345,
    "bbox": [100, 200, 300, 400]
  },
  "confidence": 0.95,
  "eventTime": "2025-01-30 12:00:00",
  "imageUrl": "https://minio.example.com/video-events/2025/01/30/evt_001.jpg",
  "areaId": 2001,
  "areaName": "A栋1楼大厅"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1706592000000
}
```

---

### 2. 批量上报AI事件

**接口描述**: 边缘设备批量上报多个AI识别事件

**请求方式**: `POST`

**请求路径**: `/api/v1/video/edge/events/batch`

**请求体**:
```json
[
  {
    "deviceId": "CAM001",
    "eventType": "FACE_DETECTED",
    "confidence": 0.95,
    "eventTime": "2025-01-30 12:00:00"
  },
  {
    "deviceId": "CAM001",
    "eventType": "PERSON_COUNT",
    "confidence": 0.98,
    "eventTime": "2025-01-30 12:00:01"
  }
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1706592000000
}
```

---

### 3. 获取最近事件

**接口描述**: 获取最近的AI事件列表（REST API）

**请求方式**: `GET`

**请求路径**: `/api/v1/video/websocket/recent-events`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| count | Integer | 否 | 返回数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "eventId": "CAM001_FACE_DETECTED_1706592000000",
      "eventType": "FACE_DETECTED",
      "deviceId": "CAM001",
      "confidence": 0.95,
      "eventTime": "2025-01-30 12:00:00"
    }
  ],
  "timestamp": 1706592000000
}
```

---

### 4. 获取设备事件

**接口描述**: 获取指定设备的最近事件列表

**请求方式**: `GET`

**请求路径**: `/api/v1/video/websocket/device-events`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | String | 是 | 设备ID |
| count | Integer | 否 | 返回数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "eventId": "CAM001_FACE_DETECTED_1706592000000",
      "eventType": "FACE_DETECTED",
      "deviceId": "CAM001",
      "confidence": 0.95,
      "eventTime": "2025-01-30 12:00:00"
    }
  ],
  "timestamp": 1706592000000
}
```

---

### 5. 获取会话统计

**接口描述**: 获取WebSocket连接会话的统计信息

**请求方式**: `GET`

**请求路径**: `/api/v1/video/websocket/session-stats`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activeSessions": 50,
    "onlineUsers": 45,
    "deviceSubscriptions": 120,
    "cachedEvents": 85
  },
  "timestamp": 1706592000000
}
```

**返回数据说明**:
- `activeSessions`: 活跃WebSocket会话数
- `onlineUsers`: 在线用户数
- `deviceSubscriptions`: 设备订阅数
- `cachedEvents`: 缓存的事件数

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 1001 | 设备ID不能为空 |
| 1002 | 事件类型不能为空 |
| 1003 | 置信度不能为空 |
| 1004 | 置信度必须在0-1之间 |
| 1005 | 无效的事件类型 |

---

## 前端集成示例

### Vue 3 集成示例

```javascript
// websocket.js
import { ref, onMounted, onUnmounted } from 'vue'
import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'

export function useVideoEvents() {
  const stompClient = ref(null)
  const connected = ref(false)
  const events = ref([])

  const connect = () => {
    const socket = new SockJS('http://localhost:8092/ws/video-events')
    stompClient.value = Stomp.over(socket)

    stompClient.value.connect({}, () => {
      connected.value = true
      console.log('WebSocket连接成功')

      // 订阅所有事件
      stompClient.value.subscribe('/topic/video-events', (message) => {
        const event = JSON.parse(message.body)
        events.value.unshift(event)
        // 限制列表长度
        if (events.value.length > 100) {
          events.value.pop()
        }
      })
    }, (error) => {
      connected.value = false
      console.error('WebSocket连接失败:', error)
    })
  }

  const disconnect = () => {
    if (stompClient.value) {
      stompClient.value.disconnect()
      connected.value = false
    }
  }

  const subscribeDevice = (deviceId) => {
    if (!stompClient.value || !connected.value) {
      console.warn('WebSocket未连接')
      return
    }

    stompClient.value.subscribe(`/topic/device-events/${deviceId}`, (message) => {
      const event = JSON.parse(message.body)
      console.log(`设备${deviceId}事件:`, event)
      // 处理设备特定事件
    })
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    events,
    subscribeDevice
  }
}
```

### React Hook 示例

```javascript
// useVideoEvents.js
import { useState, useEffect } from 'react'
import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'

export function useVideoEvents() {
  const [connected, setConnected] = useState(false)
  const [events, setEvents] = useState([])

  useEffect(() => {
    const socket = new SockJS('http://localhost:8092/ws/video-events')
    const stompClient = Stomp.over(socket)

    stompClient.connect({}, () => {
      setConnected(true)

      const subscription = stompClient.subscribe('/topic/video-events', (message) => {
        const event = JSON.parse(message.body)
        setEvents(prev => [event, ...prev].slice(0, 100))
      })

      return () => {
        subscription.unsubscribe()
        stompClient.disconnect()
        setConnected(false)
      }
    })

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect()
      }
    }
  }, [])

  const sendEvent = (event) => {
    fetch('http://localhost:8092/api/v1/video/edge/event', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(event)
    })
  }

  return { connected, events, sendEvent }
}
```

---

## 测试工具

### 使用Postman测试WebSocket

1. 打开Postman
2. 选择"New WebSocket Request"
3. 输入：`ws://localhost:8092/ws/video-events`
4. 连接后发送STOMP命令：

**连接**:
```
CONNECT
accept-version:1.2
host:localhost

\0
```

**订阅**:
```
SUBSCRIBE
id:sub-1
destination:/topic/video-events

\0
```

---

## 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-30 | 初始版本，支持WebSocket实时事件推送 |

---

**文档维护**: IOE-DREAM Video Team
**最后更新**: 2025-01-30
