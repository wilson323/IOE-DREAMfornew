# 视频录像管理API文档

## 概述

视频录像管理模块提供完整的视频录像计划管理和控制功能，支持：
- **定时录像**：按时间表自动录像
- **事件录像**：触发事件时录像
- **手动录像**：用户手动控制录像

---

## 基础信息

- **服务名称**: ioedream-video-service
- **服务端口**: 8092
- **API版本**: v1
- **包路径**: `net.lab1024.sa.video`

---

## 核心接口

### 1. 视频录像计划管理 API

#### 1.1 创建录像计划

**接口描述**: 创建新的视频录像计划

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/plan/create`

**请求体**:
```json
{
  "planName": "主入口全天录像",
  "planType": 1,
  "deviceId": "CAM001",
  "channelId": 1,
  "recordingType": 1,
  "quality": 3,
  "startTime": "2025-01-30 00:00:00",
  "endTime": "2025-01-30 23:59:59",
  "weekdays": "1,2,3,4,5",
  "enabled": true,
  "priority": 1,
  "preRecordSeconds": 5,
  "postRecordSeconds": 10,
  "eventTypes": ["MOTION_DETECTED", "FACE_DETECTED"],
  "storageLocation": "/recordings/cam001/",
  "maxDurationMinutes": 480,
  "loopRecording": true,
  "remarks": "主入口全天录像计划"
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planName | String | 是 | 计划名称 |
| planType | Integer | 是 | 计划类型: 1-定时录像 2-事件录像 3-手动录像 |
| deviceId | String | 是 | 设备ID |
| channelId | Integer | 否 | 通道ID |
| recordingType | Integer | 是 | 录像类型: 1-全天录像 2-定时录像 3-事件触发录像 |
| quality | Integer | 是 | 录像质量: 1-低质量 2-中等质量 3-高质量 4-超清质量 |
| startTime | LocalDateTime | 否 | 开始时间 |
| endTime | LocalDateTime | 否 | 结束时间 |
| weekdays | String | 否 | 星期设置（1-7，逗号分隔） |
| enabled | Boolean | 否 | 是否启用 |
| priority | Integer | 否 | 优先级（1最高） |
| preRecordSeconds | Integer | 否 | 前置录像时长（秒） |
| postRecordSeconds | Integer | 否 | 后置录像时长（秒） |
| eventTypes | List\<String\> | 否 | 事件类型列表 |
| storageLocation | String | 否 | 存储位置 |
| maxDurationMinutes | Integer | 否 | 最大录像时长（分钟） |
| loopRecording | Boolean | 否 | 循环录像 |
| detectionArea | String | 否 | 检测区域（JSON格式） |
| remarks | String | 否 | 备注 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 10001
}
```

---

#### 1.2 更新录像计划

**接口描述**: 更新已存在的视频录像计划

**请求方式**: `PUT`

**请求路径**: `/api/v1/video/recording/plan/update`

**请求体**: (同创建录像计划，需包含planId)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

---

#### 1.3 删除录像计划

**接口描述**: 删除指定的视频录像计划

**请求方式**: `DELETE`

**请求路径**: `/api/v1/video/recording/plan/delete/{planId}`

**路径参数**:
- `planId`: 计划ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

---

#### 1.4 启用/禁用录像计划

**接口描述**: 启用或禁用指定的录像计划

**请求方式**: `PUT`

**请求路径**: `/api/v1/video/recording/plan/enable/{planId}`

**路径参数**:
- `planId`: 计划ID

**查询参数**:
- `enabled`: true-启用 false-禁用

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

---

#### 1.5 获取录像计划详情

**接口描述**: 获取指定录像计划的详细信息

**请求方式**: `GET`

**请求路径**: `/api/v1/video/recording/plan/detail/{planId}`

**路径参数**:
- `planId`: 计划ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "planId": 10001,
    "planName": "主入口全天录像",
    "planType": "SCHEDULE",
    "planTypeName": "定时录像",
    "deviceId": "CAM001",
    "deviceName": "主入口摄像头",
    "channelId": 1,
    "recordingType": "FULL_TIME",
    "recordingTypeName": "全天录像",
    "quality": "HIGH",
    "qualityName": "高质量",
    "resolution": 1080,
    "bitrate": "3Mbps",
    "startTime": "2025-01-30 00:00:00",
    "endTime": "2025-01-30 23:59:59",
    "weekdays": "1,2,3,4,5",
    "weekdaysDesc": "工作日",
    "enabled": true,
    "priority": 1,
    "preRecordSeconds": 5,
    "postRecordSeconds": 10,
    "storageLocation": "/recordings/cam001/",
    "maxDurationMinutes": 480,
    "loopRecording": true,
    "remarks": "主入口全天录像计划",
    "createTime": "2025-01-30 10:00:00",
    "updateTime": "2025-01-30 10:00:00"
  }
}
```

