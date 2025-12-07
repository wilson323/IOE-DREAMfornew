# API一致性修复计划

**生成时间**: 2025-01-30  
**优先级**: P0级 (必须修复)  
**状态**: 🔄 待执行

---

## 🔴 P0级问题修复清单

### 1. 前端设备管理API路径修复

**问题**: 前端API路径与后端不匹配，导致功能无法使用

**前端文件**: `smart-admin-web-javascript/src/api/business/smart-video/device-api.js`

**需要修复的API**:

| 前端API | 当前路径 | 后端路径 | 修复方案 |
|---------|---------|---------|---------|
| `queryDeviceList` | `/device/query` | `/api/v1/video/devices/page` | 修改为`/api/v1/video/devices/page` |
| `addDevice` | `/device/add` | `/api/v1/video/devices` | 修改为`/api/v1/video/devices` (POST) |
| `updateDevice` | `/device/update` | `/api/v1/video/devices/{deviceId}` | 修改为`/api/v1/video/devices/{deviceId}` (PUT) |
| `deleteDevice` | `/device/delete/{id}` | `/api/v1/video/devices/{deviceId}` | 修改为`/api/v1/video/devices/{id}` (DELETE) |
| `getDeviceInfo` | `/device/info/{id}` | `/api/v1/video/devices/{deviceId}` | 修改为`/api/v1/video/devices/{id}` (GET) |
| `getDeviceStatistics` | `/api/v1/video/device/statistics` | `/api/v1/video/devices/statistics` | 修改为`/api/v1/video/devices/statistics` |

**修复代码示例**:
```javascript
// 修复前
queryDeviceList: (params) => {
  return postRequest('/device/query', params);
}

// 修复后
queryDeviceList: (params) => {
  return postRequest('/api/v1/video/devices/page', params);
}
```

---

### 2. 前端PTZ控制路径修复

**问题**: 前端PTZ控制路径与后端不匹配

**前端文件**: `smart-admin-web-javascript/src/api/business/smart-video/monitor-api.js`

**需要修复的API**:

| 前端API | 当前路径 | 后端路径 | 修复方案 |
|---------|---------|---------|---------|
| `ptzControl` | `/monitor/ptz/control` | `/api/v1/video/devices/{deviceId}/ptz` | 修改为`/api/v1/video/devices/{deviceId}/ptz` |

**修复代码示例**:
```javascript
// 修复前
ptzControl: (params) => postRequest('/monitor/ptz/control', params),

// 修复后
ptzControl: (deviceId, params) => postRequest(`/api/v1/video/devices/${deviceId}/ptz`, params),
```

---

### 3. 录像接口路径统一

**问题**: 前端/移动端调用`/record/start`和`/record/stop`，但后端只有`/record`接口

**方案1**: 修改后端，添加`/record/start`和`/record/stop`接口 (推荐)

**后端文件**: `RealTimeMonitorController.java`

**需要添加的接口**:
```java
@Operation(summary = "开始录制监控片段", description = "开始录制当前监控画面")
@PostMapping("/{deviceId}/record/start")
public ResponseDTO<String> startRecord(
        @PathVariable Long deviceId,
        @RequestParam(defaultValue = "MAIN") String streamType) {
    return realTimeMonitorService.recordMonitorSegment(deviceId, streamType, 0); // 0表示持续录制
}

@Operation(summary = "停止录制监控片段", description = "停止录制当前监控画面")
@PostMapping("/{deviceId}/record/stop")
public ResponseDTO<String> stopRecord(
        @PathVariable Long deviceId,
        @RequestParam String recordId) {
    return realTimeMonitorService.stopRecordMonitorSegment(deviceId, recordId);
}
```

**方案2**: 修改前端/移动端，统一使用`/record`接口，通过POST body区分操作

---

### 4. 告警API单复数统一

**问题**: 移动端使用`/alarm/*`，后端使用`/alarms/*`

**移动端文件**: `smart-app/src/api/business/video/video-api.js`

**需要修复的API**:

| 移动端API | 当前路径 | 后端路径 | 修复方案 |
|---------|---------|---------|---------|
| `getActiveAlarms` | `/api/v1/video/alarm/active` | `/api/v1/video/alarms/active` | 修改为`/api/v1/video/alarms/active` |
| `getAlarmStatistics` | `/api/v1/video/alarm/statistics` | `/api/v1/video/alarms/statistics` | 修改为`/api/v1/video/alarms/statistics` |

**修复代码示例**:
```javascript
// 修复前
getActiveAlarms: (limit = 10) =>
  getRequest('/api/v1/video/alarm/active', { limit }),

// 修复后
getActiveAlarms: (limit = 10) =>
  getRequest('/api/v1/video/alarms/active', { limit }),
```

---

## ✅ 已完成的修复

1. ✅ **网络质量检测接口**: 已在`MobileVideoController`中实现`/api/mobile/v1/video/stream/network-quality`接口

---

## 📋 修复执行计划

### 第一阶段 (立即执行)

1. ⏳ 修复前端设备管理API路径
2. ⏳ 修复前端PTZ控制路径
3. ⏳ 统一录像接口路径 (添加`/record/start`和`/record/stop`)
4. ⏳ 统一告警API单复数

### 第二阶段 (验证)

1. ⏳ 前端功能测试
2. ⏳ 移动端功能测试
3. ⏳ API契约验证

---

**修复优先级**: P0级 (必须修复)  
**预计完成时间**: 1-2天  
**责任人**: 前端团队 + 后端团队
