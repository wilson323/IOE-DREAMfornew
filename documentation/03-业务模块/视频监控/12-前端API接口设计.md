# 视频监控模块前端API接口设计

## 概述

本文档详细描述了IOE-DREAM智能视频监控系统的完整前端API接口设计，包括Web端和移动端的全功能接口支持。系统提供实时视频流、录像回放、AI智能分析、告警管理等核心功能，支持多种视频设备和分析算法。

### 技术架构
- **API协议**: RESTful API + WebSocket实时通信
- **认证方式**: Sa-Token + JWT
- **数据格式**: JSON
- **响应编码**: UTF-8
- **实时通信**: WebSocket双向消息推送
- **视频流协议**: HLS、WebRTC、RTMP
- **AI分析**: 人脸识别、行为分析、异常检测

## API 基础配置

### 请求头配置
```http
Content-Type: application/json
Authorization: Bearer ${sa-token}
X-Client-Type: ${client_type} # web/mobile/mini-program/device
X-Device-Id: ${device_id} # 设备唯一标识
X-Platform-Version: ${version}
X-Stream-Type: ${stream_type} # LIVE,VOD
```

### 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "timestamp": 1640995200000,
  "data": {},
  "pagination": {
    "current": 1,
    "size": 20,
    "total": 100,
    "pages": 5
  },
  "extra": {}
}
```

### 错误码定义
| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 40100 | 未登录或token失效 | 401 |
| 40300 | 权限不足 | 403 |
| 40400 | 资源不存在 | 404 |
| 40900 | 视频流冲突 | 409 |
| 42300 | 设备被占用 | 423 |
| 42900 | 请求过于频繁 | 429 |
| 50000 | 服务器内部错误 | 500 |
| 80101 | 设备离线 | 400 |
| 80102 | 视频流不存在 | 400 |
| 80103 | 录像文件不存在 | 400 |
| 80104 | AI分析失败 | 400 |
| 80105 | 存储空间不足 | 400 |

## 1. 设备管理
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
### 1.1 获取摄像头列表
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
```http
GET /api/v1/video/devices
```

**查询参数:**
```
deviceType=IPC,DOME,PTZ,BULLET
status=ONLINE,OFFLINE,ERROR
locationId=100
areaId=200
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "deviceId": "CAM001",
      "deviceName": "大门监控摄像头",
      "deviceType": "PTZ", // IPC固定枪机, DOME半球机, PTZ球机, BULLET筒机
      "status": "ONLINE",
      "location": {
        "locationId": 100,
        "locationName": "公司大门",
        "areaId": 200,
        "areaName": "门禁区域",
        "address": "上海市浦东新区张江高科技园区",
        "latitude": 31.2304,
        "longitude": 121.4737,
        "floor": "1F",
        "direction": "正门"
      },
      "deviceInfo": {
        "model": "HIK-DS-2CD2143G0-I",
        "manufacturer": "海康威视",
        "firmware": "V5.5.0",
        "resolution": "4K(3840×2160)",
        "fps": 25,
        "nightVision": true,
        "audioSupport": true
      },
      "streamInfo": {
        "mainStream": {
          "url": "rtmp://192.168.1.101/live/stream1",
          "protocol": "RTMP",
          "resolution": "3840×2160",
          "bitrate": 8192,
          "fps": 25
        },
        "subStream": {
          "url": "rtsp://192.168.1.101/stream2",
          "protocol": "RTSP",
          "resolution": "640×480",
          "bitrate": 512,
          "fps": 15
        }
      },
      "ptzCapability": {
        "supported": true,
        "panRange": { "min": -180, "max": 180 },
        "tiltRange": { "min": -90, "max": 90 },
        "zoomRange": { "min": 1, "max": 32 }
      },
      "aiCapability": {
        "faceDetection": true,
        "motionDetection": true,
        "intrusionDetection": true,
        "crowdDetection": true,
        "objectTracking": true
      },
      "lastOnlineTime": "2024-01-02 14:30:00",
      "uptime": 998650, // 运行时间（秒）
      "cpuUsage": 15.2,
      "memoryUsage": 45.8,
      "diskUsage": 67.3
    }
  ]
}
```

### 1.2 获取设备详情
```http
GET /api/v1/video/devices/{deviceId}
```

### 1.3 获取设备实时状态
```http
GET /api/v1/video/devices/{deviceId}/status
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "deviceId": "CAM001",
    "deviceName": "大门监控摄像头",
    "status": "ONLINE",
    "lastHeartbeat": "2024-01-02 14:35:00",
    "deviceHealth": {
      "cpuUsage": 15.2,
      "memoryUsage": 45.8,
      "diskUsage": 67.3,
      "temperature": 42.5,
      "networkStatus": "GOOD",
      "signalStrength": -45
    },
    "streamStatus": {
      "mainStream": "NORMAL",
      "subStream": "NORMAL",
      "mobileStream": "NORMAL"
    },
    "recordingStatus": {
      "isRecording": true,
      "recordingMode": "CONTINUOUS", // CONTINUOUS连续, MOTION移动触发, SCHEDULE定时
      "storageInfo": {
        "totalSpace": 2048, // GB
        "usedSpace": 1376,
        "availableSpace": 672,
        "recordingDays": 15
      }
    },
    "aiStatus": {
      "faceDetection": "RUNNING",
      "motionDetection": "RUNNING",
      "intrusionDetection": "DISABLED",
      "lastAnalysisTime": "2024-01-02 14:34:45",
      "todayEvents": 23
    },
    "alarmStatus": {
      "activeAlarms": 0,
      "todayAlarms": 3,
      "criticalAlarms": 0,
      "lastAlarmTime": "2024-01-02 08:15:30"
    }
  }
}
```

### 1.4 PTZ控制
```http
POST /api/v1/video/devices/{deviceId}/ptz/control
```

**请求参数:**
```json
{
  "command": "MOVE", // MOVE移动, ZOOM变焦, FOCUS对焦, PRESET预设位
  "parameters": {
    "pan": 45.5,        // 水平角度 (-180到180)
    "tilt": 30.2,       // 垂直角度 (-90到90)
    "zoom": 5.0,        // 变焦倍数 (1到32)
    "speed": 50,        // 移动速度 (1-100)
    "presetId": 1       // 预设位ID
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "操作员张三"
  }
}
```

### 1.5 预设位管理
```http
GET /api/v1/video/devices/{deviceId}/presets
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "presetId": 1,
      "presetName": "大门入口",
      "description": "监控大门入口位置",
      "position": {
        "pan": 0.0,
        "tilt": 0.0,
        "zoom": 1.0
      },
      "createTime": "2023-12-01 10:00:00",
      "creator": "管理员"
    },
    {
      "presetId": 2,
      "presetName": "停车场",
      "description": "监控停车场区域",
      "position": {
        "pan": 90.0,
        "tilt": -30.0,
        "zoom": 3.0
      },
      "createTime": "2023-12-01 10:15:00",
      "creator": "管理员"
    }
  ]
}
```

## 2. 实时视频流

### 2.1 获取视频流地址
```http
GET /api/v1/video/stream/live
```

**查询参数:**
```
deviceId=CAM001
streamType=MAIN,SUB,MOBILE
protocol=HLS,WEBRTC,RTMP,FLV
clientType=WEB,MOBILE
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "streamId": "STREAM_20240102_001",
    "deviceId": "CAM001",
    "streamType": "MAIN",
    "protocol": "WEBRTC",
    "streamUrl": "webrtc://video.ioe-dream.com/stream/STREAM_20240102_001",
    "backupUrl": "rtmp://backup.ioe-dream.com/live/stream1",
    "streamInfo": {
      "resolution": "3840×2160",
      "bitrate": 8192,
      "fps": 25,
      "codec": "H.264",
      "audioCodec": "AAC"
    },
    "webrtcConfig": {
      "iceServers": [
        { "urls": "stun:stun.ioe-dream.com:3478" },
        { "urls": "turn:turn.ioe-dream.com:3478", "username": "user", "credential": "pass" }
      ],
      "offerSdp": "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\n..."
    },
    "expireTime": "2024-01-02 15:35:00",
    "maxViewers": 10,
    "currentViewers": 3
  }
}
```

### 2.2 WebRTC连接建立
```http
POST /api/v1/video/stream/webrtc/connect
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "streamType": "MAIN",
  "sdpOffer": "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\n...",
  "clientInfo": {
    "clientType": "WEB",
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.100"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "streamId": "STREAM_20240102_001",
    "sdpAnswer": "v=0\r\no=- 987654321 2 IN IP4 192.168.1.101\r\n...",
    "iceCandidates": [
      {
        "candidate": "candidate:1 1 UDP 2130706431 192.168.1.101 54400 typ host",
        "sdpMLineIndex": 0,
        "sdpMid": "0"
      }
    ]
  }
}
```

### 2.3 多画面视频流
```http
GET /api/v1/video/stream/multi-view
```

**查询参数:**
```
layout=2x2,3x3,4x4,1+5
deviceIds=CAM001,CAM002,CAM003,CAM004
streamType=SUB
protocol=HLS
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "viewId": "VIEW_20240102_001",
    "layout": "2x2",
    "streams": [
      {
        "deviceId": "CAM001",
        "position": { "row": 0, "col": 0 },
        "streamUrl": "https://video.ioe-dream.com/hls/stream1.m3u8",
        "deviceName": "大门监控"
      },
      {
        "deviceId": "CAM002",
        "position": { "row": 0, "col": 1 },
        "streamUrl": "https://video.ioe-dream.com/hls/stream2.m3u8",
        "deviceName": "停车场监控"
      }
    ],
    "compositeStream": {
      "url": "https://video.ioe-dream.com/hls/composite_2x2.m3u8",
      "resolution": "1920×1080",
      "bitrate": 4096
    }
  }
}
```

## 3. 录像回放

### 3.1 获取录像列表
```http
GET /api/v1/video/recordings
```

**查询参数:**
```
deviceId=CAM001
startTime=2024-01-02T00:00:00
endTime=2024-01-02T23:59:59
recordingType=CONTINUOUS,MOTION,ALARM
eventTypes=INTRUSION,MOTION,FACE_DETECTION
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "recordingId": "REC_20240102_001",
      "deviceId": "CAM001",
      "deviceName": "大门监控摄像头",
      "startTime": "2024-01-02 08:00:00",
      "endTime": "2024-01-02 09:00:00",
      "duration": 3600,
      "recordingType": "CONTINUOUS",
      "fileSize": 524288000, // 字节
      "resolution": "3840×2160",
      "filePath": "/recordings/2024/01/02/CAM001_20240102_080000.mp4",
      "thumbnailUrl": "/recordings/2024/01/02/CAM001_20240102_080000_thumb.jpg",
      "events": [
        {
          "eventType": "MOTION",
          "eventTime": "2024-01-02 08:15:30",
          "description": "检测到移动目标",
          "confidence": 0.85,
          "boundingBox": { "x": 100, "y": 200, "width": 150, "height": 200 }
        }
      ],
      "storageInfo": {
        "storageType": "LOCAL", // LOCAL本地, CLOUD云端
        "backupStatus": "BACKED_UP",
        "retentionDays": 30
      }
    }
  ]
}
```

### 3.2 获取录像播放地址
```http
GET /api/v1/video/recordings/{recordingId}/playback
```

**查询参数:**
```
startTime=2024-01-02T08:15:30
duration=300
quality=HIGH,MEDIUM,LOW
protocol=HLS,MP4
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "playbackId": "PLAY_20240102_001",
    "recordingId": "REC_20240102_001",
    "streamUrl": "https://video.ioe-dream.com/hls/playback_20240102_001.m3u8",
    "backupUrl": "https://backup.ioe-dream.com/video/playback_20240102_001.mp4",
    "streamInfo": {
      "startTime": "2024-01-02 08:00:00",
      "endTime": "2024-01-02 09:00:00",
      "duration": 3600,
      "resolution": "3840×2160",
      "bitrate": 8192,
      "fps": 25,
      "codec": "H.264"
    },
    "seekPoints": [
      {
        "time": "2024-01-02 08:15:30",
        "type": "EVENT",
        "description": "移动检测事件",
        "thumbnail": "/thumbnails/20240102_081530.jpg"
      }
    ],
    "downloadUrl": "https://api.ioe-dream.com/video/recordings/REC_20240102_001/download",
    "expireTime": "2024-01-02 16:00:00"
  }
}
```

### 3.3 时间轴搜索
```http
GET /api/v1/video/recordings/timeline
```

**查询参数:**
```
deviceId=CAM001
date=2024-01-02
eventTypes=ALL,MOTION,INTRUSION,FACE_DETECTION
interval=3600
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "deviceId": "CAM001",
    "date": "2024-01-02",
    "timeline": [
      {
        "hour": 8,
        "recordings": [
          {
            "startTime": "2024-01-02 08:00:00",
            "endTime": "2024-01-02 09:00:00",
            "recordingType": "CONTINUOUS",
            "hasEvents": true,
            "eventCount": 3
          }
        ],
        "events": [
          {
            "time": "08:15:30",
            "type": "MOTION",
            "description": "移动检测",
            "confidence": 0.85
          },
          {
            "time": "08:32:15",
            "type": "FACE_DETECTION",
            "description": "人脸识别",
            "confidence": 0.92
          }
        ]
      }
    ],
    "summary": {
      "totalRecordings": 24,
      "totalEvents": 15,
      "totalDuration": 86400
    }
  }
}
```

### 3.4 事件检索
```http
GET /api/v1/video/recordings/events
```

**查询参数:**
```
deviceId=CAM001
eventType=INTRUSION,MOTION
startTime=2024-01-01T00:00:00
endTime=2024-01-31T23:59:59
confidence=0.8
hasFace=true
hasObject=PERSON,VEHICLE
page=1
size=20
```

## 4. AI智能分析

### 4.1 人脸识别
```http
POST /api/v1/video/ai/face-recognition
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "imageData": "BASE64_ENCODED_IMAGE",
  "imageTime": "2024-01-02 14:30:00",
  "detectionMode": "REALTIME", // REALTIME实时, BATCH批量
  "faceDatabase": "EMPLOYEE_DB", // EMPLOYEE_DB员工库, VISITOR_DB访客库, STRANGER陌生人库
  "confidence": 0.8,
  "maxResults": 5
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "analysisId": "AI_FACE_20240102_001",
    "deviceId": "CAM001",
    "imageTime": "2024-01-02 14:30:00",
    "results": [
      {
        "faceId": "FACE_001",
        "personId": "EMP001",
        "personName": "张三",
        "personType": "EMPLOYEE", // EMPLOYEE员工, VISITOR访客, STRANGER陌生人
        "confidence": 0.95,
        "boundingBox": {
          "x": 120,
          "y": 80,
          "width": 100,
          "height": 120
        },
        "landmarks": {
          "leftEye": { "x": 140, "y": 100 },
          "rightEye": { "x": 200, "y": 100 },
          "nose": { "x": 170, "y": 120 },
          "leftMouth": { "x": 150, "y": 140 },
          "rightMouth": { "x": 190, "y": 140 }
        },
        "faceFeatures": "FEATURE_VECTOR_12345",
        "age": 28,
        "gender": "MALE",
        "emotion": "NEUTRAL",
        "faceMask": false,
        "glasses": false
      }
    ],
    "processingTime": 150, // 毫秒
    "serverTime": "2024-01-02 14:30:00.150"
  }
}
```

### 4.2 行为分析
```http
POST /api/v1/video/ai/behavior-analysis
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "videoSegment": {
    "startTime": "2024-01-02 14:00:00",
    "endTime": "2024-01-02 14:05:00",
    "streamUrl": "rtmp://192.168.1.101/live/stream1"
  },
  "analysisTypes": ["INTRUSION", "LOITERING", "VIOLENCE", "FALL_DETECTION"],
  "sensitivity": "MEDIUM", // LOW低, MEDIUM中, HIGH高
  "regionOfInterest": {
    "points": [
      { "x": 100, "y": 100 },
      { "x": 500, "y": 100 },
      { "x": 500, "y": 400 },
      { "x": 100, "y": 400 }
    ]
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "analysisId": "AI_BEHAVIOR_20240102_001",
    "deviceId": "CAM001",
    "analysisPeriod": {
      "startTime": "2024-01-02 14:00:00",
      "endTime": "2024-01-02 14:05:00"
    },
    "events": [
      {
        "eventId": "EVENT_001",
        "eventType": "INTRUSION",
        "eventTime": "2024-01-02 14:02:15",
        "confidence": 0.88,
        "description": "检测到人员闯入禁区",
        "objects": [
          {
            "objectId": "OBJ_001",
            "objectType": "PERSON",
            "position": { "x": 250, "y": 200 },
            "boundingBox": { "x": 230, "y": 180, "width": 40, "height": 80 },
            "trackId": "TRACK_001"
          }
        ],
        "trajectory": [
          { "x": 200, "y": 150, "time": "14:02:10" },
          { "x": 250, "y": 200, "time": "14:02:15" }
        ],
        "evidence": {
          "snapshotUrl": "/events/20240102/140215.jpg",
          "videoClip": {
            "startTime": "14:02:10",
            "endTime": "14:02:20",
            "clipUrl": "/clips/20240102/140215.mp4"
          }
        }
      }
    ],
    "statistics": {
      "totalEvents": 1,
      "eventTypes": {
        "INTRUSION": 1,
        "LOITERING": 0,
        "VIOLENCE": 0,
        "FALL_DETECTION": 0
      },
      "processingTime": 2500
    }
  }
}
```

### 4.3 人群检测
```http
POST /api/v1/video/ai/crowd-detection
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "detectionArea": {
    "points": [
      { "x": 0, "y": 0 },
      { "x": 640, "y": 0 },
      { "x": 640, "y": 480 },
      { "x": 0, "y": 480 }
    ]
  },
  "densityThreshold": {
    "low": 5,
    "medium": 15,
    "high": 30
  },
  "alertConditions": {
    "maxPeople": 50,
    "abnormalBehavior": true,
    "socialDistancing": false
  }
}
```

### 4.4 车辆识别
```http
POST /api/v1/video/ai/vehicle-recognition
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "imageData": "BASE64_ENCODED_IMAGE",
  "recognitionTypes": ["LICENSE_PLATE", "VEHICLE_TYPE", "VEHICLE_COLOR"],
  "plateDatabase": "ALL", // ALL全部, WHITE_LIST白名单, BLACK_LIST黑名单
  "confidence": 0.7
}
```

## 5. 告警管理

### 5.1 获取告警列表
```http
GET /api/v1/video/alarms
```

**查询参数:**
```
deviceId=CAM001
alarmLevel=HIGH,MEDIUM,LOW
alarmType=INTRUSION,MOTION,FACE_DETECTION,DEVICE_OFFLINE
status=ACTIVE,ACKNOWLEDGED,CLOSED
startTime=2024-01-01T00:00:00
endTime=2024-01-31T23:59:59
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "alarmId": "ALARM_20240102_001",
      "deviceId": "CAM001",
      "deviceName": "大门监控摄像头",
      "alarmType": "INTRUSION",
      "alarmLevel": "HIGH",
      "status": "ACTIVE",
      "title": "人员闯入禁区",
      "description": "检测到人员在禁区逗留超过30秒",
      "eventTime": "2024-01-02 08:15:30",
      "acknowledgedTime": null,
      "acknowledgedBy": null,
      "closedTime": null,
      "location": {
        "locationName": "公司大门",
        "coordinates": { "x": 250, "y": 200 }
      },
      "evidence": {
        "snapshotUrl": "/alarms/20240102/081530.jpg",
        "videoClip": {
          "startTime": "08:15:20",
          "endTime": "08:15:40",
          "clipUrl": "/alarms/clips/20240102/081530.mp4"
        },
        "objects": [
          {
            "objectType": "PERSON",
            "confidence": 0.92,
            "boundingBox": { "x": 230, "y": 180, "width": 40, "height": 80 }
          }
        ]
      },
      "actions": [
        {
          "actionType": "NOTIFICATION",
          "actionTime": "08:15:31",
          "description": "发送告警通知"
        },
        {
          "actionType": "EMAIL",
          "actionTime": "08:15:32",
          "description": "发送邮件通知"
        }
      ],
      "operatorInfo": {
        "assignedTo": "安保部",
        "assignedToId": 3001
      }
    }
  ]
}
```

### 5.2 告警确认
```http
POST /api/v1/video/alarms/{alarmId}/acknowledge
```

**请求参数:**
```json
{
  "acknowledgedBy": {
    "operatorId": 1001,
    "operatorName": "安保员张三"
  },
  "comment": "已确认，正在前往处理",
  "actionTaken": "DISPATCH_SECURITY", // DISPATCH_SECURITY派遣安保, IGNORE忽略, FALSE_ALARM误报
  "nextAction": {
    "action": "PATROL_CHECK",
    "scheduledTime": "2024-01-02 08:20:00",
    "description": "安排安保巡逻检查"
  }
}
```

### 5.3 告警关闭
```http
POST /api/v1/video/alarms/{alarmId}/close
```

**请求参数:**
```json
{
  "closeReason": "RESOLVED", // RESOLVED已解决, FALSE_ALARM误报, DUPLICATE重复
  "resolution": "嫌疑人已离开现场",
  "closedBy": {
    "operatorId": 1001,
    "operatorName": "安保员张三"
  },
  "followUpRequired": false,
  "preventiveMeasures": [
    "增加巡逻频率",
    "设置物理屏障"
  ]
}
```

## 6. 移动端专用接口

### 6.1 移动端视频流优化
```http
GET /api/v1/video/mobile/stream/optimized
```

**查询参数:**
```
deviceId=CAM001
networkType=WIFI,4G,3G
deviceCapability=HIGH,MEDIUM,LOW
batteryLevel=HIGH,MEDIUM,LOW
adaptiveBitrate=true
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "streamId": "MOBILE_STREAM_20240102_001",
    "adaptiveProfile": {
      "resolution": "1280×720",
      "bitrate": 2048,
      "fps": 15,
      "codec": "H.264",
      "audioEnabled": true,
      "audioCodec": "AAC",
      "audioBitrate": 64
    },
    "streamUrl": "https://video.ioe-dream.com/hls/mobile_stream_001.m3u8",
    "fallbackUrls": [
      "https://backup.ioe-dream.com/hls/mobile_stream_001.m3u8"
    ],
    "bufferSettings": {
      "bufferLength": 10,
      "maxBufferLength": 30,
      "liveSync": true
    },
    "powerSaving": {
      "adaptiveFps": true,
      "screenOffPolicy": "PAUSE", // PAUSE暂停, REDUCE_QUALITY降低质量, CONTINUE继续
      "batteryThreshold": 20
    }
  }
}
```

### 6.2 移动端PTZ手势控制
```http
POST /api/v1/video/mobile/ptz/gesture-control
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "gestureType": "PINCH", // PINCH缩放, SWIPE滑动, TAP点击
  "gestureData": {
    "scale": 1.5,         // 缩放比例
    "direction": "LEFT",   // 滑动方向
    "velocity": 500,       // 滑动速度
    "startPoint": { "x": 100, "y": 200 },
    "endPoint": { "x": 300, "y": 200 }
  },
  "mapping": {
    "pinchIn": "ZOOM_OUT",
    "pinchOut": "ZOOM_IN",
    "swipeLeft": "PAN_LEFT",
    "swipeRight": "PAN_RIGHT",
    "swipeUp": "TILT_UP",
    "swipeDown": "TILT_DOWN",
    "doubleTap": "AUTO_FOCUS"
  }
}
```

### 6.3 移动端快照
```http
POST /api/v1/video/mobile/snapshot
```

**请求参数:**
```json
{
  "deviceId": "CAM001",
  "streamType": "MAIN",
  "quality": "HIGH", // HIGH高, MEDIUM中, LOW低
  "format": "JPEG", // JPEG, PNG
  "annotations": [
    {
      "type": "TEXT",
      "text": "2024-01-02 14:30:00",
      "position": { "x": 10, "y": 10 },
      "style": {
        "fontSize": 16,
        "color": "#FFFFFF",
        "backgroundColor": "rgba(0,0,0,0.5)"
      }
    },
    {
      "type": "RECTANGLE",
      "startPoint": { "x": 100, "y": 100 },
      "endPoint": { "x": 200, "y": 200 },
      "style": {
        "color": "#FF0000",
        "thickness": 2
      }
    }
  ],
  "watermark": {
    "enabled": true,
    "text": "IOE-DREAM监控系统",
    "position": "BOTTOM_RIGHT"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "snapshotId": "SNAP_20240102_001",
    "imageUrl": "https://api.ioe-dream.com/video/snapshots/SNAP_20240102_001.jpg",
    "thumbnailUrl": "https://api.ioe-dream.com/video/snapshots/SNAP_20240102_001_thumb.jpg",
    "captureTime": "2024-01-02 14:30:00",
    "fileSize": 1024576,
    "resolution": "3840×2160",
    "metadata": {
      "deviceId": "CAM001",
      "deviceName": "大门监控摄像头",
      "streamType": "MAIN",
      "exposure": "1/100",
      "iso": 200,
      "whiteBalance": "AUTO"
    }
  }
}
```

### 6.4 移动端离线录像
```http
GET /api/v1/video/mobile/offline-recordings
```

**查询参数:**
```
deviceId=CAM001
downloadStatus=DOWNLOADED,DOWNLOADING,PENDING
maxFileSize=50
syncStatus=SYNCED,SYNC_PENDING
```

## 7. WebSocket 实时推送

### 7.1 连接WebSocket
```
ws://localhost:8080/ws/video/{userId}?token={sa-token}
```

### 7.2 消息类型

#### 7.2.1 实时事件通知
```json
{
  "type": "REALTIME_EVENT",
  "timestamp": 1640995200000,
  "data": {
    "deviceId": "CAM001",
    "eventType": "MOTION_DETECTION",
    "eventTime": "2024-01-02 14:30:00",
    "confidence": 0.88,
    "description": "检测到移动目标",
    "snapshotUrl": "/events/20240102/143000.jpg",
    "coordinates": { "x": 250, "y": 200 },
    "message": "大门监控检测到移动目标"
  }
}
```

#### 7.2.2 设备状态变更
```json
{
  "type": "DEVICE_STATUS_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "deviceId": "CAM001",
    "deviceName": "大门监控摄像头",
    "oldStatus": "ONLINE",
    "newStatus": "OFFLINE",
    "changeTime": "2024-01-02 14:30:00",
    "reason": "网络连接中断",
    "message": "大门监控摄像头已离线"
  }
}
```

#### 7.2.3 告警推送
```json
{
  "type": "ALARM_NOTIFICATION",
  "timestamp": 1640995200000,
  "data": {
    "alarmId": "ALARM_20240102_001",
    "alarmType": "INTRUSION",
    "alarmLevel": "HIGH",
    "deviceId": "CAM001",
    "deviceName": "大门监控摄像头",
    "eventTime": "2024-01-02 14:30:00",
    "title": "人员闯入禁区",
    "description": "检测到人员在禁区逗留",
    "snapshotUrl": "/alarms/20240102/143000.jpg",
    "location": "公司大门",
    "urgency": "IMMEDIATE", // IMMEDIATE立即, HIGH高, MEDIUM中, LOW低
    "actions": [
      { "type": "VIEW", "label": "查看详情" },
      { "type": "ACKNOWLEDGE", "label": "确认告警" }
    ]
  }
}
```

#### 7.2.4 系统消息
```json
{
  "type": "SYSTEM_MESSAGE",
  "timestamp": 1640995200000,
  "data": {
    "messageType": "MAINTENANCE",
    "title": "系统维护通知",
    "content": "视频监控系统将于今晚22:00-23:00进行维护",
    "severity": "INFO", // INFO信息, WARNING警告, ERROR错误
    "startTime": "2024-01-02 22:00:00",
    "endTime": "2024-01-02 23:00:00",
    "affectedDevices": ["CAM001", "CAM002"],
    "message": "视频监控系统维护通知"
  }
}
```

## 8. 批量操作接口

### 8.1 批量设备控制
```http
POST /api/v1/video/batch/device-control
```

**请求参数:**
```json
{
  "deviceIds": ["CAM001", "CAM002", "CAM003"],
  "command": "PRESET_POSITION", // PRESET_POSITION预设位, RESTART重启, UPDATE_FIRMWARE更新固件
  "parameters": {
    "presetId": 1,
    "forceRestart": false,
    "firmwareVersion": "v2.1.0"
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "管理员张三"
  }
}
```

### 8.2 批量录像导出
```http
POST /api/v1/video/batch/export-recordings
```

**请求参数:**
```json
{
  "exportType": "ALARM_EVENTS", // ALARM_EVENTS告警事件, MOTION_EVENTS移动事件, TIME_PERIOD时间段
  "deviceIds": ["CAM001", "CAM002"],
  "timeRange": {
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-01-31T23:59:59"
  },
  "exportFormat": "MP4", // MP4, AVI, MKV
  "quality": "HIGH", // HIGH高, MEDIUM中, LOW低
  "includeAudio": true,
  "compression": true,
  "delivery": {
    "method": "DOWNLOAD_LINK", // DOWNLOAD_LINK下载链接, EMAIL邮件, CLOUD_STORAGE云存储
    "recipients": ["admin@company.com"],
    "compressionLevel": 5
  }
}
```

### 8.3 批量AI分析
```http
POST /api/v1/video/batch/ai-analysis
```

**请求参数:**
```json
{
  "analysisType": "FACE_RECOGNITION", // FACE_RECOGNITION人脸识别, BEHAVIOR_ANALYSIS行为分析, CROWD_DETECTION人群检测
  "deviceIds": ["CAM001", "CAM002", "CAM003"],
  "timeRange": {
    "startTime": "2024-01-02T00:00:00",
    "endTime": "2024-01-02T23:59:59"
  },
  "analysisParameters": {
    "confidence": 0.8,
    "faceDatabase": "EMPLOYEE_DB",
    "behaviorTypes": ["INTRUSION", "LOITERING"],
    "crowdThreshold": 20
  },
  "notification": {
    "enabled": true,
    "emailRecipients": ["admin@company.com"],
    "reportFormat": "PDF"
  }
}
```

## 9. 统计报表接口

### 9.1 设备运行统计
```http
GET /api/v1/video/reports/device-performance
```

**查询参数:**
```
deviceId=CAM001
startDate=2024-01-01
endDate=2024-01-31
groupBy=DAY,WEEK,MONTH
metrics=UPTIME,STORAGE,NETWORK,ALARMS
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "reportPeriod": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "totalDays": 31
    },
    "deviceSummary": {
      "totalDevices": 25,
      "onlineDevices": 24,
      "offlineDevices": 1,
      "faultyDevices": 2,
      "averageUptime": 99.2
    },
    "performanceMetrics": [
      {
        "date": "2024-01-01",
        "uptime": 99.5,
        "storageUsage": 65.2,
        "networkLatency": 15,
        "alarmCount": 12,
        "processingLoad": 45.8
      }
    ],
    "topIssues": [
      {
        "issueType": "NETWORK_DISCONNECT",
        "count": 5,
        "description": "网络连接中断"
      },
      {
        "issueType": "STORAGE_FULL",
        "count": 3,
        "description": "存储空间不足"
      }
    ]
  }
}
```

### 9.2 AI分析统计
```http
GET /api/v1/video/reports/ai-analysis
```

**查询参数:**
```
analysisType=FACE_RECOGNITION,BEHAVIOR_ANALYSIS
deviceId=CAM001
startDate=2024-01-01
endDate=2024-01-31
groupBy=HOUR,DAY,WEEK
```

## 10. 系统配置接口

### 10.1 获取系统配置
```http
GET /api/v1/video/config/system
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "videoConfig": {
      "defaultResolution": "3840×2160",
      "defaultFps": 25,
      "defaultCodec": "H.264",
      "streamTimeout": 300,
      "maxConcurrentStreams": 50
    },
    "storageConfig": {
      "retentionDays": 30,
      "autoCleanup": true,
      "compressionEnabled": true,
      "cloudBackup": true,
      "storagePool": "/data/video"
    },
    "aiConfig": {
      "faceDetection": {
        "enabled": true,
        "confidence": 0.8,
        "databases": ["EMPLOYEE_DB", "VISITOR_DB"]
      },
      "behaviorAnalysis": {
        "enabled": true,
        "sensitivity": "MEDIUM",
        "analysisTypes": ["INTRUSION", "LOITERING"]
      }
    },
    "alarmConfig": {
      "notificationEnabled": true,
      "emailRecipients": ["security@company.com"],
      "smsEnabled": false,
      "escalationRules": {
        "highLevelEscalation": 300,
        "mediumLevelEscalation": 600
      }
    }
  }
}
```

---

## 接口权限矩阵

| 功能模块 | 普通用户 | 安保人员 | 系统管理员 | 超级管理员 |
|---------|---------|----------|-----------|-----------|
| 实时视频 | ✓(授权设备) | ✓(全部设备) | ✓ | ✓ |
| 录像回放 | ✓(本人相关) | ✓(全部) | ✓ | ✓ |
| PTZ控制 | ✗ | ✓ | ✓ | ✓ |
| AI分析 | ✗ | ✓(基础) | ✓ | ✓ |
| 告警管理 | ✓(相关) | ✓ | ✓ | ✓ |
| 设备管理 | ✗ | ✓(状态查看) | ✓ | ✓ |
| 系统配置 | ✗ | ✗ | ✓ | ✓ |
| 统计报表 | ✗ | ✓(基础) | ✓ | ✓ |

---

## 版本说明

- **当前版本**: v2.0.0
- **发布日期**: 2024-01-15
- **兼容性**: 向下兼容v1.x版本
- **更新内容**:
  - 新增WebRTC实时视频流支持
  - 增强AI智能分析功能
  - 优化移动端视频体验
  - 完善告警处理流程
  - 新增批量操作和统计报表

---

## 技术支持

如有API使用问题，请联系：
- **技术支持**: tech-support@ioe-dream.com
- **API文档**: https://api.ioe-dream.com/docs/video
- **SDK下载**: https://github.com/ioe-dream/sdks
- **问题反馈**: https://github.com/ioe-dream/issues