---

#### 1.6 分页查询录像计划

**接口描述**: 分页查询录像计划列表

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/plan/query`

**请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "planName": "主入口",
  "planType": 1,
  "deviceId": "CAM001",
  "enabled": true,
  "recordingType": 1,
  "startTimeBegin": "2025-01-30 00:00:00",
  "startTimeEnd": "2025-01-30 23:59:59",
  "createTimeBegin": "2025-01-01 00:00:00",
  "createTimeEnd": "2025-01-31 23:59:59"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  }
}
```

---

#### 1.7 获取设备的启用录像计划

**接口描述**: 获取指定设备所有启用的录像计划

**请求方式**: `GET`

**请求路径**: `/api/v1/video/recording/plan/device/{deviceId}/enabled`

**路径参数**:
- `deviceId`: 设备ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

#### 1.8 复制录像计划

**接口描述**: 复制已存在的录像计划

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/plan/copy/{planId}`

**路径参数**:
- `planId`: 原计划ID

**查询参数**:
- `newPlanName`: 新计划名称

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 10002
}
```

---

### 2. 视频录像控制 API

#### 2.1 根据计划启动录像

**接口描述**: 根据录像计划启动录像任务

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/start/plan/{planId}`

**路径参数**:
- `planId`: 计划ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 20001
}
```

---

#### 2.2 手动启动录像

**接口描述**: 手动启动录像任务

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/start/manual`

**请求体**:
```json
{
  "deviceId": "CAM001",
  "channelId": 1,
  "operationType": 1,
  "quality": 3,
  "maxDurationMinutes": 120,
  "storageLocation": "/recordings/manual/cam001/",
  "recordingReason": "特殊情况录像"
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deviceId | String | 是 | 设备ID |
| channelId | Integer | 否 | 通道ID |
| operationType | Integer | 是 | 操作类型: 1-开始录像 2-停止录像 |
| quality | Integer | 否 | 录像质量 |
| maxDurationMinutes | Integer | 否 | 最大录像时长（分钟） |
| storageLocation | String | 否 | 存储位置 |
| recordingReason | String | 否 | 录像原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 20002
}
```

---

#### 2.3 停止录像

**接口描述**: 停止指定的录像任务

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/stop/{taskId}`

**路径参数**:
- `taskId`: 任务ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

---

#### 2.4 获取录像任务状态

**接口描述**: 获取指定录像任务的状态信息

**请求方式**: `GET`

**请求路径**: `/api/v1/video/recording/control/status/{taskId}`

**路径参数**:
- `taskId`: 任务ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 20001,
    "planId": 10001,
    "planName": "主入口全天录像",
    "deviceId": "CAM001",
    "deviceName": "主入口摄像头",
    "channelId": 1,
    "status": "RUNNING",
    "statusName": "录像中",
    "filePath": "/recordings/2025/01/30/cam001_001.mp4",
    "fileSize": 104857600,
    "fileSizeReadable": "100.00 MB",
    "startTime": "2025-01-30 09:00:00",
    "endTime": null,
    "durationSeconds": 3600,
    "durationReadable": "1小时0分钟",
    "triggerType": "SCHEDULE",
    "triggerTypeName": "定时触发",
    "quality": "HIGH",
    "qualityName": "高质量",
    "retryCount": 0,
    "maxRetryCount": 5,
    "canRetry": true,
    "isRunning": true,
    "isCompleted": false,
    "isFailed": false,
    "createTime": "2025-01-30 08:55:00",
    "updateTime": "2025-01-30 09:00:00"
  }
}
```

---

#### 2.5 获取设备当前录像状态

**接口描述**: 获取设备当前正在进行的录像任务

**请求方式**: `GET`

**请求路径**: `/api/v1/video/recording/control/status/device/{deviceId}`

**路径参数**:
- `deviceId`: 设备ID

**响应示例**: (同获取录像任务状态)

---

#### 2.6 分页查询录像任务

**接口描述**: 分页查询录像任务列表

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/query`

**请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "deviceId": "CAM001",
  "createTimeBegin": "2025-01-01 00:00:00",
  "createTimeEnd": "2025-01-31 23:59:59"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 200,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 10
  }
}
```

---

#### 2.7 处理事件触发录像

**接口描述**: 处理AI事件触发的录像（如移动侦测、人脸识别）

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/event/{deviceId}`

**路径参数**:
- `deviceId`: 设备ID

**查询参数**:
- `eventType`: 事件类型（如 MOTION_DETECTED、FACE_DETECTED）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 20003
}
```

---

#### 2.8 重试失败的录像任务

**接口描述**: 重试失败的录像任务

**请求方式**: `POST`

**请求路径**: `/api/v1/video/recording/control/retry/{taskId}`

**路径参数**:
- `taskId`: 原任务ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 20004
}
```

---

## 枚举值说明

### 计划类型 (PlanType)

| 值 | 名称 | 描述 |
|----|------|------|
| 1 | SCHEDULE | 定时录像 |
| 2 | EVENT | 事件录像 |
| 3 | MANUAL | 手动录像 |

### 录像类型 (RecordingType)

| 值 | 名称 | 描述 |
|----|------|------|
| 1 | FULL_TIME | 全天录像 |
| 2 | TIMED | 定时录像 |
| 3 | EVENT_TRIGGERED | 事件触发录像 |

### 录像质量 (RecordingQuality)

| 值 | 名称 | 分辨率 | 码率 |
|----|------|--------|------|
| 1 | LOW | 360p | 500Kbps |
| 2 | MEDIUM | 720p | 1.5Mbps |
| 3 | HIGH | 1080p | 3Mbps |
| 4 | ULTRA | 4K | 8Mbps |

### 任务状态 (TaskStatus)

| 值 | 名称 | 描述 |
|----|------|------|
| 0 | PENDING | 待执行 |
| 1 | RUNNING | 录像中 |
| 2 | STOPPED | 已停止 |
| 3 | FAILED | 失败 |
| 4 | 已完成 | COMPLETED |

### 触发类型 (TriggerType)

| 值 | 名称 | 描述 |
|----|------|------|
| 1 | SCHEDULE | 定时触发 |
| 2 | MANUAL | 手动触发 |
| 3 | EVENT | 事件触发 |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 1001 | 计划名称已存在 |
| 1002 | 录像计划不存在 |
| 1003 | 录像计划未启用 |
| 1004 | 设备正在录像中 |
| 1005 | 录像任务不存在 |
| 1006 | 录像任务未运行中 |
| 1007 | 任务未失败 |
| 1008 | 已达到最大重试次数 |

---

## 使用示例

### 创建并启动录像计划

```java
// 1. 创建录像计划
VideoRecordingPlanAddForm addForm = new VideoRecordingPlanAddForm();
addForm.setPlanName("主入口全天录像");
addForm.setPlanType(1); // 定时录像
addForm.setDeviceId("CAM001");
addForm.setRecordingType(1); // 全天录像
addForm.setQuality(3); // 高质量
addForm.setWeekdays("1,2,3,4,5"); // 工作日
addForm.setEnabled(true);

Long planId = videoRecordingPlanService.createPlan(addForm);

// 2. 启动录像
Long taskId = videoRecordingControlService.startRecordingByPlan(planId);

// 3. 查询录像状态
VideoRecordingTaskVO task = videoRecordingControlService.getRecordingStatus(taskId);
```

### 手动录像控制

```java
// 1. 启动手动录像
VideoRecordingControlForm controlForm = new VideoRecordingControlForm();
controlForm.setDeviceId("CAM001");
controlForm.setOperationType(1); // 开始录像
controlForm.setQuality(3);
controlForm.setMaxDurationMinutes(120);
controlForm.setRecordingReason("特殊情况录像");

Long taskId = videoRecordingControlService.startManualRecording(controlForm);

// 2. 停止录像
videoRecordingControlService.stopRecording(taskId);
```

### 处理事件录像

```java
// 处理移动侦测事件
Long taskId = videoRecordingControlService.handleEventRecording("CAM001", "MOTION_DETECTED");
```

---

**文档维护**: IOE-DREAM Video Team
**最后更新**: 2025-01-30
